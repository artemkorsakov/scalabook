# Testing the Runtime

## Problem: How to Test Async and Nondeterministic Code

In an ideal world, most of your application's business logic can be extracted into [_pure_ functions](../fp/fp/pure_function.md) not tied to `IO`.
They are easy to test with standard unit tests: feed inputs — check outputs.
But the real world is more complex. Often, you need to write tests for logic that is **tightly coupled with effects**:

- Functions that read files or make network calls.
- Code that runs in parallel (race conditions, data races).
- Logic that **works with time**: timeouts, retries, delays, scheduling.

It's the last point — working with time — that creates the most difficulties.

### Why Is Time Hard to Test?

**Two main reasons:**

1. **Imprecision:** Time is inherently imprecise. `IO.realTime` will give different values on every run.
   Even `IO.monotonic` — while more stable — is not ideal. The JVM is not a real-time system,
   operating systems (even Linux) are not real-time OSs,
   and hardware is almost never designed for deterministic execution.
   This means tests that directly or indirectly measure time must have an **acceptable error margin**.
   Otherwise, they will frequently fail, especially on busy CI servers.
2. **Slowness:** `IO.sleep(duration)` literally means "go to sleep."
   If the function under test sleeps for an hour, the test will take **at least an hour**.
   In practice, tests use very short intervals (e.g., `100.millis`).
   Frameworks like [MUnit](https://scalameta.org/munit/) and [Specs2](https://etorreborre.github.io/specs2/)
   run tests in parallel to mitigate this, but even short delays accumulate, making tests slow.
   Moreover, this exacerbates **imprecision**: with short delays, the error margin can be comparable to the delay itself.

This is highly undesirable. But there is a solution.

### Solution: `TestControl` — Simulating Time

Fortunately, `IO` and its type class `Temporal` encode all time primitives (`IO.realTime`, `IO.monotonic`, `IO.sleep`) as effects.
Most programs in the Cats Effect ecosystem use these functions rather than direct system calls.
Therefore, Cats Effect can offer an elegant solution: **`TestControl`**.

> **Important:** If your function uses `IO` but does NOT work with time, you most likely don't need `TestControl`.
> Use standard testing libraries: [Weaver test](https://typelevel.org/weaver-test/overview/installation.html),
> [MUnit Cats Effect](https://github.com/typelevel/munit-cats-effect)
> or [Cats Effect Testing](https://github.com/typelevel/cats-effect-testing).

To use `TestControl`, add the dependency:

```dotty
libraryDependencies += "org.typelevel" %% "cats-effect-testkit" % "3.7.0" % Test
```

## What Is `TestControl`?

`TestControl` is a **simulated (mock) runtime** for `IO`.
It fully implements all of `IO`'s functionality: fibers, async callbacks, and **time support**.
But this simulation has three important differences from the standard `IORuntime`:

### 1. Single-Threaded and Nondeterministic

`TestControl` uses **only one thread** to execute all `IO` (like JavaScript).
This helps expose concurrency bugs (data races) that are masked in the real world. But actually, `TestControl` is **nondeterministic**.
Unlike the real production environment (even single-threaded), `TestControl` **randomly chooses**
which of the ready-to-execute fibers to run next.
This helps expose subtle bugs related to execution order
that may only reproduce once in a thousand runs in a real environment.

### 2. Controlled Time

The internal representation of time in `TestControl` changes **only on external command**.
All three time functions — `realTime`, `monotonic`, and `sleep` — behave consistently within this simulation:

- If you "sleep" a fiber for `256.millis`, measuring time before and after, you'll see
  that the second measurement is **exactly 256.millis** greater than the first (or more, if you advance time further).

**Important nuance:** `TestControl` **cannot** simulate time spent on computations.
In its universe, **all computations, all side effects (including disk or network I/O) happen instantly and do not advance time**.
This can have unexpected consequences (see the "Pitfalls" section below).

### 3. Step-by-Step Execution

`TestControl` allows you to **control program execution from the outside**. You can:

- Advance execution to a specific point.
- Pause the _entire program_.
- Check state, result presence, current simulated time.
- Advance time and run execution again.

In effect, `TestControl` is a powerful **debugger** for any `IO` application, built into the test environment.

## Example: Function with Exponential Backoff (Retry)

As a demonstration, let's consider the `retry` function.
It implements a simple retry strategy with exponential backoff and random jitter.

```dotty
import cats.effect.IO
import cats.effect.std.Random
import scala.concurrent.duration.*

def retry[A](
    ioa: IO[A],
    delay: FiniteDuration,
    max: Int,
    random: Random[IO]
): IO[A] =
  if max <= 1 then ioa
  else
    ioa.handleErrorWith: _ =>
      random.betweenLong(0L, delay.toNanos).flatMap: ns =>
        IO.sleep(ns.nanos) *> retry(ioa, delay * 2, max - 1, random)
```

**Logic of operation:**

- Repeats action `ioa` up to `max` times or until the first success.
- On error, sleeps for a **random** interval from `0` to the current `delay`.
- Each time, `delay` doubles.
- If all attempts fail, the last error is propagated.

This is an ideal candidate for `TestControl`: the logic is complex, involving both time and randomness and effects.

## Mode 1: Full Execution (`executeEmbed`)

Simple case: we want to check that `retry` eventually succeeds.

```dotty
import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testkit.TestControl
import scala.concurrent.duration.*

// Example test with MUnit Cats Effect
test("retry at least 3 times until success") {
  case object TestException extends RuntimeException

  var attempts = 0
  val action   = IO {
    attempts += 1
    if attempts != 3 then throw TestException
    else "success!"
  }

  val program = Random.scalaUtilRandom[IO].flatMap { random =>
    retry(action, 1.minute, 5, random)
  }

  TestControl.executeEmbed(program).assertEquals("success!")
}
```

**What's happening here:**

- `action` throws an exception on the first and second attempts, but on the third returns `"success!"`.
- `retry` is configured for a maximum of 5 attempts with an initial delay of 1 minute.
- **In a real environment**, this test could take **up to 31 minutes** (from `1 + 2 + 4 + 8` minutes)!
- **With `TestControl.executeEmbed`** it executes in **a few milliseconds**.
  The `executeEmbed` function fully runs the program in the simulated environment, automatically managing time.

If `program` completes with an error, is cancelled, or does not complete (e.g., infinite loop),
`executeEmbed` will propagate the exception, and the test will fail.

> **Note:** `TestControl` can detect **deadlocks**, such as `IO.never`.
> But it cannot solve the [halting problem](https://en.wikipedia.org/wiki/Halting_problem).
> A program of the form `IO.unit.foreverM` (not a deadlock, but an infinite loop) will never complete even during testing.

## Mode 2: Step-by-Step Execution (`execute`)

More complex case: we want to verify that between attempts, the program "sleeps" for the correct interval (from 0 to the growing delay).
This is hard to do with `executeEmbed` without adding side channels to `retry` itself.

Enter `TestControl.execute`.

```dotty
test("backoff appropriately between attempts") {
  case object TestException extends RuntimeException

  val action  = IO.raiseError(TestException) // Always fails
  val program = Random.scalaUtilRandom[IO].flatMap { random =>
    retry(action, 1.minute, 5, random)
  }

  TestControl.execute(program).flatMap { control =>
    for
      _ <- control.results.assertEquals(None)
      _ <- control.tick

      _ <- (0 until 4).toList.traverse { i =>
        for
          _        <- control.results.assertEquals(None)
          interval <- control.nextInterval
          _        <- IO(assert(interval >= 0.nanos))
          _        <- IO(assert(interval < (1L << i).minute))
          _        <- control.advanceAndTick(interval)
        yield ()
      }

      _ <- control.results.assertEquals(Some(Outcome.failed(TestException)))
    yield ()
  }
}
```

### Step-by-Step Explanation

1. **`TestControl.execute(program)`**: Starts the program in simulation, but **does not execute it**.
   Returns `IO[TestControl[A]]` through which we control execution.
2. **`control.results.assertEquals(None)`**: Checks that the program has not yet completed.
3. **`control.tick`**: Runs execution until the first `IO.sleep`. The fiber falls asleep, `tick` returns control.
4. **Loop over attempts (0 to 4):**
     - **`control.nextInterval`**: Returns the minimum time to advance the simulated clocks
       to wake up at least one fiber. This will be our delay (from 0 to `delay`).
     - **Check the range:** `interval` must be from `0.nanos` to `(1 << i).minute` (1, 2, 4, 8 minutes...).
     - **`control.advanceAndTick(interval)`**: Advances simulated time by `interval` and runs execution.
       The program wakes up, gets an error, starts the next attempt, and falls asleep again.
5. After 5 failed attempts, the program completes with an error. **`control.results`** should return `Some(Outcome.failed(...))`.

## Important `TestControl` Functions

| Function                   | What It Does                                                                                                                |
|:---------------------------|:----------------------------------------------------------------------------------------------------------------------------|
| `tick`                     | Runs the program, **without advancing time**, until all fibers are asleep (or the program completes).                       |
| `advance(duration)`        | Advances simulated time by `duration`, **without starting** the program.                                                     |
| `advanceAndTick(duration)` | Advances time (`advance`) and immediately runs `tick`.                                                                      |
| `tickAll`                  | Loop: `tick` → `nextInterval` → `advanceAndTick(nextInterval)`, until the program completes or a deadlock is detected.     |
| `nextInterval`             | Returns the minimum time until the nearest waking of sleeping fibers (or `Duration.Inf` if there are no sleeping fibers). |
| `results`                  | Returns `Option[Outcome[Id, Throwable, A]]` — the program result (if completed).                                            |
| `isDeadlocked`             | `true` if the program has halted (no active fibers), but not completed (typical deadlock).                                 |

## How `executeEmbed` Works

`executeEmbed` is a convenience wrapper over `execute`:

```dotty
def executeEmbed[A](program: IO[A], config: IORuntimeConfig = IORuntimeConfig(), seed: Option[String] = None): IO[A] =
  execute(program, config = config, seed = seed).flatMap: control =>
    val onCancel = IO.defer(IO.raiseError(new CancellationException()))
    val onNever = IO.raiseError(new NonTerminationException())
    val embedded = control.results.flatMap(_.map(_.embed(onCancel)).getOrElse(onNever))
    control.tickAll *> embedded
```

- `tickAll` — runs the program to the very end (or until deadlock).
- The result is extracted and "embedded" back into `IO`. Cancellation and non-termination are turned into exceptions.

## Pitfalls and Important Warnings

`TestControl` is a **simulation**, and some programs may behave differently in it than in production.
**Always, if possible, test with a real runtime first.**
Use `TestControl` only when you truly need control over time.

### Pathological Example: Infinite `cede`

```dotty
IO.cede.foreverM.start.flatMap { fiber =>
  IO.sleep(1.second) *> fiber.cancel
}
```

- **On JVM/Node.js:** The program completes in ~1 second.
- **In `TestControl`:** The program **never completes**! And `isDeadlocked` won't become `true`.
  Why? Because the fiber with `foreverM` never falls asleep. It's constantly in the "active" state,
  and `tick` will infinitely switch to it, never returning control for us to advance time.

**What to do?** Don't rely on `tick` in such cases. Use `tickOne` or more complex schemes.

### Mistake: Advancing Time Before `IO.sleep`

```dotty
TestControl.execute(IO.sleep(1.second) >> IO.realTime).flatMap: control =>
  for
    _ <- control.advanceAndTick(1.second)
    _ <- control.results.assertEquals(Some(Outcome.succeeded(1.second)))
  yield ()
```

This test **will fail**. `results` will be `None`. Why?
`advanceAndTick` advanced time BEFORE `IO.sleep` had a chance to execute.
In the simulation, `sleep` will fire after `advance`, and the fiber will sleep for 1 second.

**Fix:** First do `tick` so execution reaches `sleep`, then advance time.

```dotty
TestControl.execute(IO.sleep(1.second) >> IO.realTime).flatMap: control =>
  for
    _ <- control.tick
    _ <- control.advanceAndTick(1.second)
    _ <- control.results.assertEquals(Some(Outcome.succeeded(1.second)))
  yield ()
```

## Conclusion

`TestControl` is an incredibly powerful tool for testing async, time-dependent, and concurrent code in Cats Effect.

- Use **`executeEmbed`** for simple scenarios where you only need deterministic and fast execution.
- Use **`execute`** with step-by-step control for complex scenarios
  where you need to check internal state, delays, and execution order.

Remember the pitfalls and always design tests with the understanding that `TestControl` is a simulation,
not a 100% replacement for the production runtime. But for 99% of time-related tasks, it's the ideal tool.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Test Runtime](https://typelevel.org/cats-effect/docs/core/test-runtime)

[doc]: https://typelevel.org/cats-effect/docs/concepts
