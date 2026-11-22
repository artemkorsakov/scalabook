# CyclicBarrier — Reusable Synchronization Barrier

`CyclicBarrier[F[_]]` is a **reusable synchronization primitive**
that allows a group of fibers to **wait for each other** at a specific point in execution.
The barrier is initialized with a positive integer `n` (number of participants).
When `n` fibers call `await`, **all of them are simultaneously unblocked**, and the barrier **resets** for the next round.

**Metaphor:** Imagine a tour group waiting by a bus.
The driver won't leave until **all** tourists have gathered (`await`).
When all 10 people arrive, the driver opens the door, everyone boards, and the barrier resets for the next tour.

```dotty
trait CyclicBarrier[F[_]]:
  /** Waits until all n participants call await.
    * When this happens, all are unblocked and the barrier resets. */
  def await: F[Unit]
```

**Key characteristics:**

- **Reusable** — after the barrier is passed, it can be used again (unlike `CountDownLatch`).
- **Synchronous for all** — all participants are released simultaneously.
- **Cancelable** — if a fiber is cancelled during the wait, the barrier "loses" it and will wait for the full set again.

## Creating `CyclicBarrier`

```dotty
import cats.effect.IO
import cats.effect.std.CyclicBarrier

val barrier: IO[CyclicBarrier[IO]] = CyclicBarrier[IO](3)  // barrier for 3 participants
```

The parameter `n` must be a **positive integer** (≥ 1).

## How Does It Work Under the Hood?

```
Initialization: barrier waits for 3 participants
           │
           ▼
    Fiber 1 calls await ──► waits
    Fiber 2 calls await ──► waits
    Fiber 3 calls await ──► waits
           │
           ▼
    All 3 have gathered! ──► simultaneous unblocking
           │
           ▼
    Barrier resets ──► waits for 3 participants again
```

**Important difference from `CountDownLatch`:** In `CountDownLatch`, it doesn't matter who calls `release`, the total count matters.
In `CyclicBarrier`, **each participant must personally** call `await`. You cannot "substitute" one participant with another.

## Detailed Example: Three Fibers with Different Speeds

This example shows how the barrier forces the fast fiber to wait for the slow ones.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.CyclicBarrier
import scala.concurrent.duration.*
import cats.syntax.all.*

object BarrierDemo extends IOApp.Simple:
  val run: IO[Unit] =
    for
      barrier <- CyclicBarrier[IO](2) // barrier for 2 participants

      // Fast fiber (reaches the barrier almost immediately)
      f1 <-
      (IO.println("⚡ FAST: reached the barrier") *>
        barrier.await *>
        IO.println("⚡ FAST: passed the barrier!")).start

      // Slow fiber (sleeps 2 seconds before the barrier)
      f2 <-
      (IO.sleep(2.seconds) *>
        IO.println("🐢 SLOW: reached the barrier") *>
        barrier.await *>
        IO.println("🐢 SLOW: passed the barrier!")).start

      _ <- (f1.join, f2.join).tupled
    yield ()
end BarrierDemo
```

**Output:**

```
⚡ FAST: reached the barrier
(pause ~2 seconds)
🐢 SLOW: reached the barrier
⚡ FAST: passed the barrier!
🐢 SLOW: passed the barrier!
```

**What happened:**

1. The fast fiber reaches the barrier and **blocks**, waiting for the second participant.
2. The slow fiber sleeps for 2 seconds, then reaches the barrier.
3. When both fibers reach the barrier, they are **simultaneously** unblocked and continue execution.

## Reusability (Cyclic Behavior)

Unlike `CountDownLatch`, `CyclicBarrier` can be used multiple times.
After all participants pass the barrier, it **automatically resets** and is ready for the next round.

```dotty
import cats.effect.std.CyclicBarrier
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

import scala.concurrent.duration.*

object ReusableBarrier extends IOApp.Simple:
  val run: IO[Unit] =
    for
      barrier <- CyclicBarrier[IO](3)
      fibers  <- (1 to 3).toList.traverse(i => worker(barrier, i).start)
      _       <- fibers.traverse_(_.join)
    yield ()

  def worker(barrier: CyclicBarrier[IO], id: Int): IO[Unit] =
    for
      _ <- IO.sleep((id * 500).millis)
      _ <- IO.println(s"Worker $id: round 1, waiting...")
      _ <- barrier.await
      _ <- IO.println(s"Worker $id: round 1, done!")

      _ <- IO.sleep((id * 300).millis)
      _ <- IO.println(s"Worker $id: round 2, waiting...")
      _ <- barrier.await
      _ <- IO.println(s"Worker $id: round 2, done!")
    yield ()
end ReusableBarrier
```

**Output:**

```
Worker 1: round 1, waiting...
Worker 2: round 1, waiting...
Worker 3: round 1, waiting...
Worker 3: round 1, done!
Worker 2: round 1, done!
Worker 1: round 1, done!
Worker 1: round 2, waiting...
Worker 2: round 2, waiting...
Worker 3: round 2, waiting...
Worker 3: round 2, done!
Worker 2: round 2, done!
Worker 1: round 2, done!
```

**Result:** All three workers pass through the barrier twice, synchronizing independently in each round.

## Cancelling the Wait

If a fiber awaiting on `await` is **cancelled**, the barrier loses one participant.
The remaining fibers will wait until `n` active callers are reached (or until another fiber is cancelled).

```dotty
import cats.effect.std.CyclicBarrier
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

import scala.concurrent.duration.*

object BarrierCancel extends IOApp.Simple:
  val run: IO[Unit] =
    for
      barrier <- CyclicBarrier[IO](3)

      f1 <-
      (IO.println("1: waiting...") *> barrier.await *>
        IO.println("1: passed!")).start
      f2 <-
      (IO.println("2: waiting...") *> barrier.await *>
        IO.println("2: passed!")).start

      // The third participant will be cancelled
      f3 <-
      (IO.sleep(1.second) *> IO.println("3: waiting...") *> barrier.await *>
        IO.println("3: passed!")).start

      _ <- IO.sleep(500.millis)
      _ <- IO.println("Cancelling the third participant!")
      _ <- f3.cancel

      _ <-
      (IO.sleep(2.seconds) *>
        IO.println("Adding a new third participant")).start

      f4 <-
      (IO.println("4: waiting...") *> barrier.await *>
        IO.println("4: passed!")).start

      _ <- List(f1, f2, f4).traverse_(_.join)
    yield ()
end BarrierCancel
```

**Output:**

```
1: waiting...
2: waiting...
Cancelling the third participant!
4: waiting...
1: passed!
4: passed!
2: passed!
```

**Behavior:**

- Fibers 1 and 2 are waiting.
- Fiber 3 is cancelled → barrier "loses" it.
- Fiber 4 arrives and becomes the third participant → all three (1, 2, 4) pass the barrier.

## `CyclicBarrier` vs `CountDownLatch`

| Characteristic               | `CyclicBarrier`                            | `CountDownLatch`                          |
|:-----------------------------|:-------------------------------------------|:------------------------------------------|
| **One-shot / Reusable**      | ✅ Reusable (cyclic)                        | ❌ One-shot                                |
| **State Reset**              | Automatic after reaching 0                 | No (cannot reset)                         |
| **Who Calls**                | Each participant personally (n `await` calls) | Anyone (n `release` calls)            |
| **Waiting**                  | All participants wait for each other       | Waiters wait until the counter becomes 0  |
| **Cancellation**              | Reduces the number of participants          | Doesn't affect the counter                |
| **Typical Scenario**         | Phase synchronization in a multi-step process | Waiting for a group of independent events |

## Limitations and Pitfalls

### 1. `n` Must Be ≥ 1

Creating a barrier with `n = 0` doesn't make sense.

### 2. Fibers Cancelled on `await` Don't Recover

If a fiber is cancelled during the wait, the barrier loses it permanently for the current cycle.
The remaining fibers will need `n` **other** fibers to pass the barrier.

### 3. Not for Dynamic Participant Sets

The participant set is fixed when the barrier is created. You cannot add a new participant "mid-cycle".

### 4. Not for Data Exchange

`CyclicBarrier` does not pass data between fibers. If you need to exchange results, use `Queue` or `Ref`.

## Summary: When to Use `CyclicBarrier`?

✅ **Use `CyclicBarrier` if:**

- Multiple fibers need to synchronize **repeatedly** at different stages.
- All participants must complete the current step before moving to the next.
- You don't want to recreate the barrier after each use.
- Typical scenarios: multi-phase computations, algorithms with barrier synchronization, testing parallel systems.

❌ **Don't use `CyclicBarrier` if:**

- You need to wait for a **group of independent events** (use `CountDownLatch`).
- Participants are added/removed dynamically (the barrier is not suitable).
- You need a **one-time** barrier (easier to use `CountDownLatch`).
- You need to pass data between fibers (use `Queue`, `Deferred`).

**Key idea:** `CyclicBarrier` is the ideal choice for **synchronizing recurring (cyclic) phases** in a group of fibers.
It is indispensable in algorithms where each round depends on all participants completing the previous round.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Cyclic Barrier](https://typelevel.org/cats-effect/docs/std/cyclic-barrier)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts
