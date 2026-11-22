# Async — Asynchronous FFI

`Async[F[_]]` is a typeclass that extends `Temporal[F[_]]`
and provides [an asynchronous Foreign Function Interface (FFI - Foreign Function Interface)](https://en.wikipedia.org/wiki/Foreign_function_interface).
It allows you to "semantically suspend" effects
whose results are computed **elsewhere** — for example, in a callback from a Java library, in another thread, or in a `Future`.

In simple terms: `Async` is a bridge between the **asynchronous callback world** and the **functional world of Cats Effect**.

### Why is this needed?

Imagine you are using a library that works like this:

```java
// Java HTTP request library
HttpClient.get("https://api.example.com", new Callback() {
    void onSuccess(String response) { /* ... */ }
    void onFailure(Throwable error) { /* ... */ }
});
```

`Async` lets you **wrap** such a callback into a pure `IO[A]`,
without blocking threads and without sinking into ["callback hell"](http://callbackhell.com/).

## Problem: Callback Hell

Callbacks produce deep nesting and complex error handling:

```dotty
// Typical callback hell
getUser( userId, (user) => {
  getOrders( user.id, (orders) => {
    getPayments( user.id, (payments) => {
      // processing
    }, onError)
  }, onError)
}, onError)
```

With `Async` we turn this into a readable `for`-comprehension:

```dotty
for
  user     <- getUser(userId)
  orders   <- getOrders(user.id)
  payments <- getPayments(user.id)
yield (user, orders, payments)
```

## Basic Async Methods

### 1. `async_` — Simplest FFI (now deprecated, but useful for understanding)

```dotty
def async_[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
```

This is the **simplest** way to create `F[A]` from a callback function.

- You receive a callback `cb: Either[Throwable, A] => Unit`.
- You call `cb(Right(a))` on success or `cb(Left(error))` on error.
- Cats Effect takes care of waiting for this callback.

**Important limitation:** Inside `k` you **cannot perform** `F` effects (such as `F.delay`),
and you **cannot set a finalizer for cancellation**.

#### Example: Wrapping `onComplete` from `Future`

```dotty
import scala.concurrent.{Future, ExecutionContext}
import cats.effect.Async

def fromFuture[F[_]: Async, A](fut: Future[A])(using
    ec: ExecutionContext
): F[A] =
  Async[F].async_[A] { cb =>
    fut.onComplete:
      case Success(a) => cb(Right(a))
      case Failure(e) => cb(Left(e))
  }
```

### 2. `async` — Full FFI (with effects and finalization)

```dotty
def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A]
```

This is an **extended version** that solves the limitations of `async_`:

- **Effects:** You can perform `F` inside `k` (for example, logging, changing `Ref`).
- **Finalization:** You can return `Some(finalizer)`, which will be called **on fiber cancellation**.

**How it works:**

1. You receive a callback `cb: Either[Throwable, A] => Unit`.
2. You **register** the asynchronous operation inside an `F` effect.
3. You save `cb` to call it later.
4. You return `Some(F[Unit])` — an action that would cancel the operation (if possible).
5. If no cancellation is needed, return `None`.

#### Example: from the real world — `fromCompletableFuture`

```dotty
import java.util.concurrent.CompletableFuture
import cats.effect.Async

def fromCompletableFuture[F[_]: Async, A](fut: F[CompletableFuture[A]]): F[A] =
  Async[F].flatMap(fut): cf =>
    Async[F].async[A]: cb =>
      Async[F].delay {
        // Register the result handler
        val handler: (A, Throwable) => Unit = (result, error) =>
          if error != null then cb(Left(error))
          else cb(Right(result))

        val stage = cf.handle[Unit] { (result, error) =>
          handler(result, error)
        }

        // Finalizer: cancel the CompletableFuture if the fiber is cancelled
        Some(Async[F].delay(stage.cancel(false)))
      }
```

## Thread Pool Manipulation: `executionContext` and `evalOn`

`Async` gives you control over **where your effect runs**.

### `executionContext: F[ExecutionContext]`

Returns the current `ExecutionContext` used by the Cats Effect scheduler.

```dotty
def printThread[F[_]: Async]: F[Unit] =
  Async[F].executionContext.flatMap: ec =>
    Async[F].delay(println(s"Running in: ${Thread.currentThread().getName}"))
```

### `evalOn[A](fa: F[A], ec: ExecutionContext): F[A]`

Executes the effect `fa` **on the specified** `ExecutionContext`, then returns to the original one.

```dotty
val ec1         = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
val mainProgram =
  for
    _ <- printThread                        // io-compute-1
    _ <- Async[IO].evalOn(printThread, ec1) // pool-1-thread-1
    _ <- printThread                        // io-compute-1 (back to original)
  yield ()
```

**Where is this useful?**

- You transfer **CPU-intensive** operations to a dedicated thread pool.
- You integrate with **blocking** APIs (e.g., JDBC), moving them to a separate pool.

#### Example: Isolating blocking code

```dotty
import scala.concurrent.ExecutionContext

val blockingEc = ExecutionContext.fromExecutor(
  Executors.newCachedThreadPool()
)

def readFileBlocking[F[_]: Async](path: String): F[String] =
  Async[F].evalOn(
    Async[F].delay(/* ...long file reading... */),
    blockingEc
  )
```

## Full Example: Integration with a Callback-Based Library

Imagine we have a simple Redis library (callback style):

```dotty
// Hypothetical Java library
trait RedisClient:
  def get(
      key: String,
      cb: (Either[Throwable, String]) => Unit
  ): Unit

  def set(
      key: String,
      value: String,
      cb: (Either[Throwable, Unit]) => Unit
  ): Unit
```

We want to wrap it into a type-safe `IO` interface using `Async`:

```dotty
class Redis[F[_]: Async](client: RedisClient):
  def get(key: String): F[String] =
    Async[F].async[String] { cb =>
      Async[F].delay {
        client.get(key, result => cb(result))
        None // No cancellation in this library, so no finalizer is needed
      }
    }

  def set(key: String, value: String): F[Unit] =
    Async[F].async[Unit] { cb =>
      Async[F].delay {
        client.set(key, value, result => cb(result))
        None
      }
    }
end Redis

object Redis:
  def create[F[_]: Async]: Resource[F, Redis[F]] =
    Resource.make(
      Async[F].delay(new RedisClientImpl()) // create the client
    )(client => Async[F].delay(client.close()))
      .map(new Redis(_))
```

Now you can write beautiful, cancellable code:

```dotty
val program =
  Redis.create[IO].use: redis =>
    for
      _     <- redis.set("key", "value")
      value <- redis.get("key")
      _     <- IO.println(s"Received: $value")
    yield ()
```

## Conclusion

`Async` is a bridge between the world of functional effects and callback-based asynchronous operations. It gives you:

1. **Pure FFI** — turns callbacks into `F[A]`.
2. **Cancellation support** — finalizers are automatically called on fiber cancellation.
3. **Thread control** — `evalOn` and `executionContext`.
4. **Correctness** — complex internal mechanics (`Cont`) ensure law-abiding behavior even for monad transformers.

 thanks to `Async`, Cats Effect can integrate
with any async API — from `java.nio.channels` to `js.fetch` — providing a uniformly convenient and safe interface everywhere.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Async](https://typelevel.org/cats-effect/docs/typeclasses/async)
- [Common Operators in Cats Effect 3 Part-8](https://yadukrishnan.live/common-operators-in-cats-effect-3-part-8)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts
