# Implementing the Task Service

## Implementing Special Type Transformations for Doobie

Doobie, as a type-safe database library,
requires explicit definition of transformations between Scala types and corresponding SQL types.
For most standard types (`Int`, `String`, `LocalDate`), these transformations are provided automatically.
However, for custom types and complex structures, transformations must be defined manually.

Types requiring special attention in our project:

1. **Enums**: `Status` — a custom enum with no direct SQL analog.
2. **Execution plan**: `Plan(steps: List[PlanStep])` — a list of steps to be stored as an array in PostgreSQL.
3. **Optional values**: `Option[LocalDate]` — a date that may be absent, requiring special NULL handling.
4. **Specialized measurement types**: `FiniteDuration` — a duration representation more efficiently stored as a number.

### Implementation Details of Transformations

#### 1. Transformation for the `Status` Enum

```dotty
// Domain type - task status enum
enum Status:
  case InProgress, Planned, KnowledgeBase, Completed

object Status:
  /** Manually defining the transformation between Status type and the string
    * representation in the DB. Doobie requires a Meta[Status] instance for
    * working with this type in queries.
    */
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**How the transformation works:**

- `Meta[String]` — Doobie's base transformer for string types.
- `.timap(f)(g)` — a method creating a bidirectional transformation:
    - `f: String => Status` — read function from DB (string → enum)
    - `g: Status => String` — write function to DB (enum → string)

This solution is safe under the following conditions:

- **Controlled writes**: Only our code writes status table data.
- **Input validation**: All status values are validated before writing.
- **Enum stability**: Case object names do not change arbitrarily.

**Risks during system evolution:**

Changing the enum requires coordinated action:

```dotty
enum Status:
  case InProgress, Planned, KnowledgeBase
  // Adding a new status - safe
  case Completed

  // Renaming - DANGEROUS!
  // case InProgress -> case InWork 
  // Requires data migration, as InProgress remains in the DB!
```

#### 2. Transformation for `Plan`

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

`Plan` stores a list of `PlanStep` steps, but there is no separate table for them in the database —
instead, a `VARCHAR[]` (string array) column is used.
Each step is serialized to a JSON string via `PlanStep.asJson.noSpaces`, and the plan is saved as an array of such strings.
This allows storing a complex nested structure in a single column without additional tables and JOINs,
and the JSON format makes the data readable and extensible.

When reading from the database, Doobie applies `unliftedStringArrayType.imap`:
the string array from the DB is deserialized back into `List[PlanStep]` via `decode[PlanStep]`.
The `imap` (invariant map) operator describes a two-way transformation — from `Array[String]` to `Plan` and back,
allowing `Meta[Plan]` to be used automatically in `sql"..."` queries without manual parsing.
Importantly, during deserialization, `decode[PlanStep]` parsing errors are silently ignored via `.toOption` —
invalid steps are simply skipped, making reads robust against corrupted data.

#### 3. Transformations for `Option[LocalDate]` and `FiniteDuration`

```dotty
// File: pdp/src/main/scala/ru/scalabook/pdp/models/src.scala
/** Transformation for an optional date. Option[LocalDate] corresponds
  * to a nullable DATE column in PostgreSQL.
  */
given Meta[Option[LocalDate]] =
  Meta[LocalDate].imap[Option[LocalDate]](
    // From DB (not NULL) -> Some(date)
    // From DB (NULL) -> None
    date => Option(date) 
  )(
    // To DB: Some(date) -> date, None -> NULL
    _.orNull             
  )

/** Transformation for duration. Stored as Long (minutes). */
given Meta[FiniteDuration] =
  Meta[Long].timap(
    // From DB: Long -> FiniteDuration
    FiniteDuration(_, MINUTES)
  )(
    // To DB: FiniteDuration -> Long
    _.toMinutes
  )
```

**Justification of chosen representations:**

- **`Option[LocalDate]`**: Using `null` in the DB to represent an absent value is an idiomatic approach in SQL,
  which is effectively supported by indexes and queries.
- **`FiniteDuration` as `Long`**: Storing duration in minutes as an integer.
    - Higher precision (seconds, milliseconds) is not needed for task duration.

## Principles of Organizing Implicit Instances

When the Scala 3 compiler searches for an implicit instance (given instance), it follows a specific search order.
Placing instances in the right locations is critical for predictability of the system's operation and preventing conflicts.

##### Level 1: **Companion object of the data type (Highest priority)**

```dotty
enum Status:
  case InProgress, Planned, KnowledgeBase, Completed

// Ideal placement - in the companion object of the type itself
object Status:
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Advantages:**

- **Automatic discovery**: The compiler will always find the instance when working with `Status` type.
- **Single source of truth**: Guarantees that only one instance exists for the given type.
- **Semantic grouping**: Everything related to the type is in one place.
- **Easy to find**: Developers intuitively search for transformations in the type's companion object.

##### Level 2: **Companion object of the type class**

```dotty
// Alternative - if we cannot modify the source type
object Meta:  // Companion object of the Meta type class
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
```

**Application:**

- For types from external libraries that we cannot modify.
- For grouping instances by functional characteristics.
- When one type requires multiple instances for different contexts (rare case).

##### Level 3: **Specialized object with explicit import**

```dotty
// Create an explicit container for instances
object DatabaseTypeConversions:
  given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)
  given Meta[List[TaskStep]] = ...

// Import explicitly where needed
import DatabaseTypeConversions.given
```

**Usage:**

- For organizing instances by domain areas.
- When explicit control over which instances are available is needed.
- For modular testing with different sets of instances.

#### The Danger of Orphan Instances

An [orphan instance](https://pjrt.medium.com/orphan-instances-in-scala-322caa78e382) is a `given` instance
declared outside the companion object of the data type and outside the companion object of the type class.
Such instances create serious problems in code maintenance.

##### ❌ Example of dangerous placement:

```dotty
// File: src/main/scala/utils/SomeUtility.scala
package utils

// Orphan instance
given Meta[Status] =
  Meta[String].timap(Status.valueOf)(_.toString)

// Problems:
// 1. Not associated with either Status or Meta
// 2. Can be declared in multiple places
// 3. Hard to find during debugging
```

##### Problems caused by orphan instances:

1. **Difficulty in discovery and understanding**:

    ```dotty
    // Code works "magically"
    val status: Status = sql"SELECT status FROM tasks".query[Status].unique
    // Where does the transformation come from? Where to look?
    ```

2. **Conflicts and ambiguities**:

    ```dotty
    // File A.scala
    given Meta[Status] = Meta[String].timap(Status.valueOf)(_.toString)

    // File B.scala
    given Meta[Status] = Meta[Int].timap(i => Status.values(i))(_.ordinal)

    // Result: "ambiguous implicit values" compile error
    ```

3. **Unpredictable behavior**:

    ```dotty
    // Depending on the import order, different instances may be used
    import A.given  // String representation is used
    import B.given  // Conflict! Which instance will the compiler choose?
    ```

4. **Difficulties in refactoring and maintenance**:
    - When moving types between modules, instances may get "lost".
    - Building large projects may depend on subtle compilation order issues.
    - Automated refactoring tools may not account for orphan instances.

## Implementing the Task Service

The `PostgreSQLTaskService` class is an implementation of the `TaskService[F]` trait,
working with the `tasks` table in PostgreSQL via the **Doobie** library.

### Review of Each Method

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

**What happens:**

- An `INSERT` is executed with all task fields. Values are passed via `${...}` — Doobie escapes them
  and substitutes them as prepared statement parameters, protecting against SQL injection.
- **`${task.status}::StatusEnum`** — explicit cast to PostgreSQL type `StatusEnum`.
  This is necessary because status in Scala is an `enum`/`sealed trait`, while in the database it's a custom type.
  Without the cast, PostgreSQL may not understand how to convert the string to an enum.
- **`RETURNING id`** — PostgreSQL returns the generated `UUID` of the new record directly from the query.
  This avoids a second query to obtain the ID.
- **`.query[TaskId].unique`** — Doobie expects exactly one row in the response (`.unique`), otherwise it throws an error.
  Reads the single value as `TaskId`.
- **`.transact(xa)`** — executes the query in a transaction on the given `Transactor`.

**Returns:** `F[TaskId]` — the ID of the created task.

#### `get(id: TaskId, userId: UserId): F[Option[Task]]`

```dotty
def get(id: TaskId, userId: UserId): F[Option[Task]] =
  sql"""SELECT id, name, user_id, task_type_id, status, section_id, goal_id, ordinal, start_date, end_date, information, plan, duration, time_spent FROM tasks WHERE id = $id AND user_id = $userId"""
    .query[Task]
    .option
    .transact(xa)
```

**What happens:**

- `SELECT` by primary key **with a filter by owner** (`AND user_id = $userId`) — the service itself does not return other users' tasks. All fields are listed explicitly — this is good practice to avoid depending on column order in the table.
- **`.query[Task].option`** — Doobie tries to read the result as `Task`. If no rows exist, returns `None`; if rows exist — `Some(task)`.
- The method **does not throw an exception** when the task is not found — this is important for correct handling in the HTTP layer (e.g., to return 404).

**Returns:** `F[Option[Task]]` — `None` if the task is not found.

#### `getByUser(userId: UserId): F[Vector[Task]]`

```dotty
def getByUser(userId: UserId): F[Vector[Task]] =
  sql"""SELECT id, name, user_id, task_type_id, status, section_id, goal_id, ordinal, start_date, end_date, information, plan, duration, time_spent FROM tasks WHERE user_id = $userId"""
    .query[Task].to[Vector].transact(xa)
```

**What happens:**

- `SELECT` of all tasks for a specific user.
- **`.to[Vector]`** — collects all result rows into `Vector[Task]`. Doobie supports `List`, `Vector`, `Set`, and other collections.
- Returns an **empty `Vector`** if the user has no tasks (not `None` — this is convenient for displaying a list).

**Returns:** `F[Vector[Task]]` — the user's task list.

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

**What happens:**

- `UPDATE` of all task fields by its `id`.
- **`.update.run`** — executes the query as `Update0`, which returns the number of affected rows (`Int`).
- **`.map(_ > 0)`** — converts the count to `Boolean`: `true` if a row was updated, `false` if no task with that `id` was found.
- Note the `::StatusEnum` cast — same as in `create`.

**Returns:** `F[Boolean]` — `true` if the record existed and was updated.

#### `delete(id: TaskId): F[Boolean]`

```dotty
def delete(id: TaskId): F[Boolean] =
  sql"""DELETE FROM tasks WHERE id = $id"""
    .update.run.transact(xa).map(_ > 0)
```

**What happens:**

- `DELETE` by `id`. Similar to `update`, returns the number of affected rows.
- **`.map(_ > 0)`** — `true` if something was deleted; `false` if the task did not exist.

**Returns:** `F[Boolean]` — `true` if the record was deleted.

This service is a typical example of a **repository** in hexagonal architecture: it isolates database operations from business logic,
which simplifies testing (you can replace it with an in-memory implementation) and swapping the storage (e.g., to MongoDB or Cassandra).

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

**What happens:**

- Constructs a dynamic `SELECT` from multiple `Fragment`s via `++` concatenation,
  where filters by status, section, and type are added only when the corresponding `Option` is not empty.
- Each result row is mapped to `TaskResult` — a compact task projection
  (without `information`, `timeSpent`, but with computed `progress = plan.progress` and `duration`).
  This is what is displayed in the task list on the dashboard.

**Returns:** `F[Vector[TaskResult]]` — filtered tasks for the user.

#### `existsSection(id, userId)` / `existsGoal(id, userId)` / `existsTaskType(id, userId)`

```dotty
def existsSection(id: SectionId, userId: UserId): F[Boolean] =
  sql"""SELECT 1 FROM tasks WHERE section_id = $id AND user_id = $userId LIMIT 1;"""
    .query[Int]
    .option
    .transact(xa)
    .map(_.isDefined)
```

**What happens:**

- Checks if there is at least one task referencing the section/goal/type.
  The `existsGoal` and `existsTaskType` methods are structured identically, differing only in the column (`goal_id`, `task_type_id`).
- `.option` + `.map(_.isDefined)` — returns `true` if a record is found.

**Returns:** `F[Boolean]`. Used in domain services to prevent deletion
of a section/goal/type while tasks reference it.

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

**What happens:**

- Updates the ordinal numbers of a set of tasks in a **single transaction** (batch update via `foldLeft` and `*>`).
- If at least one task was not updated, the entire operation rolls back.

**Returns:** `F[Boolean]` — `true` if all tasks were updated.

---

**References:**

- [Source repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
