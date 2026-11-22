# Concurrent — Parallel State and Synchronization

`Concurrent[F[_]]` (or `GenConcurrent[F[_], E]` with an explicit error type) is a typeclass that **extends `Spawn`**
and adds the ability to **allocate and manipulate parallel state** in a concurrent environment.

In the Cats Effect hierarchy, `Concurrent` sits between `Spawn` and `Temporal`:

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
    ↓
IO                      ← concrete implementation
```

**Key idea:** `Concurrent` provides two fundamental building blocks for concurrent programming:

1. **`Ref[F, A]`** — an [atomic, immutable reference](concurrent/ref.md) to a value of type `A`
2. **`Deferred[F, A]`** — a [one-shot container for a value](concurrent/deferred.md) that allows fibers to wait for its appearance

Using these two primitives, you can build **any concurrent data structures**: semaphores, queues, barriers, timers, locks, etc.

## Full Typeclass Signature

```dotty
trait Concurrent[F[_], E] extends Spawn[F, E]:

  /** Creates an atomic reference to an initial value. */
  def ref[A](a: A): F[Ref[F, A]]

  /** Creates an empty one-shot container.
    *
    * Used to block a fiber until another fiber
    * provides a value via `complete`.
    */
  def deferred[A]: F[Deferred[F, A]]

  /** Memoizes an effect, ensuring it runs at most once.
    *
    * The first invocation of the returned effect runs the original `fa` and caches
    * the result (or error). All subsequent invocations return the cached
    * value without re-executing.
    *
    * If multiple fibers simultaneously access the memoized effect,
    * `fa` runs only once.
    */
  def memoize[A](fa: F[A]): F[F[A]]

  /** Applies a function to elements of a structure in parallel with concurrency
    * limit.
    *
    * At most `n` tasks run at the same time. The result order in the output
    * structure matches the input element order.
    */
  def parTraverseN[T[_]: Traverse, A, B](n: Int)(ta: T[A])(f: A => F[B])
      : F[T[B]]

  /** Applies a function to elements of a structure in parallel with concurrency
    * limit, discarding results.
    *
    * Equivalent to `parTraverseN` but discards each result.
    * Efficient when only side effects matter.
    */
  def parTraverseN_[T[_]: Foldable, A, B](n: Int)(ta: T[A])(f: A => F[B])
      : F[Unit]

  /** Parallelizes execution of effects from a structure with a concurrency limit.
    *
    * Takes a structure containing effects and executes them in parallel, but not
    * more than `n` at the same time. Returns a structure of results in the same order.
    */
  def parSequenceN[T[_]: Traverse, A](n: Int)(tma: T[F[A]]): F[T[A]]

  /** Parallelizes execution of effects from a structure with a concurrency limit,
    * discarding results.
    */
  def parSequenceN_[T[_]: Foldable, A](n: Int)(tma: T[F[A]]): F[Unit]

  /** Parallelizes repeating an effect a given number of times with concurrency
    * limit.
    *
    * Runs effect `ma` `replicas` times, but no more than `n` at the same time.
    * Results are collected into a list.
    */
  def parReplicateAN[A](n: Int)(replicas: Int, ma: F[A]): F[List[A]]
```

> In real code `parTraverseN` and `parSequenceN` are usually available via syntax `list.parTraverseN(n)(f)`,
> but their implementation relies on `Concurrent`.

### `Ref` — Atomic Mutable Reference

`Ref[F, A]` is a **concurrency-safe** reference to a value of type `A` in the effect context `F`.
It provides operations for atomic reading, writing, and updating the value.
[More details in the corresponding section.](concurrent/ref.md)

### `Deferred` — One-Shot Promise

`Deferred[F, A]` is a **one-shot container** that is initially empty and can be filled exactly once. Fibers can:

- Wait for filling via `.get` (semantic blocking)
- Fill the value via `.complete(a)`

**Key property:** if multiple fibers simultaneously call `complete`, only one successfully fills the `Deferred`,
while the others receive an error or are ignored.

[More details in the corresponding section.](concurrent/deferred.md)

## `memoize` — Caching Effect Results

### What Does `memoize` Do?

```dotty
def memoize[A](fa: F[A]): F[F[A]]
```

`memoize` is a **concurrent cache** for an effect:

- The first run executes `fa` and stores the result (or error)
- All subsequent runs return the stored result without re-executing
- If multiple fibers simultaneously request the memoized effect, `fa` runs **only once**

### Detailed Example

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.unsafe.implicits.global

val action: IO[String] =
  IO.println("🔥 Heavy computation...") *>
    IO.sleep(1.second) *>
    IO.pure("result")

// Without memoize: runs every time
val withoutMemo =
  for
    r1 <- action
    r2 <- action
    r3 <- action
  yield List(r1, r2, r3)
  
// Output: "🔥 Heavy computation..." three times!
withoutMemo.unsafeRunSync()
// 🔥 Heavy computation...
// 🔥 Heavy computation...
// 🔥 Heavy computation...
// List(result, result, result)

// With memoize: runs once
val withMemo =
  for
    memoized <- action.memoize
    r1       <- memoized
    r2       <- memoized
    r3       <- memoized
  yield List(r1, r2, r3)
  
// Output: "🔥 Heavy computation..." once only!
withMemo.unsafeRunSync()
// 🔥 Heavy computation...
// List(result, result, result)
```

### Real Example: Configuration Caching

```dotty
class ConfigCache[F[_]: {Concurrent, Sync}]:
  private val configLoader: F[F[Config]] =
    loadConfigFromFile().memoize

  private def loadConfigFromFile(): F[Config] =
    Sync[F].delay {
      println("📂 Loading configuration from file...")
      // Expensive operation
      Config("localhost", 8080, "secret-key")
    }

  def getConfig: F[Config] =
    configLoader.flatMap(identity)

// Usage
val program =
  val cache = new ConfigCache[IO]

  for
    conf1 <- cache.getConfig // Loads from file
    conf2 <- cache.getConfig // Gets from cache
    conf3 <- cache.getConfig // Gets from cache
  yield ()
```

### `memoize` vs Manual Caching via `Ref`

| Aspect                        | `memoize`                     | Manual caching via `Ref`        |
|-------------------------------|-------------------------------|----------------------------------|
| **Complexity**                | Minimal                       | Need to manage state             |
| **Race on first run**         | Handled automatically         | Easy to introduce bugs           |
| **Caching errors**            | Yes (errors are also cached)  | Optional                         |
| **Cache eviction**            | None (need custom mechanism)  | Full control                     |
| **Performance**               | Optimized                     | Depends on implementation        |

## Atomic State Machine: `CountDownLatch`

Let's implement a **classic synchronization primitive** — `CountDownLatch`.

`CountDownLatch` (count-down latch) is created with an initial number `n`. Fibers can:

- **Wait** (`await`) — block until the counter reaches 0
- **Release** (`release`) — decrement the counter by 1;
- When the counter reaches 0, all waiting fibers are unblocked

### Modeling State

```dotty
sealed trait State[F[_]]
case class Awaiting[F[_]](
    latches: Int,             // How many latches remain to release
    signal: Deferred[F, Unit] // Signal to wake waiting fibers
) extends State[F]
case class Done[F[_]]() extends State[F] // Terminal state
```

Here we define an algebraic data type representing the two possible states of the latch.
`Awaiting` is the active state, storing the remaining number of latches (`latches`)
and a `Deferred` channel for notifying waiting fibers.
`Done` is the terminal state, meaning the latch is closed and all waiting fibers have already been unblocked.
This approach allows modeling the state machine of the concurrent primitive.

### `CountDownLatch` Implementation

```dotty
class CountDownLatch[F[_]: Concurrent](state: Ref[F, State[F]]):
  /** Waits until the counter reaches 0. If the latch is already closed (Done),
   * returns immediately.
   */
  def await: F[Unit] =
    state.get.flatMap:
      // Not ready yet — wait for the signal
      case Awaiting(_, signal) => signal.get
      // Already ready
      case Done() => Concurrent[F].unit

  /** Releases one latch. When the counter reaches 0, wakes all
   * waiting fibers.
   */
  def release: F[Unit] =
    Concurrent[F].uncancelable { _ =>
      state.modify {
        case Awaiting(n, signal) if n > 1 =>
          // Decrement the counter, don't send the signal yet
          (Awaiting(n - 1, signal), Concurrent[F].unit)

        case Awaiting(_, signal) =>
          // Last latch — close the latch and wake everyone
          (Done(), signal.complete(()).void)

        case d @ Done() =>
          // Already closed — do nothing
          (d, Concurrent[F].unit)
      }.flatten
    }
end CountDownLatch
```

The core logic of the primitive is implemented through atomic operations on `Ref`.

**Method `await`:** Simply reads the current state.
If the latch is in `Awaiting` state — the caller blocks on `signal.get`.
If already `Done` — returns immediately.

**Method `release`:** Wrapped in `uncancelable` to protect the critical section from cancellation.
Uses `state.modify` to atomically update the state:

- If more than 1 latch remains (`n > 1`) — decrement the counter, don't send the signal
- If this is the last latch (`n == 1`) — transition to `Done` and send the signal via `signal.complete`, waking all waiting fibers
- If already in `Done` — do nothing

`modify` returns an effect that is then folded via `.flatten`.
Thanks to `uncancelable`, transitions between states are guaranteed to be atomic even under external cancellations.

### Using `CountDownLatch`

```dotty
object LatchDemo extends IOApp.Simple:
  def run: IO[Unit] =
    for
      signal <- Concurrent[IO].deferred[Unit]
      state  <- Concurrent[IO].ref[State[IO]](Awaiting(3, signal))
      latch = new CountDownLatch[IO](state)
  
      // Launch 3 worker fibers
      _ <- (1 to 3).toList.traverse { id =>
        (
          for
            _ <- IO.sleep((id * 500).millis)
            _ <- IO.println(s"Worker $id: ready!")
            _ <- latch.release
          yield ()
        ).start
      }
  
      // Main fiber waits for all
      _ <- IO.println("Main: waiting for all workers...")
      _ <- latch.await
      _ <- IO.println("Main: all ready! Starting work!")
    yield ()
    end for
  end run
```

**Output:**

```
Main: waiting for all workers...
Worker 1: ready!
Worker 2: ready!
Worker 3: ready!
Main: all ready! Starting work!
```

Demonstration of `CountDownLatch` in a real scenario.

**Initialization:** First, a `Deferred[Unit]` for the signal is created
and a `Ref[State]` with initial state `Awaiting(3, signal)`, then a `CountDownLatch` instance.

**Starting workers:** For each of 3 worker fibers, a separate fiber is launched with a delay (`id * 500 ms`),
then a ready message is printed and `latch.release()` is called.

**Waiting:** The main fiber calls `latch.await()` and blocks until all 3 workers call `release`.

**Unblocking:** When the last (third) worker calls `release`, the counter reaches 0,
`signal.complete` wakes the main fiber, and it continues with the final message.

**Key observation:** The workers' delays (500ms, 1000ms, 1500ms) demonstrate that
the main fiber waits for all of them, regardless of the order of their readiness.
This is a classic latch use case — coordinating the start of an operation after several preconditions are met.

### Why `Ref` and `Deferred`?

`Ref` and `Deferred` are **minimal** primitives from which everything else can be built:

| Primitive       | Analogy                    | Enables                                     |
|-----------------|----------------------------|---------------------------------------------|
| **`Ref`**       | Atomic variable            | Atomic reading/updating state               |
| **`Deferred`**  | Promise / CompletableFuture| Waiting for events (semantic blocking)      |

**Built from them:**

- [`Semaphore`](../std/semaphore.md) — `Ref[(Long, Deferred[F, Unit])]`
- [`Queue`](../std/dequeue.md) — `Ref[Vector[A]]` + `Deferred[F, Unit]` for signals
- [`Mutex`](../std/mutex.md) — `Ref[Boolean]` + `Deferred[F, Unit]` for locking
- [`CyclicBarrier`](../std/cyclic-barrier.md) — `Ref` state + `Deferred` for waking

### Alternative Approach: Why Not `AtomicReference` and `Lock`?

In Java we would use `AtomicReference` and `Lock/ReentrantLock`. But in Cats Effect:

1. **`AtomicReference`** doesn't work with `F` — it's unsafe due to `null` and not integrated with cancellation
2. **`Lock`** blocks OS threads, which kills scalability
3. **`Object.wait/notify`** requires `synchronized` and blocks threads

`Ref` and `Deferred` provide a **lightweight, cancellable, resource-safe** alternative.

## Summary: What Does `Concurrent` Give?

| Capability                     | Key Primitive                                                 |
|--------------------------------|---------------------------------------------------------------|
| **Atomic state**               | `Ref` — reference with atomic updates                         |
| **Event waiting**              | `Deferred` — one-shot signal                                  |
| **Result caching**             | `memoize` — lazy single execution                             |
| **Bounded parallelism**        | `parTraverseN` — control over the number of concurrent tasks  |
| **Complex concurrent structures** | `Ref` + `Deferred` for implementing semaphores, queues, barriers |

**Key idea:** `Concurrent` provides a minimal,
but sufficient set of primitives to build **any** concurrent code in a functional style.
If you need to manage state, synchronize fibers,
or limit parallelism — `Concurrent` provides all the tools you need.

In real applications, you will use `Concurrent` constantly:

- `Ref` for counters, caches, and flags
- `Deferred` for coordination between fibers
- `Semaphore` (built on `Concurrent`) for limiting parallelism
- `memoize` for expensive operations
- `Queue` for message passing between fibers

All of this with full cancellation support, resource safety, and outstanding scalability.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Concurrent](https://typelevel.org/cats-effect/docs/typeclasses/concurrent)

[doc]: https://typelevel.org/cats-effect/docs/concepts
