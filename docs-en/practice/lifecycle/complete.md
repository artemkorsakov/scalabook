# Completing a Task

Completion is a key stage in the task lifecycle that summarizes the completed work,
allows evaluating results, and enriches statistics for future analysis.
This process creates a structured record of achievement, enriched with metadata and subjective assessment.

## "Complete Task" Operation

The "Complete" button is available **only for tasks in "In Progress" status**,
which ensures business process correctness: you cannot complete a task that has not been started or is only planned.

When completing a task, the user fills in the following fields:

- **Short task review** — a textual description of the work done, key insights, difficulties encountered, and results obtained.
- **Rating (⭐️ 1-5)** — a subjective assessment of usefulness, complexity, or satisfaction with task completion.
- **Actual completion date** — the real date of work completion, which may differ from the planned date.

### Saving to History

For subsequent analysis and accounting of completed work, the system saves a **structured snapshot of the completed task** to history, including:

1. **Identification data:**
    - Task type (book, article, course, etc.)
    - Unique task identifier (TaskId)
    - Task name
2. **Execution context:**
    - Section the task belongs to
    - Goal the task is directed toward
3. **Actual execution metrics:**
    - Actual execution duration
    - Start date and actual end date
4. **Results and assessment:**
    - User's rating
    - Short review

## Completed Task Data Model

The `CompletedTask` model encapsulates all data needed for long-term storage of information about completed tasks.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/CompletedTask.scala
/** A completed task model, representing an immutable snapshot of the task's state
  * at the moment of its completion. 
  */
final case class CompletedTask(
    taskId: TaskId,
    taskTypeId: TaskTypeId,
    name: TaskName,
    userId: UserId,
    sectionId: SectionId,
    goalId: GoalId,
    duration: FiniteDuration,
    rating: Rating,
    review: Review,
    startDate: LocalDate,
    endDate: LocalDate
)

object CompletedTask:
  /** Enumeration of possible task ratings. */
  enum Rating(val r: Byte):
    case One   extends Rating(1)
    case Two   extends Rating(2)
    case Three extends Rating(3)
    case Four  extends Rating(4)
    case Five  extends Rating(5)

  /** Meta[Rating] instance for Doobie, ensuring transformation
    * between the Scala enum and integer representation in the DB.
    */
  given Meta[Rating] =
    Meta[Int]
      .timap(i =>
        Rating.values.find(_.r.toInt == i).getOrElse(Rating.One)
      )(_.r)

  /** A refined type for review text with validity guarantees: non-empty string
    * without leading/trailing whitespace.
    */
  type Review = Review.T
  object Review extends RefinedType[String, MinLength[1] & Trimmed]
```

## History Service for Tasks

A separate service is defined for working with completed task history, following the single responsibility principle.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/HistoryService.scala
private[services] trait HistoryService[F[_]]:
  def create(task: CompletedTask): F[Int]

  def getByUser(userId: UserId): F[Vector[CompletedTask]]
```

### PostgreSQL Implementation of HistoryService

```dotty
private[pdp] class PostgreSQLHistoryService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends HistoryService[F]:

  def create(task: CompletedTask): F[Int] =
    sql"""INSERT INTO completed_tasks (task_id, task_type_id, name, user_id, section_id, goal_id, duration, rating, review, start_date, end_date) 
         VALUES (${task.taskId}, ${task.taskTypeId}, ${task.name}, ${task.userId}, ${task.sectionId}, ${task.goalId}, ${task.duration}, ${task.rating}, ${task.review}, ${task.startDate}, ${task.endDate}) 
         RETURNING id"""
      .query[Int]   // Query returns the ID of the inserted record
      .unique       // Expect exactly one result
      .transact(xa) // Execute in a transactional context

  def getByUser(userId: UserId): F[Vector[CompletedTask]] =
    fr"""SELECT task_id, task_type_id, name, user_id, section_id, goal_id, duration, rating, review, start_date, end_date 
         FROM completed_tasks WHERE user_id = $userId"""
      .query[CompletedTask] // Use automatic mapping via Meta
      .to[Vector]           // Convert result stream to a list
      .transact(xa)         // Execute the query
```

### Integrating HistoryService into the Task Lifecycle

The history service is added as a dependency to `TaskLifecycleService`,
allowing coordination of task completion and saving it to the archive.

```dotty
final case class TaskLifecycleService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F],
    sectionService: SectionService[F],
    goalService: GoalService[F],
    taskService: TaskService[F],
    historyService: HistoryService[F]  // New service for working with history
):
```

## Task Completion Command

The `complete` method encapsulates all task completion logic: validation, creating a historical record, and updating the status of the original task.

```dotty
/** Completes a task in "In Progress" status, saving its results
  * to history and updating the status to "Completed".
  */
def complete(
    id: TaskId,
    userId: UserId,
    rating: Rating,
    review: Review,
    endDate: LocalDate,
    duration: FiniteDuration
): F[Boolean] =
  for
    // Find and validate the task
    task <- findTask(id, userId, Status.InProgress)
    
    // If start date is absent or later than the end date,
    // set it to the end date
    startDate = task.startDate.filter(_.isBefore(endDate)).getOrElse(endDate)
    
    // Create a snapshot of the completed task for history
    completedTask =
      CompletedTask(
        taskId = task.id,
        taskTypeId = task.taskTypeId,
        name = task.name,
        userId = task.userId,
        sectionId = task.sectionId,
        goalId = task.goalId,
        duration = duration,
        rating = rating,
        review = review,
        startDate = startDate,
        endDate = endDate
      )
      
    // Save the task to history (independent operation)
    _ <- historyService.create(completedTask)
    
    // Update the original task's status to "Completed"
    updated = task.copy(
      status = Status.Completed,
      startDate = completedTask.startDate.some,
      endDate = completedTask.endDate.some,
      duration = duration
    )
    updateRes <- taskService.update(updated)
  yield updateRes
end complete
```

## Database Migrations

Adding the `completed_tasks` table requires creating the corresponding migration, similar to other tables in the system.

```dotty
private[pdp] class PostgreSQLMigrationService[F[_]: MonadCancelThrow](
  xa: Transactor[F]
):
  ...
  
  def initialize(): IO[Boolean] =
    val program =
      for
        ...
        _ <- createCompletedTasksTable.update.run
        _ <- addSectionFKToTable("completed_tasks").update.run
        _ <- addGoalFKToTable("completed_tasks").update.run
        _ <- addUsersFKToTable("completed_tasks").update.run
      yield true
    program.transact(xa)
    
  private val createCompletedTasksTable: Fragment =
    sql"""CREATE TABLE IF NOT EXISTS completed_tasks (
         id SERIAL PRIMARY KEY, 
         task_id UUID NOT NULL, 
         task_type_id INT NOT NULL, 
         name VARCHAR(255) NOT NULL, 
         user_id UUID NOT NULL, 
         section_id INT NOT NULL, 
         goal_id INT NOT NULL, 
         duration BIGINT NOT NULL, 
         rating INT NOT NULL, 
         review TEXT, 
         start_date DATE, 
         end_date DATE);"""  
```

This approach to task completion ensures not only correct status changes,
but also the creation of a valuable archive of completed work for subsequent analysis and tracking progress over the long term.

---

**References:**

- [Source repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
