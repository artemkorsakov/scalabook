# CountDownLatch — Waiting for a Specific Number of Events

`CountDownLatch[F[_]]` is a **one-shot synchronization primitive**
that allows one or more fibers to **block** until
a given number of events (releases) occur.
Once zero is reached, all waiting fibers are unblocked, and subsequent `await` calls are non-blocking.

**Analogy:** Imagine a 100-meter race with three participants.
The head judge won't start the race until **all three runners** are at the starting line.
Each arriving runner "releases" one latch.
When all three have arrived — the latch opens, and the judge gives the "start!" command.

```
Initialization: n = 3
[Latch open? NO]
    ↓
Runner 1 arrives → release() → n = 2
Runner 2 arrives → release() → n = 1
Runner 3 arrives → release() → n = 0
    ↓
[Latch open!] → all waiting (judge) are woken up
```

```dotty
trait CountDownLatch[F[_]]:
  /** Decreases the latch counter by 1. If the counter reaches 0, wakes up all
    * waiting fibers.
    */
  def release: F[Unit]

  /** Blocks the current fiber until the latch counter becomes 0. If the counter
    * is already 0, returns immediately.
    */
  def await: F[Unit]
```

**Key characteristics:**

- **One-shot** — once the counter reaches 0, it cannot be reset or incremented again.
- **Cumulative** — it doesn't matter which fiber calls `release`, the total count matters.
- **Cancelable** — a fiber awaiting on `await` can be cancelled, and it will be removed from the wait queue.

## Creating `CountDownLatch`

```dotty
import cats.effect.IO
import cats.effect.std.CountDownLatch

val latch: IO[CountDownLatch[IO]] = CountDownLatch[IO](3) // wait for 3 events
```

The parameter `n` must be a **positive integer**.
If you pass 0, the latch is considered already open, and `await` never blocks.

## Detailed Example: Coordinating Three Workers

In this example, the main fiber waits for three workers to report readiness.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.CountDownLatch
import scala.concurrent.duration.*
import cats.syntax.all.*

object LatchDemo extends IOApp.Simple:
  val run: IO[Unit] =
    for
      latch <- CountDownLatch[IO](3) // wait for 3 events

      // Start 3 worker fibers
      _ <- (1 to 3).toList.traverse { id =>
        val worker =
          for
            _ <-
              IO.sleep((id * 500).millis) // each worker works for its own time
            _ <- IO.println(s"Worker $id: ready!")
            _ <- latch.release // signal readiness
          yield ()
        worker.start
      }

      // Main fiber waits for all
      _ <- IO.println("Main: waiting for all workers...")
      _ <- latch.await // blocks until 3 releases are called
      _ <- IO.println("Main: all ready! Starting work!")
    yield ()
end LatchDemo
```

**Output:**

```
Main: waiting for all workers...
Worker 1: ready!
Worker 2: ready!
Worker 3: ready!
Main: all ready! Starting work!
```

**What happened:**

- The main fiber calls `await` and blocks.
- Three workers complete one by one and call `release`.
- After the third `release`, the counter becomes 0, the main fiber is woken up.

## Cancelling the Wait

`await` fully supports cancellation. If a waiting fiber is cancelled, it is correctly removed from the queue, and **the latch counter is not changed**.

```dotty
import cats.effect.IO
import cats.effect.std.CountDownLatch
import scala.concurrent.duration.*

val program =
  for
    latch <- CountDownLatch[IO](2)

    // Fiber that will be cancelled
    fiber <-
    (IO.println("Starting to wait...") *> latch.await *>
      IO.println("Never printed")).start
    _ <- IO.sleep(1.second)
    _ <- IO.println("Cancelling the wait!")
    _ <- fiber.cancel // fiber is cancelled, latch not released

    // Check: latch still waits for 2 events
    _ <- latch.release // one
    _ <- IO.println("First release")
    _ <- latch.release // two
    _ <- IO.println("Second release")
  yield ()
```

**Output:**

```
Starting to wait...
Cancelling the wait!
First release
Second release
```

## Example: Multiple Waiting Fibers

If multiple fibers call `await` before the latch is open, **all of them** will be woken up when the counter reaches 0.

```dotty
import cats.effect.IO
import cats.effect.std.CountDownLatch

val program =
  for
    latch <- CountDownLatch[IO](1)

    // 5 fibers wait for one event
    fibers <- (1 to 5).toList.traverse { id =>
      (IO.println(s"  Fiber $id: waiting...") *> latch.await *>
        IO.println(s"  Fiber $id: woken up!")).start
    }

    _ <- IO.sleep(1.second)
    _ <- IO.println("Opening the latch!")
    _ <- latch.release // → ALL 5 fibers are woken up
    _ <- fibers.traverse_(_.join)
  yield ()
```

**Output:**

```
  Fiber 1: waiting...
  Fiber 2: waiting...
  Fiber 3: waiting...
  Fiber 4: waiting...
  Fiber 5: waiting...
Opening the latch!
  Fiber 4: woken up!
  Fiber 1: woken up!
  Fiber 3: woken up!
  Fiber 5: woken up!
  Fiber 2: woken up!
```

## Comparison with Other Primitives

| Primitive               | Mechanism                  | One-shot?                                 | Num. of Events          | Wakes Up                |
|:------------------------|:---------------------------|:------------------------------------------|:------------------------|:------------------------|
| **`CountDownLatch`**    | Counter decremented to 0   | ✅ Yes                                     | `n` (set at creation)   | All waiting fibers      |
| **`CyclicBarrier`**     | Counter, resets after 0    | ❌ No (cyclic)                             | `n` (set at creation)   | All waiting fibers      |
| **`Deferred[F, Unit]`** | One-time signal            | ✅ Yes                                     | 1                       | All waiting fibers      |
| **`Semaphore(0)`**      | Permits                    | ❌ No (can `release` multiple times)       | Variable                | One per `release`       |
| **`Queue`**             | Queue of elements          | ❌ No                                      | Variable                | One per `take`          |

**Key differences of `CountDownLatch`:**

- Requires a **clearly defined number** of events (unlike `Deferred[Unit]`).
- Wakes up **all** waiting fibers simultaneously (unlike `Semaphore`).
- **Not cyclic** (unlike `CyclicBarrier`).

## Limitations and Pitfalls

### 1. Too Many `release` Calls Don't Harm

If `release` is called more than `n` times, extra calls are ignored. This is safe, but can hide logical errors.

```dotty
for
  latch <- CountDownLatch[IO](2)
  _     <- latch.release // n=1
  _     <- latch.release // n=0, open
  _     <- latch.release // ignored (n stays 0)
  _     <- latch.await   // immediate return
yield ()
```

### 2. `n == 0` — A Special Case

`CountDownLatch[IO](0)` is immediately open, `await` never blocks. Use this carefully.

### 3. Cancelling `await` Does Not Decrease the Counter

If a fiber is cancelled while waiting, this does **not** trigger a `release`.
The counter remains unchanged, and the system continues to wait for more `release` calls.

### 4. Cannot Reset After Reaching 0

Unlike `CyclicBarrier`, `CountDownLatch` cannot be reused.
If you need cyclic behavior, use [CyclicBarrier](cyclic-barrier.md).

## Summary: When to Use `CountDownLatch`?

✅ **Use `CountDownLatch` if:**

- You need one or more fibers to **wait until several independent events** occur.
- You know the **exact number** of events in advance (`n`).
- You want all waiting fibers to **wake up simultaneously**.
- You need a **one-shot** mechanism (no need to reset).

❌ **Don't use `CountDownLatch` if:**

- You need to wait for an **infinite or unknown** number of events (use `Deferred` or `Queue`).
- You need **cyclic** behavior (use `CyclicBarrier`).
- You need to limit concurrency (use `Semaphore`).
- You need to **exchange messages** (use `Queue`).

**Key idea:** `CountDownLatch` is the ideal tool for the "main waits for everyone to get ready" scenario.
It is lightweight, clear, and accurately expresses the intent: wait for `n` events, then proceed.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Count Down Latch](https://typelevel.org/cats-effect/docs/std/countdown-latch)
- [Concurrency in Cats Effect 3](https://typelevel.org/blog/concurrency-in-ce3.html)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts
