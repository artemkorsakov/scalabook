# Semaphore — Counting Semaphore for Controlling Concurrency

**Semaphore** is a classic synchronization primitive
that controls access to a shared resource using a counter of **permits**.
Imagine a turnstile in a metro that lets only `n` people through per minute, and the rest must wait.

- **Permit counter** — a non-negative integer indicating how many threads (fibers) can simultaneously access the resource.
- **Acquire (acquiring)** — decreases the counter by 1. If there are no permits (counter = 0), the fiber **blocks** and waits.
- **Release (releasing)** — increases the counter by 1, waking up one of the waiting fibers.

### Basic Interface

```dotty
abstract class Semaphore[F[_]]:
  /** Current number of available permits (non-blocking operation). */
  def available: F[Long]

  /** Requests one permit. Blocks if none are available. */
  def acquire: F[Unit]

  /** Returns one permit back, increasing the counter. */
  def release: F[Unit]

  // ... and much more: tryAcquire, count, withPermit, etc.
```

## Semantic Blocking (Not Thread Blocking)

The key feature of a semaphore in Cats Effect is **semantic blocking**:

- When a fiber calls `acquire` and waits for a permit, **the system thread is not blocked**.
- Instead, the fiber is suspended, and the OS thread switches to executing another fiber.
- This allows scaling to **millions of simultaneous waiting fibers**,
  unlike traditional `Semaphore` from `java.util.concurrent`.

**Important:** The blocking `acquire` operation is fully **cancelable**.
If a waiting fiber is cancelled, it will be correctly removed from the wait queue, and the counter won't "lose" a permit.

## Example from Documentation: Three Processes Fighting for a Resource

In this example, three processes (`R1`, `R2`, `R3`) simultaneously try to access a **single** (`available = 1`) resource.
The semaphore guarantees that only one of them can work at any given time.

```dotty
import cats.effect.{IO, Temporal}
import cats.effect.std.{Console, Semaphore}
import cats.syntax.all.*
import scala.concurrent.duration.*

class PreciousResource[F[_]: {Temporal, Console as console}](
    name: String,
    semaphore: Semaphore[F]
):
  def use: F[Unit] =
    for
      // 1. Check how many permits are available
      availableBefore <- semaphore.available
      _               <- console.println(s"$name >> Available: $availableBefore")

      // 2. Request a permit (decreases the counter)
      _                     <- semaphore.acquire // if 0, fiber blocks
      availableAfterAcquire <- semaphore.available
      _                     <-
        console.println(s"$name >> STARTED | Available: $availableAfterAcquire")

      // 3. Simulate working with the resource (3-second delay)
      //    Upon completion, the permit is automatically returned via `release`
      _ <- console.println(s"$name >> Working...")

      // 4. Return the permit with a delay:
      _                     <- Temporal[F].sleep(3.seconds) // Simulate work
      _                     <- semaphore.release
      availableAfterRelease <- semaphore.available
      _                     <- console.println(
        s"$name >> DONE | Available: $availableAfterRelease"
      )
    yield ()
end PreciousResource

val program: IO[Unit] =
  for
    sem <- Semaphore[IO](1) // Create semaphore with 1 permit
    r1 = new PreciousResource[IO]("R1", sem)
    r2 = new PreciousResource[IO]("R2", sem)
    r3 = new PreciousResource[IO]("R3", sem)
    _ <- List(r1.use, r2.use, r3.use).parSequence.void
  yield ()
```

### Possible Output (order may vary)

```
R1 >> Available: 1
R3 >> Available: 1
R2 >> Available: 1
R2 >> STARTED | Available: 0
R2 >> Working...
R1 >> STARTED | Available: 0
R3 >> STARTED | Available: 0
R2 >> DONE | Available: 0
R1 >> Working...
R1 >> DONE | Available: 0
R3 >> Working...
R3 >> DONE | Available: 1
```

### Analysis of What Happens

1. **Start of the race**: All three fibers simultaneously call `use`.
2. **Reading `available`**: `R1`, `R2`, and `R3` see `1`.
3. **Acquire**: `R2` executes `acquire` fastest, grabs the only permit, the counter becomes `0`.
4. **Blocking**: `R1` and `R3` can now not execute `acquire` and **semantically block**, entering the wait queue.
5. **Working**: `R2` works for 3 seconds, then calls `release`, increasing the counter to `1`.
6. **Waking up**: The waiting fibers (`R1` and `R3`) wake up. The order depends on the queue policy (usually FIFO).
   Suppose `R1` grabs the next permit, and `R3` continues to wait.
7. **Completion**: The process repeats until all have finished.

## Advanced Methods

Modern `Semaphore` provides many useful methods:

| Method                            | Description                                                                                                                           |
|:----------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------|
| `tryAcquire`                      | Non-blocking attempt to acquire a permit. Returns `Boolean`.                                                                          |
| `available`                       | Current number of permits.                                                                                                            |
| `count`                           | Same as `available`, when permits are available, but returns the number of waiting fibers (negative number) when no permits are left. |
| `acquireN(n)`                     | Requests `n` permits.                                                                                                                 |
| `releaseN(n)`                     | Returns `n` permits.                                                                                                                  |

### Example with `acquireN` and `releaseN`

```dotty
// Semaphore with 5 permits
for
  sem <- Semaphore[IO](5)
  // Request 3 permits at once
  _ <- sem.acquireN(3)
  // Now 2 are available
  _ <- sem.acquireN(2)
  // Now 0 are available
  _ <- sem.releaseN(4)
// 4 are available
yield ()
```

## Example: Limiting Concurrency of HTTP Requests

A real scenario: you need to send 1000 requests to an external API, but it accepts no more than 10 simultaneously.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Semaphore
import cats.syntax.all.*

object ApiClient extends IOApp.Simple:
  def fetchUrl(url: String): IO[String] =
    IO.pure(s"Data from $url") // simulate request

  def run: IO[Unit] =
    val urls = (1 to 1000).map(i => s"https://api.example.com/data/$i").toList

    Semaphore[IO](10).flatMap {
      semaphore => // max 10 simultaneous requests
        urls.parTraverse { url =>
          semaphore.permit.use { _ =>
            fetchUrl(url)
          }
        }.flatMap(IO.println)
    }
```

**Result:** No more than 10 simultaneous requests will be active, the rest will wait for the semaphore to be released.

## Semaphore with a Dynamic Number of Permits

A semaphore doesn't have to be static.
You can increase the number of available permits at runtime by calling `release` without a corresponding `acquire`.

```dotty
for
  sem <- Semaphore[IO](0) // start with no permits
  // Add 5 permits
  _ <- sem.releaseN(5)
// Now 5 are available
yield ()
```

## Comparison with Other Primitives

| Primitive        | Purpose              | Permits         | Multiple Access       |
|:-----------------|:---------------------|:----------------|:----------------------|
| **`Semaphore`**  | Concurrency control  | Counting (n)    | Yes (up to n fibers)  |
| **`Mutex`**      | Mutual exclusion     | 0 or 1          | No (only 1 fiber)     |
| **`Deferred`**   | One-time signal      | One-time        | No (wakes all)        |
| **`Queue`**      | Data buffering       | Depends on capacity | Yes (producer-consumer) |

## Summary: When to Use `Semaphore`?

✅ **Use a semaphore if you need to:**

- Limit the number of simultaneous accesses to a resource (API, DB, file system).
- Implement a "connection pool" pattern.
- Control resource utilization in a high-throughput system.
- Orchestrate access to shared state with a non-binary lock.

❌ **Don't use a semaphore if:**

- You need the resource to belong to only one fiber — use `Mutex`.
- You need to wait for a single signal — use `Deferred`.
- You need an asynchronous message queue — use `Queue`.

**Key idea:** `Semaphore` is an indispensable tool for fighting system overload.
It allows handling peak loads without choking,
and is a fundamental building block for many concurrent structures in Cats Effect.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Semaphore](https://typelevel.org/cats-effect/docs/std/semaphore)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts
