# AtomicCell — Safe Cell for Exclusive Access

## What is `AtomicCell`?

`AtomicCell[F[_], A]` is a **synchronized, concurrent, atomic reference** that guarantees
that **only one fiber** can perform a _modifying_ operation at any given time.

In simple terms: this is a **room with a key**. Inside sits a value (`A`).
Only one fiber can take the key and enter the room.
Other fibers wanting to change the value line up in a queue and wait until the room becomes available.
Peeking into the room (`get`) can be done without the key and without the queue.

`AtomicCell` can be thought of as a convenient, ready-made combination of two primitives:

- **`Ref[F, A]`** — for storing the value.
- **`Mutex[F]`** — for ensuring exclusive access.

### Basic Interface

```dotty
abstract class AtomicCell[F[_], A]:
  /** Simply reads the current value. Does not block or modify state. */
  def get: F[A]

  /** Replaces the value. Blocks the fiber during replacement. */
  def set(a: A): F[Unit]

  /** Atomically modifies the state and returns an arbitrary value. Read ->
    * computation -> write happen under a single lock.
    */
  def modify[B](f: A => (A, B)): F[B]

  /** Like `modify`, but the function `f` returns an effect `F[(A, B)]`. Allows
    * performing effects (e.g., IO calls) inside the critical section.
    */
  def evalModify[B](f: A => F[(A, B)]): F[B]

  /** Updates the value using an effectful function. */
  def evalUpdate(f: A => F[A]): F[Unit]

  // ... and much more: update, evalUpdateAndGet, modifyEither, etc.
```

### `AtomicCell` vs `Ref`

This is the key comparison that you need to understand.

| Operation                   | `Ref[F, A]`                                                                       | `AtomicCell[F, A]`                                                                                       |
|:----------------------------|:----------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------|
| **`get`**                   | Instantly, without blocking.                                                      | Instantly, without blocking.                                                                             |
| **`set`**                   | Instantly.                                                                        | **Blocks** the fiber until the mutex is acquired.                                                        |
| **`update` (fast)**         | `ref.update(_ + 1)` — non-blocking CAS loop.                                      | `cell.update(_ + 1)` — lock + modification.                                                              |
| **`update` (slow)**         | `ref.update(doHeavyStuff)` — CAS loop may repeat, redoing heavy work.             | `cell.update(doHeavyStuff)` — lock, heavy work is performed **once**, other fibers wait.                 |
| **`modify` with effects**   | ❌ No. Need to combine `Ref` + `Mutex` manually.                                   | ✅ `evalModify` allows executing `IO` inside the modification.                                            |

**Main selection rule:**

- Use **`Ref`** when modification operations are **fast, non-blocking**, and can be safely retried (CAS loop).
- Use **`AtomicCell`** when the modification operation is **"expensive" or effectful** (network call, heavy computation)
  and it is undesirable to run it multiple times due to optimistic locking.

## Deep Dive: `evalModify` and Guarantees

The most powerful function of `AtomicCell` is `evalModify`. It solves a problem that `Ref` cannot solve.

### Problem with `Ref` for effectful operations

Imagine you have a `Ref[IO, Data]`, and you want to update it based on the result of an external call.

```dotty
// PROBLEM: Don't do this!
def updateData(ref: Ref[IO, Data], newId: String): IO[Unit] =
  ref.update { currentData =>
    // This WON'T work! update doesn't accept IO effects inside.
    // Assume this is a heavy computation or IO.sleep
    val newData = heavyComputation(currentData, newId)
    newData
  }
```

But what if `heavyComputation` is actually an `IO` (e.g., a database query)?
`Ref` doesn't allow performing effects inside `update`.

The only way with `Ref` is to use a race condition:

```dotty
// UNSAFE: Race condition with Ref
def updateDataBad(ref: Ref[IO, Data], newId: String): IO[Unit] =
  for
    current <- ref.get            // thread 1 reads A
    _       <- IO.sleep(1.second) // simulating work
    newData <- compute(current, newId)
    _       <- ref.set(newData)   // thread 1 writes B
  yield ()
// If another thread changed ref in the meantime, Thread 1's change will be lost!
```

This leads to a **race condition** (lost update). `AtomicCell` solves this problem elegantly.

### Solution with `AtomicCell`: `evalModify`

```dotty
def updateData(cell: AtomicCell[IO, Data], newId: String): IO[Unit] =
  cell.evalModify { currentData =>
    // Now we are INSIDE the lock. Only one fiber is here.
    for newData <- compute(currentData, newId) // Safely perform IO effect
    yield (newData, ()) // (new_state, returned_value)
  }
```

**Guarantees of `evalModify`:**

1. **Exclusivity:** The fiber that enters `evalModify` has **exclusive ownership** of access to the `AtomicCell`.
   No other fiber can perform `set`, `modify`, or `evalModify` until the current one finishes.
2. **Atomicity:** The entire block inside `evalModify` (read -> effectful computation -> write) runs as a single,
   indivisible action from the perspective of other modifying operations.
3. **No retries:** Unlike the CAS loop in `Ref`, the **result of `compute` won't be discarded**.
   It will be written guaranteed.

## Real Example: Random Data Generator with Side Effects

Consider the following code:

```dotty
import cats.effect.IO
import cats.effect.std.AtomicCell

trait Data

class RandomDataGenerator(cell: AtomicCell[IO, Data]):

  /** Generates a new random value based on the previous one. */
  def next: IO[Data] =
    cell.evalUpdateAndGet(generate) // Update and return the new value

  /** Effectful generation function, e.g., accesses a database or
    * external API.
    */
  private def generate(previous: Data): IO[Data] = ???
```

**Why is `AtomicCell` necessary here?**

Imagine `generate` does the following:

1.  Requests a new "seed" from the database.
2.  Performs complex cryptographic computations.
3.  Saves the result to cache.

If we used `Ref`, we would face the "race" or multiple executions of expensive operations problem.
`AtomicCell` guarantees that:

- Each `next` call will be executed strictly sequentially.
- Concurrent `next` calls won't interfere with each other.
- The expensive `generate` operation will be called **exactly once** per state change.

## Real-World Example: Bank Account Balance

Imagine a bank account. Debit and credit operations must be strictly sequential to avoid errors.

```dotty
import cats.effect.IO
import cats.effect.std.AtomicCell

case class Account(balance: Long)

class Bank(cell: AtomicCell[IO, Account]):

  /** Withdraw an amount, but only if the balance allows it. */
  def withdraw(amount: Long): IO[Boolean] =
    cell.evalModify { account =>
      if account.balance >= amount then
        // Simulate fraud check, call to external system...
        val fraudCheck = IO.sleep(100.millis).as(true)
        fraudCheck.map {
          case true => (Account(account.balance - amount), true)
          // Fraud: don't withdraw, don't change state either
          case false => (account, false)
        }
      else
        IO.pure((account, false)) // Insufficient funds
    }

  /** Deposit an amount. */
  def deposit(amount: Long): IO[Unit] =
    cell.evalUpdate(account => IO.pure(Account(account.balance + amount)))

  def getBalance: IO[Long] = cell.get.map(_.balance)
end Bank
```

**Key points:**

- `evalModify` in `withdraw` atomically checks the balance, **possibly waits for a response from the fraud service**, and then updates the balance.
- While waiting for the fraud service response, **other fibers cannot change the balance**. This is critical for correctness.
- Implementing such logic with `Ref` would be extremely difficult and error-prone.

## Important Characteristics and Behavior

### 1. Blocking Only for **Modifying** Operations

- `get` is **never blocking**. This is an instant, non-blocking call.
- `set`, `update` (and all `eval*` variants) **block** the fiber until the mutex is acquired.

### 2. Fairness of Locking

`AtomicCell` usually ensures a **fair** (FIFO) waiting queue.
Fibers attempting to modify state line up in order of arrival.
This prevents "starvation".

### 3. Cancellability

The lock on `AtomicCell` **is cancelable**. If a fiber waiting in the queue is canceled,
it is correctly removed from the wait queue, and the right to acquire the mutex passes to the next one.

```dotty
// Example: fiber will be removed from the queue on cancellation
val waitingFiber = cell.update(dosomething).start
waitingFiber.cancel // waiting fiber will correctly leave the queue
```

### 4. "Lazy" Evaluation of `evalModify`

Note: inside `evalModify` you **should not** perform very long or infinite operations (e.g., `IO.never`),
as this will block **all** other modifying operations forever.

## `AtomicCell` Compared to Alternatives

| Scenario                                        | Best Choice                     | Why                                                                      |
|:------------------------------------------------|:--------------------------------|:-------------------------------------------------------------------------|
| Simple counter, increment without I/O           | `Ref`                           | Non-blocking CAS, maximum performance.                                   |
| Counter with DB access on each increment        | `AtomicCell`                    | Heavy operation must be executed once in exclusive mode.                 |
| Cache updated from external source              | `AtomicCell` (with `evalUpdate`) | Guarantees cache update won't be lost.                                   |
| Just distributing immutable state               | `Ref`                           | `get` is enough, locking is not needed.                                  |
| Complex coordination with queues and signals    | `Queue`, `Deferred`, `Semaphore` | `AtomicCell` is a "one slot" primitive.                                  |

## Summary: When to Use `AtomicCell`?

✅ **Use `AtomicCell` if:**

- The state modification operation involves **effects** (I/O, external calls, `IO.sleep`).
- You want **blocking, exclusive** access for modification ("critical section").
- You need a **guarantee** that the heavy computation for the new value will execute **once**, without retry attempts in a CAS loop.
- You are porting code that previously used `synchronized` or `Lock` from Java.

❌ **Don't use `AtomicCell` if:**

- You need maximum performance for very simple, fast operations (`x = x + 1`). `Ref` will be faster.
- `get` and simple `set` without atomic modifications are sufficient.
- You need a non-exclusive, merely atomic reference.

`AtomicCell` is a powerful and safe tool that fills the gap between the fast,
but "optimistic" `Ref` and the verbose, manual `Mutex` + `Ref` management.
It is ideal for coordinating access to shared resources when modification operations are "expensive".

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Atomic Cell](https://typelevel.org/cats-effect/docs/std/atomic-cell)

[doc]: https://typelevel.org/cats-effect/docs/concepts
