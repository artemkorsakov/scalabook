# `Deferred` — One-Shot Container for Fiber Synchronization

`Deferred[F[_], A]` is a **purely functional synchronization primitive**
that represents a **one-shot container** for a value of type `A`.
At the moment of creation, the container is **empty**.
You can put a value into it **exactly once**, after which the container becomes **filled** forever.

Imagine a "waiting room" with one door:

- Initially, the room is empty.
- Fibers can **wait outside** (`get`) until someone opens the door.
- The **first** person who arrives with a key (`complete(a)`) opens the door and places the value.
- All waiting fibers immediately receive this value and "enter".
- All subsequent attempts to bring a key (`complete`) are ignored.

### Basic Interface

In its simplest form, `Deferred` looks like this:

```dotty
abstract class Deferred[F[_], A]:
  /** Waits for the value to appear. */
  def get: F[A]

  /** Tries to set a value (only if the container is empty). */
  def complete(a: A): F[Boolean]
  
  /** Returns the value if it exists. */
  def tryGet: F[Option[A]]
```

## Detailed Behavior of Operations

### `get` — Waiting for a Value

The behavior of the `get` method is strictly defined and predictable:

| `Deferred` State            | `get` Behavior                                                                                                                                                                               |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Empty**                   | The fiber **semantically blocks** until another call performs `complete`. The block **does not block an OS thread** (uses asynchronous waiting).                                                |
| **Filled**                  | The value is returned **immediately**.                                                                                                                                                       |
| **Cancellation during wait**| If a fiber that called `get` is cancelled, it **automatically unsubscribes** from the notification and will not be woken on `complete`. This prevents memory leaks and "dangling" fibers. |

> **Important:** The block in `get` is **semantic**. Under the hood, Cats Effect does not block system threads,
> but switches the fiber to a waiting mode, allowing other fibers to use compute resources.

### `complete` — Setting a Value

The behavior of the `complete` method is also strictly defined:

| `Deferred` State | `complete(a)` Behavior                                                              | Return Value |
|------------------|-------------------------------------------------------------------------------------|--------------|
| **Empty**        | Sets value `a`, **immediately wakes** all fibers waiting in `get`.                  | `true`       |
| **Filled**       | **Does nothing.** The value remains as originally set.                              | `false`      |

The returned `Boolean` lets you determine whether your `complete` was first (or the only successful one).

### `tryGet` — Non-blocking Inspection

Returns `Some(a)` if `Deferred` is filled, and `None` if empty. **Never blocks.**

```dotty
for
  d     <- Deferred[IO, String]
  empty <- d.tryGet // None
  _     <- d.complete("done")
  full  <- d.tryGet // Some("done")
yield ()
```

## Simple Example: Coordinating Two Fibers

This example shows how one fiber waits for a signal from another.

```dotty
object DeferredExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Create an empty Deferred for a signal (Unit value)
      signal <- Deferred[IO, Unit]

      // Waiting fiber: started first, will block
      waiter <-
      (IO.println("Waiting for signal...") >> signal.get >>
        IO.println("Signal received! Continuing.")).start

      // Signaling fiber: sleeps 2 seconds, then signals
      _ <-
      (IO.sleep(2.seconds) >>
        IO.println("Sending signal!") >>
        signal.complete(())).start

      // Give fibers time to finish
      _ <- waiter.join
    yield ()
end DeferredExample
```

**Output:**

```
Waiting for signal...
(after 2 seconds)
Sending signal!
Signal received! Continuing.
```

## Pattern: "One Time Only" (First to Win)

A classic scenario: multiple concurrent processes try to perform an action,
but **only the first successful result is needed**.

```dotty
def firstSuccessful[F[_]: Concurrent, A](tasks: List[F[A]]): F[A] =
  for
    deferred <- Deferred[F, A] // Create an empty container
    // Launch all tasks in a race
    fibers <- tasks.traverse { task =>
      task.flatMap(a => deferred.complete(a).void).start
    }
    // Wait for the first one to fill the deferred
    result <- deferred.get
    // Cancel the rest (optional, to save resources)
    _ <- fibers.traverse_(_.cancel)
  yield result

// Usage
val program = firstSuccessful(List(
  IO.sleep(3.seconds) >> IO.pure("Slow"),
  IO.sleep(1.second) >> IO.pure("Fast"),
  IO.sleep(2.seconds) >> IO.raiseError(new Exception("Error"))
)).flatMap(IO.println) // Prints "Fast"
```

**How it works:**

1. All three tasks start simultaneously.
2. Each task, upon successful completion, tries to call `deferred.complete`.
3. The first successful task sets the value — all other `complete` calls get `false` and are ignored.
4. `deferred.get` waits for the first value and returns it.
5. Remaining tasks can be cancelled (optional).

## Comparison with Other Primitives

| Primitive           | Purpose           | Key Feature                                                      |
|---------------------|-------------------|------------------------------------------------------------------|
| **`Ref[A]`**        | Atomic variable   | Can be updated infinitely, always holds an accessible value      |
| **`Deferred[A]`**   | One-shot container| Filled exactly once, allows blocking until filled                |
| **`Semaphore`**     | Counting semaphore| Allows limiting parallelism, repeatedly `acquire`/`release`     |
| **`Queue`**         | Concurrent queue  | Stores multiple values, Producer-Consumer pattern                |
| **`Mutex`**         | Mutual exclusion  | A lock that only one fiber can hold at a time                    |

## Typical Use Cases for `Deferred`

| Scenario                                                          | Why It Fits                                                                  |
|-------------------------------------------------------------------|------------------------------------------------------------------------------|
| **Waiting for resource initialization to complete**                | Need to ensure all consumers wait while one creator prepares the resource.   |
| **Implementing a race between operations**                         | The first successful operation fills the `Deferred`, others are cancelled.   |
| **Coordinating the start of multiple fibers**                     | All fibers call `get` and start simultaneously after one `complete`.         |
| **Signaling an error in a concurrent process**                    | One fiber uses `complete(error)` as a channel to notify others about an issue.|
| **One-time cache for an expensive computation**                  | The value is computed once, then stored forever.                             |

`Deferred` is a simple but incredibly powerful tool.
Combined with `Ref`, it allows building virtually any concurrent structure:
semaphores, barriers, channels, thread pools, and even software transactional memory (STM).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Deferred](https://typelevel.org/cats-effect/docs/std/deferred)
- [Baeldung: Difference Between delay, defer, and deferred in Cats Effect](https://www.baeldung.com/scala/cats-effect-delay-defer-vs-deferred)
- [Concurrency in Cats Effect 3](https://typelevel.org/blog/concurrency-in-ce3.html)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts
