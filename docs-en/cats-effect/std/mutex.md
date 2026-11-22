# Mutex — Mutual Exclusion for Fibers

`Mutex` (Mutual Exclusion) is a synchronization primitive that guarantees
that access to a critical section of code can be obtained by **only one fiber** at a time.
Other fibers line up in a queue and wait until the current owner releases the lock.

In essence, `Mutex` is a `Semaphore` **with one permit** (Semaphore(1)), but with a more convenient and safer API.

### Basic Interface

```dotty
trait Mutex[F[_]]:
  /** Acquires the lock and returns a Resource that will automatically
    * release it upon completion.
    */
  def lock: Resource[F, Unit]
```

**Key feature:** `lock` returns not just `F[Unit]`, but `Resource[F, Unit]`. This means:

1. **Acquiring the lock** happens when the resource is created (inside `Resource.make`).
2. **Releasing the lock** happens automatically when the `use` block ends — no need to call `unlock` manually.
3. Even if an error or cancellation occurs, the lock is **guaranteed to be released**.

### Simplest Example

```dotty
import cats.effect.IO
import cats.effect.std.Mutex

val criticalSection: IO[Int] = IO.println("Working...") *> IO.pure(42)

val program: IO[Unit] =
  for
    mutex  <- Mutex[IO]
    // acquire -> work -> release
    result <- mutex.lock.surround(criticalSection)
    _      <- IO.println(s"Result: $result")
  yield ()
```

## Mutex vs Semaphore(1)

| Characteristic                    | `Semaphore(1)`                              | `Mutex`                                         |
|:----------------------------------|:--------------------------------------------|:------------------------------------------------|
| **Number of permits**             | 1 (but can be more)                         | 1                                               |
| **Return type for acquiring**     | `F[Unit]` (via `acquire`)                   | `Resource[F, Unit]`                             |
| **Release**                       | Manual (`release`)                          | Automatic via `Resource`                        |
| **Reentrancy**                    | No (deadlock risk)                          | No (deadlock risk)                              |
| **When to use**                   | When you need multiple permits, a counter   | When you need simple "one fiber at a time"      |

**Conclusion:** For simple mutual exclusion, `Mutex` is preferable due to built-in safety via `Resource`.

## Detailed Breakdown: `lock` as `Resource`

The `lock` method returns `Resource[F, Unit]`. This allows using it in two main ways:

### 1. `lock.surround(fa)` — The Shortest Way

```dotty
mutex.lock.surround {
  // critical section
  IO.println("Only one fiber is here!")
}
```

`surround` is syntactic sugar for `lock.use(_ => fa)`. It acquires the lock, executes `fa`, and automatically releases.

### 2. `lock.use(_ => fa)` — The Explicit Way

```dotty
mutex.lock.use { _ =>
  // critical section
  doSensitiveWork()
}
```

### 3. `Resource` in for-comprehension — For Composition with Other Resources

```dotty
val guardedResource: Resource[IO, Connection] =
  for
    mutex <- Resource.eval(Mutex[IO])
    conn  <- Resource.make(createConnection())(_.close)
    _     <- mutex.lock // lock will be held for the entire lifetime of the resource
  yield (mutex, conn)
```

## Example: Safe State Update

In this example, `Mutex` protects a `Ref` from a race condition during an update that requires multiple steps.

```dotty
import cats.effect.{IO, Ref}
import cats.effect.std.Mutex

trait State

class Service(mutex: Mutex[IO], state: Ref[IO, State]):
  def modify(f: State => IO[State]): IO[Unit] =
    mutex.lock.surround {
      for
        current <- state.get  // 1. Read current state
        next    <- f(current) // 2. Compute new (possibly with IO effect!)
        _ <- state.set(next)  // 3. Write new
      yield ()
    }
```

**Why `Mutex` here, not just `Ref.update`?**

| Method           | What it does                                          | Problem                                                                                  |
|:-----------------|:-------------------------------------------------------|:-----------------------------------------------------------------------------------------|
| `Ref.update`     | Read → apply function → CAS loop                       | Function must be **pure** (no IO) and fast.                                              |
| `Ref.modify`     | Same CAS loop                                          | Function must also be pure. CAS may repeat, calling `f` multiple times.                  |
| **Mutex + Ref**  | Lock → read → IO function → write → unlock             | Function `f` can contain **IO effects** (DB queries, API calls) and will execute exactly once. |

**Key takeaway:** If your update requires **effectful** operations (IO), `Mutex` is the only safe way.

## Lock Does Not Support Reentrancy

A **reentrant** lock allows the same fiber to acquire the lock multiple times without blocking. `Mutex` **does not support reentrancy**.

```dotty
// ❌ DEADLOCK
mutex.lock.surround {
  mutex.lock.surround {
    IO.println("This will never execute")
  }
}
```

**What happens:**

1. The fiber acquires the lock (first `lock`).
2. Inside the critical section, it tries to acquire the **same** lock again.
3. Since the lock is already held by this same fiber, and `Mutex` doesn't support reentrancy,
   the fiber **blocks forever**, waiting for itself.
4. The program deadlocks.

**How to avoid:** Design code so that there are no nested `lock` calls from the same fiber.
If such a situation is possible, consider using a `Ref` for a "already inside lock" flag or rethinking the architecture.

## Real Example 1: Atomic Update with External Call

Imagine a bank account: debiting funds should check the balance,
possibly call an antifraud service, and only then change the state.

```dotty
import cats.effect.IO
import cats.effect.std.Mutex

class BankAccount(
    balanceRef: Ref[IO, Long],
    antifraud: AntifraudService,
    mutex: Mutex[IO]
):

  def withdraw(amount: Long): IO[Boolean] =
    mutex.lock.surround {
      for
        current <- balanceRef.get
        // This can be complex logic with IO calls
        isSafe <- antifraud.checkWithdrawal(current, amount)
        result <- if current >= amount && isSafe then
          balanceRef.update(_ - amount).as(true)
        else
          IO.pure(false)
      yield result
    }
```

**Without `Mutex`**, two fibers could simultaneously read the same `current`,
both check `>= amount`, both pass the check, and the balance would become negative.
`Mutex` eliminates such a race.

## Real Example 2: Cache with Single-Load

```dotty
import cats.effect.{IO, Ref}
import cats.effect.std.Mutex

class Cache[K, V](mutex: Mutex[IO], storage: Ref[IO, Map[K, V]]):
  def getOrLoad(key: K, load: K => IO[V]): IO[V] =
    mutex.lock.surround {
      storage.get.flatMap: map =>
        map.get(key) match
          case Some(value) => IO.pure(value)
          case None        =>
            load(key).flatMap: value =>
              storage.update(_ + (key -> value)).as(value)
    }
```

**Why is `Mutex` needed here?** Without it, two fibers could simultaneously notice the missing value
and start loading it twice, which is expensive and incorrect.
`Mutex` guarantees that loading will happen only once, while others wait.

## Mutex and Fiber Cancellation

`Mutex` works correctly with cancellation:

- **Waiting fiber** (in the queue for `lock`) can be cancelled.
  It will be removed from the queue, and the lock will pass to the next one.
- **A fiber holding the lock**, on cancellation, will automatically release the lock,
  but only within `Resource` — that is, after finalizers run.

```dotty
mutex.lock.surround {
  IO.never // infinite operation
}.start.flatMap { fiber =>
  IO.sleep(1.second) >> fiber.cancel
} // lock is guaranteed to be released after cancellation
```

## Mutex Compared to Alternatives

| Scenario                                                    | Best Choice                | Why                                                    |
|:------------------------------------------------------------|:---------------------------|:-------------------------------------------------------|
| Need to protect a critical section **with effects** (IO)    | **`Mutex`**                | `Ref.update` is not suitable due to CAS loops.        |
| Need to protect a **pure** function, fast update             | `Ref.update` / `Ref.modify` | No locking, maximum performance.                      |
| Need multiple permits (connection pool)                     | `Semaphore(n)`             | `Mutex` is only for one.                              |
| Need to coordinate multiple fibers with condition waiting   | `Deferred`, `Queue`        | `Mutex` is only for mutual exclusion.                 |
| Need to atomically read and modify several `Ref`s           | `Mutex` + composition      | Won't work without locking.                           |

## Errors and Best Practices

### ❌ Forgetting to Use `surround` or `use`

```dotty
val lockResource = mutex.lock
// Nothing acquired and not released!
lockResource
```

**Correct:** always use `lock.use` or `lock.surround`.

### ❌ Acquiring Lock Outside `Resource` (via `allocated`)

```dotty
val (_, release) = mutex.lock.allocated.unsafeRunSync()
// ... lots of code where you might forget release ...
release.unsafeRunSync()
```

This is an anti-pattern. Use `Resource` for automatic management.

## Summary: When to Use Mutex?

✅ **Use `Mutex` if:**

- You need a **simple lock** "one fiber at a time".
- The critical section includes **effectful operations** (`IO`, DB calls, external API calls).
- You need a guarantee of lock release even on error or cancellation (via `Resource`).
- You can't use `Ref.update`/`modify` because the update function in these methods is **pure**.

❌ **Don't use `Mutex` if:**

- You need multiple permits — use `Semaphore(n)`.
- The update function is **pure and fast** — `Ref.update` will be faster and locking-free.
- You need reentrancy — `Mutex` doesn't support it.
- You need locking between fibers and external threads — `Mutex` only works with Cats Effect fibers.

`Mutex` is a **simple, safe, and efficient** tool for protecting critical sections in concurrent functional code.
Paired with `Resource`, it provides an idiomatic way to manage locks that is resilient to errors and cancellation.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Mutex](https://typelevel.org/cats-effect/docs/std/mutex)

[doc]: https://typelevel.org/cats-effect/docs/concepts
