# Data Access Layer Architecture

## Defining the Domain

Our application manages tasks that make up a personal development plan (PDP). 
These tasks require constant persistence of their state and attributes. 
Before deciding on a specific storage technology (e.g., PostgreSQL, MongoDB, in-memory), 
we must clearly define what behavior we expect from the data storage system.

The application operates with four main entities (domain types) that need to be persistent:

1. **User Task Types** — custom categories or task templates 
   that users create themselves to classify their learning activities (e.g., "Book", "Exercises").
2. **Sections** — logical groups or categories for structuring and organizing tasks 
   (e.g., "Scala", "Machine Learning", "Algorithms").
3. **Goals** — target states or metrics the user plans to achieve 
   as a result of completing a set of tasks (e.g., "Learn Scala 3 basics", "Prepare for an interview").
4. **Tasks** — concrete, actionable plan elements that belong to a certain type, 
   are in a section, and work toward achieving a goal. This is the core of the domain.

For each of these entities, a standard set of lifecycle operations is required, known as CRUD (Create, Read, Update, Delete):

- **`create`** — creating a new record in the database. Returns a unique identifier for the created entity.
- **`get`** — retrieving a record by its unique identifier. Returns `Option`, since the record may not exist.
    - **`getByUser`** — retrieving all records for a given user. Returns a collection (list, vector), which may be empty.
- **`update`** — updating an existing record. Expects that the identifier in the updated object 
  corresponds to the identifier in the storage.
    - **`updateAll`** — updating a collection of records of a given type.
- **`delete`** — irreversible deletion of a record by its unique identifier.

### Single Responsibility Principle at the Data Access Level

The service responsible for interacting with the database must strictly follow the single responsibility principle. 
Its only task is to perform low-level operations for saving, retrieving, and deleting data. 
It **should not** contain business logic, such as checking the validity of relationships between entities, 
applying complex rules, or coordinating transactions spanning multiple aggregates.

#### Example to Illustrate Separation of Concerns:

Consider a scenario: attempting to create a task (`Task`) and link it to a section (`Section`) that does not exist in the database.

Is this the database service's concern? **No**.

**Argumentation:**

1. The DB service (`TaskService`) is only responsible for saving the passed `CreateTask` object to storage. 
    Its contract is to write the data.
2. Checking whether the passed `sectionId` exists in the sections table is a matter of **business logic**. 
    This is an application rule: "a task cannot be created for a non-existent section." 
    Such a check should be performed at a higher level, in a Domain Service, 
    which can coordinate calls to `SectionService` and `TaskService`.

### Protecting Data Integrity and Encapsulation

To prevent accidental data integrity violations 
(e.g., directly deleting a section that tasks still reference) 
and to ensure all operations pass through the business logic layer, we apply strict encapsulation:

- DB services are declared as **`private[services]`** in Scala. 
  This limits their visibility to only the current package and its subpackages.
- External code (e.g., HTTP API controllers) **cannot directly access** these services.
- All data requests must pass through the application service layer (or "interpreters"), 
  which contains the business logic and coordinates calls to repositories.

### Choosing an Architectural Pattern: Tagless Final

To implement the above principles (separation of concerns, testability, polymorphism over effects) 
we use the **[Tagless Final][tagless]** architectural pattern. This approach allows:

1.  Abstracting from a specific effect monad `F[_]` (e.g., `IO`, `Future`, `Task`) at the algebra (interface) level.
2.  Easily swapping implementations (e.g., mock vs. real database) without changing business logic code.
3.  Clearly separating operation declaration (algebra) from their execution (interpreter).

## Designing Algebras (Interfaces)

An algebra is an abstract API that describes **what** can be done, not **how** it's implemented. 
Let's start by creating an algebra for working with user task types.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/TaskTypeService.scala
private[services] trait TaskTypeService[F[_]]:

  /** Creates a new task type. */
  def create(newItem: CreateTaskType): F[TaskTypeId]

  /** Gets a task type by its ID. */
  def get(id: TaskTypeId, userId: UserId): F[Option[TaskType]]

  /** Gets a list of all task types. */
  def getByUser(userId: UserId): F[Vector[TaskType]]

  /** Updates an existing task type. */
  def update(taskType: TaskType): F[Boolean]

  /** Updates a list of task types. */
  def updateAll(taskTypes: NonEmptyVector[TaskType]): F[Boolean]

  /** Deletes a task type by ID. */
  def delete(id: TaskTypeId): F[Boolean]  
```

### Return Type Semantics in the Context of Effects

- **`F[_]`** — a type parameter representing an [abstract effect](../fp/external-effects.md) 
  (e.g., an asynchronous computation, a side-effecting operation). 
  This allows the algebra to be independent of the specific runtime library (Cats Effect, ZIO, Monix).
- **`F[TaskTypeId]`** — an operation that, during execution (`F`), will produce a side effect (writing to the DB) and return a result — `TaskTypeId`.
- **`F[Option[TaskType]]`** — a read operation that may complete successfully, finding the entity (`Some[TaskType]`), 
  or return `None` if an entity with that ID does not exist.
- **`F[Vector[TaskType]]`** — a read operation that always returns a collection. 
  The collection may be empty if there are no matching records in the DB yet.
- **`F[Boolean]`** — an operation that is executed for its side effect (update, delete). 
  `Boolean` serves as a completion signal.

### Placing Business Logic: Outside the Algebra

Complex operations that require coordinating multiple database calls or checking business rules 
**should not** be part of the DB service algebra. 
For example, the operation "move all tasks from section A to section B" or "delete a goal and all tasks associated with it."

According to the [Tagless Final][tagless] pattern, such logic is described as a **program** — a function 
that takes instances of the required algebras (via type parameters or context bounds) 
and, using them, describes a sequence of actions. 
Thus, `TaskTypeService` remains a simple and stable repository, 
and all complex logic is lifted to the program level, which we will examine in the ["Task Lifecycle"](lifecycle.md) section.

### The Remaining Algebras

The algebras `SectionService`, `GoalService`, and `TaskService` are designed following the same pattern as `TaskTypeService`, 
differing only in model types (`Section`, `SectionId`, `Goal`, `GoalId`). 
Their implementations can be found in the [project source code](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/services).

Creating an entity requires a new model.
The `create` operation cannot accept a ready-made `Task`, since a new task doesn't yet have an assigned identifier (`id`). 
Therefore, we define a separate type hierarchy `CreateTask` (`CreateTaskType`, `CreateSection`, `CreateGoal`), 
which mirrors the main structures (e.g., `Task`) but without the `id` field.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/TaskService.scala  
private[services] trait TaskService[F[_]]:

  /** Creates a new task. */
  def create(task: CreateTask): F[TaskId]

  /** Gets a task by its ID. */
  def get(id: TaskId, userId: UserId): F[Option[Task]]

  /** Gets a list of all tasks. */
  def getByUser(userId: UserId): F[Vector[Task]]

  /** Updates an existing task. */
  def update(task: Task): F[Boolean]

  /** Deletes a task by its ID. */
  def delete(id: TaskId): F[Boolean]
```

The type `CreateTask` and similar types are exact copies of the corresponding types, but without the `id` field. 
[Full definition link.](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/http/dto/CreateTask.scala)

```dotty
final case class CreateTask(
    name: TaskName,
    userId: UserId,
    taskTypeId: TaskTypeId,
    status: Status,
    sectionId: SectionId,
    goalId: GoalId,
    ordinal: Ordinal,
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    information: String,
    plan: Plan,
    duration: FiniteDuration,
    timeSpent: FiniteDuration
)
```

## Implementing Interpreters: Mocks

For each algebra, there can be several **interpreters** — concrete implementations 
on a specific effect type `F[_]` (e.g., `IO`) and for a specific storage.

- **Mock implementation** — stores data in memory (e.g., in `Ref[IO, Map]`). 
  Used for unit and integration testing of business logic without deploying a real database. 
  Fast and isolated.
- **PostgreSQL implementation** — uses JDBC or an asynchronous driver to execute SQL queries against a production relational database. 
  This is the production implementation.

We start development with the mock implementation, as it allows quickly testing 
and debugging the entire call chain from controllers to services, without worrying about database migrations and connectivity.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/mock/MockTaskTypeService.scala
/**
 * Mock implementation of TaskTypeService, using an in-memory immutable Map
 * for state storage, protected by Cats Effect's Ref.
 *
 * @param userTypes Ref containing the current state of the storage (Map from ID to TaskType).
 * @param nextId Ref counter for generating new unique IDs.
 */
private[services] class MockTaskTypeService[F[_]: Monad](
    taskTypes: Ref[F, Map[TaskTypeId, TaskType]],
    nextId: Ref[F, TaskTypeId]
) extends TaskTypeService[F]: // Interpreting the algebra in a specific effect F

  /**
   * Creates a new task type.
   * Atomically: 1) takes the next ID, 2) increments the counter, 3) saves the record.
   */
  def create(newItem: CreateTaskType): F[TaskTypeId] =
    for
      // Get the current ID and increment the counter for the next call.
      // applyUnsafe is safe here, as we know 
      // that id.value + 1 is a positive number.
      id <- nextId.getAndUpdate(id => TaskTypeId.applyUnsafe(id.value + 1))
      // Create the TaskType object and save it in the Map.      
      taskType = TaskType(
        id = id,
        name = newItem.name,
        userId = newItem.userId,
        displayed = newItem.displayed,
        ordinal = newItem.ordinal
      )
      // Use the update method for saving.      
      _ <- update(taskType)
    yield id // Return the ID under which the type was saved.
       
  /** Extracts a type by ID. Returns None if the key is not found or the type doesn't belong to the user. */
  def get(id: TaskTypeId, userId: UserId): F[Option[TaskType]] =
    taskTypes.get.map(_.get(id).filter(_.userId == userId)) // Read the Map and perform a lookup with owner verification.

  /** Extracts all values for a given user, 
    * converting Map values to Vector. 
    */
  def getByUser(userId: UserId): F[Vector[TaskType]] =
    taskTypes.get.map(_.values.filter(_.userId == userId).toVector)

  /** Updates (or adds) a record in the Map. */
  def update(taskType: TaskType): F[Boolean] =
    taskTypes.update(_ + (taskType.id -> taskType)).as(true)

  /** Updates all provided records in the Map. */
  def updateAll(items: NonEmptyVector[TaskType]): F[Boolean] =
    taskTypes.update(_ ++ items.toVector.map(t => t.id -> t)).as(true)
     
  /** Deletes a record by ID. If the ID doesn't exist, the operation has no effect. */
  def delete(id: TaskTypeId): F[Boolean] =
    taskTypes.update(_ - id).as(true)

/** Companion object providing a safe Resource constructor 
  * for creating the service. 
  */
object MockTaskTypeService:
  /**
   * Factory method returning Resource[F, TaskTypeService[F]].
   * Guarantees that state (Ref) will be properly initialized.
   * Resource manages the lifecycle, though it's not critical for Ref.
   */
  def make[F[_]: Sync]: Resource[F, TaskTypeService[F]] =
    Resource.eval: // Create a resource from a pure effect (eval).
      for
        // Initialize storage with an empty Map.
        types <- Ref[F].of(Map.empty[TaskTypeId, TaskType])
        // Initialize ID counter to 0 (first ID will be 0).
        id    <- Ref[F].of(TaskTypeId(0))
      yield new MockTaskTypeService(types, id) // Return a new service instance.    
```

---

**Links:**

- [Source code repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[tagless]: ../fp/patterns/tagless-final.md
