# Health Check Monitoring System

It is important to have a mechanism for controlling service state. Health check allows:

- **Detecting problems** before they affect users
- **Informing orchestration systems** (Kubernetes, Docker Swarm) about the need to restart
- **Monitoring dependent services** (databases, caches, queues)
- **Providing metrics** for monitoring systems (Prometheus, Grafana)

The application implements a health check that includes two critical components: PostgreSQL and Redis connectivity.

### Data Model for Application Status

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/AppStatus.scala
final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)
    derives Encoder

object AppStatus:
  // These types are not interchangeable at the type level, even though internally they are simple statuses
  opaque type RedisStatus    = Status
  opaque type PostgresStatus = Status

  object RedisStatus:
    def apply(status: Boolean): RedisStatus =
      Status.find(status)

  object PostgresStatus:
    def apply(status: Boolean): PostgresStatus =
      Status.find(status)

  enum Status(isOk: Boolean):
    case Okay()        extends Status(true)
    case Unreachable() extends Status(false)

  object Status:
    def find(status: Boolean): RedisStatus =
      if status then Status.Okay()
      else Status.Unreachable()

  given encoderStatus: Encoder[Status] =
    Encoder.encodeString.contramap(_.productPrefix)
end AppStatus
```

**Key features of the model:**

1. **Opaque types** — ensure type safety by not allowing confusion between `RedisStatus` and `PostgresStatus`
2. **Strict state type safety** — the enum guarantees only two possible values
3. **JSON serialization** — ready to send via HTTP API

### HealthCheck Service Interface

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/HealthCheck.scala
trait HealthCheck[F[_]]:
  def status: F[AppStatus]
```

### HealthCheck Implementation

```dotty
object HealthCheck:
  def make[F[_]: Temporal](
      postgres: PostgreSQLHealthCheck[F],
      redis: RedisCommands[F, String, String]
  ): HealthCheck[F] =
    new HealthCheck[F]:
      /** Redis status check
        * Uses the ping command, which should return a non-empty response "PONG"
        */
      val redisHealth: F[RedisStatus] =
        redis.ping
          .map(_.nonEmpty)                          // Check that the response is not empty
          .timeout(1.second)                        // Limit wait time
          .map(RedisStatus.apply)                   // Convert to Redis status
          .orElse(RedisStatus(false).pure[F].widen) // On error - Unreachable

      /** PostgreSQL status check.
        * Executes a simple query against a system table
        */
      val postgresHealth: F[PostgresStatus] =
        postgres
          .status
          .timeout(1.second)                           // Limit wait time
          .map(PostgresStatus.apply)                   // Convert to Postgres status
          .orElse(PostgresStatus(false).pure[F].widen) // On error - Unreachable

      val status: F[AppStatus] =
        (redisHealth, postgresHealth).parMapN(AppStatus.apply)

end HealthCheck
```

**Architectural decisions in the implementation:**

1. **Parallel execution** — `parMapN` runs both checks simultaneously, 
    reducing total response time to the maximum of a single check (1 second)
2. **Timeouts** — each check is limited to 1 second, preventing health check from "hanging"
3. **Fault tolerance** — the `orElse` combinator guarantees that connection errors don't "crash" the entire service
4. **Effect composition** — pure functional programming with Cats Effect

### PostgreSQLHealthCheck Implementation

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/postgresql/PostgreSQLHealthCheck.scala
trait PostgreSQLHealthCheck[F[_]]:
  def status: F[Boolean]
        
object PostgreSQLHealthCheck:
  def make[F[_]: MonadCancelThrow](xa: Transactor[F])
      : PostgreSQLHealthCheck[F] =
    new PostgreSQLHealthCheck[F]:
      // Query to the pg_stat_activity system table
      // Returns a list of active processes - if the list is not empty, the DB is working
      def status: F[Boolean] =
        sql"""SELECT pid FROM pg_stat_activity;"""
          .query[Int]
          .to[List]
          .transact(xa)
          .map(_.nonEmpty)          
```

**Why this query?**

- `pg_stat_activity` is always available even in the most restricted PostgreSQL configurations
- The query is minimal in terms of load (returns only process IDs)
- Checks not only connectivity but also the ability to execute queries

### Redis Check

For Redis, the built-in `PING` command is used:

- The Redis client provides a `.ping` method that sends the `PING` command
- The expected response is the string `PONG` (or any non-empty response)
- A non-empty response is interpreted as a successful connection

### Health Check Response Example

```json
{
  "redis": "Okay",
  "postgres": "Okay"
}
```

In case of problems:

```json
{
  "redis": "Unreachable",
  "postgres": "Unreachable"
}
```

## Dependency Configuration for Web Framework

For implementing UI, we will use the [Krop web framework](https://www.creativescala.org/krop/).
Krop is a Scala 3 framework built on top of http4s and Cats Effect. 
It provides a convenient DSL for describing routes and handlers, while remaining fully type-safe: 
the correspondence between request and response is checked by the compiler.

Before starting work, it is necessary to connect Krop and its dependencies.

### Connecting Dependencies

All project dependencies are collected in the `Dependencies` object in the `project/Dependencies.scala` file.
For convenience, libraries are grouped by purpose: `circe` — serialization, `http4s` — HTTP layer, 
`iron` — refinement types, `krop` — web framework.

```dotty
// Library for JSON serialization/deserialization
object circe {
  private val version = "0.14.16"

  val core    = "io.circe" %% "circe-core"    % version
  val generic = "io.circe" %% "circe-generic" % version
  val literal = "io.circe" %% "circe-literal" % version
  val parser  = "io.circe" %% "circe-parser"  % version
}

// Main HTTP library
object http4s {
  private val version = "1.0.0-M47"

  val client      = "org.http4s" %% "http4s-client"       % version
  val core        = "org.http4s" %% "http4s-core"         % version
  val dsl         = "org.http4s" %% "http4s-dsl"          % version
  val emberClient = "org.http4s" %% "http4s-ember-client" % version
  val emberServer = "org.http4s" %% "http4s-ember-server" % version
  val server      = "org.http4s" %% "http4s-server"       % version
}

// JWT (Jots) — type-safe signed tokens for Cats Effect
object jots {
  val core   = "se.vlovgr" %% "jots"        % "0.4.0"
  val crypto = "se.vlovgr" %% "jots-crypto" % "0.4.0"
}

// Iron integration with Circe
object iron {
  private val version = "3.3.2"
  val circe           = "io.github.iltotore" %% "iron-circe"      % version
  // ... other Iron dependencies
}

object krop {
  private val version = "0.11.0"
  val asset           = "org.creativescala" %% "krop-asset" % version
  val core            = "org.creativescala" %% "krop-core"  % version
}
```

**Why each group of libraries is needed:**

| Library | Purpose                                                                                                                                                                                                      |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **circe**  | JSON serialization and deserialization. `core` — main types (`Json`, `Encoder`, `Decoder`); `generic` — automatic codec derivation via `derives`; `literal` — convenient syntax `json"""..."""` in tests; `parser` — parsing strings into `Json`. |
| **http4s** | HTTP layer. `core` — basic types (`Request`, `Response`, `Status`); `dsl` — DSL for describing requests (`GET(uri"...")`); `ember-server` — server engine; `ember-client` — client engine for integration tests. |
| **jots**   | JWT tokens. `core` — type-safe signed tokens (`SignedJwt`), signature builders and verifiers; `crypto` — working with secret keys. |
| **krop**   | Web framework. `core` — `Route`, `Handler`, `Request`, `Response`; `asset` — static resources (CSS, JS, icons). |

Don't forget to add dependencies to `pdpLibs`:

```dotty
val pdpLibs: Seq[ModuleID] =
  Seq(
    // ...
    circe.core,
    circe.generic,
    circe.parser,
    ciris.core,
    http4s.core,
    http4s.emberClient,
    http4s.server,
    jots.core,
    jots.crypto,
    krop.asset,
    krop.core,
    // ...
  )
```

### Route Descriptions (Routes)

In Krop, a route (`Route`) is a declaration of **which HTTP request** and **which response** should be handled.
Routes are described declaratively.

For health-check, the route is declared directly in `HealthHandler`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/handlers/HealthHandler.scala
final case class HealthHandler(healthCheck: HealthCheck[IO]):
  private val route =
    Route(
      Request.get(Path.root / "health_check"),
      Response.ok(Entity.json)
    )
```

**Route breakdown:**

- **`Request.get(Path.root / "health_check")`** — describes a GET request to path `/health_check`.
  `Path.root` is the root path, `/ "health_check"` adds a segment.
- **`Response.ok(Entity.json)`** — describes a successful response (`200 OK`) with a body in JSON format.

Important: `Route` is **only a description**, not the handler itself. 
It says "if such a request comes, answer like this", but does not contain logic. 
The logic will appear in the handler.

### Creating a Handler

The handler connects a route with a specific implementation. In Krop, the `.handle` or `.handleIO` method is used for this.
The second option is needed when the logic requires an `IO` effect (e.g., accessing DB or Redis).

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/http/handlers/HealthHandler.scala
final case class HealthHandler(healthCheck: HealthCheck[IO]):
  private val route =
    Route(
      Request.get(Path.root / "health_check"),
      Response.ok(Entity.json)
    )

  val handler: Handler =
    route.handleIO(() =>
      healthCheck.status.map(_.asJson)
    )
```

**What is happening here:**

1. **Dependency `healthCheck: HealthCheck[IO]`** is passed to the constructor. This allows easily replacing
    the implementation in tests (e.g., using a mock that always returns "OK").
2. **`private val route`** — the route is declared inside the handler.
3. **`.handleIO(() => ...)`** — we specify that upon receiving a request, an `IO` effect should be executed.
    Note the `()` — this indicates that the request contains no parameters.
4. **`healthCheck.status`** — calls the service that checks the state of Redis and PostgreSQL.
5. **`.map(_.asJson)`** — converts the result (`AppStatus`) to `Json` using Circe's automatic encoder.

**Result:** `Handler` is a function from `Request` to `F[Response]`, ready to be registered in the application.

### Testing the Handler

For testing handlers, the base class `HandlerSuite` is used, which provides 
helpers for checking HTTP responses: `expectJsonAndStatus`, `expectStatus`, `expectBody`.

```dotty
// pdp/src/test/scala/ru/scalabook/pdp/http/handlers/HealthHandler.scala
object HealthHandlerSuite extends HandlerSuite:
  override type Res = HealthCheck[IO]

  override def sharedResource: Resource[IO, Res] =
    Resource.pure(
      new HealthCheck[IO]:
        def status: IO[AppStatus] =
          IO.pure(AppStatus(
            RedisStatus(true),
            PostgresStatus(false)
          ))
    )

  /** The test checks that a GET request to /health_check returns:
    *   - Status 200 OK
    *   - JSON with the actual state of all dependencies
    */
  test("GET /health_check returns 200 OK with status"): healthCheck =>
    val handler: Handler = HealthHandler(healthCheck).handler

    val request = GET(uri"/health_check")

    val expectedBody =
      json"""{
                "redis": "Okay",
                "postgres": "Unreachable"
              }"""

    expectJsonAndStatus(handler, request)(expectedBody, HttpStatus.Ok)
```

**Test breakdown:**

1. **`override type Res = HealthCheck[IO]`** — the type of resource that will be available in tests.
    In our case — a mock service `HealthCheck[IO]`.
2. **`sharedResource`** — describes how to create the resource. Returns `Resource.pure` with a mock implementation
    that always says: "Redis is available, PostgreSQL — is not."
3. **`test("...")`** — each test receives a ready `healthCheck` from `sharedResource`.
4. **`GET(uri"/health_check")`** — creates an HTTP request using the http4s DSL. `uri` — a string interpolator.
5. **`expectJsonAndStatus`** — compares the status and body of the response with expected values. If anything doesn't match —
    it will show a diff in the test report.

**Why this is important:**

- The test **does not require a real DB or Redis** — a mock is used.
- The test **checks the handler contract**: that it returns 200 OK and correct JSON.
- If someone accidentally changes the response format (e.g., adds a field), the test will immediately show it.

---

**Links:**

- [Repository with source code][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[pfp]: https://leanpub.com/pfp-scala
[http4s]: https://http4s.org/
