# Supervisor — Managing Fiber Lifecycles

## Motivation: The Problem with `start` and `background`

[`Spawn`](../typeclasses/spawn.md) provides two main ways to launch [fibers](../foundation/fibers.md),
but each has limitations:

### 1. `start` — "Launch and Forget"

```dotty
fiber <- fa.start  // Fiber B is launched in the background
```

**Lifecycle:**

```
Lifecycle of fiber A (creator)
+-----------------------------------+
|                                   |
|   A launches B                    |
|   ↓                               |
|   Lifecycle of fiber B            |
|   +-------------------------+     |
|   |                         |     |
|   |                         |     |
|   +-------------------------+     |
|                                   |
+-----------------------------------+
```

**Problem:** Fiber `B` lives **independently** of `A`.
Even if `A` completes or fails with an error, `B` will continue executing in the background.
This can lead to:

- Resource leaks (fiber "hangs" forever)
- Unexpected behavior on errors
- Debugging difficulty

### 2. `background` — Linked Lifecycle

```dotty
Resource.eval(fa.background).use { fiber =>
  // use fiber
}
```

**Lifecycle:**

```
Lifecycle of fiber A (creator)
+-----------------------------------+
|                                   |
|   Lifecycle of fiber B            |
|   +-------------------------+     |
|   |   A launches B          |     |
|   |                         |     |
|   +-------------------------+     |
|                                   |
+-----------------------------------+
```

**Feature:** Fiber `B` is **bound** to lifecycle `A`.
When `A` completes, `B` is **automatically cancelled**.

**Problem:** What if `B` should live **longer** than `A`? For example:

- `A` — an HTTP request handler that launches a background task
- The task should continue executing even after the response is sent to the client
- But we still want to be able to cancel all such tasks when the entire server shuts down

## Supervisor: Solution for "Long-Lived" Background Tasks

`Supervisor` is a **fiber manager** that:

1. Creates and launches child fibers.
2. Binds their lifecycle to **its own** lifecycle.
3. Allows child fibers to **outlive** the creator.
4. Guarantees their stop upon supervisor completion.

### Basic Interface

```dotty
trait Supervisor[F[_]]:
  /** Launches a fiber under the supervisor's management. Returns a fiber that
    * can be manually cancelled before the supervisor completes.
    */
  def supervise[A](fa: F[A]): F[Fiber[F, Throwable, A]]

object Supervisor:
  /** Creates a supervisor as a Resource.
    *
    * @param await
    *   - true: wait for all child fibers to complete upon finalization
    *   - false: cancel all child fibers upon finalization
    */
  def apply[F[_]](await: Boolean)(using
      F: Concurrent[F]
  ): Resource[F, Supervisor[F]] = ???
```

### Lifecycle with Supervisor

```
Lifecycle of supervisor (resource)
+===================================+
|                                   |
|   Lifecycle of fiber B            |
|   +---------------------------+   |
|   |                           |   |
|   |   A launches B            |   |
|   |   (via supervise)         |   |
|   |                           |   |
|   +---------------------------+   |
|                                   |
|   Lifecycle of fiber A            |
|   +---------------------------+   |
|   |                           |   |
|   |                           |   |
|   +---------------------------+   |
|                                   |
+===================================+
```

**Key difference:** Fiber `A` (creator) and fiber `B` (child) live independently of each other,
but **both** are bound to the supervisor's lifecycle.
The supervisor lives inside `Resource.use` — as soon as it completes, all child fibers stop.

## Two Completion Strategies

The `await` parameter in the supervisor constructor determines **how** child fibers are terminated when the supervisor stops.

| `await = true`                                                                               | `await = false`                                                            |
|:---------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------|
| **Wait** for all active fibers to complete                                                   | **Immediate cancellation** of all active fibers                            |
| `Supervisor` will wait for each child fiber to complete correctly (success or error)         | All child fibers receive a `cancel` signal                                 |
| Useful for graceful shutdown (shutdown with waiting)                                         | Useful for fast stop (e.g., on panic or timeout)                           |
| **Risk:** infinite fiber (`IO.never`) will hang forever                                      | **Risk:** data may be lost if the fiber was in a critical section          |

## Example 1: Background Task Outliving an HTTP Request

```dotty
import cats.syntax.all.*
import cats.effect.{IO, IOApp}
import cats.effect.std.Supervisor

import scala.concurrent.duration.*

object HttpHandler extends IOApp.Simple:

  val run: IO[Unit] =
    Supervisor[IO](await = true).use: supervisor =>
      List(1, 2, 3).traverse(handleRequest(_, supervisor)).void

  private def handleRequest(id: Int, supervisor: Supervisor[IO]): IO[Unit] =
    def longJob: IO[Unit] =
      IO.sleep(1.second) *>
        IO.println(s"Task $id: started") *>
        IO.sleep(1.second) *>
        IO.println(s"Task $id: completed")

    for
      // Start a long background task
      _ <- supervisor.supervise(longJob)
      // Send response to the client immediately
      _ <- IO.println(s"Request $id: processed, response sent")
    yield ()
end HttpHandler
```

Output:

```text
Request 1: processed, response sent
Request 2: processed, response sent
Request 3: processed, response sent
... after a second ...
Task 1: started
Task 2: started
Task 3: started
... after a second ...
Task 1: completed
Task 2: completed
Task 3: completed
```

**What happens:**

1. 3 requests arrive, each launches a 2-second background task via `supervise`.
2. The response is sent to the client **instantly**.
3. All 3 background tasks continue executing independently.
4. When the server completes, `supervisor.use` completes,
   and all background tasks **stop** (wait or cancel — depends on `await`).

## Example 2: Graceful Shutdown with Waiting for Tasks

```dotty
import cats.effect.{IO, Resource}
import cats.effect.std.Supervisor
import scala.concurrent.duration.*

class EmailService(supervisor: Supervisor[IO]):
  def sendBulk(emails: List[Email]): IO[Unit] =
    emails.traverse { email =>
      supervisor.supervise {
        IO.println(s"Sending email to ${email.address}") *>
          IO.sleep(2.seconds) *>
          email.send()
      }.void
    }.void

val program: Resource[IO, Unit] =
  for
    supervisor <- Supervisor[IO](await = true) // Wait for all emails to complete
    service = new EmailService(supervisor)
    _ <- Resource.eval(service.sendBulk(bigEmailList))
  yield ()

// Upon program completion, supervisor.use waits for all emails to be sent
```

## Example 3: Fast Stop with Cancellation (Anticipating Shutdown)

```dotty
val quickShutdown: Resource[IO, Supervisor[IO]] =
  Supervisor[IO](await = false) // On completion — cancel all background tasks

def longRunningTask(i: Int): IO[Unit] =
  IO.sleep(i.seconds) *> IO.println(s"Launch $i")

val run = quickShutdown.use { supervisor =>
  for
    // Launch 100 background tasks
    fibers <- (1 to 100).toList.traverse(i =>
      supervisor.supervise(longRunningTask(i))
    )
    _ <- IO.sleep(5.seconds) // Work for 5 seconds
  // Supervisor will be closed after this block, forcibly cancelling all tasks
  yield ()
}

run.unsafeRunSync()

// Launch 1
// Launch 2
// Launch 3
// Launch 4
// Launch 5
```

## Example 4: Self-Healing Service with Supervisor

A supervisor is ideal for implementing the "supervisor tree" pattern from Erlang/OTP.

```dotty
import cats.effect.{IO, Temporal}
import cats.effect.std.Supervisor
import scala.concurrent.duration.*

class ResilientService(supervisor: Supervisor[IO]):
  def startWorkers: IO[Unit] =
    supervisor.supervise(runWorker("Worker1", runJob)).void *>
      supervisor.supervise(runWorker("Worker2", runJob)).void *>
      supervisor.supervise(runWorker("Worker3", runJob)).void

  private def runWorker(name: String, job: Int => IO[Unit]): IO[Unit] =
    def loop(attempt: Int): IO[Unit] =
      job(attempt).handleErrorWith { error =>
        IO.println(s"Worker $name fell: ${error.getMessage}") *>
          IO.sleep(1.second) *>
          loop(attempt + 1)
      }
    loop(1)

  private def runJob(attempt: Int): IO[Unit] =
    if attempt >= 3 then IO.println("Success!")
    else
      IO.raiseError(
        new Exception(s"Attempt $attempt is sent for restart")
      )
end ResilientService

val program: Resource[IO, Unit] =
  for
    supervisor <- Supervisor[IO](await = true)
    service = new ResilientService(supervisor)
    _ <- Resource.eval(service.startWorkers)
  yield ()

// Worker Worker3 fell: Attempt 1 is sent for restart
// Worker Worker2 fell: Attempt 1 is sent for restart
// Worker Worker1 fell: Attempt 1 is sent for restart
// Worker Worker3 fell: Attempt 2 is sent for restart
// Worker Worker2 fell: Attempt 2 is sent for restart
// Worker Worker1 fell: Attempt 2 is sent for restart
// Success!
// Success!
// Success!
```

## Important Nuances and Limitations

### 1. Infinite Fiber + `await = true` = Eternal Blocking

```dotty
// ⚠️ DANGER: program will hang forever
Supervisor[IO](await = true).use { supervisor =>
  supervisor.supervise(IO.never)  // infinite task
}.unsafeRunSync()  // never completes!
```

**Solution:** Use `await = false` if there is a risk of infinite tasks, or ensure a mechanism for their proper shutdown.

### 2. Nested Supervisors

Supervisors can be nested within each other, creating hierarchies.

```dotty
Supervisor[IO](await = true).use { outer =>
  Supervisor[IO](await = false).use { inner =>
    // outer waits for inner upon its completion
    inner.supervise(IO.never) // inner with await=false will cancel this task
  }
}
```

### 3. Manual Cancellation of a Child Fiber

A fiber launched via `supervise` can be manually cancelled **before** the supervisor completes.

```dotty
supervisor.supervise(longTask).flatMap { fiber =>
  IO.sleep(1.second) *> fiber.cancel // cancel a specific task
}
```

## Supervisor vs Alternatives

|                                   | `start`                               | `background`                            | `Supervisor`                                            |
|:----------------------------------|:--------------------------------------|:----------------------------------------|:--------------------------------------------------------|
| **Dependency on creator**         | No (fully independent)                | Yes (cancelled upon creator completion) | No (independent of creator)                             |
| **Lives on creator error**        | Yes                                   | No                                      | Yes                                                   |
| **Can be stopped externally**     | Only via `fiber.cancel`               | Automatically                           | Group stop (upon supervisor completion)                 |
| **Binding to `Resource`**         | No                                    | Yes (via `background`)                  | Yes (supervisor as `Resource`)                          |
| **Graceful shutdown**             | Must track all fibers manually        | Automatic                               | Two strategies (`await` true/false)                     |

## Summary: When to Use Supervisor?

✅ **Use `Supervisor` if:**

- A task should outlive the creator (e.g., background export after sending an HTTP response).
- You want to group several background tasks under a common lifecycle.
- You want to implement a "supervisor tree" for error resilience.
- You need graceful shutdown (wait for tasks to finish before stopping).
- It is problematic to manually save all fibers and call `traverse(_.cancel)`.

❌ **Don't use `Supervisor` if:**

- A task should be bound to the creator (`background` is simpler).
- You have a simple program where you can use `start` and collect all fibers into a list.
- You create only one or two background fibers — `Supervisor` may be overkill.

**Key idea:** `Supervisor` is a lifecycle manager for "orphan" fibers,
which should live their own lives but still comply with a common stop rule upon system completion.
It is the missing link between "launch and forget" (`start`) and "live with me" (`background`).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Supervisor](https://typelevel.org/cats-effect/docs/std/supervisor)

[doc]: https://typelevel.org/cats-effect/docs/concepts
