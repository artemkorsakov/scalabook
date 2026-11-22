# Implementing Concrete Database Services for PostgreSQL

## Concretizing Abstract Algebras: From Interface to Implementation

The **"Program to an interface, not an implementation"** principle is fundamental in functional programming.
In our context, this means:

- **Abstract level**: The trait `TaskTypeService[F[_]]` defines **what** needs to be done (the contract).
- **Concrete level**: The class `PostgreSQLTaskTypeService` defines **how** to do it for PostgreSQL (the implementation).

This separation provides several key advantages:

- **Freedom to swap implementations**: The ability to switch from PostgreSQL to another database (MySQL, SQLite)
  or even to an in-memory store for tests, by modifying only the implementation layer.
- **Simplified testing**: Business logic can be tested with mock implementations, without requiring a running database.
- **Adherence to the dependency inversion principle**: High-level modules (business logic) depend on abstractions,
  not on the details of low-level modules (database operations).

## Structure and Implementation of PostgreSQLTaskTypeService

The class `PostgreSQLTaskTypeService` is a concrete implementation of the `TaskTypeService[F]` algebra, using PostgreSQL as the backend.

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLTaskTypeService.scala
private[pdp] class PostgreSQLTaskTypeService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends TaskTypeService[F]:

  /** Creates a new task type.
    *
    * @param newItem
    *   The new task type.
    * @return
    *   {{{F[TaskTypeId]}}} - an effect that, when executed, returns the ID of the
    *   created record.
    */
  def create(newItem: CreateTaskType): F[TaskTypeId] =
    sql"""INSERT INTO task_types (name, user_id, displayed, ordinal) 
          VALUES (${newItem.name}, ${newItem.userId}, ${newItem.displayed}, ${newItem.ordinal}) 
          RETURNING id"""
      .query[TaskTypeId] // Specify that the query returns TaskTypeId
      .unique            // Expect exactly one record (the returned ID)
      .transact(xa)      // Execute within a transactional context

  /** Gets a task type by its ID.
    *
    * @param id
    *   The task type identifier.
    * @return
    *   {{{F[Option[TaskType]]}}} - an effect returning an Optional result.
    */
  def get(id: TaskTypeId, userId: UserId): F[Option[TaskType]] =
    sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE id = $id AND user_id = $userId"""
      .query[TaskType] // Specify the result type 
      .option          // May return 0 or 1 record
      .transact(xa)
      
  /** Gets all task types.
    *
    * @return
    *   {{{F[Vector[TaskType]]}}} - an effect returning a list of all types.
    */
  def getByUser(userId: UserId): F[Vector[TaskType]] =
    sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE user_id = $userId"""
      .query[TaskType] // Select all fields
      .to[Vector]      // Convert the result stream to a Vector
      .transact(xa)  

  /** Updates an existing task type.
    *
    * @param taskType
    *   A TaskType object with updated data (must contain an existing
    *   ID).
    * @return
    *   {{{F[Boolean]}}} - an effect confirming the operation's execution.
    */
  def update(taskType: TaskType): F[Boolean] =
    getUpdateFragment(taskType)
      .update        // Specify that this is an update query
      .run           // Execute
      .transact(xa)
      .map(_ > 0)    // Convert F[Int] to F[Boolean]
      
  def updateAll(taskTypes: NonEmptyVector[TaskType]): F[Boolean] =
    // Fold over the tail of the list, starting from the first element.
    // Use the *> (sequencing) operator for sequential execution of queries
    // ignoring intermediate results (only the side effect matters).
    taskTypes.tail.foldLeft(getUpdateFragment(taskTypes.head).update.run) {
      case (acc, taskType) =>
        acc *> getUpdateFragment(taskType).update.run
    }
      .transact(xa) // Execute the entire sequence in a single transaction
      .map(_ > 0)

  /** Deletes a task type by ID.
    *
    * @param id
    *   The task type ID to delete.
    * @return
    *   {{{F[Boolean]}}} - an effect confirming the operation's execution.
    */
  def delete(id: TaskTypeId): F[Boolean] =
    sql"""DELETE FROM task_types WHERE id = $id"""
      .update.run.transact(xa).map(_ > 0)

  private def getUpdateFragment(taskType: TaskType): Fragment =
    sql"""UPDATE task_types 
         SET name = ${taskType.name}, user_id = ${taskType.userId}, displayed = ${taskType.displayed}, ordinal = ${taskType.ordinal} 
         WHERE id = ${taskType.id}"""
end PostgreSQLTaskTypeService
```

For `UserId`, since it is an opaque type, we need to explicitly define [`Meta[UserId]`](https://typelevel.org/doobie/docs/12-Custom-Mappings.html):

```dotty
import org.typelevel.doobie.*
import org.typelevel.doobie.postgres.*
import org.typelevel.doobie.postgres.implicits.*

object UserId:
  given Meta[UserId] =
    Meta[UUID].imap[UserId](UserId(_))(identity)
```

## Principle of Minimal and Exact Contract Compliance

A crucial aspect of implementation is strictly adhering to the contract defined in the `TaskTypeService` interface.

### What Does "Implementing Only Necessary Methods" Mean?

1. **Exact signature matching**: The class implements exactly the methods declared in the trait,
   with the exact same parameter and return types.
2. **No additional public methods**: The class does not add new public methods
   that are not part of the contract.
3. **Encapsulation of implementation details**: All helper functions specific to PostgreSQL
   should be private methods or functions in a companion object.

**Example of violating the principle (anti-pattern):**

```dotty
// ❌ INCORRECT: Class adds methods not declared in the interface
class PostgreSQLTaskService extends TaskService[IO]:
  // Methods from the interface - correct
  def create(task: CreateTask): IO[TaskId] = ...
  def get(id: TaskId, userId: UserId): IO[Option[Task]] = ...

  // Extra public method - INCORRECT!
  def internalDatabaseOptimization(): IO[Unit] = ...

  // Method with altered signature - INCORRECT!
  def getByUser(limit: Int): IO[Vector[Task]] = ... // No limit parameter in the interface!
```

**Example of correct implementation:**

```dotty
// ✅ CORRECT: Exact interface compliance
class PostgreSQLTaskService extends TaskService[IO]:
  // Only methods from the interface
  def create(task: CreateTask): IO[TaskId] = ...
  def get(id: TaskId, userId: UserId): IO[Option[Task]] = ...
  def getByUser(userId: UserId): IO[Vector[Task]] = ... 
  def update(task: Task): IO[Boolean] = ...
  def delete(id: TaskId): IO[Boolean] = ...

  // Private helper methods - acceptable
  private def buildWhereClause(filters: Map[String, String]): String = ...
```

## Type Conversion Mechanism: The Role of Implicit Parameters (given instances)

The key element enabling the code to work is the system of implicit parameters (given instances) in Scala 3.

### What Happens When Calling `.query[UserType]`?

```dotty
sql"""SELECT id, name, user_id, displayed, ordinal FROM task_types WHERE id = $id"""
  .query[UserType]  // "Magical" conversion!
  .option
  .transact(xa)
```

1. **Doobie searches for an implicit `Get[TaskType]`**: The `query[T]` method requires the presence of a `Get[T]` type class instance in the implicit scope — a Doobie type class that knows how to convert an SQL result row into a `T` typed object.

2. **Import `iron.doobie.given` solves the problem**:

    ```dotty
    import io.github.iltotore.iron.doobie.given
    ```

    This import provides automatic `Get` and `Put` instances for refined types,
    such as `TypeTypeName`, `TypeTypeId`, and compound case classes containing such fields.

3. **Automatic derivation**: For case classes such as `TypeType`,
    Doobie can automatically generate `Get[TypeType]`,
    if `Get` instances exist for all its fields.

## Rules for Correct Use of Implicit Parameters

Implicit parameters are a powerful tool, but their misuse can make code confusing and hard to understand.

### ❌ Common mistakes and anti-patterns:

```dotty
// 1. Using implicit parameters for passing business data
def processOrder()(using order: Order): IO[Unit] =  
  // ❌ ANTI-PATTERN!
  // Where to get order? Who should set it?

// 2. Changing behavior through implicit parameters
def calculatePrice(using discountCalculator: DiscountCalculator): Money =  
  // ❌ Dangerous!
  // Now the result depends on a hidden context.

// 3. Overuse for "convenience"
def getUser(using id: UserId, db: Database, logger: Logger): IO[User] =  
  // ❌ Too many!
  // Code becomes tangled, dependencies are hard to understand.
```

### ✅ Correct usage scenarios:

```dotty
// 1. Configuration and settings (immutable)
def initializeService(using config: AppConfig): IO[Service] =  // ✅ Correct

// 2. Sessions and authentication (within a request)
def getCurrentUser(using session: UserSession): IO[User] =  // ✅ Correct

// 3. Codecs and serializers (infrastructure)
def decodeResponse[T](response: String)(using decoder: Decoder[T]): IO[T] =  // ✅ Correct

// 4. Logging and telemetry
def businessOperation(input: Input)(using logger: Logger): IO[Output] =  // ✅ Correct
```

## Integration with Other Services: Sections and Goals

The implementation of services for `Section` and `Goal` follows the exact same pattern as for `TypeType`.
The differences are minimal and concern only:

1. **Domain model types**: Instead of `TypeType`, `Section` and `Goal` are used.
2. **Identifier types**: `SectionId` and `GoalId` instead of `TypeTypeId`.
3. **Creation parameter types**: `CreateSection` and `CreateGoal` instead of `CreateTypeType`.

Reference to full implementations:
[PostgreSQLSectionService](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLSectionService.scala)
and [PostgreSQLGoalService](https://gitverse.ru/scalabook/pdp/content/master/pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLGoalService.scala).

---

**References:**

- [Source repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
