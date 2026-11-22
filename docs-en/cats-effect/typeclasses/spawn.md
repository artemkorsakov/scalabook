# Spawn — Creating and Managing Fibers

`Spawn[F[_]]` (or `GenSpawn[F[_], E]` with an explicit error type) is a **typeclass**
that represents the foundation of concurrent programming in Cats Effect.
It provides the ability to **spawn lightweight fibers** and manage them.

In the Cats Effect ecosystem, the typeclass hierarchy looks like this:

```
MonadError[F, E]
    ↓
MonadCancel[F, E]       ← can cancel computations
    ↓
Spawn[F, E]             ← can create parallel fibers
    ↓
Concurrent[F, E]        ← adds synchronization primitives (Ref, Deferred)
    ↓
Temporal[F, E]          ← ability to suspend thread execution
    ↓
Async[F]                ← working with callback-based APIs
```

**Key idea:** `Spawn` is the minimal set of operations required for **concurrent execution of effects**.
Anything that requires parallelism — `start`, `race`, operations like `parTraverse` — ultimately relies on `Spawn`.

## Full Typeclass Signature

```dotty
import cats.effect.*

trait Spawn[F[_], E] extends MonadCancel[F, E], Unique[F]:

  /** Launches effect `fa` in a new fiber. Returns a `Fiber` that allows
    * managing that fiber.
    */
  def start[A](fa: F[A]): F[Fiber[F, E, A]]

  /** An effect that never completes (hangs). Useful for tests,
    * fallback branches, implementing infinite servers.
    */
  def never[A]: F[A]

  /** "Yield control" — voluntarily yields the processor to the scheduler.
    * Allows other fibers to execute and serves as a cancellation point.
    */
  def cede: F[Unit]

  /** A race between two effects with access to the losing fiber.
    * A low-level primitive for building complex concurrent operations.
    */
  def racePair[A, B](fa: F[A], fb: F[B]): F[Either[
    (Outcome[F, E, A], Fiber[F, E, B]), // fa wins
    (Fiber[F, E, A], Outcome[F, E, B])  // fb wins
  ]]
end Spawn
```

> **Note:** In Cats Effect 3, the error type `E` is typically fixed as `Throwable`: `type Spawn[F[_]] = GenSpawn[F, Throwable]`

## `start` Method — Creating a Fiber

```dotty
def start[A](fa: F[A]): F[Fiber[F, E, A]]
```

1. Calling `start` **does not execute** `fa` immediately.
2. It creates a new fiber and binds it to the Cats Effect scheduler's thread pool.
3. The fiber begins executing **concurrently** with the current fiber — without blocking the calling code.
4. A `Fiber` object is returned, which can be used to:
    - Wait for the result (`.join`)
    - Cancel execution (`.cancel`)
    - Check the state (`.poll`)

### Simple Example

```dotty
import cats.effect.*
import scala.concurrent.duration.*

object StartExample extends IOApp.Simple:
  def run: IO[Unit] =
    for
      // Start a background task
      fiber <- IO.println("Background work...").foreverM.start

      // Main work
      _ <- IO.println("Main work")
      _ <- IO.sleep(2.seconds)

      // Stop the background task
      _ <- fiber.cancel
      _ <- IO.println("Done")
    yield ()
```

**Output:**

```
Main work
Background work...
...
...
...
Background work...
Done
```

| Property       | Description                                                              |
|----------------|--------------------------------------------------------------------------|
| **Lightweight**   | Can create millions of fibers without memory issues         |
| **Cancellable**   | A fiber can be cancelled at any safe point                    |
| **Non-blocking**  | `start` doesn't wait for `fa` to execute — returns immediately |
| **Scheduling**    | The fiber will be scheduled on the nearest available pool capacity |

## `never` Method — Infinite Wait

```dotty
def never[A]: F[A]
```

`never` is an effect that **never completes**. It can be:

- Cancelled from outside (then it will complete with `Outcome.Canceled`)
- Wait infinitely, ignoring all signals (if protected by `uncancelable`)

### `never` vs `sleep(Duration.Inf)`

| Aspect          | `never`                   | `sleep(Inf)`                  |
|-----------------|---------------------------|-------------------------------|
| **Behavior**    | Never completes           | Waits infinitely              |
| **Semantics**   | Pure "hanging" state      | Waiting on a timer            |
| **Cancellation**| Yes, at cancellation points | Yes, upon waking          |
| **Resources**   | Allocates nothing         | May allocate a timer          |
| **Usage**       | Fallback, stubs           | Timeouts, delays              |

## `cede` Method — Voluntary Yield

```dotty
def cede: F[Unit]
```

`cede` (from Latin "to yield") is a **cooperative context-switching point**:

1. The current fiber **voluntarily** yields the processor to the scheduler.
2. The scheduler may choose another ready fiber for execution.
3. If no other ready fibers exist, the current fiber will continue execution immediately.

### When to Use `cede`?

```dotty
// 1. In CPU-intensive loops — for responsiveness
def cpuHeavy[F[_]: {Spawn, MonadCancelThrow}](n: Int): F[Unit] =
  def loop(i: Int): F[Unit] =
    if i >= n then MonadCancelThrow[F].unit
    else
      // Allow other fibers to work and check cancellation
      Spawn[F].cede >> computeChunk(i).flatMap(_ => loop(i + 1))
  loop(0)

// 2. For fair scheduling
def fairLoop[F[_]: Spawn](items: List[String]): F[Unit] =
  items.traverse_ { item =>
    for
      _ <- processItem(item) // processing
      _ <- Spawn[F].cede     // give a chance to other fibers
    yield ()
  }

// 3. In infinite loops — to avoid "starvation"
def heartbeat[F[_]: {Spawn, Temporal}]: F[Unit] =
  (Spawn[F].cede >> Temporal[F].sleep(1.second) >> heartbeat).foreverM
```

### `cede` as a Cancellation Point

An important property: `cede` **is a cancellation point**.
If a fiber has received a cancellation signal, calling `cede` will stop it.

```dotty
def cancelableLoop[F[_]: Spawn]: F[Unit] =
  def loop(): F[Unit] =
    Spawn[F].cede >> heavyComputation() *> loop()

  loop()    
```

## `racePair` Method — Low-Level Race

```dotty
def racePair[A, B](fa: F[A], fb: F[B]): F[Either[
  (Outcome[F, E, A], Fiber[F, E, B]),
  (Fiber[F, E, A], Outcome[F, E, B])
]]
```

### Why `racePair` If There's Already `race`?

`race` (which is built on `racePair`) returns only the **winner's result**. `racePair` gives **access to the losing fiber** — this allows:

- Cancelling the loser (by default)
- Continuing its execution in the background
- Getting its result later
- Performing finalization with access to `Outcome`

### Example: Race With Loser Retention

```dotty
import cats.MonadThrow
import cats.effect.*

def raceWithLoser[A, B](
    taskA: IO[A],
    taskB: IO[B]
): IO[Either[(A, Fiber[IO, Throwable, B]), (Fiber[IO, Throwable, A], B)]] =
  Spawn[IO].racePair(taskA, taskB).flatMap:
    case Left((outcomeA, fiberB)) =>
      // A won — extract the result
      outcomeToResult(outcomeA).map(a => Left((a, fiberB)))

    case Right((fiberA, outcomeB)) =>
      // B won
      outcomeToResult(outcomeB).map(b => Right((fiberA, b)))

def outcomeToResult[F[_]: MonadThrow, A](outcome: Outcome[F, Throwable, A])
    : F[A] =
  outcome match
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    =>
      MonadThrow[F].raiseError(new Exception("Race cancelled"))
```

### Implementing `race` via `racePair`

```dotty
def race[F[_]: {Spawn, MonadThrow}, A, B](fa: F[A], fb: F[B]): F[Either[A, B]] =
  Spawn[F].racePair(fa, fb).flatMap:
    case Left((outcomeA, fiberB)) =>
      fiberB.cancel >> outcomeToResult(outcomeA).map(Left(_))

    case Right((fiberA, outcomeB)) =>
      fiberA.cancel >> outcomeToResult(outcomeB).map(Right(_))
```

## Laws of `Spawn`

Any correct implementation of `Spawn` must satisfy the laws:

1. **`start` law:**

    ```dotty
    start(fa).flatMap(_.join) <-> MonadCancel[F].uncancelable(_ => fa)
    ```

    Starting a fiber and immediately joining to it is equivalent to running `fa` in an uncancelable block.

2. **`never` law:**

    ```dotty
    race(never[A], fa) <-> fa.map(Right(_))
    race(fa, never[A]) <-> fa.map(Left(_))
    ```

    Racing with `never` lets the other participant win.

3. **`cede` law:**

    ```dotty
    cede >> fa <-> fa >> cede <-> fa
    ```

    `cede` is the "unit" in the [monoid](../../typeclass/monoid/monoid.md) of context switches (idempotent and doesn't change semantics).

4. **`racePair` associativity:** A complex law guaranteeing that races can be composed.

## Summary: What Does `Spawn` Give?

| Capability                     | Expressed As                        |
|--------------------------------|-------------------------------------|
| **Concurrent execution**       | `start` and `race`                  |
| **Lifecycle management**       | `Fiber` with `join`, `cancel`       |
| **Cooperative multitasking**   | `cede` for yielding control         |
| **Infinite processes**         | `never` for "everlasting" tasks     |
| **Low-level race control**     | `racePair` with access to the loser |

**Key idea:** `Spawn` is the "minimal parallel primitive",
sufficient for building any parallel code.

In real applications, you rarely call `Spawn[F].start` directly — you more often use `parTraverse`, `race`, `timeout`
and other high-level combinators, which internally rely on `Spawn`.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Spawn](https://typelevel.org/cats-effect/docs/typeclasses/spawn)

[doc]: https://typelevel.org/cats-effect/docs/concepts
