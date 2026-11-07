{%
showPrev = true
prevPage = ../lifecycle.html
showNext = true
nextPage = complete.html
%}

# Сервис цикла задач

## Создание задачи

Процесс создания задачи является фундаментальной операцией в системе управления индивидуальным развитием.
Каждый статус, в котором создается задача, накладывает определенные требования к полноте заполнения данных:

- **База знаний** и **Запланировано**: Требуется заполнение всех базовых полей (название, описание, тип задачи, раздел, цель),
  но **не требуется** план выполнения и даты.
- **В работе**: Требуется **полное заполнение всех полей** задачи, включая дату начала и планируемую дату завершения,
  кроме плана выполнения.

План выполнения заполняется при редактировании задачи на отдельной странице для большего удобства пользователя
(в модальном окне место ограничено по размеру).

### Реализация операции создания

Сервис жизненного цикла задач (`TaskLifecycleService`) агрегирует все необходимые сервисы для валидации и сохранения задачи.

```dotty
final case class TaskLifecycleService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F], // Для проверки пользовательских типов
    sectionService: SectionService[F],   // Для проверки раздела
    goalService: GoalService[F],         // Для проверки цели
    taskService: TaskService[F],         // Для непосредственного сохранения задачи
    historyService: HistoryService[F]    // Для хранения истории задач
):
  def create(task: CreateTask): F[TaskId] =
    // 1. Сбор и валидация всех зависимых сущностей
    validateTask(
      userId = task.userId,
      sectionId = task.sectionId,
      goalId = task.goalId,
      taskTypeId = task.taskTypeId,
      status = task.status,
      startDate = task.startDate,
      endDate = task.endDate
    ).flatMap:
      // Вся валидация пройдена → создаем задачу
      case Validated.Valid(_)        => taskService.create(task)
      case Validated.Invalid(errors) =>
      // Накопленные ошибки объединяются и выбрасываются как исключение
        MonadThrow[F].raiseError(
          new IllegalArgumentException(errors.toList.mkString(", "))
        )

  private def validateTask(
      userId: UserId,
      sectionId: SectionId,
      goalId: GoalId,
      taskTypeId: TaskTypeId,
      status: Status,
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  ) =
    for
      sectionOpt  <- sectionService.get(sectionId, userId)
      goalOpt     <- goalService.get(goalId, userId)
      taskTypeOpt <- taskTypeService.get(taskTypeId, userId)
    yield (
      // Преобразование Option в Validated с ошибкой
      sectionOpt
        .toValidNel(s"Section with id '$sectionId' not found"),
      goalOpt
        .toValidNel(s"Goal with id '$goalId' not found"),
      taskTypeOpt
        .toValidNel(s"User type with id '$taskTypeId' not found"),
      // Валидация статуса
      validateStatus(
        status = status,
        startDate = startDate,
        endDate = endDate
      )
    )
      // Комбинирование результатов: успех, только если все Valid
      .mapN { (_, _, _, _) => () }
end TaskLifecycleService

// Сопутствующий объект, содержит чистые функции валидации, зависящие только от статуса.
object TaskLifecycleService:
  // Основная функция валидации статуса. Определяет, какие дополнительные проверки нужны.
  private[services] def validateStatus(
      status: Status,
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  ): ValidatedNel[String, Unit] =
    status match
      // Для статуса "В работе" и "Завершено" обязательны корректные даты
      case Status.InProgress | Status.Completed =>
        validateDates(startDate = startDate, endDate = endDate)
      case Status.Planned | Status.KnowledgeBase =>
        Validated.valid(())

  // Проверка корректности дат: обе даты должны быть указаны,
  // а дата окончания — быть после даты начала.
  private[services] def validateDates(
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  )
      : ValidatedNel[String, Unit] =
    (startDate, endDate) match
      case (Some(start), Some(end)) if end.isBefore(start) =>
        Validated.invalidNel(
          s"Ошибка: дата начала $start позже даты окончания $end"
        )
      case (Some(start), Some(end)) => Validated.valid(())
      case _                        =>
        Validated.invalidNel(
          "Даты начала и окончания обязательны для заполнения"
        )
```

Обновление задачи происходит аналогично, но с добавлением проверки на существование задачи:

```dotty
def update(task: Task): F[Boolean] =
  taskService.get(task.id, task.userId).flatMap:
    case Some(_) =>
      validateTask(
        ...
      ).flatMap:
        // При обновлении план выполнения обязателен для всех статусов, кроме "База знаний"
        case Validated.Valid(_)
            if task.status == KnowledgeBase || task.plan.steps.nonEmpty =>
          taskService.update(task)
        case Validated.Valid(_) =>
          MonadThrow[F].raiseError(
            new IllegalArgumentException(
              "Для задач в статусе \"В работе\" и \"Запланировано\" план выполнения обязателен!"
            )
          )
        case Validated.Invalid(errors) =>
          MonadThrow[F].raiseError(
            new IllegalArgumentException(errors.toList.mkString(", "))
          )
    case None => MonadThrow[F].raiseError(
        new IllegalArgumentException(s"Task with id '${task.id}' not found")
      )
```

## Постановка задачи в очередь

Эта операция переводит задачу из стадии идеи ("База знаний") в стадию готового к выполнению плана ("Запланировано").
Ключевые действия:

1. **Смена статуса** с `KnowledgeBase` на `Planned`.
2. **Обязательное предоставление плана выполнения** — задача не может быть запланирована без детального плана.
3. **Назначение порядкового номера**: Система может предлагать первые свободные порядковые номера среди задач того же типа,
   чтобы облегчить пользователю организацию очереди.

```dotty
def enqueue(
    id: TaskId,
    userId: UserId,
    ordinal: Ordinal,
): F[Boolean] =
  // 1. Находим задачу, проверяя её текущий статус
  findTask(id, userId, Status.KnowledgeBase).flatMap: task =>
    // 2. Создаем обновленную версию: меняем статус и привязываем план
    val updated = task.copy(
      status = Status.Planned,
      ordinal = ordinal
    )
    // 3. Сохраняем изменения
    taskService.update(updated)

// Вспомогательная функция для безопасного поиска задачи
// с предварительной валидацией статуса.
private def findTask(
    id: TaskId,
    userId: UserId,
    status: Status
): F[Task] =
  taskService.get(id, userId).flatMap:
    case Some(task) if task.status == status =>
      MonadThrow[F].pure(task) // Задача найдена и имеет ожидаемый статус
    case Some(_) =>
      // Задача найдена, но в неправильном статусе (ошибка бизнес-логики)
      MonadThrow[F].raiseError(
        new IllegalArgumentException(
          s"Task with id '$id' is not in '$status' state"
        )
      )
    case None =>
      // Задача не найдена (ошибка данных)
      MonadThrow[F].raiseError(
        new IllegalArgumentException(s"Task with id '$id' not found")
      )
```

## Начало работы над задачей

Операция переводит задачу в активное состояние. При этом необходимо:

1.  **Установить дату начала работы** (может подставляться текущая дата по умолчанию).
2.  **Установить планируемую дату завершения**, которая должна быть логически корректной (после даты начала).

```dotty
def start(
    id: TaskId,
    userId: UserId,
    startDate: LocalDate,
    endDate: LocalDate
): F[Boolean] =
  // Предварительная проверка логики дат на уровне сервиса
  if endDate.isBefore(startDate) then
    MonadThrow[F].raiseError(
      new IllegalArgumentException(
        s"End date $endDate is before start date $startDate"
      )
    )
  else
    // Поиск задачи в статусе "Запланировано"
    findTask(id, userId, Status.Planned).flatMap: task =>
      val updated = task.copy(
        status = Status.InProgress,
        startDate = startDate.some, // Фиксация даты начала
        endDate = endDate.some      // Фиксация дедлайна
      )
      taskService.update(updated)
```

## Откладывание задачи

Данная операция позволяет временно снять задачу с активного выполнения, не отменяя её полностью.
Это полезно при смене приоритетов или возникновении блокировок.

1.  **Смена статуса** с `InProgress` обратно на `Planned`.
2.  **Очистка дат**: Даты начала и окончания обнуляются, так как при повторном взятии в работу они будут другими.

```dotty
def defer(id: TaskId, userId: UserId): F[Boolean] =
  findTask(id, userId, Status.InProgress).flatMap: task =>
    val updated = task.copy(
      status = Status.Planned,
      startDate = none,
      endDate = none
    )
    taskService.update(updated)
```

## Получение задачи

Эта операция возвращает детальную информацию о задаче для отображения на отдельной странице.
В отличие от простого чтения из БД, здесь требуется **обогащение данными** — идентификаторы типа, раздела и цели
заменяются на их читаемые названия, а также вычисляются производные поля: прогресс по плану,
описания продолжительности, затраченного и оставшегося времени.

Для получения задачи используется `TaskCard` — «толстая» модель, специально предназначенная для отображения.
Она содержит не только сырые данные задачи, но и все необходимые для UI-представления поля.

```dotty
final case class TaskCard(
    id: TaskId,
    name: TaskName,
    taskTypeId: TaskTypeId,
    taskType: String,
    status: Status,
    sectionId: SectionId,
    section: String,
    goalId: GoalId,
    goal: String,
    ordinal: Ordinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: List[PlanRowCard],
    progress: ProgressCard,
    duration: FiniteDuration,
    timeSpent: FiniteDuration
):
  val durationDesc: String      = duration.render
  val timeSpentDesc: String     = timeSpent.render
  val remainingTimeDesc: String = duration.minus(timeSpent).render
```

Метод `get` сначала загружает справочники типов, разделов и целей пользователя, а затем — саму задачу.
Если какая-то из связанных сущностей не найдена (например, раздел был удалён), в качестве запасного значения
используется `"—"`, чтобы страница не падала с ошибкой.

```dotty
def get(id: TaskId, userId: UserId): F[Option[TaskCard]] =
  for
    types     <- taskTypeService.getByUser(userId)
    sections  <- sectionService.getByUser(userId)
    goals     <- goalService.getByUser(userId)
    maybeTask <- taskService.get(id, userId)
  yield maybeTask.map: task =>
    val typesMap    = types.map(t => (t.id, t.name)).toMap
    val sectionsMap = sections.map(t => (t.id, t.name)).toMap
    val goalsMap    = goals.map(t => (t.id, t.name)).toMap

    TaskCard(
      id = task.id,
      name = task.name,
      taskType = typesMap.get(task.taskTypeId).map(_.value).getOrElse("—"),
      status = task.status,
      section = sectionsMap.get(task.sectionId).map(_.value).getOrElse("—"),
      goal = goalsMap.get(task.goalId).map(_.value).getOrElse("—"),
      ordinal = task.ordinal,
      startDate = task.startDate,
      endDate = task.endDate,
      information = task.information,
      plan = task.plan.steps.map(row =>
        PlanRowCard(
          number = row.number.value,
          step = row.step.value,
          completed = row.completed
        )
      ),
      completedCount = task.plan.steps.count(_.completed),
      count = task.plan.steps.length,
      progress = math.round(task.plan.progress * 10000.0) / 100.0,
      duration = task.duration,
      timeSpent = task.timeSpent
    )
```

**Что вычисляется дополнительно:**

- `completedCount` — сколько шагов плана уже выполнено.
- `count` — общее количество шагов.
- `progress` — процент выполнения, округлённый до двух знаков после запятой.
- `durationDesc`, `timeSpentDesc`, `remainingTimeDesc` — человекочитаемые описания (`"2 ч 30 мин"`, `"45 мин"`, `"1 дн"`).
- `remainingTimeDesc` — разница между планируемой продолжительностью и затраченным временем (может быть отрицательной, если превысили оценку).

## Получение списка задач

Этот метод используется для отображения таблиц на главной странице — во вкладках
"В работе", "Запланировано" и "База знаний". В отличие от `get`, который возвращает одну «толстую» карточку,
здесь возвращается **список компактных карточек** плюс справочники типов, разделов и целей для построения фильтров.

В слое представления эти «raw» данные превращаются в разные карточки в зависимости от вкладки:

- `TableInProgressTaskCard` — вкладка «В работе»;
- `TablePlannedTaskCard` — вкладка «Запланировано»;
- `TableKnowledgeBaseTaskCard` — вкладка «База знаний».

Каждая карточка содержит только те поля, которые нужны в своей таблице. Например,
`TablePlannedTaskCard` несёт название, тип, раздел, цель и порядковый номер — именно
они нужны для строки таблицы и клиентских фильтров:

```dotty
final case class TablePlannedTaskCard(
    id: TaskId,
    name: TaskName,
    taskType: String,
    section: String,
    goal: String,
    ordinal: Ordinal
)
```

А `TableKnowledgeBaseTaskCard` дополнительно содержит продолжительность (`duration`),
по которой вычисляется категория размера: менее 60 минут — `Small` (🌱), менее 8 часов — `Medium` (🌳),
иначе — `Large` (🌲). Это используется для визуальной группировки задач по размеру.

Метод `getAll` поддерживает **три опциональных фильтра**: по статусу, по типу задачи и по разделу.
Они передаются в `taskService.getAll`, где преобразуются в динамический SQL-запрос.
Отличительная особенность: сервис **не собирает** карточки, а возвращает «сырые» результаты
`Vector[TaskResult]` вместе со справочниками типов, разделов и целей. Преобразование
`TaskResult` → карточка (`TablePlannedTaskCard.fromTaskResult` и т.д.) происходит уже в слое представления
`WelcomeViews`, где строятся словари `id → name` и обогащаются строки таблицы.

```dotty
def getAll(
    userId: UserId,
    maybeStatus: Option[Status],
    maybeTaskType: Option[TaskTypeId],
    maybeSectionId: Option[SectionId]
): F[(Vector[TaskResult], Vector[TaskType], Vector[Section], Vector[Goal])] =
  for
    types    <- taskTypeService.getByUser(userId)
    sections <- sectionService.getByUser(userId)
    goals    <- goalService.getByUser(userId)
    tasks    <- taskService.getAll(
      userId = userId,
      maybeStatus = maybeStatus,
      maybeTaskType = maybeTaskType,
      maybeSectionId = maybeSectionId
    )
  yield (tasks, types, sections, goals)
```

**Возвращаемое значение** — кортеж из четырёх элементов:

1. `Vector[TaskResult]` — «сырые» задачи для таблицы.
2. `Vector[TaskType]` — типы задач пользователя (для фильтра и обогащения).
3. `Vector[Section]` — разделы пользователя (для фильтра и обогащения).
4. `Vector[Goal]` — цели пользователя (для фильтра и обогащения).

Такое возвращаемое значение избавляет от необходимости делать дополнительные HTTP-запросы
для загрузки справочников — всё приходит вместе с задачами.

**Зачем цели, а не только типы и разделы?** Помимо обогащения строк (подстановка имени
цели вместо id), список целей используется для фильтра «Цель» на вкладках «Запланировано»
и «База знаний». `WelcomeViews` отбирает только те цели, которые реально встречаются
в задачах вкладки, и передаёт их шаблону как опции клиентского фильтра.

## Увеличение затраченного времени

Эта операция вызывается, когда пользователь останавливает таймер на странице задачи.
В отличие от редактирования, где `timeSpent` задаётся напрямую, здесь значение **прибавляется** к уже
накопленному времени. Это важно, потому что пользователь может запускать таймер несколько раз в течение дня,
и каждый сеанс должен аккумулироваться, а не перезаписывать предыдущий.

Метод принимает количество минут (`timeSpent: Int`), конвертирует его в `FiniteDuration` и добавляет
к текущему значению `task.timeSpent`. Если задача не найдена или не принадлежит пользователю, возвращается `false`.

```dotty
def addTimeSpent(
    id: TaskId,
    userId: UserId,
    timeSpent: Int :| Positive
): F[Boolean] =
  taskService.get(id, userId).flatMap:
    case Some(task) if task.status != Completed =>
      val updated = task.copy(
        timeSpent =
          task.timeSpent.plus(FiniteDuration(timeSpent, TimeUnit.MINUTES))
      )
      taskService.update(updated)
    case _ => false.pure[F]
```

**Особенности:**

- **Возвращает `Boolean`**, а не `Unit`, чтобы вызывающий код мог отличить «задача не найдена»
  от «успешно обновлено».

## Удаление задачи

Важная и необратимая операция.
Сервис жизненного цикла делегирует её напрямую в `TaskService`, который отвечает за постоянное хранение.
Перед удалением выполняется проверка, что задача принадлежит пользователю (`taskService.get(id, userId)`),
чтобы предотвратить удаление чужих данных.

```dotty
def delete(id: TaskId, userId: UserId): F[Boolean] =
  taskService.get(id, userId).flatMap:
    case Some(_) => taskService.delete(id)
    case None    => MonadThrow[F].pure(false)
```

**Возвращаемое значение:**

- `true` — задача существовала, принадлежала пользователю и была удалена.
- `false` — задача не найдена или не принадлежит пользователю.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
