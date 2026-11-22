# Fiber Join

Not all parallel operations are "fire and forget". In real applications you typically need to:

- Start several tasks in parallel.
- Wait for them to complete.
- Collect results.
- Handle successes and errors.

**Java Threads** offer the `join()` method for this, which blocks the current thread until the other finishes. 
This gives no information about _how_ the thread completed (success, error, interruption).

**Cats Effect fibers** provide a far more powerful mechanism -- `Fiber#join`, 
which returns not just a value, but an **`Outcome`** -- a structure encoding three possible completion states.

## Basic example: parallel execution of two tasks

```dotty
// Don't use this in production -- there are safer alternatives!
def both[F[_]: Spawn, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
  for
    fiberA <- fa.start
    fiberB <- fb.start
    a      <- fiberA.joinWithNever
    b      <- fiberB.joinWithNever
  yield (a, b)
```

**What happens here:**

1. We start `fa` and `fb` in separate fibers (non-blocking start).
2. `joinWithNever` waits for each fiber to complete and extracts the result.
3. If the fiber was cancelled, `joinWithNever` **hangs forever** (never) -- not suitable for all cases.

> **Why `joinWithNever`?** This is a simplified method over `join` that hides the complexity of working with `Outcome`. 
> But for real code you need to understand what happens under the hood.

### `Outcome` -- three faces of fiber completion

```dotty
sealed trait Outcome[F[_], E, A]
object Outcome:
  case class Succeeded[F[_], E, A](value: F[A]) extends Outcome[F, E, A]
  case class Errored[F[_], E, A](error: E)      extends Outcome[F, E, A]
  case class Canceled[F[_], E, A]()             extends Outcome[F, E, A]
```

**What these three options mean:**

| Option           | When it arises                     | What it contains                            | How to handle                                 |
|------------------|------------------------------------|---------------------------------------------|-----------------------------------------------|
| **`Succeeded`**  | Fiber completed successfully       | Effect `F[A]` (not `A` itself!)             | `flatMap` or `>>` to extract the value        |
| **`Errored`**    | Fiber failed with an error         | Error of type `E` (usually `Throwable`)     | `raiseError` or transformation                |
| **`Canceled`**   | Fiber was cancelled before completion | Nothing                                  | Depends on semantics -- see below             |

> **Important nuance:** `Succeeded` contains not a pure value `A`, but an **effect** `F[A]`. Why? 
> Because Cats Effect supports lazy effects -- the value itself may be computed only within the `F` context. 
> Usually you just call `flatMap` or use `>>` to "unwrap" it.

## Detailed example with handling all variants

```dotty
import cats.MonadThrow
import cats.effect.{Fiber, IO, MonadCancel, Outcome, Spawn}
import cats.syntax.all.*

def joinWithCustomLogic[F[_]: MonadThrow, A](
    fiber: Fiber[F, Throwable, A]
)(defaultValue: A)(using MonadCancel[F, Throwable]): F[A] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) =>
      // Success -- just extract the value
      fa

    case Outcome.Errored(e) =>
      // Error -- propagate further
      MonadThrow[F].raiseError(e)

    case Outcome.Canceled() =>
      // Cancellation -- use default value
      MonadCancel[F].canceled.as(defaultValue)
```

**Usage:**

```dotty
val task: IO[String] = IO.sleep(1.second) >> IO.pure("Hello")
val fiber: IO[Fiber[IO, Throwable, String]] = task.start

// Wait for result, on cancellation return "Timeout"
val result: IO[String] = fiber.flatMap(joinWithCustomLogic(_)("Timeout"))
```

## The problem: what to do with `Outcome.Canceled()`?

Cancellation is the most complex case. You have several strategies, each with its own tradeoffs.

### Strategy 1: Interpret cancellation as an error

```dotty
case Outcome.Canceled() =>
  MonadThrow[F].raiseError(new RuntimeException("Child fiber was cancelled"))
```

**When to use:** If cancelling the child fiber is an exceptional situation in your domain. 
For example, you awaited a result, and its absence breaks the contract.

**Problem:** If the current fiber is inside `uncancelable`, 
`raiseError` will still fire -- the error will propagate, but cancellation will not happen. 
This can be good or bad depending on context.

### Strategy 2: Cancel yourself on child's cancellation

```dotty
case Outcome.Canceled() =>
  MonadCancel[F].canceled  // ← returns F[Unit]
```

**Problem:** Types don't match! We need `F[A]`, but `canceled` returns `F[Unit]`. The compiler won't allow it.

**Solution -- use `>> never`:**

```dotty
case Outcome.Canceled() =>
  MonadCancel[F].canceled >> Spawn[F].never[A]
```

What this gives:

- First, try to cancel the current fiber (`canceled`).
- If cancellation is allowed -- the current fiber will stop.
- If the current fiber is protected by `uncancelable` -- `canceled` will do nothing, and we'll proceed to `never`, which hangs forever.

> This is exactly the logic implemented by `joinWithNever` from the earlier example.

### Strategy 3: Return a default value (via `Option`)

The most practical approach is to use `Option[A]`:

```dotty
def joinToOption[F[_]: MonadThrow, A](
    fiber: Fiber[F, Throwable, A]
)(using MonadCancel[F, Throwable]): F[Option[A]] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) =>
      fa.map(Some(_))

    case Outcome.Errored(e) =>
      MonadThrow[F].raiseError(e)

    case Outcome.Canceled() =>
      MonadCancel[F].canceled.as(None)
```

**Advantages:**

- Clear separation: `None` means cancellation, `Some(a)` means success.
- Type `Option[A]` honestly reflects the possibility of "no result".
- Easy to combine with `orElse`, `getOrElse`, `fold`.

**Disadvantage:** If you absolutely need `A` (e.g., for further `flatMap` without extra wrappers), you'll need `getOrElse`.

## Real-world example: parallel requests with fault tolerance

```dotty
import cats.effect.IO
import cats.syntax.all.*
import scala.concurrent.duration.*

case class User(id: String, name: String)
case class Order(id: String, amount: Double)

def fetchUser(userId: String): IO[User] = IO.sleep(1.second) >>
  IO.pure(User(userId, "Alice"))
def fetchOrders(userId: String): IO[List[Order]] = IO.sleep(2.seconds) >>
  IO.pure(List(Order("1", 100.0)))

def loadUserData(userId: String): IO[(User, List[Order])] =
  for
    fiberUser   <- fetchUser(userId).start
    fiberOrders <- fetchOrders(userId).start

    // Wait for both results, but handle cancellation via Option
    userOpt   <- joinToOption(fiberUser)
    ordersOpt <- joinToOption(fiberOrders)

    // Combine, handling possible cancellations
    result <- (userOpt, ordersOpt) match
      case (Some(user), Some(orders)) => IO.pure((user, orders))
      case (None, Some(orders))       => IO.raiseError(
          new Exception("User fetch was cancelled, but orders are ready")
        )
      case (Some(user), None) => IO.raiseError(
          new Exception("Orders fetch was cancelled, but user is ready")
        )
      case (None, None) =>
        IO.raiseError(new Exception("Both fetches were cancelled"))
  yield result
```

**What's important here:** We don't lose cancellation information -- each `None` signals that 
the corresponding fiber was cancelled before completion.

## Advanced pattern: racing via `join`

```dotty
def race[F[_]: {Spawn, MonadThrow}, A, B](
    fa: F[A],
    fb: F[B]
)(using MonadCancel[F, Throwable]): F[Either[A, B]] =
  for
    fiberA <- fa.start
    fiberB <- fb.start

    // Wait for the first to complete
    result <- fiberA.join.race(fiberB.join).flatMap {
      case Left(outcomeA) =>
        // A completed first → cancel B
        fiberB.cancel >> outcomeToResult(outcomeA).map(Left(_))

      case Right(outcomeB) =>
        // B completed first → cancel A
        fiberA.cancel >> outcomeToResult(outcomeB).map(Right(_))
    }
  yield result

def outcomeToResult[F[_]: MonadThrow, A](outcome: Outcome[F, Throwable, A])
    : F[A] =
  outcome match
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    =>
      MonadThrow[F].raiseError(new Exception("Race cancelled"))
```

**This code implements a classic "race" of two tasks:**

- Whichever finishes first is the winner.
- The losing task is immediately cancelled.
- If the winner fails with an error -- the error is propagated.

Cats Effect provides a built-in `IO.race`, but this example shows how 
low-level `join` and `Outcome` allow building complex concurrent logic.

## Semantics of `joinWithNever`: when is it suitable?

```dotty
def joinWithNever[F[_]: Spawn, A](fiber: Fiber[F, Throwable, A])(using
    MonadCancel[F, Throwable]
)
    : F[A] =
  fiber.join.flatMap:
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e)    => MonadThrow[F].raiseError(e)
    case Outcome.Canceled()    => MonadCancel[F].canceled >> Spawn[F].never[A]
```

**Behavior on child fiber cancellation:**

1. Try to cancel the current fiber.
2. If cancellation succeeds -- the current fiber stops.
3. If cancellation fails (we are inside `uncancelable`) -- hang forever (`never`).

**When this is useful:**

- You are certain you will never be in a non-cancellable context.

**When this is dangerous:**

- Inside `uncancelable` you will hang forever -- this could be equivalent to a resource leak.
- If you don't understand the consequences, it's simpler to use `joinToOption` or explicit handling.

## Summary

Decision table for `Outcome.Canceled()`

| Your need                                                    | Recommended strategy                   | Code                                                       |
|--------------------------------------------------------------|----------------------------------------|------------------------------------------------------------|
| Cancellation is an error                                     | `raiseError`                           | `MonadThrow[F].raiseError(new Exception("Cancelled"))`     |
| Cancel yourself + hang if cannot                             | `canceled >> never` (as `joinWithNever`)  | `MonadCancel[F].canceled >> Spawn[F].never[A]`           |
| Use a default value (but carefully)                          | `canceled.as(default)`                 | `MonadCancel[F].canceled.as(defaultValue)`                 |
| Clearly distinguish success/error/cancel                     | `Option` or `Either`                   | `canceled.as(None)` or `canceled.as(Left(cancelError))`    |
| Restart task on cancellation                                 | `join` + loop                          | `fiber.join.flatMap { case Canceled() => retry }`          |

1. **`Fiber#join` returns `Outcome`** -- three options: success, error, cancellation.
2. **`Outcome.Succeeded` contains not a value, but an effect** -- requires additional `flatMap`.
3. **`Outcome.Canceled()` is the most complex case** -- requires an explicit decision for each domain.
4. **`joinWithNever` is convenient but not universal** -- only suitable when "hanging" on cancellation is acceptable.
5. **Using `Option` or `Either`** -- the most pragmatic way to handle cancellation.
6. **You can build complex concurrent patterns** (races, bounded parallelism, timeouts) based on low-level `join`.

Key takeaway: Cats Effect gives you **choice**. Unlike Java threads, where `join()` hides all details, 
here you control the reaction to success, error, and cancellation. 
This is power, but also responsibility -- choose your strategy consciously for your task.

---

**References:**

- [Cats effect documentation][doc]
- [Cats effect MonadCancel](https://typelevel.org/cats-effect/docs/typeclasses/monadcancel)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts
