# Выравнивание порядка

На вкладке **«Запланировано»** задачи отображаются в строгом порядке выполнения — пользователь сам задаёт последовательность, 
в которой собирается выполнять план. Однако этот порядок может быть нарушен в четырёх ситуациях:

- **Создание новой задачи** — пользователь указывает порядок, который может совпасть с уже существующим.
- **Редактирование задачи** — при изменении порядкового номера существующей задачи.
- **Постановка задачи в очередь** — перевод из статуса `KnowledgeBase` в `Planned` с указанием порядка.
- **Откладывание задачи** — возврат из `InProgress` обратно в `Planned`.

Каждая из этих операций может привести к **дубликатам порядковых номеров**. 
Например, если в плане есть задачи с порядками `1, 2, 3, 5, 6, 7`, а пользователь вставляет новую задачу с порядком `2`, 
то две задачи получат номер `2`. Это ломает сортировку и делает список неоднозначным.

Чтобы этого избежать, вводится механизм **«Выровнять порядок»** — при активации признака все задачи того же статуса, 
у которых порядок **больше или равен** указанному, сдвигаются на единицу, а новая задача занимает освободившееся место.

## Как работает выравнивание

### Пример

Пусть в плане есть задачи с порядками `1, 2, 3, 5, 6, 7`. Пользователь добавляет задачу с порядком `2`.

**Без выравнивания:** получится `1, 2, 2, 3, 5, 6, 7` — два порядковых номера `2`.

**С выравниванием:** получится `1, 2, 3, 4, 5, 6, 7`:

- Задачи с порядком `< 2` остаются без изменений: `1`.
- Новая задача получает порядок `2`.
- Задачи с порядком `>= 2` сдвигаются: `2 → 3`, `3 → 4`, `5 → 5` (поскольку появилась новая задача, 
  но порядок сдвига идёт **строго по арифметической прогрессии** от указанного).

### Алгоритм

1. **Отфильтровать** задачи того же статуса, у которых `ordinal >= ordinal_новой_задачи`.
2. **Отсортировать** их по возрастанию исходного `ordinal`.
3. **Присвоить** им новые порядки, начиная с `ordinal_новой_задачи` и инкрементируя на 1 для каждой.
4. **Сохранить** изменения в одной транзакции.

Задачи с `ordinal < ordinal_новой_задачи` остаются без изменений.

## Операция `updateOrdinal` в `TaskService`

Первым шагом добавляем в трейт `TaskService` новую операцию, которая позволяет массово обновить порядковые номера задач:

```dotty
def updateOrdinal(ordinals: NonEmptyVector[(TaskId, Ordinal)]): F[Boolean]
```

**Разбор сигнатуры:**

- **`NonEmptyVector[(TaskId, Ordinal)]`** — непустой вектор пар «идентификатор задачи → новый порядок». 
  Использование `NonEmptyVector` вместо `Vector` — это типобезопасная гарантия, что список не пустой. 
  Не нужно проверять `isEmpty` — компилятор этого не допустит.
- **`F[Boolean]`** — эффект, возвращающий `true`, если все обновления прошли успешно.

### Реализация в `PostgreSQLTaskService`

```dotty
def updateOrdinal(ordinals: NonEmptyVector[(TaskId, Ordinal)]): F[Boolean] =
  val (taskId, ordinal) = ordinals.head
  ordinals.tail.foldLeft(
    sql"""UPDATE tasks SET ordinal = $ordinal WHERE id = $taskId""".update.run
  ) {
    case (acc, (taskId, ordinal)) =>
      acc *>
        sql"""UPDATE tasks SET ordinal = $ordinal WHERE id = $taskId""".update.run
  }
    .transact(xa)
    .map(_ > 0)
```

**Пошаговый разбор:**

1. **`val (taskId, ordinal) = ordinals.head`** — извлекаем первую пару. Она используется как начальное значение для `foldLeft`.

2. **`foldLeft(...)`** — проходим по оставшимся парам (`ordinals.tail`), накапливая `ConnectionIO`. На каждом шаге:
    - **`acc *>`** — композиция эффектов: сначала выполняется предыдущее обновление, потом текущее. 
      Оператор `*>` (звёздочка-больше) означает «сначала левый, потом правый».
    - **`sql"""UPDATE tasks SET ordinal = $ordinal WHERE id = $taskId""".update.run`** — `UPDATE` для одной задачи.

3. **`.transact(xa)`** — оборачивает всю последовательность `UPDATE` в одну транзакцию. Если хотя бы один `UPDATE` упадёт — все изменения откатятся.

4. **`.map(_ > 0)`** — превращает количество затронутых строк в `Boolean`. Если обновлено хотя бы одно — `true`.

**Почему `foldLeft`, а не `traverse` или `sequence`?**

`foldLeft` даёт явный контроль над порядком выполнения: все `UPDATE` выполняются **последовательно**, а не параллельно. 
Это важно для `UPDATE`-операций, потому что параллельные обновления могут привести к дедлокам на уровне БД.

### Реализация в `MockTaskService`

```dotty
def updateOrdinal(ordinals: NonEmptyVector[(TaskId, Ordinal)]): F[Boolean] =
  tasksSrc.update: source =>
    ordinals.foldLeft(source) {
      case (source, (taskId, ordinal)) =>
        source.get(taskId) match
          case None       => source
          case Some(task) =>
            source + (taskId -> task.copy(ordinal = ordinal))
    }.as(true)
```

**Разбор:**

- **`tasksSrc.update`** — атомарно обновляет внутреннее состояние мока (обычно `Ref[F, Map[TaskId, Task]]`).
- **`foldLeft(source)`** — проходим по всем парам, обновляя карту задач.
- **`source.get(taskId)`** — если задача не найдена, пропускаем её.
- **`task.copy(ordinal = ordinal)`** — создаём копию задачи с новым порядком.
- **`.as(true)`** — всегда возвращаем `true`, потому что мок не может «сломаться».

## Логика выравнивания в `TaskLifecycleService`

Метод `reorderTasks` реализует сам алгоритм сдвига:

```dotty
def reorderTasks(
    userId: UserId,
    ordinal: Ordinal,
    status: Status
): F[Boolean] =
  taskService.getAll(
    userId = userId,
    maybeStatus = status.some,
    maybeTaskType = none,
    maybeSectionId = none
  ).flatMap: tasks =>
    val reorderedTasks: Vector[(TaskId, Ordinal)] =
      tasks.collect {
        case task if task.ordinal.value >= ordinal.value =>
          task.id -> task.ordinal
      }
        .sortBy(_._2)
        .foldLeft((Vector.empty[(TaskId, Ordinal)], ordinal.next)) {
          case ((acc, ordinal), (taskId, _)) =>
            (
              acc :+ (taskId -> ordinal),
              ordinal.next
            )
        }._1

    NonEmptyVector
      .fromVector(reorderedTasks)
      .map(taskService.updateOrdinal)
      .getOrElse(true.pure[F])
```

**Пошаговый разбор:**

1. **`taskService.getAll(...)`** — загружаем все задачи пользователя с указанным статусом.
2. **`tasks.collect { case task if task.ordinal.value >= ordinal.value => ... }`** — отбираем только те задачи, 
   у которых порядок **не меньше** указанного. Задачи с меньшим порядком не затрагиваются.
3. **`.sortBy(_._2)`** — сортируем по возрастанию исходного `ordinal`. Это важно: сдвиг должен идти от меньшего порядкового номера к большему.
4. **`.foldLeft((Vector.empty, ordinal.next))`** — проходим по задачам, начиная с `ordinal.next` (следующий за указанным). На каждом шаге:
    - Добавляем задачу с новым порядком в аккумулятор.
    - Инкрементируем счётчик для следующей задачи.

   **Почему `ordinal.next`, а не `ordinal`?** Потому что порядок `ordinal` **зарезервирован** для новой или обновляемой задачи. 
   Все остальные сдвигаются начиная со следующего номера.

5. **`._1`** — извлекаем только вектор пар, игнорируя финальный счётчик.
6. **`NonEmptyVector.fromVector(reorderedTasks)`** — превращаем в `NonEmptyVector`. 
   Если список пустой (не было задач с порядком >= указанного), возвращаем `true` — нечего выравнивать.
7. **`.map(taskService.updateOrdinal)`** — если список не пустой, вызываем batch-update.
8. **`.getOrElse(true.pure[F])`** — если список пустой, возвращаем `true` в эффекте `F`.

### Пример работы

Пусть есть задачи `1, 2, 6, 7, 3, 5` и пользователь добавляет задачу с порядком `2`.

**Отбор:** задачи с `ordinal >= 2` — это `[2, 6, 7, 3, 5]`.

**Сортировка:** `[2, 3, 5, 6, 7]`.

**FoldLeft с начальным `ordinal.next = 3`:**

- Задача с порядком `2` → получает `3`
- Задача с порядком `3` → получает `4`
- Задача с порядком `5` → получает `5`
- Задача с порядком `6` → получает `6`
- Задача с порядком `7` → получает `7`

**Результат:** `[3, 4, 5, 6, 7]` для сдвинутых задач.

**Итоговый план:** `1, 2 (новая), 3, 4, 5, 6, 7`.

Обратите внимание: задачи `5, 6, 7` получили те же порядки, что и были (5 → 5, 6 → 6, 7 → 7), 
потому что после сдвига `2 → 3, 3 → 4` следующим в очереди идёт `5`, и он уже совпадает со своим номером. 
Это соответствует **арифметической прогрессии** от указанного порядка: `2, 3, 4, 5, 6, 7`.

## Обновление моделей запросов

Во все request-модели, которые участвуют в операциях с порядком, добавляется поле `ordinalShift`:

```dotty
final case class QueueTaskRequest(
    token: String,
    ordinal: Int,
    ordinalShift: Option[String]
) derives FormCodec
```

**Почему `Option[String]`, а не `Boolean`?**

Потому что HTML-чекбокс отправляет значение `"on"`, если отмечен, и **вообще не отправляет поле**, если не отмечен. 
`Option[String]` корректно моделирует оба случая:

- `Some("on")` — чекбокс отмечен.
- иначе — чекбокс не отмечен.

Проверка в коде: `request.ordinalShift.contains("on")`.

**Аналогично** поле добавляется в `CreateTaskRequest` и `EditTaskRequest`.

## Обновление `createHandler` (`TaskHandlers`)

Теперь создание задачи выполняется в два шага: сначала выравнивание, потом само создание.

```dotty
val runReorderTasks: IO[Boolean] =
  if request.ordinalShift.contains("on") then
    taskLifecycleService.reorderTasks(
      userId = user.id,
      ordinal = task.ordinal,
      status = task.status
    )
  else IO(true)

val run: IO[String] =
  for
    _    <- runReorderTasks
    _    <- taskLifecycleService.create(task)
    view <-
      TaskViews.getViewAfterCreateTask(
        settingsService = settingsService,
        userId = user.id,
        token = jwtToken,
        tab = request.tab
      )
  yield view
```

**Порядок действий критичен:**

1. **`runReorderTasks`** — сдвигаем существующие задачи, чтобы освободить место для новой.
2. **`taskLifecycleService.create(task)`** — создаём новую задачу с указанным порядком.

Если поменять местами — новая задача будет создана до сдвига, и получится дубликат. Поэтому сначала выравниваем, потом создаём.

**Что если выравнивание не нужно?**

`IO(true)` — no-op. Эффект ничего не делает, но возвращает `true`. Это позволяет писать единый for-comprehension независимо от флага.

## Обновление `editHandler` (`TaskHandlers`)

Аналогично для редактирования:

```dotty
val runReorderTasks: IO[Boolean] =
  if request.ordinalShift.contains("on") then
    taskLifecycleService.reorderTasks(
      userId = user.id,
      ordinal = task.ordinal,
      status = task.status
    )
  else IO(true)

val run: IO[Option[String]] =
  for
    _    <- runReorderTasks
    _    <- taskLifecycleService.update(task)
    view <- getViewTask(
      taskLifecycleService = taskLifecycleService,
      userId = user.id,
      taskId = task.id,
      jwtToken = jwtToken,
      errorMessage = none
    )
  yield view
```

Здесь **важен нюанс**: если старый порядок был `5`, а новый `2`, то при `reorderTasks(ordinal = 2)` 
в отбор попадут задачи с `ordinal >= 2`, включая саму задачу с `ordinal = 5`. 
Она тоже сдвинется — но затем `update` перезапишет её порядок на `2`. 
Итоговый результат корректный, хотя промежуточное состояние и содержит «лишнее» обновление.

## Обновление `queueHandler` (`TaskHandlers`)

Для постановки в очередь — та же логика, но с фиксированным статусом `Planned`:

```dotty
case Right(ordinal) =>
  val runReorderTasks: IO[Boolean] =
    if request.ordinalShift.contains("on") then
      taskLifecycleService.reorderTasks(
        userId = user.id,
        ordinal = ordinal,
        status = Status.Planned
      )
    else IO(true)

  for
    _ <- runReorderTasks
    _ <- taskLifecycleService.enqueue(
      id = taskId,
      userId = user.id,
      ordinal = ordinal
    )
    maybeView <- getViewTask(...)
  yield maybeView
```

Задача переводится из `KnowledgeBase` в `Planned` с указанным порядком. Перед этим выравниваются все задачи статуса `Planned`.

## Обновление шаблонов

Во все формы, где есть поле ввода порядка, добавляется чекбокс:

### `View.createTask`

Вызывает `CreateTaskRequest.render`, который рендерит форму создания задачи. Секция порядкового номера:

```dotty
group("ordinal", "Порядок:")(input(
  attr("id")    := "ordinal",
  attr("name")  := "ordinal",
  attr("type")  := "number",
  attr("min")   := "1",
  attr("value") := request.ordinal,
  required
)),
group("ordinalShift", "Выровнять порядок?")(
  input(
    attr("id")   := "ordinalShift",
    attr("name") := "ordinalShift",
    attr("type") := "checkbox"
  )
),
```

### `View.editTask`

Вызывает `EditTaskRequest.render`, который рендерит форму редактирования задачи. Секция порядкового номера:

```dotty
group("ordinal", "Порядок:")(
  input(
    attr("id")    := "ordinal",
    attr("name")  := "ordinal",
    attr("type")  := "number",
    attr("min")   := "1",
    attr("value") := request.ordinal,
    cls           := "form-input",
    required
  )
),
group("ordinalShift", "Выровнять порядок?")(
  input(
    attr("id")   := "ordinalShift",
    attr("name") := "ordinalShift",
    attr("type") := "checkbox"
  )
)
```

### `View.task` (модальное окно выбора порядка)

Вызывает `QueueTaskRequest.render`, который рендерит форму в модальном окне `ordinalModal`. Содержит поля `ordinal` и `ordinalShift`:

```dotty
form(
  attr("id")     := "ordinalForm",
  attr("hx-put") := s"/task/${id.value}/queue",
  Htmx.appTarget,
  Htmx.appSwap,
  Htmx.hiddenToken(token.value),
  div(
    cls := "form-group",
    label(`for` := "ordinalSelect")("Порядок:"),
    div(
      cls := "ordinal-info-block compact",
      div(cls := "ordinal-info-title")(
        s"""Доступные порядки задач типа "$taskType":"""
      ),
      div(cls := "ordinal-info-list")(ordinalListChildren*)
    ),
    input(
      attr("id")    := "ordinalSelect",
      attr("name")  := "ordinal",
      attr("type")  := "number",
      cls           := "form-input",
      placeholder   := "Введите порядок",
      attr("value") := ordinal
    ),
    div(
      cls := "form-group",
      label(`for` := "ordinalShift")("Выровнять порядок?"),
      input(
        attr("id")   := "ordinalShift",
        attr("name") := "ordinalShift",
        attr("type") := "checkbox"
      )
    )
  ),
  div(
    cls := "modal-actions",
    button(
      `type`  := "submit",
      cls     := "save-btn",
      onclick := "closeModal('ordinalModal')"
    )("Подтвердить"),
    button(
      `type`  := "button",
      cls     := "cancel-btn",
      onclick := "closeModal('ordinalModal')"
    )("Отмена")
  )
)
```

Чекбокс по умолчанию **не отмечен**. 
Если пользователь его отметит, при отправке формы поле `ordinalShift` будет содержать `"on"`, и сервер выполнит выравнивание.

## Предрассчитанный порядок

Отдельная задача — **предложить пользователю корректный порядок по умолчанию**. 
Когда пользователь открывает форму создания задачи, поле «Порядок» должно быть уже заполнено — следующим свободным номером.

Для статусов **«В работе»** и **«Запланировано»** порядок рассчитывается так:

```dotty
val createModalHandler: Handler =
  createModalRoute.handleIO: (tab: String, authorization: Authorization) =>
    withUserFromHeader(authorization): (user, token) =>
      val ordinalIO: IO[Ordinal] =
        TaskStatus.find(tab) match
          case Some(status)
              if status == TaskStatus.InProgress ||
                status == TaskStatus.Planned =>
            taskLifecycleService.taskService
              .getAll(
                userId = user.id,
                maybeStatus = status.some,
                maybeTaskType = none,
                maybeSectionId = none
              )
              .map(_.map(_.ordinal.next).maxOption.getOrElse(Ordinal.Min))
          case _ => Ordinal.Max.pure[IO]

      ordinalIO.flatMap: ordinal =>
        val request = CreateTaskRequest.empty(
          token = token.value,
          tab = tab,
          ordinal = ordinal.value
        )

        TaskViews.getViewEmptyCreateTaskModal(
          settingsService = settingsService,
          userId = user.id,
          request = request
        )
```

`createModalHandler` объявлен внутри `TaskHandlers`; используемый им маршрут
`createModalRoute` приватный.

**Пошаговый разбор:**

1. **`withUserFromHeader(authorization)(...)`** — стандартная проверка токена из заголовка.
   `authorization` — первый аргумент, лямбда `(user, token)` — второй.
2. **`TaskStatus.find(tab)`** — определяем статус по имени вкладки.
   Вкладки `"in-progress"`, `"planned"`, `"knowledge-base"` маппятся на соответствующие `Status`.

3. **Случай для `InProgress` и `Planned`:**
    - Загружаем все задачи с этим статусом.
    - Для каждой вычисляем `ordinal.next` — следующий порядковый номер.
    - Берём **максимум** из них: `_.ordinal.next`.
    - Если список пустой — возвращаем `Ordinal.Min` (обычно 1).

   **Почему `max` от `next`?** Потому что мы хотим поставить новую задачу **в конец** очереди. 
   Если последняя задача имеет порядок `10`, новая должна получить `11`. 
   `max` гарантирует это, даже если в списке есть пропуски или задачи с большими порядками.

4. **Случай для `KnowledgeBase` и других:**
    - Возвращаем `Ordinal.Max` — большое число, которое пользователь заменит при постановке в очередь.
    - Для статуса `KnowledgeBase` порядок не важен: задачи из базы знаний ещё не в очереди.

5. **`ordinalIO.flatMap`** — получив порядок, создаём `CreateTaskRequest.empty` с этим порядком и рендерим модальное окно.

**Что пользователь видит в форме:**

- Открывая форму на вкладке «В работе», поле «Порядок» уже содержит следующий свободный номер.
- Если в списке 5 задач с порядками `1, 2, 3, 4, 5`, поле покажет `6`.
- Если в списке `1, 3, 5, 7` (с пропусками), поле покажет `8` (максимум `7.next = 8`).

![Ordinal](images/ordinal.png)

Пользователь может принять это значение или изменить его. 
Если изменит на меньшее — ему стоит отметить чекбокс «Выровнять порядок», чтобы не создавать дубликат.

## Итог

Механизм **«Выровнять порядок»** решает проблему вставки задачи в середину очереди:

1. **`updateOrdinal`** в `TaskService` — операция для обновления порядков.
2. **`reorderTasks`** в `TaskLifecycleService` — алгоритм сдвига задач в арифметическую прогрессию.
3. **`ordinalShift`** в request-моделях — флаг, включающий выравнивание.
4. **Обновления хэндлеров** — вызов `reorderTasks` перед созданием/обновлением/постановкой в очередь.
5. **Чекбокс в формах** — UI для управления флагом.
6. **Предрассчитанный порядок** — заполнение поля «Порядок» следующим свободным номером.

Этот механизм делает работу с порядком задач **предсказуемой**: пользователь может вставить задачу в любое место очереди, 
и все остальные задачи автоматически сдвинутся, сохраняя непрерывную нумерацию.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
