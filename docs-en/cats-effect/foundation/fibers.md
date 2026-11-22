# Fibers

## Introduction

Cats Effect is a library for effectful functional programming in Scala 
that introduces several powerful concepts for working with asynchrony, concurrency, and side effects. 
These concepts, being simple and elegant individually, may seem unusual for developers 
accustomed to traditional approaches (Java threads, `Future`, `Promise`, etc.). 
Moreover, Cats Effect uses its own terminology, 
which may differ from terms used in other frameworks.

The goal of this documentation is to provide a high-level understanding of the Cats Effect runtime, 
introduce key concepts and terminology that will be used in further discussions.

## Fibers -- the heart of Cats Effect

### What are fibers?

**Fibers** are Cats Effect's fundamental abstraction. 
The term was chosen intentionally to evoke **threads**, 
since fibers are essentially **lightweight threads** (also called "green threads" or "coroutines").

Like regular OS threads (Java `Thread`), fibers represent a sequence of actions 
that execute in a given order. However, there are critical differences between them:

| Characteristic | OS Threads                       | Cats Effect Fibers                          |
|----------------|----------------------------------|---------------------------------------------|
| Weight         | Heavy (~1 MB per thread)         | Ultra-lightweight (~150 bytes per fiber)    |
| Creation       | Expensive, limited by OS         | Cheap, millions possible                    |
| Switching      | Through OS scheduler             | Within JVM, faster                          |
| Asynchrony     | Blocking calls                   | Non-blocking, callback-based                |
| Cancellation   | Via `interrupt` (unsafe)         | Coordinated, safe, with guarantees          |

### Why is "lightweight" important?

Due to their lightweight nature, you can create **tens of millions of fibers** in a single process, 
and the main limiting factor will only be available memory. 
This opens up new architectural possibilities:

- **Each request is a fiber**: A typical Cats Effect client-server application creates a new fiber 
  for each incoming request, similar to how a classical Java server on `ServerSocket` creates a thread per request. 
  But unlike threads, fiber creation is so cheap 
  that you can create even **very short-lived** fibers for micro-tasks.
- **Fibers without explicit parallelism**: Many Cats Effect functions (e.g., memoization) use fibers internally, 
  even if it doesn't look like concurrent code from the outside.

### First-class asynchrony

Cats Effect fibers have **first-class support for asynchronous callbacks**. 
What does this mean in practice?

In traditional programming there are two types of operations:

- **Synchronous**: execute immediately, return a result or an error.
- **Asynchronous**: register a callback that will be called later (e.g., network read, timer).

In Cats Effect there is **no fundamental difference** between these two types from a composition perspective. 
Both synchronous and asynchronous steps are simply "fiber steps". 
You can write business logic that passes through async calls just as naturally as synchronous ones.

> **Key idea**: In the world of Cats Effect fibers, **there is no difference between a callback and a return**. 
> Everything is just a sequence of effects.

### How fibers are defined

Each fiber step is an **effect** (a value of type `IO[_]`). 
Steps are composed sequentially using the `flatMap` method (or syntactic sugar of for-comprehension):

```dotty
import cats.effect.*

// Explicit flatMap
IO.println("Hello").flatMap: _ =>
  IO.println("World")

// for-comprehension (syntactic sugar)
for
  _ <- IO.println("Hello")
  _ <- IO.println("World")
yield ()
```

Note: `flatMap` is an ordinary method, just like `map`, `filter`, etc. 
This allows us to create convenient operators for common patterns. 
For example, the `*>` operator (or `>>`) ignores the result of the first effect and returns the second:

```dotty
IO.println("Hello") *> IO.println("World")
IO.println("Hello") >> IO.println("World")  // synonym
```

### Main fiber (Main Fiber)

Every Cats Effect application has a **main fiber** -- the entry point from which execution begins. 
This is an analogue of the `main` method in a regular Scala/Java program. 
The main fiber is defined in an object extending `IOApp`:

```dotty
object Hi extends IOApp.Simple:
  val run: IO[Unit] = IO.println("Hello") *> IO.println("World")
```

When the application starts, the main fiber is created, which executes the `run` effect. 
After its completion, the process ends.

### Parent and child fibers

When one fiber starts another (via `.start`), the first is called the **parent**, the second the **child**. 
Important properties of these relationships:

- **Not a strict hierarchy**: The parent can complete before the child -- this is fine.
- **Error tracking**: The parent can track the child's errors via `.join` and handle them (like `try/catch`).
- **Cancellation**: The parent can cancel the child fiber via `.cancel`.
- **Cannot recover from cancellation**: A fiber that has been cancelled 
  cannot resume execution -- only run finalizers (release resources) and terminate.

## Cancellation -- controlled interruption

### Why is cancellation needed?

By default **any fiber's execution can be cancelled at any time**. This allows:

- **Timeouts**: Interrupt long operations.
- **Races**: Once one race participant wins, the others are cancelled.
- **Errors in parallel computations**: If one of the parallel computations fails, the others are cancelled.
- **Server shutdown**: During graceful shutdown, all processed requests receive a cancellation signal.

### Example: timeout

```dotty
import scala.concurrent.duration.*

lazy val loop: IO[Unit] = IO.println("Hello, World!") >> loop

loop.timeout(5.seconds)  // => IO[Unit]
```

What happens here:

1. A fiber is created that infinitely prints "Hello, World!".
2. The `timeout(5.seconds)` method starts this fiber and simultaneously starts a timer for 5 seconds.
3. After 5 seconds, the timer calls `.cancel` on the `loop` fiber.
4. The `loop` fiber receives the cancellation signal, completes, and releases resources.
5. The outer `IO` completes (with `Unit` result or an error if the timeout was critical).

### Cancellation vs Thread.interrupt

If you are familiar with Java threads, you might draw a parallel with `Thread.interrupt()`. 
But there are **three important differences** that make cancellation in Cats Effect safer and more reliable:

| Aspect              | Java Thread.interrupt                              | Cats Effect Cancellation                             |
|---------------------|----------------------------------------------------|------------------------------------------------------|
| **Consistency**     | Immediate return, even if thread not interrupted   | Async wait until cancellation becomes possible       |
| **Suppression**     | Impossible (can check flag, but not disabled)      | Possible via `IO.uncancelable` for critical sections |
| **Granularity**     | Checked only in blocking calls                     | Checked **between every `flatMap` operator**         |

Let's examine each difference in more detail.

#### 1. Consistent cancellation

When one thread calls `.cancel` on another, it is a **request**, not an order. 
If the target fiber cannot be cancelled at the moment (e.g., is in a critical section), 
the cancelling thread will **asynchronously wait** until cancellation becomes possible. 
Once cancellation begins, the target fiber runs all its finalizers (releases files, connections, etc.) 
and only then passes control back.

In Java `interrupt()` returns control immediately, even if the target thread ignores interrupts. 
This leads to race conditions and resource leaks.

#### 2. Suppressing cancellation via uncancelable

There are situations where several actions must execute **atomically** -- all or nothing. 
For example, allocating a resource and registering it in a pool. 
If cancellation happens "in the middle", the resource may leak.

Cats Effect provides `IO.uncancelable { poll => ... }` -- a region 
where the cancellation signal is **masked** (deferred) until exit from the block:

```dotty
import cats.effect.IO

val criticalSection: IO[Resource] = IO.uncancelable { poll =>
  for
    resource <- allocateResource()
    // Cancellation is allowed here if needed
    _ <- poll(registerInPool(resource))
  yield resource
}
```

- Inside `uncancelable` the cancellation signal is **not lost**, but deferred.
- If a cancellation point is needed inside (e.g., a long operation), it can be wrapped in `poll(...)`.
- This is significantly safer than `synchronized` or atomic flags in Java.

#### 3. High granularity of cancellation

In Cats Effect, **every fiber step** (every `flatMap` operator) contains a **cancellation check**. 
This means even an infinite loop without blocking can be cancelled:

```dotty
// Infinite loop without blocking
lazy val busyLoop: IO[Unit] = IO.unit >> busyLoop

// But it can still be cancelled!
busyLoop.timeout(1.second)
```

In Java, `Thread.interrupt()` is not checked in regular computations -- only in methods that throw `InterruptedException`. 
An infinite `while(true) {}` loop cannot be interrupted from outside.

Thanks to this granularity, cancellation in Cats Effect works **predictably and reliably** even in the most complex scenarios.

## Practical examples and patterns

### Example 1: Starting a child fiber

```dotty
import cats.effect.*
import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  val run: IO[Unit] =
    for
      // Start a child fiber that "ticks" every second
      ticker <- (IO.sleep(1.second) *> IO.println("Tick")).foreverM.start

      // Wait 5 seconds
      _ <- IO.sleep(5.seconds)

      // Cancel the ticker
      _ <- ticker.cancel

      _ <- IO.println("Ticker stopped")
    yield ()
```

Output:

```
Tick
Tick
Tick
Tick
Ticker stopped
```

### Example 2: Race (race) with automatic cancellation

```dotty
import cats.effect.*
import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  val run: IO[Unit] =
    val task1 = IO.sleep(2.seconds) *> IO.println("Task 1 done")
    val task2 = IO.sleep(1.seconds) *> IO.println("Task 2 done")

    IO.race(task1, task2).flatMap:
      case Left(value)  => IO.println("Task 1 won")
      case Right(value) => IO.println("Task 2 won")
      
// Output: "Task 2 done", then Task 1 is cancelled, and "Task 2 won" is printed
```

### Example 3: Atomic operation with uncancelable

```dotty
import cats.effect.{IO, Ref}

def transferMoney(
    from: Ref[IO, Int],
    to: Ref[IO, Int],
    amount: Int
): IO[Unit] =
  IO.uncancelable: _ =>
    for
      _ <- from.modify { balance =>
        if balance < amount then throw new Exception("Insufficient funds")
        else (balance - amount, ())
      }
      _ <- to.update(_ + amount)
    yield ()
```

In this example, the balance check and account updates are protected from cancellation.

## Terminology glossary

| Term           | Description                                                                      |
|----------------|----------------------------------------------------------------------------------|
| **Fiber**      | Lightweight thread (~150 bytes), primary unit of execution                       |
| **Effect**     | Description of an effectful computation (type `IO[_]`)                           |
| **Cancellation**  | Mechanism for safe interruption of a fiber with resource release              |
| **Uncancelable** | Region where cancellation is deferred until exit                                |
| **Poll**         | Function inside `uncancelable` allowing cancellation at the call point            |
| **Main Fiber** | Main fiber, application entry point                                              |
| **Parent/Child** | Relationships between fibers when started via `.start`                          |
| **Finalizer**    | Action executed on fiber cancellation or completion (e.g., resource release)      |

## Summary

1. **Fibers** are ultra-lightweight threads (~150 bytes) enabling millions of concurrent tasks.
2. **Asynchrony** is a first-class concept; no difference between synchronous and asynchronous steps.
3. **Composition** -- via `flatMap`, for-comprehension, and operators (`*>` etc.).
4. **Cancellation** -- coordinated, with guarantees, suppressible via `uncancelable`.
5. **Safety** -- cancellation check at every step, automatic finalizer execution.
6. **Hierarchy** -- parent and child fibers, with tracking and cancellation capability.

These concepts make Cats Effect one of the most powerful and safe platforms 
for concurrent and asynchronous programming on the JVM.


## Exercises

### Exercise 1.1.1: Timeout with fallback

> [Repository with exercises and instructions][sample]

Write a function:

```dotty
def withTimeout[A](io: IO[A], fallback: A, d: FiniteDuration): IO[A]
```

Requirements:

- If `io` fits within `d` -- return its result.
- If not -- cancel `io` and return `fallback`.
- If `io` fails with an error -- propagate the error.

@:note(🔍 Show solution)

The `withTimeout` function implements a timeout via `IO.race`, launching two fibers -- the original effect `io` 
and the timer `IO.sleep(d)` -- and returning the result of whichever completes first: 
if `io` succeeds in time, `race` returns `Left(a)` and value `a` is passed through, 
and the timer fiber is automatically cancelled; 
if the timer fires first, `race` returns `Right(())` and the function substitutes `fallback`, 
while the `io` fiber is cancelled with all its finalizers executed. 
Errors inside `io` are not caught and are propagated further, as required.

```dotty
def withTimeout[A](io: IO[A], fallback: A, d: FiniteDuration): IO[A] =
  IO.race(io, IO.sleep(d)).map:
    case Left(a)  => a
    case Right(_) => fallback
```

@:@

---

**References:**

- [Link to repository with exercises][sample]
- [Threads & Fibers](https://habr.com/ru/articles/1006576/)
- [Cats effect documentation][doc]
- [Cats effect Concepts](https://typelevel.org/cats-effect/docs/concepts)
- [Concurrent Execution in Cats Effect using Fibers Part 4](https://yadukrishnan.live/concurrent-execution-in-cats-effect-using-fibers)

[doc]: https://typelevel.org/cats-effect/docs/concepts
[sample]: https://gitverse.ru/scalabook/cats-effect
