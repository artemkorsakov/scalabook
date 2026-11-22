# `Ref` — Atomic Reference for Concurrent Access

`Ref[F[_], A]` is a **concurrency-safe, atomic reference** to a value of type `A` in the effect context `F`.
Think of it as a functional analog of Java's `AtomicReference`, but with several key advantages:

- **Purity:** All operations are wrapped in effect `F`, preserving referential transparency.
- **Cancellability:** Works within the Cats Effect cancellation system.
- **Composability:** Easily combined with other effects in for-comprehensions.
- **Lightweight:** Uses non-blocking algorithms (CAS — Compare-And-Swap).

### Basic Interface

Here is a simplified definition of `Ref` with the main operations:

```dotty
abstract class Ref[F[_], A]:
  /** Reads the current value. */
  def get: F[A]

  /** Replaces the current value with a new one (without returning the old one). */
  def set(a: A): F[Unit]

  /** Updates the value using a function. */
  def update(f: A => A): F[Unit]

  /** Atomically changes state, returning an arbitrary value `B`. */
  def modify[B](f: A => (A, B)): F[B]

  // ... and much more (updateAndGet, getAndUpdate, getAndSet, etc.)
```

> **Important:** `Ref` must be initialized with an **initial value**. There is no empty state.

### `modify` — The Core of Atomicity

Of all the methods, `modify` is the most important. It allows you to **in one atomic step**:

1. Read the current state.
2. Compute the new state and a side result.
3. Replace the state.
4. Return the side result.

**How it works under the hood:** `modify` uses a `compare-and-set` (CAS) loop.
If another thread changes the value during execution, the operation is retried.
This guarantees that the entire sequence of actions (read → compute → write) is performed without interference.

```dotty
// Example: atomic get and reset of a counter
def getAndReset[F[_], A](ref: Ref[F, A])(initialValue: A): F[A] =
  ref.modify: currentValue =>
    (initialValue, currentValue) // (new state, return value)
```

## Main Operations with Examples

Let's look at each operation with a simple counter example.

```dotty
import cats.effect.{IO, Ref}

val program: IO[Unit] =
  for
    // Create a Ref with initial value 0
    counter <- Ref[IO].of(0)
    // `get` — Read the value
    current <- counter.get
    _       <- IO.println(s"get result: $current")
    // `set` — Full replacement of the value
    _       <- counter.set(1)
    current <- counter.get
    _       <- IO.println(s"set(1) result: $current")
    // `update` — Simple update (without returning a result)
    _       <- counter.update(_ + 1)
    current <- counter.get
    _       <- IO.println(s"update(_ + 1) result: $current")
    // `updateAndGet` — Update with returning the new value
    incrementAndGet <- counter.updateAndGet(_ + 1)
    _               <- IO.println(s"updateAndGet(_ + 1) result: $incrementAndGet")
    // `getAndUpdate` — Update with returning the old value
    getAndIncrement <- counter.getAndUpdate(_ + 1)
    _               <- IO.println(s"getAndUpdate(_ + 1) result: $getAndIncrement")
    // `modify` — Arbitrary atomic transformation
    // Increment even values by 1, leave odd values as is,
    // and return a boolean flag: was the value changed?
    current <- counter.get
    _       <- IO.println(s"Value before modify: $current")
    changeIfEven <- counter.modify { current =>
      if current % 2 == 0 then (current + 1, true)
      else (current, false)
    }
    _ <- IO.println(s"Was the number even: $changeIfEven")
  yield ()
```

**Output:**

```text
get result: 0
set(1) result: 1
update(_ + 1) result: 2
updateAndGet(_ + 1) result: 3
getAndUpdate(_ + 1) result: 3
Value before modify: 4
Was the number even: true
```

## Concurrent Operations Counter

This is the most classic scenario for `Ref`. Several parallel fibers simultaneously update one counter.

### Example Code

```dotty
import cats.effect.{IO, IOApp, Sync}
import cats.effect.kernel.Ref
import cats.syntax.all.*

class Worker[F[_]](id: Int, ref: Ref[F, Int])(implicit F: Sync[F]):

  // Safe analog of println for effects
  private def putStrLn(value: String): F[Unit] =
    F.blocking(println(value))

  def start: F[Unit] =
    for
      // 1. Read the current value before updating
      before <- ref.get
      _      <- putStrLn(show"Worker #$id >> current: $before")

      // 2. Atomically increment the counter and get the new value
      after <- ref.updateAndGet(_ + 1)
      _     <- putStrLn(show"Worker #$id >> after: $after")
    yield ()

object RefExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Shared resource for all workers
      ref <- Ref[IO].of(0)

      // Create three workers
      w1 = new Worker[IO](1, ref)
      w2 = new Worker[IO](2, ref)
      w3 = new Worker[IO](3, ref)

      // Run them in parallel
      _ <- List(w1.start, w2.start, w3.start).parSequence.void
    yield ()
```

**Possible Output:**

```
Worker #3 >> current: 0
Worker #1 >> current: 0
Worker #2 >> current: 0
Worker #1 >> after: 1
Worker #3 >> after: 2
Worker #2 >> after: 3
```

1. **Race beginning:** All three workers simultaneously read `get` and saw **0**.
    This is called a *race condition*, but it's safe here because we are only reading.
2. **Atomic update:** The `updateAndGet` operation is atomic.
    Even though all three workers started from `0`, they will sequentially (within the CAS loop) call `_ + 1` and write the result.
3. **Unpredictable order:** The execution order is determined by the scheduler.
    The `after` values can be `1`, `2`, `3` (or any permutation of them),
    but the **total result is always 3**, since each worker incremented the counter exactly once.

> **Key takeaway:** `Ref` guarantees **atomicity** of each individual operation (`get`, `updateAndGet`),
> but does not guarantee atomicity of the `get` -> `updateAndGet` sequence. For that, use `modify`.

## Important Limitation: Don't Store Mutable Data!

`Ref` uses reference equality (`==`) inside the CAS logic. This only works with **immutable (immutable)** data.

```dotty
// ❌ BAD: Storing a mutable list
Ref[IO].of(mutable.ListBuffer(1, 2, 3)).flatMap: badRef =>
  badRef.update(_ += 4) // Won't work as expected!
  
// ✅ GOOD: Storing an immutable list
Ref[IO].of(List(1, 2, 3)).flatMap: goodRef =>
  goodRef.update(0 :: _) // Works correctly
```

If you modify the contents of an object *by reference* (e.g., adding an element to `ArrayBuffer`),
`Ref` won't know about it, and atomicity will be broken.
**Always use immutable data structures** (case classes, List, Vector, Map, Set).

## `Ref` vs `AtomicReference`

| Characteristic           | `java.util.concurrent.atomic.AtomicReference`  | `cats.effect.kernel.Ref`             |
|--------------------------|------------------------------------------------|--------------------------------------|
| **Environment**          | JVM (platform-dependent)                       | JVM, JS, Native (platform-independent)|
| **Computation Model**    | Direct method calls                            | Effects in `F` (e.g., `IO`)          |
| **Cancellability**       | No                                             | Yes (integration with `MonadCancel`) |
| **Blocking**             | Non-blocking (CAS)                             | Non-blocking (CAS)                   |
| **Referential Transparency** | No                                         | Yes (via effects)                    |
| **Composition of Operations** | Requires manual CAS loop                 | `modify` gives clean composition     |

## Summary: When to Use `Ref`?

✅ **Use `Ref` if you need to:**

- Atomic read/write of a single value from multiple fibers.
- Simple state accumulation (counters, flags, aggregators).
- Caching immutable data.
- Managing finite state machine (FSM) state.

❌ **Don't use `Ref` if you need to:**

- Block fibers until a condition is met (use `Deferred`).
- Coordinate a complex message queue (use `Queue`).
- Limit parallelism (use `Semaphore`).

`Ref` is a fundamental building block.
Together with `Deferred`, it forms the basis of all concurrent structures in Cats Effect (`Semaphore`, `Queue`, `Mutex`, and others).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Ref](https://typelevel.org/cats-effect/docs/std/ref)
- [Concurrency in Cats Effect 3](https://typelevel.org/blog/concurrency-in-ce3.html)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Synchronisation and Concurrent Access in Cats Effect 3 Part 7](https://yadukrishnan.live/synchronisation-and-concurrent-access-in-cats-effect-3-part-7)

[doc]: https://typelevel.org/cats-effect/docs/concepts
