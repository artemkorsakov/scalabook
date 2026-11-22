# Task Lifecycle Service

## Creating a Task

The process of creating a task is a fundamental operation in the individual development management system.
Each status in which a task is created imposes certain requirements on data completeness:

- **KnowledgeBase** and **Planned**: All basic fields must be filled in (name, description, task type, section, goal),
  but **no execution plan and dates are required**.
- **InProgress**: **All fields of the task must be fully filled in**, including start date and planned end date,
  except for the execution plan.

The execution plan is filled in when editing the task on a separate page for better user convenience
(space in the modal window is limited).

### Implementing the Creation Operation

The task lifecycle service (`TaskLifecycleService`) aggregates all necessary services for task validation and saving.

```dotty
final case class TaskLifecycleService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F], // For validating custom types
    sectionService: SectionService[F],   // For validating sections
    goalService: GoalService[F],         // For validating goals
    taskService: TaskService[F],         // For directly saving tasks
    historyService: HistoryService[F]    // For storing task history
):
  def create(task: CreateTask): F[TaskId] =
    // 1. Collect and validate all dependent entities
    validateTask(
      userId = task.userId,
      sectionId = task.sectionId,
      goalId = task.goalId,
      taskTypeId = task.taskTypeId,
      status = task.status,
      startDate = task.startDate,
      endDate = task.endDate
    ).flatMap:
      // All validation passed → create the task
      case Validated.Valid(_)        => taskService.create(task)
      case Validated.Invalid(errors) =>
        // Accumulated errors are combined and thrown as an exception
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
      // Convert Option to Validated with error
      sectionOpt
        .toValidNel(s"Section with id '$sectionId' not found"),
      goalOpt
        .toValidNel(s"Goal with id '$goalId' not found"),
      taskTypeOpt
        .toValidNel(s"User type with id '$taskTypeId' not found"),
      // Status validation
      validateStatus(
        status = status,
        startDate = startDate,
        endDate = endDate
      )
    )
      // Combine results: success only if all are Valid
      .mapN { (_, _, _, _) => () }
end TaskLifecycleService

// Companion object contains pure validation functions depending only on status.
object TaskLifecycleService:
  // Main status validation function. Determines what additional checks are needed.
  private[services] def validateStatus(
      status: Status,
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  ): ValidatedNel[String, Unit] =
    status match
      // For "In Progress" and "Completed" statuses, valid dates are required
      case Status.InProgress | Status.Completed =>
        validateDates(startDate = startDate, endDate = endDate)
      case Status.Planned | Status.KnowledgeBase =>
        Validated.valid(())

  // Check date correctness: both dates must be specified,
  // and end date must be after start date.
  private[services] def validateDates(
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  )
      : ValidatedNel[String, Unit] =
    (startDate, endDate) match
      case (Some(start), Some(end)) if end.isBefore(start) =>
        Validated.invalidNel(
          s"Error: start date $start is after end date $end"
        )
      case (Some(start), Some(end)) => Validated.valid(())
      case _                        =>
        Validated.invalidNel(
          "Start and end dates are required"
        )
```

Updating a task occurs similarly, but with an added check for the task's existence:

```dotty
def update(task: Task): F[Boolean] =
  taskService.get(task.id, task.userId).flatMap:
    case Some(_) =>
      validateTask(
        ...
      ).flatMap:
        // During update, execution plan is required for all statuses except "KnowledgeBase"
        case Validated.Valid(_)
            if task.status == KnowledgeBase || task.plan.steps.nonEmpty =>
          taskService.update(task)
        case Validated.Valid(_) =>
          MonadThrow[F].raiseError(
            new IllegalArgumentException(
              "Execution plan is required for tasks in \"In Progress\" and \"Planned\" status!"
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

## Enqueuing a Task

This operation moves a task from the idea stage ("KnowledgeBase") to the ready-to-execute stage ("Planned").
Key actions:

1. **Status change** from `KnowledgeBase` to `Planned`.
2. **Mandatory provision of an execution plan** — a task cannot be scheduled without a detailed plan.
3. **Assigning an ordinal number**: The system can suggest the first available ordinal numbers among tasks of the same type,
   to help the user organize the queue.

```dotty
def enqueue(
    id: TaskId,
    userId: UserId,
    ordinal: Ordinal,
): F[Boolean] =
  // 1. Find the task, checking its current status
  findTask(id, userId, Status.KnowledgeBase).flatMap: task =>
    // 2. Create an updated version: change status and attach the plan
    val updated = task.copy(
      status = Status.Planned,
      ordinal = ordinal
    )
    // 3. Save changes
    taskService.update(updated)

// Helper function for safely searching a task
// with pre-validation of status.
private def findTask(
    id: TaskId,
    userId: UserId,
    status: Status
): F[Task] =
  taskService.get(id, userId).flatMap:
    case Some(task) if task.status == status =>
      MonadThrow[F].pure(task) // Task found and has the expected status
    case Some(_) =>
      // Task found but in the wrong status (business logic error)
      MonadThrow[F].raiseError(
        new IllegalArgumentException(
          s"Task with id '$id' is not in '$status' state"
        )
      )
    case None =>
      // Task not found (data error)
      MonadThrow[F].raiseError(
        new IllegalArgumentException(s"Task with id '$id' not found")
      )
```

## Starting Work on a Task

This operation moves a task to an active state. It requires:

1.  **Set the start date** (the current date can be used as the default).
2.  **Set the planned completion date**, which must be logically valid (after the start date).

```dotty
def start(
    id: TaskId,
    userId: UserId,
    startDate: LocalDate,
    endDate: LocalDate
): F[Boolean] =
  // Preliminary date logic check at the service level
  if endDate.isBefore(startDate) then
    MonadThrow[F].raiseError(
      new IllegalArgumentException(
        s"End date $endDate is before start date $startDate"
      )
    )
  else
    // Search for a task in "Planned" status
    findTask(id, userId, Status.Planned).flatMap: task =>
      val updated = task.copy(
        status = Status.InProgress,
        startDate = startDate.some, // Record the start date
        endDate = endDate.some      // Record the deadline
      )
      taskService.update(updated)
```

## Deferring a Task

This operation allows temporarily removing a task from active execution without fully cancelling it.
This is useful when priorities change or blockers arise.

1.  **Status change** from `InProgress` back to `Planned`.
2.  **Clearing dates**: Start and end dates are cleared, as they will be different when taken into work again.

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

## Getting a Task

This operation returns detailed task information for display on a single page.
Unlike simple DB reading, here **data enrichment** is required — type, section, and goal identifiers
are replaced with their readable names, and derived fields are computed: plan progress,
duration descriptions, time spent, and remaining time.

To get a task, `TaskCard` is used — a "thick" model specifically designed for display.
It contains not only raw task data but also all UI representation fields.

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

The `get` method first loads reference data for types, sections, and goals for the user, then loads the task itself.
If any related entity is not found (e.g., the section was deleted), a fallback value
of `"—"` is used so the page does not crash with an error.

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

**What is additionally computed:**

- `completedCount` — how many plan steps are already completed.
- `count` — total number of steps.
- `progress` — completion percentage, rounded to two decimal places.
- `durationDesc`, `timeSpentDesc`, `remainingTimeDesc` — human-readable descriptions (`"2h 30min"`, `"45min"`, `"1d"`).
- `remainingTimeDesc` — difference between planned duration and time spent (can be negative if the estimate was exceeded).

## Getting a Task List

This method is used to display tables on the main page — in the tabs
"In Progress", "Planned", and "KnowledgeBase". Unlike `get`, which returns a single "thick" card,
here a **list of compact cards** is returned, plus reference data for types, sections, and goals for building filters.

In the presentation layer, these "raw" data are turned into different cards depending on the tab:

- `TableInProgressTaskCard` — "In Progress" tab;
- `TablePlannedTaskCard` — "Planned" tab;
- `TableKnowledgeBaseTaskCard` — "KnowledgeBase" tab.

Each card contains only the fields needed in its table. For example,
`TablePlannedTaskCard` carries name, type, section, goal, and ordinal number — precisely
what is needed for a table row and client-side filters:

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

And `TableKnowledgeBaseTaskCard` additionally contains duration (`duration`),
from which the size category is computed: less than 60 minutes — `Small` (🌱), less than 8 hours — `Medium` (🌳),
otherwise — `Large` (🌲). This is used for visual task grouping by size.

The `getAll` method supports **three optional filters**: by status, task type, and section.
They are passed to `taskService.getAll`, where they are transformed into a dynamic SQL query.
A distinctive feature: the service **does not combine** the cards, but returns "raw" results
`Vector[TaskResult]` along with reference data for types, sections, and goals. The transformation
`TaskResult` → card (`TablePlannedTaskCard.fromTaskResult`, etc.) happens in the presentation layer
`WelcomeViews`, where dictionaries of `id → name` are built and table rows are enriched.

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

**Return value** — a tuple of four elements:

1. `Vector[TaskResult]` — "raw" tasks for the table.
2. `Vector[TaskType]` — user's task types (for filtering and enrichment).
3. `Vector[Section]` — user's sections (for filtering and enrichment).
4. `Vector[Goal]` — user's goals (for filtering and enrichment).

This return value eliminates the need to make additional HTTP requests
to load reference data — everything comes together with the tasks.

**Why goals, and not just types and sections?** Besides enriching rows (substituting the goal name
instead of id), the list of goals is used for the "Goal" filter on the "Planned"
and "KnowledgeBase" tabs. `WelcomeViews` selects only goals that actually appear
in tasks of the tab, and passes them to the template as client filter options.

## Increasing Time Spent

This operation is called when the user stops the timer on the task page.
Unlike editing, where `timeSpent` is set directly, here the value is **added to** the already
accumulated time. This is important because the user may start the timer several times a day,
and each session should accumulate rather than overwrite the previous one.

The method takes the number of minutes (`timeSpent: Int`), converts it to `FiniteDuration`, and adds it
to the current `task.timeSpent`. If the task is not found or does not belong to the user, `false` is returned.

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

**Features:**

- **Returns `Boolean`**, not `Unit`, so the calling code can distinguish "task not found"
  from "successfully updated".

## Deleting a Task

An important and irreversible operation.
The lifecycle service delegates it directly to `TaskService`, which is responsible for persistent storage.
Before deletion, a check is performed that the task belongs to the user (`taskService.get(id, userId)`),
to prevent deletion of other users' data.

```dotty
def delete(id: TaskId, userId: UserId): F[Boolean] =
  taskService.get(id, userId).flatMap:
    case Some(_) => taskService.delete(id)
    case None    => MonadThrow[F].pure(false)
```

**Return value:**

- `true` — the task existed, belonged to the user, and was deleted.
- `false` — the task was not found or does not belong to the user.

---

**References:**

- [Source repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
