# Cats Effect Thread Model

Cats Effect is a powerful tool for building high-performance, concurrent systems in Scala.
However, to use it effectively, you must understand the underlying thread model and how [fibers](foundation/fibers.md)
are scheduled on top of OS threads.

## Key Goals of the Thread Model

The key principles of multithreading in Cats Effect were carefully described in [Daniel Spiewak's gist](https://gist.github.com/djspiewak/46b543800958cf61af6efa8e072bfd5c).
Here is a summary of these principles.

### Thread Pool Architecture

The optimal configuration for a Cats Effect application on JVM:

1.  **Compute pool**
    - Size: approximately equal to the number of available CPU cores.
    - Purpose: executing **non-blocking**, compute-intensive fiber operations.
    - _Note:_ Depending on the application, you may get better performance
      by leaving one or two cores free for garbage collection or other system needs.
2.  **Blocking pool**
    - Size: unbounded, caching thread pool.
    - Purpose: executing **blocking I/O operations** (file I/O, JDBC queries, reading from sockets, etc.).
3.  **Async I/O pool**
    - Size: 1–2 high-priority threads.
    - Purpose: handling async I/O events (e.g., from `java.nio.channels`).
      These events themselves must be immediately redirected to the compute pool for further processing.

**The main goal** is to minimize the number of costly context switches and maximize the time
the compute pool spends doing useful work.

### Why Not `scala.concurrent.ExecutionContext.global`?

It's important to understand that `scala.concurrent.ExecutionContext.global` is a **poor choice** for the compute pool in Cats Effect. Reasons:

- Its architecture (`ForkJoinPool`) assumes blocking operations will run in it,
  so it may **allocate additional threads** to "mask" them, increasing overhead.
- There's no way to prevent libraries in your classpath from scheduling arbitrary code on this executor,
  making it an unreliable foundation.

## The `IO` Execution Loop: How It Works Under the Hood

To understand the thread model, you need to understand how `IO` executes. A simplified model of `IO` might look like this:

```dotty
sealed abstract class IO[A]:
  def flatMap[B](f: A => IO[B]): IO[B] = FlatMap(this, f)
  def unsafeRun(): A                   = this match
    case Pure(a)        => a
    case Suspend(thunk) => thunk()
    case FlatMap(io, f) => f(io.unsafeRun()).unsafeRun()

case class Pure[A](a: A)                           extends IO[A]
case class Suspend[A](thunk: () => A)              extends IO[A]
case class FlatMap[A, B](io: IO[B], f: B => IO[A]) extends IO[A]
```

This model omits error handling, async behavior, and stack safety, but well illustrates the key point:
`unsafeRun()` is a **CPU-intensive loop** that recursively "unrolls" nested `IO` structures.

In real Cats Effect, this loop is executed for each fiber,
which is essentially a live instance of an `IO` computation.

### Fibers

Fibers are **lightweight, logical execution streams**. They are not directly bound to OS threads:

- Fibers execute on an **m:n** basis: many fibers (`m`) can be scheduled on fewer OS threads (`n`).
- They are created via `IO#start`, `IO.race`, `IO.parTraverse`, and other methods.

**Important:** Fibers use **cooperative multitasking**.
This means a fiber must **voluntarily** yield control to the processor so that other fibers can execute.
Otherwise, one fiber can "monopolize" a CPU core.

Fortunately, fibers automatically yield at **async boundaries**,
such as waiting for a result from `Deferred.get`, `IO.sleep`, or reading from `Queue.take`.
However, if a fiber executes a **long CPU-intensive operation** without such boundaries, it can monopolize the thread:

```dotty
// Dangerous! Can block a compute thread.
def calculate(data: Vector[BigInt]): IO[BigInt] =
  IO(longSubTaskA(data))
    .map(xs => longSubTaskB(xs))
    .map(ys => longSubTaskC(ys)) // No yield points.
```

If you have such operations, explicitly add a yield point using `IO.cede`.
This gives other fibers a chance to execute on the same thread.

```dotty
// Safe: yield between stages.
def calculate(data: Vector[BigInt]): IO[BigInt] =
  IO(longSubTaskA(data)).flatMap { xs =>
    IO.cede.map { _ =>
      longSubTaskB(xs)
    }
  }.map(ys => longSubTaskC(ys))
```

## Blocking Calls: How to Handle Them Properly

A direct consequence of the model described above is that **executing blocking code in the compute pool is a disaster**.

Imagine a server on a 2-CPU node. If you execute a blocking call, for example:

```dotty
IO(Source.fromFile(path).getLines().mkString)
```

then for the duration of its execution, **one of the two cores will be completely occupied waiting**.
This immediately cuts the overall application performance in half. Two such blocking calls would almost halt processing.

**Solution:** Redirect blocking operations to a dedicated blocking pool,
and after they complete, return the result back to the compute pool.

## Semantic Blocking: Don't Confuse It with Thread Blocking

We also often need a fiber to **logically wait** for a condition to be met
(e.g., an element appearing in a queue).
But we can't use regular Java/Scala locks (`synchronized`, `Lock`, `BlockingQueue`)
for this, as they would stop a whole OS thread.

Instead, Cats Effect provides the concept of **semantic blocking**.
The fiber suspends its work and yields control,
but the **OS thread remains free** to execute other fibers.

A classic example is `IO.sleep(duration)`. **Never** use `IO(Thread.sleep(duration))` (thread blocking),
always use `IO.sleep` (semantic blocking).

The foundation for arbitrary semantic blocking is `Deferred` — a one-shot, thread-safe "promise."

```dotty
trait Deferred[F[_], A]:
  // semantically blocks until a value arrives
  def get: F[A]
  // sets a value, wakes up waiting fibers
  def complete(a: A): F[Boolean]
```

All other synchronization primitives are built on `Deferred`: `Semaphore`, `Queue`, `Mutex`, etc.

## Cats Effect 3

### Async Effects

**CE3** introduces a new `Async` type class with semantics resembling a reader monad:

```dotty
trait Async[F[_]]:
  // Execute `fa` on the given ExecutionContext, RETURN BACK
  def evalOn[A](fa: F[A], ec: ExecutionContext): F[A]

  // Get the current ExecutionContext (usually the compute pool)
  def executionContext: F[ExecutionContext]
```

Importantly, after the `evalOn` block executes, execution **automatically returns** to the pool defined by `executionContext`.
This makes code more predictable and safe.

### Blocking Operations

**CE3** provides built-in methods:

- `IO.blocking(thunk)` / `Sync[F].blocking(thunk)`: execute `thunk` in the blocking pool, wait for the result.
- `IO.interruptible(thunk)`: similar to `blocking`, but attempt to interrupt `thunk` via `Thread.interrupt()` on fiber cancellation.

```dotty
// In CE3, everything is simple
IO.println("Compute pool") >>
  IO.blocking(println("Blocking pool")) >>
  IO.println("Back to compute pool")
```

### Work-Stealing Pool

CE3 ships with its own high-performance work-stealing pool implementation.
It has key advantages:

1. **Per-core task queues:** Rather than one global queue shared by all threads,
   this pool uses a separate queue for each core. This **significantly reduces contention** for queue access,
   which is critical for systems with a large number of cores (64+).
2. **Locality:** The pool tries to reschedule a recently yielded fiber onto the same OS thread.
   This allows keeping "warm" CPU caches (L1/L2) and avoids costly cache misses on switching.
3. **Auto-yielding:** To prevent accidental CPU monopolization by one fiber,
   the pool can automatically insert `IO.cede` calls after a certain number of loop iterations in `IO`.

## Conclusion

Understanding the Cats Effect thread model is key to writing high-performance, reliable, and predictable applications.

- **Use the right pools:** compute pool for CPU work, blocking pool for I/O.
- **Avoid thread blocking:** prefer semantic blocking (`Deferred`, `IO.sleep`).
- **Mark blocking calls:** use `IO.blocking` for any operations that may block an OS thread.
- **Yield control:** for long CPU operations, insert `IO.cede` to maintain system responsiveness.
- **Trust `IOApp`:** it automatically configures optimal thread pools for you.

Following these principles, you can build systems capable of handling massive loads while remaining pure and composable.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Thread Model](https://typelevel.org/cats-effect/docs/thread-model)

[doc]: https://typelevel.org/cats-effect/docs/concepts
