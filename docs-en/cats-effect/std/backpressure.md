# Backpressure — Controlling Data Flow Under Overload

`Backpressure` is a flow control mechanism
that allows a system to **regulate the speed of processing incoming tasks** when resources are limited.
If a task arrives faster than the system can process it, `Backpressure` applies an overload management strategy.

Imagine this:

- **A coffee shop** where the barista can only prepare 10 cups of coffee per hour.
  If 20 customers arrive, `Backpressure` decides what to do with the "extra" orders.
- **An API server** processing 1000 requests per second, but the database can only handle 800.
  `Backpressure` won't let the DB "choke".

```dotty
trait Backpressure[F[_]]:
  /** Runs effect f, applying a rate-limiting strategy. */
  def metered[A](f: F[A]): F[Option[A]]
```

**Note:** `Backpressure` returns `F[Option[A]]`, not `F[A]`.
Because depending on the strategy and current load, the effect may be **discarded** — returning `None`.

## Two Strategies: `Lossy` and `Lossless`

`Backpressure` can be created with one of two behavior strategies on overflow:

### 1. Lossy (with losses)

> **"If the system is overloaded — just ignore the new task."**

- The effect **will not** be executed if backpressure is currently active.
- A new call returns `None` instantly, without blocking.
- Suitable for scenarios where **losing some data** is acceptable without serious consequences.

### 2. Lossless (without losses)

> **"If the system is overloaded — queue up and wait."**

- The effect **will** be executed, but the calling fiber is **semantically blocked** until a resource becomes available.
- Returns `Some(result)` when the task is finally executed.
- Suitable for scenarios where **data cannot be lost** (e.g., payment transactions, orders).

## How Does It Work Under the Hood?

Internally, `Backpressure` holds:

- **An active tasks counter** (current load).
- **A maximum number of concurrent tasks** (the `limit`).
- **A wait queue** (for the `Lossless` strategy).

```
Incoming metered(f) call
         │
         ▼
    Limit reached?
         │
    ┌────┴────┐
    ❌         ✅
    │         │
    │         └──────► execute f
    │                    (active tasks +1)
    │
    ▼
  ┌─────────────────┐
  │  Strategy?      │
  └─────────────────┘
    │           │
 Lossy       Lossless
    │           │
    ▼           ▼
  return      queue up
    None       and block
               fiber
```

When a task finishes, the active tasks counter decreases → if there are fibers waiting in the queue (Lossless),
one of them is unblocked and starts execution.

## Example 1: Lossless — Task Queue with Limited Concurrency

Suppose we have a service that can process at most 2 tasks at a time.
We want no task to be lost.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Backpressure
import cats.syntax.all.*

import scala.concurrent.duration.*

object LosslessExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Create Backpressure with a limit of 2 active tasks, Lossless strategy
      backpressure <- Backpressure[IO](Backpressure.Strategy.Lossless, 2)

      // Start 5 "long" tasks (each for 2 seconds)
      fibers <- (1 to 5).toList.traverse { i =>
        backpressure.metered(
          IO.sleep(2.seconds) *> IO.println(s"✅ Task $i completed")
        ).start
      }

      // Wait for all to finish
      _ <- fibers.traverse_(_.join)
    yield ()

// After 2 seconds
// ✅ Task 1 completed
// ✅ Task 2 completed
// After 2 seconds
// ✅ Task 3 completed
// ✅ Task 4 completed
// After 2 seconds
// ✅ Task 5 completed
```

**Expected behavior:**

- The first 2 tasks start immediately.
- Tasks 3, 4, 5 are **blocked** (but not lost).
- As soon as one of the first two finishes, the next task starts, and so on.
- At the end, **all 5 tasks will be executed** (Lossless guarantees this).

## Example 2: Lossy — Telemetry / Metrics (loss is acceptable)

Imagine collecting metrics from millions of sensors, but we can only process 10 per second.
Losing some metrics is not scary — what matters are the ones that do get processed.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Backpressure
import scala.concurrent.duration.*
import cats.syntax.all.*

object LossyExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Limit: 3 concurrent metrics, loss is allowed
      backpressure <- Backpressure[IO](Backpressure.Strategy.Lossy, 3)

      // Simulate 50 incoming metrics
      _ <- (1 to 50).toList.traverse { i =>
        backpressure.metered(
          IO.sleep(500.millis) *> IO.println(s"📊 Metric $i processed")
        ).flatMap {
          case Some(_) => IO.unit // processed
          case None    => IO.println(s"⚠️ Metric $i discarded (overload)")
        }
      }
    yield ()
```

**Result:** First, a few metrics will be processed, but once the limit of 3 is reached,
new calls will start returning `None` instantly.
The system won't choke, but some data will be lost.

## When to Use `Backpressure` vs Other Primitives?

| Tool                        | Purpose                                | Behavior on Overload                                           |
|:----------------------------|:---------------------------------------|:---------------------------------------------------------------|
| **`Backpressure.Lossless`** | Concurrency limiting with waiting      | Tasks are blocked, but not lost                                |
| **`Backpressure.Lossy`**    | Concurrency limiting with dropping     | Extra tasks are ignored                                        |
| **`Semaphore`**             | Concurrency limiting                   | Tasks are blocked, but without the concept of "dropping" (all wait) |
| **`Queue.bounded`**         | Task buffering                         | Queue is bounded, the producer can be blocked (`offer`)        |
| **`Queue.dropping`**        | Dropping buffering                     | Overflow elements are dropped                                  |
| **`Dispatcher`**            | Launching from impure code             | No backpressure — all effects launch                           |

**Conclusion:** `Backpressure` is a specialized `Semaphore` + additional logic for task flow,
where you can either block (Lossless) or drop (Lossy) new tasks when the limit is reached.

## Implementation Details and Warnings

### 1. `Backpressure` is Not a `Resource`

In the examples above, we create `Backpressure` via `Backpressure[IO](strategy, limit)`,
and it lives as long as there is a reference to it. When garbage collected, active tasks and the queue will be lost.
If guaranteed shutdown is needed, wrap it in a `Resource`:

```dotty
def backpressureResource[F[_]: Concurrent](
    strategy: Backpressure.Strategy,
    limit: Int
): Resource[F, Backpressure[F]] =
  Resource.make(Backpressure[F](strategy, limit))(_ =>
    ???
  ) // No stop required (no resources)
```

But in practice, `Backpressure` doesn't consume system resources (except memory), so `Resource` is not required.

### 2. Task Cancellation

If a fiber waiting in a `Lossless` queue is canceled, it will be correctly removed from the queue,
and the active tasks counter will not be affected.

### 3. Races During Creation

Creating `Backpressure` (via `apply`) is cheap but not free — it uses `Ref` and `Deferred` under the hood.
Create it once for the entire task pool, not for each metric.

### 4. Queue Size for Lossless

`Backpressure` has no configurable wait queue size — it is potentially unbounded.
If you expect a huge number of blocked fibers (millions), `Lossless` may consume a lot of memory.
In such cases, consider `Queue.bounded`.

## Summary

**Key idea:** `Backpressure` is a declarative way to tell the system:
_"Don't take on a new task if we are already overloaded. If the task can be lost — return `None` immediately, otherwise — queue up and wait."_
It is the ideal tool for writing **overload-resistant** and **self-regulating** applications.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Backpressure](https://typelevel.org/cats-effect/docs/std/backpressure)

[doc]: https://typelevel.org/cats-effect/docs/concepts
