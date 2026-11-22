# Temporal — Working with Time and Timeouts

`Temporal[F[_]]` is a typeclass that **extends `Concurrent[F[_]]`**
and adds the ability to **suspend fiber execution for a certain duration** without blocking OS threads.

Put simply, `Temporal` is a "clock with an alarm" for your effects.
It allows organizing delays, timeouts, and other time-based operations in a purely functional, non-blocking, and cancellable style.

### Simplest Example

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def myProgram[F[_]: Temporal]: F[Unit] =
  Temporal[F].sleep(5.seconds) // ← suspend for 5 seconds
```

This code looks just as simple as `Thread.sleep(5000)`, but works **radically differently** under the hood.

## Why Can't You Use `Thread.sleep`?

Let's try to implement a delay like this:

```dotty
// ❌ VERY BAD IDEA
import cats.effect.Sync

def badDelay[F[_]: Sync]: F[Unit] =
  Sync[F].delay(Thread.sleep(5.seconds.toMillis))
```

**Why is this categorically wrong?**

| Problem                   | Explanation                                                                                                                                                               |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Blocks an OS thread**   | `Thread.sleep` stops a whole system thread on which the fiber runs.                                                                                                       |
| **Kills scalability**     | If you have 1000 simultaneously "sleeping" tasks, `Thread.sleep` will require 1000 OS threads (→ memory overhead, frequent context switches).                             |
| **Breaks the scheduler**  | Cats Effect uses a limited thread pool (usually equal to the number of CPU cores). `Thread.sleep` "steals" one of these precious threads, making it unavailable for other fibers.|
| **Cancellation-resistant**| `Thread.sleep` doesn't react to Cats Effect cancellation signals. Cancelling such a fiber is practically impossible.                                                     |

**The correct approach — `Temporal[F].sleep`:**

```dotty
// ✅ CORRECT
import cats.effect.Temporal

def goodDelay[F[_]: Temporal]: F[Unit] =
  Temporal[F].sleep(5.seconds)
```

`sleep` semantically suspends the **fiber**, but doesn't touch the OS thread.
The scheduler simply removes this fiber from the execution queue and schedules a "wake-up" via the system timer.
The thread meanwhile can execute other fibers.

## Basic `Temporal` Methods

Although the full `Temporal` interface includes several methods, two are essential to understand:

### sleep

Suspends the current fiber for a specified duration (or **until cancellation**).

```dotty
def sleep(duration: FiniteDuration): F[Unit]
```

**Important properties:**

- **Doesn't block the thread** — the Cats Effect scheduler removes the fiber and returns to it after the specified duration.
- **Cancellable** — if the fiber that was sleeping inside `sleep` is cancelled, it wakes up with a cancellation error.
- **Precision** — depends on the platform and scheduler, but accurate enough for most applications.

### timeout

Executes effect `fa`, but if it doesn't complete within the allotted time, **forcibly cancels it** and returns a timeout error.

```dotty
def timeout[A](fa: F[A], duration: Duration): F[A]
```

This is probably the most frequently used method of `Temporal`.

## Deep Dive: How Does `sleep` Work Under the Hood?

Briefly, inside `sleep` the following happens:

1. A special `Deferred[F, Unit]` is created — the "wake-up signal".
2. A request is sent to the Cats Effect scheduler: "Wake me up after `duration`."
3. The scheduler adds the task to a timer collection (e.g., `HashedWheelTimer` on JVM).
4. The fiber is put into a "sleeping" mode — it no longer occupies a thread.
5. When the timer fires, the scheduler receives a notification and restarts the fiber.

**If the fiber is cancelled during sleep:**

- A command to cancel the timer is sent to the scheduler.
- The fiber receives a cancellation signal and can execute finalizers.

All of this happens **asynchronously** and without blocking threads.

## Practical Examples

### Example 1: Timeout for an External Service (timeoutTo)

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def fetchFromRemoteService[F[_]: Temporal]: F[String] =
  // simulate a slow service
  Temporal[F].sleep(5.seconds) >> Temporal[F].pure("result")

def cachedValue[F[_]: Temporal]: F[String] =
  Temporal[F].pure("cached response")

val data: IO[String] = fetchFromRemoteService[IO]
  .timeoutTo(2.seconds, cachedValue) // ← wait 2 seconds, then get the cache

// After 2 seconds, "cached response" will be returned
```

### Example 2: Race With Timeout

```dotty
def fetchWithFallback[F[_]: Temporal, A](
    primary: F[A],
    fallback: F[A],
    timeout: FiniteDuration
): F[A] =
  Temporal[F].race(primary, Temporal[F].sleep(timeout) >> fallback).flatMap {
    case Left(result)  => Temporal[F].pure(result) // success on time
    case Right(result) => Temporal[F].pure(result) // fallback triggered
  }

fetchWithFallback[IO, String](
  IO.sleep(2.seconds).as("loser"),
  IO.pure("fallback"),
  1.second
).unsafeRunSync() 
// val res0: String = "fallback"
```

## `timeout` vs `timeoutTo` vs `race`

| Method                                   | What it does                                        | Return Type               |
|------------------------------------------|-----------------------------------------------------|---------------------------|
| `timeout(dur)`                           | Cancels `fa` on timeout, throws `TimeoutException`  | `F[A]`                    |
| `timeoutTo(dur, fallback)`               | Cancels `fa` on timeout, executes `fallback`        | `F[A]`                    |
| `race(fa, Temporal.sleep(dur) >> fb)`    | The first to complete wins                          | `F[Either[A, B]]`         |


## Full Example: A Resilient Client With Timeouts and Retries

```dotty
import cats.effect.{IO, IOApp, Temporal}

import scala.concurrent.duration.*

object ResilientClient extends IOApp.Simple:
  val run: IO[Unit] = fetchWithRetry(3, 500.millis)
    .flatMap(result => IO.println(s"Success! Data: $result"))
    .handleErrorWith(err => IO.println(s"Total failure: ${err.getMessage}"))

  private def fetchWithRetry(attempts: Int, delay: FiniteDuration): IO[String] =
    def loop(remaining: Int): IO[String] =
      fetchData(attempts - remaining + 1)
        .timeout(1.second)
        .handleErrorWith { error =>
          if remaining > 1 then Temporal[IO].sleep(delay) >> loop(remaining - 1)
          else IO.raiseError(new Exception("All attempts failed"))
        }
    loop(attempts)

  private def fetchData(attempt: Int): IO[String] =
    IO.println(s"Attempt #$attempt...") >>
      IO.sleep(2.seconds) >> // simulate work
      IO.raiseError(new Exception("Network error"))

end ResilientClient
```

**What's happening here:**

- Each attempt has a 1-second timeout.
- If an attempt fails or times out, we wait `500ms` and retry.
- 3 attempts total.

```text
Attempt #1...
Attempt #2...
Attempt #3...
Total failure: All attempts failed
```

## Summary

`Temporal` gives you a **pure, composable, and safe way** to work with time in functional concurrent programs.

Three concepts:

1. **`sleep`** — non-blocking delay for a fiber.
2. **`timeout`** — limiting execution time of an effect.
3. **Cancellation** — everything inside `Temporal` works beautifully with Cats Effect's cancellation system.

Thanks to `Temporal`, you can write code with delays and timeouts
that scales to millions of simultaneously waiting fibers, while remaining simple and readable.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Temporal](https://typelevel.org/cats-effect/docs/typeclasses/temporal)

[doc]: https://typelevel.org/cats-effect/docs/concepts
