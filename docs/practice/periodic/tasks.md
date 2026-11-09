# Формирование периодических задач

Периодические задачи — это **экземпляры** периодических шаблонов, созданные на конкретную дату. 
Если шаг «Просмотр доклада» (ежедневный) — это шаблон,
то **периодическая задача** — это «15.03.2026 - Ежедневная задача» с планом `[1. Просмотр доклада]`, созданная на 15 марта. 
Каждый день система генерирует новую задачу из шаблона, и пользователь работает с ней как с обычной задачей: отмечает шаги, завершает, редактирует план.

Это ключевое разделение: **шаблон** (что делать) и **экземпляр** (когда делать). 
Шаблон настраивается один раз в настройках, экземпляры создаются автоматически по расписанию.

## Модель `PeriodicTask`

```dotty
final case class PeriodicTask(
    id: PeriodicTaskId,
    userId: UserId,
    periodicType: PeriodicType,
    date: LocalDate,
    plan: Plan,
    completed: Boolean
)
```

**Разбор полей:**

| Поле               | Тип              | Назначение                           |
| ------------------ | ---------------- | ------------------------------------ |
| **`id`**           | `PeriodicTaskId` | Уникальный идентификатор экземпляра  |
| **`userId`**       | `UserId`         | Владелец задачи                      |
| **`periodicType`** | `PeriodicType`   | Тип периода (`Daily`, `Weekly`, ...) |
| **`date`**         | `LocalDate`      | Дата, на которую создана задача      |
| **`plan`**         | `Plan`           | План выполнения (список шагов)       |
| **`completed`**    | `Boolean`        | Завершена ли задача целиком          |

**Почему `date`, а не `startDate`/`endDate`?** Потому что периодическая задача привязана к **одной конкретной дате**. 
Она не длится неделю — она создана на понедельник и должна быть выполнена в понедельник. 
`LocalDate` — точная модель: «задача на 15 марта».

**Почему `completed: Boolean`, а не статус?** Потому что у периодической задачи **два состояния**: открыта и завершена. 
Статусы `InProgress`, `Planned`, `KnowledgeBase` — это для проектных задач с длинным жизненным циклом. 
Периодическая задача живёт один день (или неделю) — ей не нужен промежуточный статус. `Boolean` — проще и точнее.

**Почему `plan` — это `Plan`, а не просто список строк?** 
Потому что периодическая задача может иметь **иерархический план** с вложенными шагами. 
`Plan` даёт ту же структуру, что и у обычных задач: `PlanStep` с номером, текстом и флагом. 
Это единообразие: пользователь работает с планом одинаково в обоих типах задач.

### `PeriodicTaskId`

```dotty
object PeriodicTask:
  type PeriodicTaskId = PeriodicTaskId.T
  object PeriodicTaskId extends PdpUUID
```

**Разбор:**

- **`PeriodicTaskId`** — уточняющий тип над `UUID`. 
  Защищает от случайной путаницы: `PeriodicTaskId` и `UserId` — оба `UUID`, но несовместимы на уровне компилятора.

## DTO `CreatePeriodicTask`

```dotty
final case class CreatePeriodicTask(
    userId: UserId,
    periodicType: PeriodicType,
    date: LocalDate,
    plan: Plan
)
```

DTO для создания экземпляра. Отличается от `PeriodicTask` отсутствием `id` — ID генерируется БД (`DEFAULT gen_random_uuid()`).

## Сервис `PeriodicTaskService`

```dotty
private[services] trait PeriodicTaskService[F[_]]:
  def create(newItem: CreatePeriodicTask): F[PeriodicTaskId]
  def get(id: PeriodicTaskId, userId: UserId): F[Option[PeriodicTask]]
  def getByUser(userId: UserId): F[Vector[PeriodicTask]]
  def update(item: PeriodicTask): F[Boolean]
  def delete(id: PeriodicTaskId): F[Boolean]
```

**Разбор методов:**

- **`create`** — создаёт экземпляр и возвращает ID.
- **`get`** — получает экземпляр по ID **с проверкой владельца**: принимает `userId`
  и фильтрует по `user_id`. `Option`, потому что может не существовать (или принадлежать другому пользователю).
- **`getByUser`** — получает все экземпляры пользователя. Используется для **фильтрации незавершённых** и **проверки, создана ли уже задача на эту дату**.
- **`update`** — обновляет экземпляр. Возвращает `Boolean` — был ли он найден.
- **`delete`** — удаляет экземпляр.

**Почему нет `updateAll`?** Потому что периодические задачи **не переупорядочиваются**. 
Их порядок определяется **датой** и **типом периода**, а не ручной сортировкой. 
`updateAll` нужен только для шагов плана (где пользователь задаёт порядок), но не для экземпляров.

**Почему нет `reorder`?** Аналогично: 
экземпляры упорядочены по `(periodicType.ordinal, date)` — это **естественный порядок**, не требующий ручного управления.

## Миграция БД

```dotty
private val createPeriodicTasksTable: Fragment =
  sql"""CREATE TABLE IF NOT EXISTS periodic_tasks (
       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
       user_id UUID NOT NULL,
       periodic_type PeriodicTypeEnum NOT NULL,
       date DATE NOT NULL,
       plan VARCHAR[] NOT NULL,
       completed BOOLEAN NOT NULL DEFAULT FALSE
       );"""
```

**Разбор колонок:**

| Колонка             | Тип                              | Назначение                                                     |
| ------------------- | -------------------------------- | -------------------------------------------------------------- |
| **`id`**            | `UUID DEFAULT gen_random_uuid()` | Первичный ключ. PostgreSQL генерирует UUID автоматически.      |
| **`user_id`**       | `UUID NOT NULL`                  | Владелец. `NOT NULL` — задача всегда принадлежит кому-то.      |
| **`periodic_type`** | `PeriodicTypeEnum NOT NULL`      | Тип периода. Тот же enum, что и в `periodic_steps`.            |
| **`date`**          | `DATE NOT NULL`                  | Дата задачи. `DATE`, а не `TIMESTAMP` — точность до дня.       |
| **`plan`**          | `VARCHAR[] NOT NULL`             | Массив строк. Каждая строка — JSON-сериализованный `PlanStep`. |
| **`completed`**     | `BOOLEAN NOT NULL`               | Флаг завершения.                                               |

**Почему `gen_random_uuid()`?** Потому что UUID генерируется **на стороне БД**. Это надёжнее, чем генерировать в Scala:

- Нет риска коллизий при параллельной вставке.
- Нет необходимости передавать ID из приложения.
- `RETURNING id` возвращает сгенерированный ID.

**Почему `VARCHAR[]`, а не `JSONB`?** 
Потому что `Plan` сериализуется в **массив JSON-строк** — каждая строка это `PlanStep` в JSON. 
`VARCHAR[]` проще для Doobie: `Meta[Plan]` уже реализован через `unliftedStringArrayType`. 
`JSONB` потребовал бы дополнительной настройки.

**Почему `DATE`, а не `TIMESTAMP`?** Потому что периодическая задача привязана к **дню**, а не к моменту времени. 
`DATE` компактнее и точнее отражает модель. `LocalDate` в Scala соответствует `DATE` в PostgreSQL.

## Реализация `PostgreSQLPeriodicTaskService`

### `create`

```dotty
def create(newItem: CreatePeriodicTask): F[PeriodicTaskId] =
  sql"""INSERT INTO periodic_tasks (user_id, periodic_type, date, plan, completed)
        VALUES (${newItem.userId}, ${newItem.periodicType}::PeriodicTypeEnum, ${newItem.date}, ${newItem.plan}, false)
        RETURNING id"""
    .query[PeriodicTaskId]
    .unique
    .transact(xa)
```

**Разбор:**

- **`INSERT`** — вставляет экземпляр.
- **`${newItem.periodicType}::PeriodicTypeEnum`** — приведение к enum.
- **`${newItem.plan}`** — Doobie автоматически сериализует `Plan` в `VARCHAR[]` через `Meta[Plan]`.
- **`RETURNING id`** — возвращает сгенерированный UUID.

### `get`

```dotty
def get(id: PeriodicTaskId, userId: UserId): F[Option[PeriodicTask]] =
  sql"""SELECT id, user_id, periodic_type, date, plan, completed FROM periodic_tasks WHERE id = $id AND user_id = $userId"""
    .query[PeriodicTask]
    .option
    .transact(xa)
```

Возвращает `Option[PeriodicTask]`. Doobie десериализует `VARCHAR[]` в `Plan` через `Meta[Plan]`.
Запрос фильтрует и по `id`, и по `user_id` — так сервис сам гарантирует, что пользователь
не получит чужую задачу.

### `getByUser`

```dotty
def getByUser(userId: UserId): F[Vector[PeriodicTask]] =
  sql"""SELECT id, user_id, periodic_type, date, plan, completed FROM periodic_tasks WHERE user_id = $userId"""
    .query[PeriodicTask]
    .to[Vector]
    .transact(xa)
```

Возвращает **все** экземпляры пользователя. Фильтрация по `completed` и **проверка на существование** выполняются в `PeriodicTaskLifecycleService`.

**Почему не фильтруется в SQL?** 

- **`getTasks`** — фильтрует `completed == false` после создания недостающих.
- **`createTask`** — проверяет, есть ли уже задача на эту дату: `tasks.exists(task => task.periodicType == periodicType && task.date == date)`. Эта проверка не зависит от `completed`.

Если бы SQL фильтровал по `completed == false`, этот `exists`-проверка не увидела бы завершённые задачи — и создала бы дубликат. Поэтому фильтрация — на уровне сервиса.

### `update`

```dotty
def update(item: PeriodicTask): F[Boolean] =
  sql"""UPDATE periodic_tasks
       SET user_id = ${item.userId}, periodic_type = ${item.periodicType}::PeriodicTypeEnum, date = ${item.date}, plan = ${item.plan}, completed = ${item.completed}
       WHERE id = ${item.id}"""
    .update
    .run
    .transact(xa)
    .map(_ > 0)
```

Обновляет все поля экземпляра. `.map(_ > 0)` — `true`, если строка была обновлена.

### `delete`

```dotty
def delete(id: PeriodicTaskId): F[Boolean] =
  sql"""DELETE FROM periodic_tasks WHERE id = $id"""
    .update.run.transact(xa).map(_ > 0)
```

Удаляет экземпляр. `.map(_ > 0)` — `true`, если строка была удалена.

## Генерация дат: `PeriodicDates`

```dotty
final case class PeriodicDates(
    daily: LocalDate,
    weekly: LocalDate,
    monthly: LocalDate,
    quarterly: LocalDate,
    yearly: LocalDate
)

object PeriodicDates:
  def apply(now: LocalDate): PeriodicDates =
    val month = now.getMonthValue
    val year  = now.getYear

    val startOfTheWeek    = now.minusDays(now.getDayOfWeek.getValue - 1)
    val startOfTheMonth   = now.withDayOfMonth(1)
    val quarterStartMonth = ((month - 1) / 3) * 3 + 1
    val startOfTheQuarter = LocalDate.of(year, quarterStartMonth, 1)
    val startOfTheYear    = now.withDayOfYear(1)

    PeriodicDates(
      daily = now,
      weekly = startOfTheWeek,
      monthly = startOfTheMonth,
      quarterly = startOfTheQuarter,
      yearly = startOfTheYear
    )
```

**Назначение:** для каждой периодичности вычисляет **дату, к которой относится задача**. 
Это ключ к **дедупликации**: задачи создаются **на начало периода**, а не на текущую дату.

**Разбор:**

- **`daily = now`** — ежедневная задача создаётся на **сегодня**.
- **`weekly = startOfTheWeek`** — еженедельная — на **понедельник текущей недели**.
- **`monthly = startOfTheMonth`** — ежемесячная — на **первое число текущего месяца**.
- **`quarterly = startOfTheQuarter`** — ежеквартальная — на **первое число текущего квартала**.
- **`yearly = startOfTheYear`** — ежегодная — на **1 января текущего года**.

**Формула квартала:**

```dotty
val quarterStartMonth = ((month - 1) / 3) * 3 + 1
```

- **`(month - 1) / 3`** — целочисленное деление: `0` для Q1, `1` для Q2, `2` для Q3, `3` для Q4.
- **`* 3`** — превращает в `0`, `3`, `6`, `9`.
- **`+ 1`** — сдвиг на 1: `1`, `4`, `7`, `10`.

**Пример:** `month = 5` (май) → `(5 - 1) / 3 = 1` → `1 * 3 = 3` → `3 + 1 = 4` → апрель (Q2).

## DTO `PeriodicTaskCard`

```dotty
final case class PeriodicTaskCard(
    id: PeriodicTaskId,
    name: String,
    periodicType: PeriodicType,
    date: LocalDate,
    plan: List[PlanRowCard],
    progress: Double,
    completedCount: Int,
    count: Int
)
```

**Разбор полей:**

| Поле                 | Назначение                                            |
| -------------------- | ----------------------------------------------------- |
| **`id`**             | ID для ссылки `/periodic_task/{id}`                   |
| **`name`**           | Сформированное имя (`15.03.2026 - Ежедневная задача`) |
| **`periodicType`**   | Тип периода                                           |
| **`date`**           | Дата задачи                                           |
| **`plan`**           | План в виде `List[PlanRowCard]`                       |
| **`progress`**       | Прогресс в процентах (0.00–100.00)                    |
| **`completedCount`** | Количество выполненных шагов                          |
| **`count`**          | Общее количество шагов                                |

### Фабричный метод `apply`

```dotty
object PeriodicTaskCard:
  private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  def apply(task: PeriodicTask): PeriodicTaskCard =
    PeriodicTaskCard(
      id = task.id,
      name = s"${task.date.format(formatter)} - ${task.periodicType.show}",
      periodicType = task.periodicType,
      date = task.date,
      plan = task.plan.steps.map(row =>
        PlanRowCard(
          number = row.number.value,
          step = row.step.value,
          completed = row.completed
        )
      ),
      progress = math.round(task.plan.progress * 10000.0) / 100.0,
      completedCount = task.plan.steps.count(_.completed),
      count = task.plan.steps.length
    )
```

**Пошаговый разбор:**

1. **`name`** — `s"${task.date.format(formatter)} - ${task.periodicType.show}"`:
    - Дата форматируется в `dd.MM.yyyy`.
    - Тип отображается через `Show` («Ежедневная задача»).
    - Результат: `"15.03.2026 - Ежедневная задача"`.

2. **`plan`** — конвертация `PlanStep` → `PlanRowCard`:
    - `row.number.value` — номер как строка (`"1.2.3"`).
    - `row.step.value` — текст шага.
    - `row.completed` — флаг.

3. **`progress`** — `math.round(task.plan.progress * 10000.0) / 100.0`:
    - `plan.progress` — доля от 0.0 до 1.0.
    - `* 10000.0` — превращает в целое число сотых.
    - `math.round` — округление.
    - `/ 100.0` — превращает в проценты с двумя знаками.

   **Пример:** `progress = 0.3333` → `3333.0` → `3333` → `33.33`.

4. **`completedCount`** — количество выполненных шагов.

5. **`count`** — общее количество шагов.

**Почему `math.round(... * 10000.0) / 100.0`, а не просто `* 100`?** 
Потому что `* 100` даёт `33.33333333333333` — некрасиво в UI. Округление до сотых даёт `33.33` — читаемо. 
Та же формула, что и в `calculateProgress`, но для `Plan.progress` (который возвращает `Double`, а не считает через `FiniteDuration`).

## `PeriodicTaskLifecycleService`

Это **ключевой сервис** — он решает, какие задачи создать, и предоставляет операции над экземплярами.

```dotty
final case class PeriodicTaskLifecycleService[F[_]: MonadThrow](
    periodicTaskService: PeriodicTaskService[F],
    settingsService: SettingsService[F]
):
```

**Разбор зависимостей:**

- **`periodicTaskService`** — работа с экземплярами в БД.
- **`settingsService`** — загрузка шаблонов (`getSteps`).

### `getTasks`

```dotty
def getTasks(userId: UserId): F[Vector[PeriodicTaskCard]] =
  val dates = PeriodicDates(LocalDate.now())

  for
    tasks            <- periodicTaskService.getByUser(userId)
    periodicSettings <- settingsService.getSteps(userId)
    settings = periodicSettings.groupBy(_.periodicType)
    maybeDailyTask <- createTask(tasks, settings, PeriodicType.Daily, dates.daily, userId)
    maybeWeeklyTask <- createTask(tasks, settings, PeriodicType.Weekly, dates.weekly, userId)
    maybeMonthlyTask <- createTask(tasks, settings, PeriodicType.Monthly, dates.monthly, userId)
    maybeQuarterlyTask <- createTask(tasks, settings, PeriodicType.Quarterly, dates.quarterly, userId)
    maybeYearlyTask <- createTask(tasks, settings, PeriodicType.Yearly, dates.yearly, userId)
  yield (
    tasks ++ maybeDailyTask ++ maybeWeeklyTask ++ maybeMonthlyTask ++
      maybeQuarterlyTask ++ maybeYearlyTask
  )
    .withFilter(_.completed == false)
    .map(PeriodicTaskCard.apply)
    .sortBy(t => (t.periodicType.ordinal, t.date))
end getTasks
```

**Пошаговый разбор:**

**1. Вычисление дат:**

```dotty
val dates = PeriodicDates(LocalDate.now())
```

Для каждой периодичности — дата, к которой относится задача. Это **ключ к дедупликации**.

**2. Загрузка существующих задач и шаблонов:**

```dotty
tasks            <- periodicTaskService.getByUser(userId)
periodicSettings <- settingsService.getSteps(userId)
settings = periodicSettings.groupBy(_.periodicType)
```

- **`tasks`** — все экземпляры пользователя (включая завершённые).
- **`periodicSettings`** — все шаблоны пользователя.
- **`settings`** — шаблоны, сгруппированные по типу. Это ускоряет доступ: `settings(Daily)` — все ежедневные шаблоны.

**3. Создание недостающих задач:**

```dotty
maybeDailyTask <- createTask(tasks, settings, PeriodicType.Daily, dates.daily, userId)
...
```

Для каждого типа вызывается `createTask`. Он возвращает:

- **`Some(task)`** — если задача была создана.
- **`None`** — если нечего создавать (шаблонов нет или задача на эту дату уже существует).

**4. Объединение:**

```dotty
tasks ++ maybeDailyTask ++ maybeWeeklyTask ++ maybeMonthlyTask ++
  maybeQuarterlyTask ++ maybeYearlyTask
```

К существующим задачам добавляются только что созданные. `++` для `Option` работает как «добавить, если `Some`».

**5. Фильтрация завершённых:**

```dotty
.withFilter(_.completed == false)
```

Пользователь видит только **открытые** задачи.

**6. Конвертация в карточки:**

```dotty
.map(PeriodicTaskCard.apply)
```

Каждый `PeriodicTask` превращается в `PeriodicTaskCard` — модель для UI.

**7. Сортировка:**

```dotty
.sortBy(t => (t.periodicType.ordinal, t.date))
```

- **Сначала по типу периода** (`Daily` → `Weekly` → ...).
- **При равных типах — по дате** (старые сверху).

Пользователь видит ежедневные задачи первыми — они самые важные.

### `createTask`

```dotty
private def createTask(
    tasks: Vector[PeriodicTask],
    settings: Map[PeriodicType, Vector[PeriodicStep]],
    periodicType: PeriodicType,
    date: LocalDate,
    userId: UserId
): F[Option[PeriodicTask]] =
  val steps: List[PlanStep] =
    settings
      .getOrElse(periodicType, Vector.empty[PeriodicStep])
      .map: periodicStep =>
        PlanStep(
          number = periodicStep.number,
          step = periodicStep.name,
          completed = false
        )
      .toList
      .sortBy(_.number)

  if steps.isEmpty ||
    tasks.exists(task =>
      task.periodicType == periodicType &&
        task.date == date
    )
  then
    none.pure[F]
  else
    val createItem = CreatePeriodicTask(
      userId = userId,
      periodicType = periodicType,
      date = date,
      plan = Plan(steps),
      completed = false
    )
    periodicTaskService.create(createItem).map: id =>
      PeriodicTask(
        id = id,
        userId = createItem.userId,
        periodicType = createItem.periodicType,
        date = createItem.date,
        plan = createItem.plan,
        completed = createItem.completed
      ).some
  end if
end createTask
```

**Пошаговый разбор:**

**1. Построение плана из шаблонов:**

```dotty
val steps: List[PlanStep] =
  settings
    .getOrElse(periodicType, Vector.empty[PeriodicStep])
    .map: periodicStep =>
      PlanStep(
        number = periodicStep.number,
        step = periodicStep.name,
        completed = false
      )
    .toList
    .sortBy(_.number)
```

- **`settings.getOrElse(periodicType, Vector.empty)`** — шаблоны для этого типа. Если их нет — пустой вектор.
- **`.map`** — каждый `PeriodicStep` превращается в `PlanStep`:
    - `number = periodicStep.number` — номер шага.
    - `step = periodicStep.name` — название.
    - `completed = false` — новый шаг всегда невыполнен.
- **`.sortBy(_.number)`** — сортировка по номеру. Это **важно**: шаги в плане должны идти в правильном порядке (1, 2, 3, ...), а не в порядке вставки в БД.

**2. Проверка «нечего создавать»:**

```dotty
if steps.isEmpty ||
  tasks.exists(task =>
    task.periodicType == periodicType &&
      task.date == date
  )
then
  none.pure[F]
```

Два условия для пропуска создания:

- **`steps.isEmpty`** — нет шаблонов этого типа. Нечего создавать.
- **`tasks.exists(...)`** — задача на эту дату **уже существует**. Это защита от дубликатов.

**3. Создание:**

```dotty
val createItem = CreatePeriodicTask(
  userId = userId,
  periodicType = periodicType,
  date = date,
  plan = Plan(steps),
  completed = false
)
periodicTaskService.create(createItem).map: id =>
  PeriodicTask(
    id = id,
    userId = createItem.userId,
    ...
  ).some
```

- **`createItem`** — DTO для создания.
- **`periodicTaskService.create(createItem)`** — создаёт в БД, возвращает ID.
- **`.map: id => PeriodicTask(...)`** — собирает полную модель из DTO + ID.
- **`.some`** — оборачивает в `Option`, чтобы `getTasks` мог объединить с существующими.

### `get`

```dotty
def get(id: PeriodicTaskId, userId: UserId): F[Option[PeriodicTaskCard]] =
  periodicTaskService.get(id, userId).map(_.map(PeriodicTaskCard.apply))
```

**Разбор:**

- **`periodicTaskService.get(id, userId)`** — загружает задачу с проверкой владельца.
- **`.map(_.map(PeriodicTaskCard.apply))`** — если задача есть, конвертирует в карточку.

**Почему напрямую `periodicTaskService.get(id, userId)`?** В `cb36973` проверка владельца переехала в низкоуровневый сервис: метод принимает `userId` и фильтрует по нему. `LifecycleService` просто передаёт `userId`. Прошлый приватный helper `getByUser(id, userId)` удалён.

### `completeStep`

```dotty
def completeStep(
    id: PeriodicTaskId,
    userId: UserId,
    number: String
): F[Boolean] =
  periodicTaskService.get(id, userId).flatMap:
    case Some(task) =>
      val updatedSteps =
        task.plan.steps.map: step =>
          if step.number.value == number then step.copy(completed = true)
          else step

      periodicTaskService.update(task.copy(plan = Plan(updatedSteps)))
    case None => MonadThrow[F].raiseError(
        new IllegalArgumentException(s"Periodic task with id '$id' not found")
      )
```

**Разбор:**

1. **`periodicTaskService.get(id, userId)`** — загружает задачу с проверкой владельца.
2. **`case Some(task)`** — если задача найдена:
    - **`.map`** по шагам: если номер совпадает — помечаем выполненным.
    - **`periodicTaskService.update`** — сохраняем обновлённый план.
3. **`case None`** — если задачи нет или она чужая — `IllegalArgumentException`.

**Почему ошибка, а не `false`?** Потому что «задача не найдена» — это **ошибка**, а не «операция не удалась». 
Пользователь ожидает, что задача существует (он кликнул на неё). Если её нет — что-то пошло не так. `false` скрыл бы проблему.

**Почему сравнение по `number.value`, а не по `number`?** `StepNumber` — opaque type. 
Сравнение через `.value` — проще и не требует `given`-инстанса `Eq[StepNumber]`.

### `complete`

```dotty
def complete(id: PeriodicTaskId, userId: UserId): F[Boolean] =
  periodicTaskService.get(id, userId).flatMap:
    case Some(task) => periodicTaskService.update(task.copy(completed = true))
    case None       => false.pure[F]
```

**Разбор:**

- **`Some(task)`** — помечаем задачу завершённой.
- **`None`** — возвращаем `false` (задача не найдена или чужая).

**Почему `false`, а не ошибка?** Потому что завершение задачи — **идемпотентная** операция. 
Если задача уже завершена или удалена — ничего страшного, просто `false`. Это не ошибка, а нормальное поведение.

### `updatePlan`

```dotty
def updatePlan(id: PeriodicTaskId, userId: UserId, plan: Plan): F[Boolean] =
  periodicTaskService.get(id, userId).flatMap:
    case Some(task) => periodicTaskService.update(task.copy(plan = plan))
    case None       => false.pure[F]
```

**Разбор:** обновляет **только план**, не трогая остальные поля. Используется при редактировании периодической задачи.

**Почему `updatePlan`, а не `update(task)`?** 
Потому что пользователь может редактировать **только план** — тип, дата, владелец не меняются. 
Отдельный метод делает намерение **явным** и предотвращает случайное изменение других полей.

### `delete`

```dotty
def delete(id: PeriodicTaskId, userId: UserId): F[Boolean] =
  periodicTaskService.get(id, userId).flatMap:
    case Some(_) => periodicTaskService.delete(id)
    case None    => false.pure[F]
```

**Разбор:** удаляет задачу, только если она принадлежит пользователю.

**Почему проверка владельца не в `LifecycleService`?** В `cb36973` она переехала в низкоуровневый сервис — `periodicTaskService.get(id, userId)` уже фильтрует по `userId`. `LifecycleService` не держит собственный приватный helper и просто вызывает `periodicTaskService.get(id, userId)`.

## UI: вкладка «В работе»

Вкладка «В работе» рендерится методом `Views.welcomeInProgress`, а блок периодических задач
выводится через `periodicTasks.map(_.render)` — каждая карточка `TablePeriodicTaskCard`
рендерит себя сама. Готовый HTML выглядит так:

```html
<div class="periodic-tasks-section">
    <h3 class="periodic-tasks-title">🔄 Периодические задачи</h3>

    <div class="tasks-grid">
        <div id="{task.id}"
             class="task-card periodic-task-card"
             onclick="openPeriodicTask('{task.id}')">

            <div class="task-title">{task.name}</div>

            <div class="task-progress">
                <div class="progress-label">
                    <span>Прогресс</span>
                    <span class="progress-percent">{task.progress}%</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: {task.progress}%"></div>
                </div>
            </div>
        </div>
    </div>
</div>
```

Полный блок (с заголовком и сеткой) выводится только если `periodicTasks` не пуст —
`if periodicTasks.nonEmpty then ... else frag()` в scalatags.

**Разбор:**

- **`🔄 Периодические задачи`** — заголовок с иконкой цикла.
- **`onclick="openPeriodicTask('{task.id}')"`** — клик открывает `/periodic_task/{id}`.
- **`task.name`** — готовое имя (`15.03.2026 - Ежедневная задача`).
- **Прогресс-бар** — процент выполнения плана.

**Почему только имя и прогресс, без дат?** Потому что **дата уже в имени**. Дублировать её в метаданных — избыточно. 
Даты начала/окончания у периодической задачи нет — есть только одна дата. `name` содержит всю нужную информацию.

## Роуты

Пути периодических задач объявлены локально внутри `PeriodicTaskHandlers` (см. ниже). Это позволяет держать
всю связку «маршрут + обработчик» для периодических задач в одном классе.

```dotty
private val path = Path.root / "periodic_task" / uuidParam

private val openPeriodicTask = Route(
  Request.get(path).extractHeader[`Cookie`],
  Response.ok(Entity.html)
)

private val completePlanStepPeriodicTask = Route(
  Request.put(path / "completeStep" / Param.string)
    .extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)

private val completeFormPeriodicTask = Route(
  Request.put(path / "complete").extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)

private val editFormPeriodicTask = Route(
  Request.get(path / "edit").extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)

private val editPeriodicTask = Route(
  Request.put(path).withEntity(Entity.formOf[EditPeriodicTaskRequest]),
  Response.ok(Entity.html).orNotFound
)

private val deletePeriodicTask = Route(
  Request.delete(path).extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

Обратите внимание на **`uuidParam`** вместо `Param.string`: это `Param[UUID]`,
который декодирует путь сразу в `UUID` (см. `UrlFormParser.uuidParam`), а не в строку.
Это убирает необходимость парсить UUID вручную в каждом хэндлере.

**Разбор маршрутов:**

| Маршрут                                         | Метод  | Назначение           |
| ----------------------------------------------- | ------ | -------------------- |
| **`/periodic_task/{id}`**                       | GET    | Страница задачи      |
| **`/periodic_task/{id}/completeStep/{number}`** | PUT    | Завершить шаг        |
| **`/periodic_task/{id}/complete`**              | PUT    | Завершить задачу     |
| **`/periodic_task/{id}/edit`**                  | GET    | Форма редактирования |
| **`/periodic_task/{id}`**                       | PUT    | Сохранить изменения  |
| **`/periodic_task/{id}`**                       | DELETE | Удалить задачу       |

## `EditPeriodicTaskRequest`

```dotty
final case class EditPeriodicTaskRequest(
    token: String,
    plan: List[PlanStepRequest]
)

object EditPeriodicTaskRequest:
  given FormCodec[EditPeriodicTaskRequest] =
    new FormCodec[EditPeriodicTaskRequest](
      decode = { (urlForm: UrlForm) =>
        urlForm.getValue("token")
          .map: token =>
            EditPeriodicTaskRequest(
              token = token,
              plan = EditTaskRequest.decodePlanSteps(urlForm)
            )
          .leftMap(e => Chain(e))
      },
      encode = { (request: EditPeriodicTaskRequest) =>
        val baseFields = Map(
          "token" -> Chain(request.token)
        )

        UrlForm(baseFields ++ EditTaskRequest.encodePlanSteps(request.plan))
      }
    )
end EditPeriodicTaskRequest
```

**Разбор:**

- **`token: String`** — JWT-токен.
- **`plan: List[PlanStepRequest]`** — план в виде списка шагов.

**Почему только `token` и `plan`?** Потому что **редактировать можно только план**. 
Тип, дата, владелец — не меняются. Это соответствует требованию: «Редактировать можно только строки плана».

**Переиспользование `EditTaskRequest.decodePlanSteps`:** 
логика декодирования плана из формы (`plan[0].number`, `plan[0].step`, `plan[0].completed`) уже реализована в `EditTaskRequest`. 
`EditPeriodicTaskRequest` **переиспользует** её — это устраняет дублирование.

## Хэндлеры

Для периодических задач **все хэндлеры собраны в один класс** — `PeriodicTaskHandlers`, который:

- объявляет все маршруты периодических задач (`private val openPeriodicTask`, `completeFormPeriodicTask` и т.д.);
- содержит по одному `val`-публичному хэндлеру на маршрут (`openHandler`, `editFormHandler`, ...);
- регистрируется в `Main` как единая зависимость.

```dotty
final case class PeriodicTaskHandlers(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    service: PeriodicTaskLifecycleService[IO]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  private val path = Path.root / "periodic_task" / uuidParam

  private val openPeriodicTask = Route(
    Request.get(path).extractHeader[`Cookie`],
    Response.ok(Entity.html)
  )
  private val completePlanStepPeriodicTask = Route(
    Request.put(path / "completeStep" / Param.string)
      .extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )
  private val completeFormPeriodicTask = Route(
    Request.put(path / "complete").extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )
  private val editFormPeriodicTask = Route(
    Request.get(path / "edit").extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )
  private val editPeriodicTask = Route(
    Request.put(path).withEntity(Entity.formOf[EditPeriodicTaskRequest]),
    Response.ok(Entity.html).orNotFound
  )
  private val deletePeriodicTask = Route(
    Request.delete(path).extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )

  val openHandler: Handler =
    openPeriodicTask.handleIO: (uuid: UUID, cookie: Cookie) =>
      withUserFromCookieOpt(cookie)(
        f = (user, jwtToken) =>
          service.get(PeriodicTaskId(uuid), user.id).map {
            _.map { periodicTask =>
              val html = View.periodicTask(task = periodicTask, errorMessage = none)
              View.base(Title, html)
            }
          },
        fallbackPage = NotFoundView
      )

  val compileStepHandler: Handler =
    completePlanStepPeriodicTask.handleIO:
      (uuid: UUID, number: String, authorization: Authorization) =>
        withUserFromHeaderOpt(authorization) { (user, jwtToken) =>
          val periodicTaskId = PeriodicTaskId(uuid)
          for
            _         <- service.completeStep(periodicTaskId, user.id, number)
            maybeTask <- service.get(periodicTaskId, user.id)
          yield maybeTask.map(task => View.periodicTask(task, none))
        }

  val completeHandler: Handler =
    completeFormPeriodicTask.handleIO:
      (uuid: UUID, authorization: Authorization) =>
        withUserFromHeader(authorization): (user, token) =>
          service.complete(PeriodicTaskId(uuid), user.id).as(HomeView)

  val editFormHandler: Handler =
    editFormPeriodicTask.handleIO: (uuid: UUID, authorization: Authorization) =>
      withUserFromHeaderOpt(authorization): (user, jwtToken) =>
        val periodicTaskId = PeriodicTaskId(uuid)
        service.get(periodicTaskId, user.id).map {
          _.map { task =>
            View.editPeriodicTask(
              periodicTaskId = task.id,
              name = task.name,
              request = EditPeriodicTaskRequest(
                token = jwtToken.value,
                plan = task.plan.map(PlanStepRequest.apply)
              ),
              token = jwtToken,
              errorMessage = none
            )
          }
        }

  val editHandler: Handler =
    editPeriodicTask.handleIO: (uuid: UUID, request: EditPeriodicTaskRequest) =>
      val run: ((UserInfo, JwtToken)) => IO[Option[String]] =
        (user, jwtToken) =>
          request.plan.traverse(PlanStep.parse)
            .map { steps =>
              val periodicTaskId = PeriodicTaskId(uuid)
              val run: IO[Option[String]] =
                for
                  _ <- service.updatePlan(periodicTaskId, user.id, Plan(steps.sortBy(_.number)))
                  maybeTask <- service.get(periodicTaskId, user.id)
                yield maybeTask.map(task => View.periodicTask(task, none))
              run
                .recover:
                  case ex: IllegalArgumentException =>
                    View.editPeriodicTask(
                      periodicTaskId = periodicTaskId,
                      name = "Ошибка при редактировании задачи",
                      request = request,
                      token = jwtToken,
                      errorMessage = ex.getMessage.some
                    ).some
            }.valueOr(errors =>
              Logger[IO].error(errors.toChain.toVector.mkString("; ")).as(none)
            )
      // ToDo: https://github.com/creativescala/krop/pull/57
      val authorization: Authorization =
        Authorization(Token(AuthScheme.Bearer, request.token))
      withUserFromHeaderOpt(authorization)(run)

  val deleteHandler: Handler =
    deletePeriodicTask.handleIO: (uuid: UUID, authorization: Authorization) =>
      withUserFromHeader(authorization): (user, token) =>
        service.delete(PeriodicTaskId(uuid), user.id).as(HomeView)
end PeriodicTaskHandlers
```

### Хэндлер открытия `openHandler`

`withUserFromCookieOpt(cookie)(f, fallbackPage)` извлекает токен из куки `token`, проверяет его,
вызывает `f`, передавая `(user, jwtToken)`. Если токена нет/невалиден или задача не найдена
(лямбда вернула `None`) — подставляется `fallbackPage` (`NotFoundView`).
Извлечение токена из куки инкапсулировано в `AuthorizationHandler`.

### Хэндлер завершения шага `compileStepHandler`

`withUserFromHeaderOpt(authorization)(...)` проверяет токен из заголовка, `service.completeStep` отмечает
шаг выполненным, после чего возвращается обновлённая страница.

### Хэндлер завершения задачи `completeHandler`

`service.complete(...)` помечает задачу завершённой. Возвращается `HomeView` — главная страница,
поскольку завершённая задача покидает список «В работе».

**Почему `HomeView`, а не страницу задачи?** Потому что после завершения задача **покидает** список открытых.
Логичнее вернуть пользователя к списку, а не показывать завершённую задачу.

### Хэндлер формы редактирования `editFormHandler`

Аналогичен `openHandler`, но рендерит **форму редактирования** (`View.editPeriodicTask`),
а не страницу просмотра.

### Хэндлер сохранения правок `editHandler`

`request.plan.traverse(PlanStep.parse)` валидирует каждый шаг плана. Если все шаги валидны —
`service.updatePlan` обновляет план и возвращается страница задачи. При `IllegalArgumentException`
(например, задача не найдена) — возвращается форма редактирования с сообщением об ошибке.

**Почему `Plan(steps.sortBy(_.number))`?** Потому что `traverse` возвращает шаги в порядке из формы
(который может быть любым). `sortBy(_.number)` гарантирует, что план будет в правильном порядке.

### Хэндлер удаления `deleteHandler`

`service.delete(...)` удаляет задачу, только если она принадлежит пользователю. Возвращает `HomeView`.

**Почему `HomeView`?** Потому что удалённой задачи больше нет — показывать её страницу бессмысленно.
Возврат к списку — логичное поведение.

## Полный поток работы

### Открытие списка задач

1. **Пользователь открывает главную страницу** → `getTasks` вызывается в хэндлере.
2. **`getTasks`:**
    - Загружает существующие задачи.
    - Загружает шаблоны.
    - Для каждого типа создаёт недостающие задачи на соответствующие даты.
    - Возвращает карточки **открытых** задач, отсортированные по `(periodicType, date)`.
3. **Пользователь видит блок «🔄 Периодические задачи»** с карточками (только имя + прогресс).

### Завершение шага

1. **Пользователь открывает задачу** → `/periodic_task/{id}`.
2. **Видит план** с шагами.
3. **Кликает «✅ Выполнить»** на шаге → htmx отправляет PUT на `/periodic_task/{id}/completeStep/{number}`.
4. **`compileStepHandler`:**
    - Находит шаг по номеру.
    - Помечает `completed = true`.
    - Сохраняет.
    - Возвращает обновлённую страницу.
5. **htmx заменяет `#app`** — шаг помечен выполненным, прогресс-бар обновлён.

### Завершение задачи

1. **Пользователь кликает «✅ Завершить задачу»** → PUT на `/periodic_task/{id}/complete`.
2. **`completeHandler`:**
    - Помечает `completed = true`.
    - Возвращает `HomeView`.
3. **htmx заменяет `#app`** — задача исчезает из «В работе» (фильтр `completed == false`).

### Редактирование плана

1. **Пользователь кликает «✏️ Редактировать»** → GET на `/periodic_task/{id}/edit`.
2. **`editFormHandler`** рендерит форму редактирования плана.
3. **Пользователь меняет шаги** → PUT на `/periodic_task/{id}` с телом `token` и `plan[...]`.
4. **`editHandler`:**
    - Валидирует шаги плана (uuid уже декодирован в `UUID` на уровне маршрута).
    - Обновляет план.
    - Возвращает страницу задачи.
5. **htmx заменяет `#app`** — пользователь видит обновлённый план.

### Удаление задачи

1. **Пользователь кликает «🗑️ Удалить»** → confirm → DELETE на `/periodic_task/{id}`.
2. **`deleteHandler`:**
    - Удаляет задачу.
    - Возвращает `HomeView`.
3. **htmx заменяет `#app`** — задача исчезает из списка.

## Итог

Периодические задачи превращают рутину в **автоматический процесс**: пользователь настраивает шаблоны один раз, 
а система каждый день создаёт новые экземпляры. 
Это освобождает ментальное пространство для творчества и помогает не пропускать важные повторяющиеся действия.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
