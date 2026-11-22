# Task Lifecycle

After successfully defining the algebras (interface of operations) and their interpreters (implementations for specific effects), 
a key design stage arrives — creating programs that encapsulate business logic. 
These programs, built on top of algebra abstractions, ensure portability, testability, and purity of the domain.

## Settings Management Service

In the individual development system, one of the fundamental subsystems is the settings service. 
It is responsible for managing metadata and task structure: 
user task types, global goals, and sections.

### Architecture and Composition

The service is implemented as an immutable case class that aggregates more specialized services. 
This demonstrates the principle of composition and separation of concerns:

```dotty
final case class SettingsService[F[_]: MonadThrow](
    taskTypeService: TaskTypeService[F], // Service for working with task types
    sectionService: SectionService[F],   // Service for working with sections
    goalService: GoalService[F],         // Service for working with goals
    taskService: TaskService[F],         // Task service
    sourcesService: SourcesService[F],   // Service for working with sources (knowledge base)
    periodicStepService: PeriodicStepService[F] // Service for periodic task steps
)
```

The contextual boundary `MonadThrow` equips the service with the ability to work with effects that 
support sequential computations (`Monad`) and 
error handling through throwing exceptions within the effect context (`Throw`).

### Functional Responsibility

`SettingsService` provides a full CRUD (Create, Read, Update, Delete) cycle, as well as reorder operations for each entity:

- **Creating** new types, goals, and sections.
- **Reading/getting** lists of existing entities.
- **Updating** attributes of existing entities.
- **Reordering** the sequence of entities (e.g., for display in UI).
- **Deleting** entities.

Let's look at each operation in detail.

## Creating a Custom Task Type

The creation operation guarantees uniqueness of the type name within the system, preventing duplicates.

```dotty
def createTaskType(newItem: CreateTaskType): F[TaskTypeId] =
  createItem[TaskType, TaskTypeName, TaskTypeId](
    getAll = taskTypeService.getByUser(newItem.userId),
    getName = _.name,
    name = newItem.name,
    create =
      taskTypeService.create(newItem).flatTap { taskTypeId =>
        val newSource = CreateSource(
          taskTypeId = taskTypeId,
          userId = newItem.userId,
          information = ""
        )
        sourcesService.create(newSource)
      }
  )

private def createItem[A, B, C](
    getAll: F[Vector[A]],
    getName: A => B,
    name: B,
    create: => F[C]
): F[C] =
  getAll.flatMap: items =>
    if items.exists(getName(_) == name) then
      MonadThrow[F].raiseError(
        new IllegalArgumentException(
          s"An item with name '$name' already exists"
        )
      )
    else create    
```

**How it works:**

1. Request the full list of existing types for the given user via `getByUser`.
2. Check if a type with the requested name already exists (`items.exists(getName(_) == name)`).
3. If a duplicate is found, the operation is interrupted with an `IllegalArgumentException`.
4. If the name is unique, delegates creation to `typeService.create`, which returns the ID of the new type (`TaskTypeId`).
5. Additionally, for each created type, `flatTap` creates a related source (`Source`) entry with empty content — it becomes the "Knowledge Base" for that type in the dashboard.

**Generalized private logic `createItem`:** To avoid code duplication, the common logic for uniqueness checking and creation is extracted into a parameterized method `createItem`. 
It is abstracted from specific entity types (`A`), their names (`B`), and IDs (`C`), 
taking appropriate access functions and operations.

## Updating Types, Goals, and Sections

The update operation must ensure two key prerequisites:

1. The entity being updated must exist.
2. The new name of the entity must not conflict with names of other entities (except itself).

```dotty
def updateTaskType(taskType: TaskType): F[Boolean] =
  updateItem[TaskType, TaskTypeId, TaskTypeName](
    getAll = taskTypeService.getByUser(taskType.userId),
    updated = taskType,
    getId = _.id,
    getName = _.name,
    update = taskTypeService.update
  )  
  
private def updateItem[A, B, C](
    getAll: F[Vector[A]],
    updated: A,
    getId: A => B,
    getName: A => C,
    update: A => F[Boolean]
): F[Boolean] =
  getAll.flatMap: types =>
    val id      = getId(updated)
    val newName = getName(updated)

    if types.forall(getId(_) != id) then
      MonadThrow[F].raiseError(
        new IllegalArgumentException(s"An item with id '$id' not found")
      )
    else if types.exists(t => getName(t) == newName && getId(t) != id) then
      MonadThrow[F].raiseError(
        new IllegalArgumentException(
          s"Can't update! Task type with name '$newName' already exists"
        )
      )
    else update(updated)    
```

**Validation logic in `updateItem`:**

1. **Existence check:** `types.forall(getId(_) != id)` — if an element with ID `id` is not found in the database, an error is generated.
2. **Name uniqueness check:** `types.exists(t => getName(t) == newName && getId(t) != id)` — an error occurs if 
    the name `newName` is already taken by another entity (whose ID does not match the one being updated).
3. If both checks pass, the element is updated.

## Reordering Entities

This operation allows the user to customize the display order of elements (types, goals, sections) in the interface. 
It takes a vector of IDs in the desired order as input.

```dotty
def reorderTaskTypes(
    ids: NonEmptyVector[TaskTypeId],
    userId: UserId
): F[Boolean] =
  reorderItems[TaskType, TaskTypeId](
    getAll = taskTypeService.getByUser(userId),
    ids = ids,
    getId = _.id,
    update = item => ordinal => item.copy(ordinal = ordinal),
    updateAll = taskTypeService.updateAll
  )
  
private def reorderItems[A, B](
    getAll: F[Vector[A]],
    ids: NonEmptyVector[B],
    getId: A => B,
    update: A => Ordinal => A,
    updateAll: NonEmptyVector[A] => F[Boolean]
): F[Boolean] =
  getAll.flatMap: items =>
    NonEmptyVector.fromVector(items) match
      case None =>
        MonadThrow[F].raiseError(
          new IllegalArgumentException("Items list is empty")
        )
      case Some(neItems) =>
        val notFoundIds = ids.filterNot(id => neItems.exists(getId(_) == id))
        if notFoundIds.nonEmpty then
          MonadThrow[F].raiseError(
            new IllegalArgumentException(
              s"Some ids are not found: ${notFoundIds.mkString(", ")}"
            )
          )
        else
          val updated =
            neItems.map: item =>
              val index   = ids.toVector.indexOf(getId(item))
              val ordinal =
                if index >= 0 then Ordinal.option(index + 1) else None
              update(item)(ordinal.getOrElse(Ordinal.Max))
          updateAll(updated)
```

**Algorithm in `reorderItems`:**

1. **Existence check:** if the `items` list is empty, return an error.
2. **Input validation:** IDs from the request `ids` that are absent from the database are identified (`notFoundIds`). 
    If such IDs are found, the operation completes with an error.
3. **Matching and order calculation:** For each existing entity (`item`), its new position (`index`) is calculated 
    in the passed `ids` vector. Based on the index, the order `ordinal` is calculated, starting from 1. 
    If an entity is not specified in `ids`, it is assigned the maximum order.
4. **Forming updated objects:** Elements are updated using the `updateAll` function.

### 4. Getting a List of Entities

The simplest operation, delegated to the corresponding specialized service.

```dotty
def getTaskTypes(userId: UserId): F[Vector[TaskType]] = typeService.getByUser(userId)
```

Analogous methods `getGoals` and `getSections` will call `goalService.getByUser(userId)` 
and `sectionService.getByUser(userId)` respectively.

## Deleting Entities: Ensuring Data Integrity

The operation of deleting entities (such as sections, goals, or task types) 
is critical in terms of data integrity. 
Before deleting any entity, it is necessary to ensure that 
it is not used in other parts of the system — specifically, that there are no tasks 
that reference this entity through foreign keys.

For each type of entity (section, goal, task type), a separate method is created in `TaskService` 
that checks whether this entity is used in at least one task of any type.

```dotty
trait TaskService[F[_]: Monad]:
  ...

  /** Checks for the existence of a task with the given section. */
  def existsSection(id: SectionId, userId: UserId): F[Boolean]

  /** Checks for the existence of a task with the given goal. */
  def existsGoal(id: GoalId, userId: UserId): F[Boolean]

  /** Checks for the existence of a task with the given custom task type. */
  def existsTaskType(id: TaskTypeId, userId: UserId): F[Boolean]
```

And the implementation:

```dotty
private[pdp] class PostgreSQLTaskService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends TaskService[F]:
  ...
  
  def existsSection(id: SectionId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE section_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]        // Query returns a column with value 1 on record existence
      .option            // Converts result to Option[Int] (Some(1) or None)
      .transact(xa)      // Executes query in transaction context
      .map(_.isDefined)  // Converts Option to Boolean: true if record is found

  def existsGoal(id: GoalId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE goal_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]
      .option
      .transact(xa)
      .map(_.isDefined)

  def existsTaskType(id: TaskTypeId, userId: UserId): F[Boolean] =
    sql"""SELECT 1 FROM tasks WHERE task_type_id = $id AND user_id = $userId LIMIT 1;"""
      .query[Int]
      .option
      .transact(xa)
      .map(_.isDefined)
```

#### Integrating checks into delete operations

Check methods are integrated into delete operations through the settings service (`SettingsService`), 
ensuring safe deletion of only entities that are not in use.

```dotty
def deleteTaskType(id: TaskTypeId, userId: UserId): F[Boolean] =
  // First, check for dependencies
  taskService.existsTaskType(id, userId).ifM(
    // If existsTaskType returned true (type is in use) → call an error
    MonadThrow[F].raiseError(
      new IllegalArgumentException(
        s"Cannot delete task type with ID $id: type is used in tasks"
      )
    ),
    // If existsTaskType returned false (type is not used) → perform deletion
    taskTypeService.get(id, userId).flatMap:
      case Some(_) =>
        sourcesService.delete(id, userId) *>
          taskTypeService.delete(id)
      case None => false.pure[F]
  )
```

The same logic is applied for deleting sections and goals. The owner check (`userId`)
is passed to `get(id, userId)` — the low-level service filters by `user_id`.

### Summary for the Settings Service

`SettingsService` represents a coherent facade over subsystems, 
ensuring integrity and consistency of data within the domain.

With the settings service designed, the work on the system infrastructure is complete. 
The next step is defining the **task lifecycle service (`TaskLifecycleService`)**, 
which will operate directly on task instances, 
using the configured structure (types, goals, sections) for their organization 
and state management (creation, execution, completion, archiving).

---

**Links:**

- [Repository with source code][repo]

[repo]: https://gitverse.ru/scalabook/pdp
