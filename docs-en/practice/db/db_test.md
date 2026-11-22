# Integration Testing with the Database

## The Problem of Testing Code That Interacts with a Database

When developing applications that work intensively with databases, a question arises:
**how to test such code efficiently and reliably?**

Consider common approaches and their drawbacks:

- **Mock objects** — while they allow isolating the tested module,
  they completely abstract away real interaction with the RDBMS.
  This approach does not verify the correctness of SQL queries, transaction handling, database error handling, or type compatibility.
  Tests pass against an "idealized" version of the database, reducing their practical value.

- **In-memory databases (e.g., H2)** — while providing more realistic interaction than mocks,
  they often differ in behavior, SQL syntax, and supported features from production RDBMS (e.g., PostgreSQL).
  This can lead to situations where code passes tests but breaks when deployed on real infrastructure.

- **A separately maintained test database** — requires significant effort to maintain consistent state,
  clean data between test runs, and prevent conflicts between concurrently running tests.
  This approach is hard to automate and reproduce in CI/CD environments.

The [**Testcontainers**](https://testcontainers.com/getting-started/) library offers an elegant compromise between realism and manageability.
It allows running real services (such as PostgreSQL, Redis, Kafka) inside Docker containers
that are specifically intended for running tests.
This provides testing against a real production-like environment while maintaining automation and isolation.

#### How Testcontainers works:

1. **Dynamic startup** — the library programmatically starts a Docker container with the required RDBMS
   (e.g., PostgreSQL of a specified version) right before test execution.
2. **Full isolation** — a fresh database instance is created for each test run,
   ensuring no side effects between tests.
3. **Automatic lifecycle management** — after all tests complete, the container is automatically stopped
   and removed, freeing system resources. This eliminates the need for manual cleanup.

## Connecting Necessary Dependencies to the Project

To integrate Testcontainers into the project, you need to configure dependencies correctly.
It is important to separate production dependencies from test dependencies,
to avoid accidentally including test libraries in the final artifact.

```dotty
object Dependencies {
  // ... other dependencies ...

  // Configuration for logging libraries
  object log4cats {
    private val version = "2.8.0"
    
    val slf4j   = "org.typelevel" %% "log4cats-slf4j"  % version 
    val logback = "ch.qos.logback" % "logback-classic" % "1.6.6"
  }

  // Block for Testcontainers dependencies (testcontainers-scala)
  object testcontainers {
    private val version = "0.44.1" 

    val postgres = "com.dimafeng" %% "testcontainers-scala-postgresql" % version
  }

  // ... other dependencies ...

  // Combined list of libraries needed exclusively for testing
  val testLibs: Seq[ModuleID] =
    Seq(
      log4cats.logback,       
      log4cats.slf4j,         
      doobie.postgres,        
      testcontainers.postgres,
      weaver.cats             
    )
    // the `% Test` modifier guarantees that these libraries will not be included 
    // in the production build and are only available during test compilation and execution
    .map(_ % Test)                     
  // ... rest of project configuration ...
}
```

## Creating a Centralized Service for Managing the Test Database

To reuse resources across test suites, a specialized `SharedResources` object is created.
This object implements the `GlobalResource` pattern from the [Weaver framework][weaver],
allowing shared resources to be initialized once before all tests and properly released after completion.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/SharedResources.scala
package ru.scalabook.pdp.services

...

object SharedResources extends GlobalResource:
  // A set of data access services provided to tests.
  // Each works with the same Transactor.
  final case class PDPServices(
      taskTypeService: PostgreSQLTaskTypeService[IO],
      taskService: PostgreSQLTaskService[IO]
  )

  // Logging system configuration for the test environment
  private given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  private given log: Logger[IO]                  = loggerFactory.getLogger

  // Key for accessing shared resources from Weaver's global storage
  val TasksServiceKey: Option[String] = "tasksServices".some

  // Custom SQL query logging handler for Doobie queries.
  private val sqlLogHandler: LogHandler[IO] = (logEvent: LogEvent) =>
    log.info(s"${logEvent.sql}")

  // The main method defining shared resources for all tests.
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for
      // 1. Start PostgreSQL container.
      postgresContainer <- postgresResource()
      // 2. Create a Doobie Transactor (DB connection abstraction).
      transactor = getTransactor(postgresContainer)
      // 3. Execute database migrations.
      _ <- Resource.eval(migration(transactor))
      // 4. Create data access services, injected with the Transactor.
      taskTypeService = new PostgreSQLTaskTypeService[IO](transactor)
      taskService     = new PostgreSQLTaskService[IO](transactor)
      // 5. Register services in the global storage.
      _ <- global.putR(
        PDPServices(
          taskTypeService = taskTypeService,
          taskService = taskService
        ),
        TasksServiceKey
      )
    yield () // Resource returns Unit, since services are placed in global storage.
  end for
  end sharedResources

  private def postgresResource(): Resource[IO, PostgreSQLContainer] =
    Resource.make {
      IO {
        val container = PostgreSQLContainer(dockerImageNameOverride =
          DockerImageName.parse("postgres:18")
        )
        container.start()
        container
      }
    }(container => IO(container.stop()))

  private def getTransactor(container: PostgreSQLContainer)
      : Aux[[x] =>> IO[x], Unit] =
    Transactor.fromDriverManager[[x] =>> IO[x]](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      password = container.password,
      logHandler = Some(sqlLogHandler)
    )

  private def migration(transactor: Aux[[x] =>> IO[x], Unit]): IO[Boolean] =
    new PostgreSQLMigrationService(transactor).initialize()
end SharedResources
```

## Test Structure and Writing Tests

The [Weaver framework][weaver], integrated with Cats Effect,
is used to organize test scenarios.
The test class extends `IOSuite` and uses the shared resources mechanism.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLTaskTypeServiceSuite.scala
package ru.scalabook.pdp.services.postgresql

...

// Test suite class for PostgreSQLTaskTypeService.
// The `global: GlobalRead` parameter is provided by Weaver
// and gives access to previously registered shared resources.
class PostgreSQLTaskTypeServiceSuite(global: GlobalRead) extends IOSuite:
  // Type Res — a tuple of the service and the user ID,
  // under whose identity operations are performed.
  override type Res =
    (
        service: PostgreSQLTaskTypeService[IO],
        userId: UserId
    )

  // Weaver calls this method once (before the first test).
  // Extracts the service from global storage and sets the ID
  // of the owner user for created entities.
  override def sharedResource: Resource[IO, Res] =
    for
      pdpServices <- global.getOrFailR[PDPServices](TasksServiceKey)
      // An arbitrary owner user ID for created entities.
      userId      <- Resource.eval(IO(UserId(UUID.randomUUID())))
    yield (pdpServices.taskTypeService, userId)

  // Example test for creating an entity.
  test("PostgreSQLTaskTypeService.create"): resource =>
    val newType = CreateTaskType(
      name = TaskTypeName("Check create"),
      userId = resource.userId
    )
    for
      id            <- resource.service.create(newType)
      maybeTaskType <- resource.service.get(id, resource.userId)
    yield whenSuccess(maybeTaskType): actual =>
      expect.same(id, actual.id) &&
        expect.same(newType.name, actual.name) &&
        expect.same(newType.userId, actual.userId) &&
        expect.same(newType.displayed, actual.displayed) &&
        expect.same(newType.ordinal, actual.ordinal)

  // Test for handling the case when an entity is not found.
  test("PostgreSQLTaskTypeService.get - not found"): resource =>
    val nonExistentId = TaskTypeId(9999)
    resource.service.get(nonExistentId, resource.userId).map: result =>
      expect(result.isEmpty)

  // Test for getting all entities.
  test("PostgreSQLTaskTypeService.getByUser"): resource =>
    val opensource = CreateTaskType(
      name = TaskTypeName("Open Source"),
      userId = resource.userId
    )
    val myBlog = CreateTaskType(
      name = TaskTypeName("My Blog"),
      userId = resource.userId
    )
    for
      osId  <- resource.service.create(opensource)
      mbId  <- resource.service.create(myBlog)
      types <- resource.service.getByUser(resource.userId)
    yield expect(types.nonEmpty) &&
      expect(types.exists(_.id == osId)) &&
      expect(types.exists(_.id == mbId))

  // Test for updating an existing entity.
  test("PostgreSQLTaskTypeService.update"): resource =>
    val userId    = resource.userId
    val exercises = CreateTaskType(
      name = TaskTypeName("Exercises"),
      userId = userId
    )
    val newName = TaskTypeName("Exercises (updated)")
    for
      id <- resource.service.create(exercises)
      updated = TaskType(
        id = id,
        name = newName,
        userId = userId,
        displayed = !exercises.displayed,
        ordinal = Ordinal(10)
      )
      _            <- resource.service.update(updated)
      maybeUpdated <- resource.service.get(updated.id, updated.userId)
    yield whenSuccess(maybeUpdated): actual =>
      expect.same(updated.id, actual.id) &&
        expect.same(updated.name, actual.name) &&
        expect.same(updated.userId, actual.userId) &&
        expect.same(updated.displayed, actual.displayed) &&
        expect.same(updated.ordinal, actual.ordinal)

  // Test for deleting an entity.
  test("PostgreSQLTaskTypeService.delete"): resource =>
    val newType = CreateTaskType(
      name = TaskTypeName("Deprecated"),
      userId = resource.userId
    )
    for
      id        <- resource.service.create(newType)
      initial   <- resource.service.getByUser(newType.userId)
      _         <- resource.service.delete(id)
      finalList <- resource.service.getByUser(newType.userId)
    yield expect(initial.exists(_.id == id)) &&
      expect(finalList.forall(_.id != id))

end PostgreSQLTaskTypeServiceSuite
```

[Full list of tests][tests]

## Detailed Explanation of Lifecycle and Testing Process

### Complete test execution lifecycle with Testcontainers and Weaver:

1. **Initialization phase (before all tests)**:
   - The Weaver framework calls the `sharedResources` method of the `SharedResources` object.
   - A new Docker container with a clean PostgreSQL instance is started.
   - Based on the parameters of the started container (URL, login, password), a
     `Transactor` Doobie is created — the main abstraction for executing queries.
   - Database migrations (DDL scripts) are executed, which create the required tables, indexes
     and possibly populate reference data.
   - Data access service instances (`PostgreSQLTaskTypeService`, `PostgreSQLTaskService`) are
     created, with the shared `Transactor` injected.
   - Created services are registered in Weaver's global storage under unique keys.

2. **Execution phase of each test suite**:
   - When creating an instance of a test class (e.g., `PostgreSQLTaskTypeServiceSuite`),
     Weaver extracts the corresponding service (`PostgreSQLTaskTypeService`) from global storage via the `sharedResource` method.
   - This service instance is provided to each test in the suite as a test function parameter.
   - The test performs business operations (create, get, update, delete) through the service,
     which in turn executes real SQL queries to PostgreSQL in the container.
   - After all tests in the suite are executed, Weaver may (depending on configuration) roll back the transaction
     or clean the database to isolate the next test suite.

3. **Finalization phase (after all tests)**:
   - After all test suites are executed, Weaver properly releases all shared resources.
   - The release action for the container resource is called, which stops and removes the PostgreSQL Docker container.
   - All allocated system resources (memory, network ports) are released.

## Conclusion

The presented approach to integration testing using Testcontainers and Weaver provides
a comprehensive solution combining several critical qualities:

- **High reliability and accuracy** — testing is performed on **real PostgreSQL**,
  guaranteeing that code that passes tests will work correctly in a production environment.
- **Full process automation** — the developer is not required to manually set up the environment.
  Tests can be easily run both on a developer's local machine and in any CI/CD system supporting Docker.
- **Optimal performance** — despite using a real RDBMS in a container,
  the approach provides reasonably fast test execution by reusing the container across tests
  and efficiently managing resource lifecycle.
- **Effective debugging** — thanks to SQL query logging integration
  and detailed error messages from Weaver, the process of identifying and fixing defects is significantly simplified.

---

**References:**

- [Source repository][repo]
- [Full list of tests][tests]

[repo]: https://gitverse.ru/scalabook/pdp
[tests]: https://gitverse.ru/scalabook/pdp/content/master/pdp/src/test/scala/ru/scalabook/pdp/services/postgresql
[weaver]: https://typelevel.org/weaver-test/
