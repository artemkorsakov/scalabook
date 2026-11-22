# Dispatcher — Bridge Between the World of Effects and Impure Code

## Motivation: The Problem of Executing Effects in Callbacks

In an ideal world, all your code is written in `F[_]` style (e.g., `IO`), and you never deal with side effects.
But the reality is that many libraries (GUI frameworks, web sockets, databases, queue messages)
provide an **imperative API** based on callbacks:

```dotty
abstract class ImpureInterface:
  def onMessage(msg: String): Unit // called by the outside world
  def init(): Unit = onMessage("init")
```

You need to implement `onMessage` and perform an **effect** (e.g., put the message into a `Queue`) inside it.
Directly calling `queue.offer(msg)` won't work, because it returns `IO[Unit]` — a **description** of the effect, not its execution.

```dotty
// ❌ YOU CAN'T DO THIS: IO encapsulates the effect, but doesn't execute it
new ImpureInterface:
  override def onMessage(msg: String): Unit =
    queue.offer(msg) // returns IO[Unit], which just "hangs" in the air
```

Under normal circumstances, we would use `IO.println(msg).unsafeRunSync()`, but this is **extremely dangerous**:

- Blocks the thread
- Violates the async model of Cats Effect
- Can lead to deadlocks and fiber leaks

This is where `Dispatcher` comes to the rescue.

## What is `Dispatcher`?

`Dispatcher[F[_]]` is a utility based on [`Supervisor`](supervisor.md),
which allows you to **safely execute `F` effects at the boundary with impure code**.
It provides methods that take an effect `F[A]` and **execute it** in a controlled environment
(in a separate fiber under supervision), returning the result in an imperative style.

## Creating `Dispatcher`

`Dispatcher` is created as a `Resource`, ensuring proper cleanup of all fibers launched through it upon closure:

```dotty
import cats.effect.IO
import cats.effect.std.Dispatcher

val resource: Resource[IO, Dispatcher[IO]] = Dispatcher.sequential[IO]
```

There are two main operating modes:

| Creation Method             | Behavior                                                                                    |
|:----------------------------|:--------------------------------------------------------------------------------------------|
| `Dispatcher.sequential[F]`  | Executes all effects in a single dedicated thread (order is guaranteed, FIFO).              |
| `Dispatcher.parallel[F]`    | Executes effects in parallel, using a thread pool (maximum performance).                    |

> For most cases (callback processing, queue insertion), `sequential` is sufficient.
> For high-throughput systems where callbacks may arrive simultaneously, prefer `parallel`.

## `Dispatcher` Methods

```dotty
trait Dispatcher[F[_]] extends DispatcherPlatform[F]:

  /** Returns (Future[A], canceling function). The most flexible method. */
  def unsafeToFutureCancelable[A](fa: F[A]): (Future[A], () => Future[Unit])

  /** Runs the effect, returns Future[A] (without explicit cancellation). */
  def unsafeToFuture[A](fa: F[A]): Future[A] =
    unsafeToFutureCancelable(fa)._1

  /** Runs the effect, returns a cancel function. */
  def unsafeRunCancelable[A](fa: F[A]): () => Future[Unit] =
    unsafeToFutureCancelable(fa)._2

  /** Runs the effect and forgets the result (fire-and-forget). Not cancelable.
    */
  def unsafeRunAndForget[A](fa: F[A]): Unit

  // Only available on JVM:
  // Blocks the current thread until the effect completes!
  def unsafeRunSync[A](fa: F[A]): A
end Dispatcher
```

### Method Selection Table

| Task                                   | Method                       | Returns              | Cancelable?                                           |
|:---------------------------------------|:-----------------------------|:---------------------|:------------------------------------------------------|
| "Run and forget"                       | `unsafeRunAndForget`         | `Unit`               | ❌ No                                                  |
| Need result, but no blocking           | `unsafeToFuture`             | `Future[A]`          | ❌ No (but `Future` can be cancelled via `Future.cancel`) |
| Need manual cancellation               | `unsafeRunCancelable`        | `() => Future[Unit]` | ✅ Yes                                                 |
| Need result and cancellation           | `unsafeToFutureCancelable`   | `(Future[A], cancel)` | ✅ Yes                                                 |
| Blocking call (tests only)             | `unsafeRunSync`              | `A`                  | ❌ No (blocks the thread!)                              |

> **Caution:** `unsafeRunSync` blocks the OS thread! Use it **only** in tests
> or demonstration examples, **never** in production code.

## Integrating with an Imperative Interface

Back to the task: implement an `ImpureInterface` that will actually execute `queue.offer(msg)`.

### Without `Dispatcher` — Doesn't Work

```dotty
import cats.effect.IO
import cats.effect.std.Queue

val program =
  for
    queue <- Queue.unbounded[IO, String]
    impureInterface = new ImpureInterface:
      override def onMessage(msg: String): Unit =
        queue.offer(msg) // ❌ IO[Unit] is simply ignored!
    _     <- IO.delay(impureInterface.init()) // calls onMessage("init")
    value <- queue.tryTake
    _     <- IO.println(s"Result: $value") // Always None!
  yield ()
```

### With `Dispatcher` — Works

```dotty
import cats.effect.IO
import cats.effect.std.{Dispatcher, Queue}

Dispatcher.sequential[IO].use { dispatcher =>
  for
    queue           <- Queue.unbounded[IO, String]
    impureInterface <- IO.delay {
      new ImpureInterface:
        override def onMessage(msg: String): Unit =
          dispatcher.unsafeRunSync(queue.offer(msg))
        // ✅ Effect IS EXECUTED
    }
    _     <- IO.delay(impureInterface.init())
    value <- queue.tryTake
    _     <- IO.println(s"Result: $value") // Some("init")!
  yield ()
}
```

**What changed?**

- `dispatcher.unsafeRunSync(queue.offer(msg))` **executes** the `IO`.
- The message `"init"` is put into the queue.
- We retrieve it via `tryTake`.

## Important Nuances and Warnings

### 1. `Dispatcher` Is Not Free

Creating a `Dispatcher` is cheap but not free (it creates an internal `Supervisor` and thread pool).
It is recommended to create it where it is needed, and not make it a global singleton.

### 2. Errors in `unsafeRunAndForget` and `unsafeRunCancelable`

If an effect launched via `unsafeRunAndForget` completes with an error,
it will be **ignored** (logging is at the implementation's discretion).
For error handling, use `unsafeToFuture` and work with `Future.failed`.

### 3. Cancellation ≠ Resource Release

Cancellation via `cancel` sends a cancellation signal to the fiber, but it doesn't guarantee immediate stop.
Effects inside may continue executing longer, but `bracket` and `uncancelable` will be respected.

### 4. `unsafeRunSync` — Tests Only

This method **blocks the current thread** and is intended exclusively for testing and REPL.
Using it in production code can lead to deadlocks and terrible performance.

### 5. Closing `Dispatcher` (via `Resource.use`) Cancels All Launched Effects

When you exit the `use` block, `Dispatcher` calls `supervisor.cancel` for all launched fibers.
This is important for preventing leaks.

## `Dispatcher` Compared to Alternatives

| Mechanism                          | Purpose                              | Pros                              | Cons                                     |
|:-----------------------------------|:-------------------------------------|:----------------------------------|:-----------------------------------------|
| **`Dispatcher`**                   | Executing effects from impure code    | Safe, controlled, cancelable      | Requires `Resource` (not eternal)        |
| `unsafeRunSync`                    | Running `IO` in tests                 | Simplicity                        | Blocks thread, dangerous in production   |
| `IO.println(...).unsafeRunAsync`   | "Pretend to execute"                  | Quick                             | No cancellation control, hard to debug   |

## Summary: When to Use `Dispatcher`?

✅ **Use `Dispatcher` if:**

- You are integrating with an **imperative API** (callbacks, listeners, GUI events, Java libraries).
- You need to **execute an effect** `F` in response to an external call.

❌ **Don't use `Dispatcher` if:**

- Your entire program is already inside `F` (just combine effects).
- You need only a single `unsafeRunSync` in a test — easier to use `unsafeRunSync()` directly on the `IO`.

**Key idea:** `Dispatcher` is a controlled, safe,
and powerful "hatch" from the pure functional world into the world of imperative callbacks.
It allows you to leverage all the advantages of Cats Effect while still working with libraries
that were not written in a functional style.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Dispatcher](https://typelevel.org/cats-effect/docs/std/dispatcher)

[doc]: https://typelevel.org/cats-effect/docs/concepts
