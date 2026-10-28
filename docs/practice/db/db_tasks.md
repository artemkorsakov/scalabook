{%
showPrev = true
prevPage = db_crud.html
showNext = true
nextPage = db_test.html
%}

# Реализация сервиса работы с задачами

## Реализация специальных преобразований типов для работы с Doobie

Doobie, как библиотека для типобезопасной работы с БД, 
требует явного определения преобразований между типами Scala и соответствующими SQL-типами. 
Для большинства стандартных типов (`Int`, `String`, `LocalDate`) эти преобразования предоставляются автоматически. 
Однако для пользовательских типов и сложных структур необходимо определить преобразования вручную.

Типы, требующие специального внимания в нашем проекте:

1. **Перечисления (enum)**: `Status` — пользовательский enum, не имеющий прямого аналога в SQL.
2. **План выполнения**: `Plan(steps: List[PlanStep])` — список шагов, которые должны храниться как массив в PostgreSQL.
3. **Опциональные значения**: `Option[LocalDate]` — дата, которая может отсутствовать, требует особой обработки NULL.
4. **Специализированные типы измерений**: `FiniteDuration` — представление длительности, которое эффективнее хранить как число.

### Детали реализации преобразований

#### 1. Преобразование для перечисления `Status`

```dotty
// Доменный тип - перечисление статусов задачи
enum Status:
  case InProgress, Planned, KnowledgeBase, Completed

object Status:
  /** Ручное определение преобразования между типом Status и строковым
    * представлением в БД. Doobie требует наличия экземпляра Meta[Status] для
    * работы с этим типом в запросах.
    */
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Механизм работы преобразования:**

- `Meta[String]` — базовый преобразователь Doobie для строкового типа.
- `.timap(f)(g)` — метод, создающий двунаправленное преобразование:
    - `f: String => Status` — функция чтения из БД (строка → enum)
    - `g: Status => String` — функция записи в БД (enum → строка)

Данное решение безопасно при следующих условиях:

- **Контролируемая запись**: Только наш код пишет данные в таблицы статусов.
- **Валидация на входе**: Все значения статусов проходят валидацию перед записью.
- **Стабильность enum**: Имена case-объектов произвольно не меняются.

**Риски при эволюции системы:**

Изменение перечисления требует скоординированных действий:

```dotty
enum Status:
  case InProgress, Planned, KnowledgeBase
  // Добавление нового статуса - безопасно
  case Completed

  // Переименование - ОПАСНО!
  // case InProgress -> case InWork 
  // Требует миграции данных, т.к. в БД остается InProgress!
```

#### 2. Преобразование для `Plan`

```dotty
object Plan:
  given Meta[Plan] =
    unliftedStringArrayType.imap(array =>
      Plan(array.flatMap(step => decode[PlanStep](step).toOption).toList)
    )(plan =>
      plan.steps.map(step => step.asJson.noSpaces).toArray
    )
    
final case class PlanStep(
    number: StepNumber,
    step: StepName,
    completed: Boolean
) derives Decoder, Encoder    
```

`Plan` хранит список шагов `PlanStep`, но в базе данных нет отдельной таблицы для них — 
вместо этого используется колонка типа `VARCHAR[]` (массив строк). 
Каждый шаг сериализуется в JSON-строку через `PlanStep.asJson.noSpaces`, а сам план сохраняется как массив таких строк. 
Это позволяет хранить сложную вложенную структуру в одной колонке без дополнительных таблиц и JOIN-ов, 
а формат JSON делает данные читаемыми и расширяемыми.

При чтении из базы Doobie применяет `unliftedStringArrayType.imap`: 
массив строк из БД десериализуется обратно в `List[PlanStep]` через `decode[PlanStep]`. 
Оператор `imap` (invariant map) описывает двустороннее преобразование — из `Array[String]` в `Plan` и обратно, 
что позволяет `Meta[Plan]` автоматически использоваться в запросах `sql"..."` без ручного парсинга. 
Важно отметить, что при десериализации ошибки парсинга `decode[PlanStep]` молча игнорируются через `.toOption` — 
некорректные шаги просто пропускаются, что делает чтение устойчивым к повреждённым данным.

#### 3. Преобразования для `Option[LocalDate]` и `FiniteDuration`

```dotty
// Файл: pdp/src/main/scala/ru/scalabook/pdp/models/src.scala
/** Преобразование для опциональной даты. Option[LocalDate] соответствует
  * nullable колонке DATE в PostgreSQL.
  */
given Meta[Option[LocalDate]] =
  Meta[LocalDate].imap[Option[LocalDate]](
    // Из БД (не NULL) -> Some(date)
    // Из БД (NULL) -> None
    date => Option(date) 
  )(
    // В БД: Some(date) -> date, None -> NULL
    _.orNull             
  )

/** Преобразование для длительности. Храним как Long (минуты). */
given Meta[FiniteDuration] =
  Meta[Long].timap(
    // Из БД: Long -> FiniteDuration
    FiniteDuration(_, MINUTES)
  )(
    // В БД: FiniteDuration -> Long
    _.toMinutes
  )
```

**Обоснование выбранных представлений:**

- **`Option[LocalDate]`**: Использование `null` в БД для представления отсутствующего значения — это идиоматичный подход в SQL, 
  который эффективно поддерживается индексами и запросами.
- **`FiniteDuration` как `Long`**: Хранение длительности в минутах как целого числа.
    - Большая точность (секунды, миллисекунды) для продолжительности задач не требуется.

## Принципы организации неявных экземпляров

Когда компилятор Scala 3 ищет неявный экземпляр (given instance), он следует определенному порядку поиска. 
Размещение экземпляров в правильных местах критически важно для предсказуемости работы системы и предотвращения конфликтов.

##### Уровень 1: **Сопутствующий объект типа данных (Наивысший приоритет)**

```dotty
enum Status:
  case InProgress, Planned, KnowledgeBase, Completed

// Идеальное размещение - в сопутствующем объекте самого типа
object Status:
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Преимущества:**

- **Автоматическое обнаружение**: Компилятор всегда найдет экземпляр при работе с типом `Status`.
- **Единственность источника**: Гарантирует, что существует только один экземпляр для данного типа.
- **Семантическая группировка**: Все связанное с типом находится в одном месте.
- **Простота поиска**: Разработчики интуитивно ищут преобразования в сопутствующем объекте типа.

##### Уровень 2: **Сопутствующий объект класса типов**

```dotty
// Альтернатива - если не можем изменить исходный тип
object Meta:  // Сопутствующий объект класса типов Meta
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Применение:**

- Для типов из внешних библиотек, которые мы не можем изменить.
- Для группировки экземпляров по функциональному признаку.
- Когда один тип требует нескольких экземпляров для разных контекстов (редкий случай).

##### Уровень 3: **Специализированный объект с явным импортом**

```dotty
// Создаем явный контейнер для экземпляров
object DatabaseTypeConversions:
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
  given Meta[List[TaskStep]] = ...

// Явно импортируем там, где нужно
import DatabaseTypeConversions.given
```

**Использование:**

- Для организации экземпляров по доменным областям.
- Когда нужен явный контроль над тем, какие экземпляры доступны.
- Для модульного тестирования с разными наборами экземпляров.

#### Опасность сиротствующих экземпляров (Orphan Instances)

[Сиротствующий экземпляр](https://pjrt.medium.com/orphan-instances-in-scala-322caa78e382) — это given instance, 
который объявлен вне сопутствующего объекта типа данных и вне сопутствующего объекта класса типов. 
Такие экземпляры создают серьезные проблемы в поддержке кода.

##### ❌ Пример опасного размещения:

```dotty
// Файл: src/main/scala/utils/SomeUtility.scala
package utils

// Сиротствующий экземпляр
given Meta[Status] =
  Meta[String].timap(Status.valueOf)(_.toString)

// Проблемы:
// 1. Не связано ни с Status, ни с Meta
// 2. Может быть объявлено в нескольких местах
// 3. Трудно найти при отладке
```

##### Проблемы, создаваемые сиротствующими экземплярами:

1. **Сложность обнаружения и понимания**:

   ```dotty
   // Код работает "магически"
   val status: Status = sql"SELECT status FROM tasks".query[Status].unique
   // Откуда берется преобразование? Где искать?
   ```

2. **Конфликты и неоднозначности**:

   ```dotty
   // Файл A.scala
   given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)

   // Файл B.scala
   given Meta[Status] = Meta[Int].timap(i => Status.values(i))(_.ordinal)

   // Результат: ошибка компиляции "ambiguous implicit values"
   ```

3. **Непредсказуемое поведение**:

   ```dotty
   // В зависимости от порядка импортов может использоваться разный экземпляр
   import A.given  // Используется строковое представление
   import B.given  // Конфликт! Какой экземпляр выберет компилятор?
   ```

4. **Сложности при рефакторинге и поддержке**:
    - При перемещении типов между модулями экземпляры могут "потеряться".
    - Сборка больших проектов может зависеть от тонкостей порядка компиляции.
    - Автоматический рефакторинг инструментов может не учитывать сиротствующие экземпляры.

## Реализация сервиса работы с задачами

Класс `PostgreSQLTaskService` — это реализация трейта `TaskService[F]`, 
которая работает с таблицей `tasks` в PostgreSQL через библиотеку **Doobie**. 

### Разбор каждого метода

#### `create(task: CreateTask): F[TaskId]`

```dotty
def create(task: CreateTask): F[TaskId] =
  sql"""INSERT INTO tasks (name, user_id, task_type_id, status, section_id, goal_id, ordinal, start_date, end_date, information, plan, duration, time_spent)
       VALUES (${task.name}, ${task.userId}, ${task.taskTypeId}, ${task.status}::StatusEnum, ${task.sectionId}, ${task.goalId}, ${task.ordinal}, ${task.startDate}, ${task.endDate}, ${task.information}, ${task.plan}, ${task.duration}, ${task.timeSpent})
       RETURNING id"""
    .query[TaskId]
    .unique
    .transact(xa)
```

**Что происходит:**

- Выполняется `INSERT` со всеми полями задачи. Значения передаются через `${...}` — Doobie сам экранирует их 
  и подставляет как параметры prepared statement, что защищает от SQL-инъекций.
- **`${task.status}::StatusEnum`** — явное приведение к PostgreSQL-типу `StatusEnum`. 
  Это необходимо, потому что статус в Scala — это `enum`/`sealed trait`, а в базе — пользовательский тип. 
  Без каста PostgreSQL может не понять, как преобразовать строку в enum.
- **`RETURNING id`** — PostgreSQL возвращает сгенерированный `UUID` новой записи прямо из запроса. 
  Это позволяет избежать второго запроса для получения ID.
- **`.query[TaskId].unique`** — Doobie ожидает ровно одну строку в ответ (`.unique`), иначе бросит ошибку. 
  Читает единственное значение как `TaskId`.
- **`.transact(xa)`** — выполняет запрос в транзакции на указанном `Transactor`.

**Возвращает:** `F[TaskId]` — идентификатор созданной задачи.

#### `get(id: TaskId): F[Option[Task]]`

```dotty
def get(id: TaskId): F[Option[Task]] =
  sql"""SELECT id, name, user_id, task_type_id, status, section_id, goal_id, ordinal, start_date, end_date, information, plan, duration, time_spent FROM tasks WHERE id = $id"""
    .query[Task]
    .option
    .transact(xa)
```

**Что происходит:**

- `SELECT` по первичному ключу. Все поля перечислены явно — это хорошая практика, чтобы не зависеть от порядка колонок в таблице.
- **`.query[Task].option`** — Doobie пытается прочитать результат как `Task`. Если строк нет, возвращается `None`; если есть — `Some(task)`.
- Метод **не бросает исключение** при отсутствии задачи — это важно для корректной обработки в HTTP-слое (например, чтобы вернуть 404).

**Возвращает:** `F[Option[Task]]` — `None`, если задача не найдена.

#### `getByUser(userId: UserId): F[Vector[Task]]`

```dotty
def getByUser(userId: UserId): F[Vector[Task]] =
  sql"""SELECT id, name, user_id, task_type_id, status, section_id, goal_id, ordinal, start_date, end_date, information, plan, duration, time_spent FROM tasks WHERE user_id = $userId"""
    .query[Task].to[Vector].transact(xa)
```

**Что происходит:**

- `SELECT` всех задач конкретного пользователя.
- **`.to[Vector]`** — собирает все строки результата в `Vector[Task]`. Doobie поддерживает `List`, `Vector`, `Set` и другие коллекции.
- Возвращает **пустой `Vector`**, если у пользователя нет задач (а не `None` — это удобно для отображения списка).

**Возвращает:** `F[Vector[Task]]` — список задач пользователя.

#### `update(task: Task): F[Boolean]`

```dotty
def update(task: Task): F[Boolean] =
  sql"""UPDATE tasks
       SET name = ${task.name}, user_id = ${task.userId}, task_type_id = ${task.taskTypeId}, status = ${task.status}::StatusEnum,
       section_id = ${task.sectionId}, goal_id = ${task.goalId}, ordinal = ${task.ordinal},
       start_date = ${task.startDate}, end_date = ${task.endDate}, information = ${task.information},
       plan = ${task.plan}, duration = ${task.duration}, time_spent = ${task.timeSpent} WHERE id = ${task.id}"""
    .update.run.transact(xa).map(_ > 0)
```

**Что происходит:**

- `UPDATE` всех полей задачи по её `id`.
- **`.update.run`** — выполняет запрос как `Update0`, который возвращает количество затронутых строк (`Int`).
- **`.map(_ > 0)`** — преобразует число в `Boolean`: `true`, если строка была обновлена, `false` — если задача с таким `id` не найдена.
- Обратите внимание на `::StatusEnum` — то же приведение, что и в `create`.

**Возвращает:** `F[Boolean]` — `true`, если запись существовала и была обновлена.

#### `delete(id: TaskId): F[Boolean]`

```dotty
def delete(id: TaskId): F[Boolean] =
  sql"""DELETE FROM tasks WHERE id = $id"""
    .update.run.transact(xa).map(_ > 0)
```

**Что происходит:**

- `DELETE` по `id`. Аналогично `update`, возвращает количество затронутых строк.
- **`.map(_ > 0)`** — `true`, если что-то удалили; `false`, если задачи не было.

**Возвращает:** `F[Boolean]` — `true`, если запись была удалена.

Такой сервис — типичный пример **репозитория** в гексагональной архитектуре: он изолирует работу с БД от бизнес-логики, 
что упрощает тестирование (можно подменить на in-memory реализацию) и замену хранилища (например, на MongoDB или Cassandra).

#### `getAll(userId, maybeStatus, maybeTaskType, maybeSectionId): F[Vector[TaskResult]]`

```dotty
def getAll(
    userId: UserId,
    maybeStatus: Option[Status],
    maybeTaskTypeId: Option[TaskTypeId],
    maybeSectionId: Option[SectionId]
): F[Vector[TaskResult]] =
  getTasks(
    userId = userId,
    maybeStatus = maybeStatus,
    maybeSectionId = maybeSectionId,
    maybeTaskTypeId = maybeTaskTypeId
  ).transact(xa)
```

**Что происходит:**

- Формирует динамический `SELECT` из нескольких `Fragment`‑ов через конкатенацию `++`, 
  где фильтры по статусу, разделу и типу добавляются только тогда, когда соответствующие `Option` не пусты.
- Каждая строка результата отображается в `TaskResult` — компактную проекцию задачи 
  (без `information`, `timeSpent`, но с вычисленным `progress = plan.progress` и `duration`). 
  Это то, что отображается в списке задач на дашборде.

**Возвращает:** `F[Vector[TaskResult]]` — отфильтрованные задачи пользователя.

#### `existsSection(id, userId)` / `existsGoal(id, userId)` / `existsTaskType(id, userId)`

```dotty
def existsSection(id: SectionId, userId: UserId): F[Boolean] =
  sql"""SELECT 1 FROM tasks WHERE section_id = $id AND user_id = $userId LIMIT 1;"""
    .query[Int]
    .option
    .transact(xa)
    .map(_.isDefined)
```

**Что происходит:**

- Проверяет, есть ли хоть одна задача, ссылающаяся на раздел/цель/тип. 
  Методы `existsGoal` и `existsTaskType` устроены идентично, отличаясь лишь колонкой (`goal_id`, `task_type_id`).
- `.option` + `.map(_.isDefined)` — возвращает `true`, если запись найдена.

**Возвращает:** `F[Boolean]`. Используются в доменных сервисах для запрета удаления 
раздела/цели/типа, пока на них ссылаются задачи.

#### `updateOrdinal(ordinals: NonEmptyVector[(TaskId, Ordinal)]): F[Boolean]`

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

**Что происходит:**

- Обновляет порядковые номера набора задач в **одной транзакции** (batch update через `foldLeft` и `*>`).
- Если хотя бы одна задача не обновилась, вся операция откатывается.

**Возвращает:** `F[Boolean]` — `true`, если обновлены все задачи.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
