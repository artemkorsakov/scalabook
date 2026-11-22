# Effects

## Introduction

The notion of an **effect** is central to Cats Effect and to the entire ecosystem of effectful functional programming. 
Understanding what an effect is, how it differs from a side effect, and how to work with effects properly -- 
this is the key to writing correct, compositional, and predictable code.

## What is an effect?

### Definition

An **effect** is a **description of an action (or a sequence of actions)** that will be performed upon evaluation. 
The most important property of an effect: it **does not perform the action immediately**, but merely describes it.

The most common type of effect in Cats Effect is `IO`:

```dotty
import cats.effect.IO

// printer is an effect describing the printing of a string
val printer: IO[Unit] = IO.println("Hello, World")

// printAndRead is an effect describing a sequence of actions
val printAndRead: IO[String] = IO.print("Enter your name: ") *> IO.readLine
```

Note the keyword `val`. `printer` is a **value**. 
It exists in the program as an object, but by itself it **does not print anything**. 
Printing will happen only when the effect is **run** (usually inside `IOApp.run` or via `unsafe` methods).

### Effectful functions

A function that returns an effect is called an **effectful function**:

```dotty
def foo(str: String): IO[String] = IO.pure(str.toUpperCase)
```

In casual speech one says: "`foo` is an effectful function". It is important to understand the difference between:

- **Effectful function** -- returns a description of an action (`IO[String]`)
- **Function that performs an effect** -- produces side effects directly

This distinction is often overlooked, but it is critically important for understanding effectful functional programming.

## Effect vs Side Effect: the key distinction

### Side Effect

A **side effect** is a change that goes beyond simply returning a value. Indicators of a side effect:

- **It matters how many times the code runs** (once, twice, not at all)
- **It matters when the code runs** (now, in a second, after another action)
- The code interacts with the outside world (console, files, network)
- The code mutates state (variables, collections)

A classic example is `System.out.println`:

```dotty
def double(x: Int): Int =
  System.out.println("Hello, World")  // ← Side effect!
  x + x
```

When calling `double(5)`:

1. Output is printed to the console (side effect)
2. `10` is computed and returned

If `double` is called three times, printing will happen three times. 
You cannot "undo" or "alter" this side effect -- it inevitably occurs at the moment of invocation.

**Problems with side effects:**

- Difficult to test easily (need to intercept `stdout`)
- Cannot be executed in parallel without additional synchronization
- Cannot be retried on error
- Cannot be deferred
- Hard to reason about the program's behavior

### Effect as a description

In Cats Effect, code with side effects is **always wrapped** in special constructors, 
turning it into an **effect** -- a description of the action:

```dotty
// Side effect (executes immediately)
System.out.println("Hello, World")

// Effect (description, prints nothing)
val wrapped: IO[Unit] = IO(System.out.println("Hello, World"))
```

Now `wrapped` is a value that you can:

- Pass to functions
- Combine with other effects
- Transform (`map`, `flatMap`)
- Execute in parallel
- Retry on error
- Cancel

**Key consequence:** The code with side effects still contains side effects, 
but **outside the lexical scope of the constructor** we work only with the effect, not with the side effect itself.

## Comparison with Future: why IO is better

Many Scala developers are familiar with `Future`. At first glance `Future` and `IO` seem similar -- both represent asynchronous computations. 
But there is a fundamental difference:

### Future: a result, not a description

```dotty
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val printer: Future[Unit] = Future(println("Hello, World"))

printer  // ← does nothing? NO! It already printed "Hello, World"!
printer  // ← does not print again
printer  // ← does not print again
```

**What happened here?**

1. `Future(println("Hello, World"))` **immediately starts execution** in the thread pool
2. The variable `printer` stores not a description, but the **result** of an already-running computation
3. Subsequent references to `printer` simply return the already completed (or still-running) result

This leads to problems:

- Unpredictable behavior: how many times will it print?
- Execution cannot be deferred
- Difficult to retry
- Closures capture state in complex ways

### Attempting to fix Future via def

```dotty
def printer: Future[Unit] = Future(println("Hello, World"))

printer  // prints
printer  // prints again
printer  // prints again
```

Now it prints three times -- but this is also not a description of an action, but a **new start of computation each time**. Moreover:

- No control over when execution begins
- No single description that can be composed
- Easy to accidentally run the computation many times when it should be once

### IO: true description

```dotty
import cats.effect.IO

val printer: IO[Unit] = IO.println("Hello, World")

printer  // does nothing
printer  // does nothing
printer  // does nothing

// Only when we explicitly run the effect -- printing occurs
printer.unsafeRunSync()  // prints once
```

**Advantages of `IO`:**

- Full control over execution timing
- Predictable behavior with multiple uses
- Ease of composition
- Ability to transform before execution

## Effect constructors: how to wrap side effects

Cats Effect provides several constructors for wrapping side-effecting code depending on the nature of the operation.

### Synchronous constructors

Synchronous constructors are used for code that runs on the current thread and either returns a value or throws an exception.

#### `IO.apply / IO.delay` -- basic synchronous constructor

```dotty
import cats.effect.IO

// Basic way
val effect: IO[Int] = IO(42)

// With a side effect
val printing: IO[Unit] = IO(System.out.println("Hello"))

// With a possible exception
val risky: IO[Int] = IO(10 / 0)  // Will describe an error, not throw it now
```

**When to use:** For fast operations without blocking (computations, light side effects).

#### `IO.blocking` -- for blocking operations

```dotty
import cats.effect.IO

// Blocking file read
val readFile: IO[String] = IO.blocking {
  val src = scala.io.Source.fromFile("data.txt")
  val res = src.mkString
  src.close()
  res
}

// JDBC query
val dbQuery: IO[ResultSet] = IO.blocking {
  statement.executeQuery()
}
```

**When to use:** For operations that block OS threads (disk I/O, JDBC, legacy network libraries). 
Cats Effectdedicates a separate thread pool for such operations.

#### `IO.interruptible / IO.interruptibleMany` -- blocking but interruptible

```dotty
import cats.effect.IO

// Interruptible blocking operation
val interruptibleSleep: IO[Unit] = IO.interruptible {
  Thread.sleep(10000) // Can be interrupted via cancel
}
```

**Difference between interruptible and interruptibleMany:**

- `interruptible` -- checks for interruption once before execution
- `interruptibleMany` -- periodically checks for interruption during execution

**When to use:** For long blocking operations that should respond to cancellation.

### Asynchronous constructors

Asynchronous constructors are used for code that uses callback mechanisms (e.g., Netty, NIO, JavaScript setTimeout).

#### `IO.async / IO.async_` -- for callback-based APIs

```dotty
import cats.effect.IO
import java.util.concurrent.ScheduledExecutorService

def delayedEffect(scheduler: ScheduledExecutorService, delayMs: Long): IO[Unit] =
  IO.async_ { callback =>
    scheduler.schedule(
      new Runnable {
        def run(): Unit = callback(Right(()))
      },
      delayMs,
      java.util.concurrent.TimeUnit.MILLISECONDS
    )
    ()
  }
```

**Structure of `async_`:**

1. Accepts a callback: `Either[Throwable, A] => Unit`
2. Registers this callback in the asynchronous API
3. Returns `Unit` (no finalizer)

**IO.async** is a more powerful version, returning `IO[Option[IO[Unit]]]` for cancellation capability.

**When to use:** For integration with callback-based libraries, network frameworks, timers.

### Comparative table of constructors

| Constructor            | Execution Type           | Interruptibility           | Thread Usage        | Use Case                            |
|------------------------|--------------------------|----------------------------|---------------------|-------------------------------------|
| `IO.apply`             | Synchronous              | N/A (fast)                 | Current             | Computations, light operations       |
| `IO.blocking`          | Synchronous, blocking    | No                         | Dedicated pool      | Disk I/O, JDBC                       |
| `IO.interruptible`     | Synchronous, blocking    | Yes (one check)            | Dedicated pool      | Long operations with cancel support  |
| `IO.interruptibleMany` | Synchronous, blocking    | Yes (periodically)         | Dedicated pool      | Very long loops                      |
| `IO.async`             | Asynchronous, callback   | Yes (via finalizer)        | Non-blocking        | Network calls, timers                |

## Polymorphic effects (`F[_]`)

In real Cats Effect projects, code is often written not for a concrete `IO` type, but for an **abstract effect type** `F[_]`. This allows:

- Easy testing (using `IO` or special test effects)
- Code reuse with different effects
- Integration with other libraries (fs2, http4s, doobie)

### Example of a polymorphic function

```dotty
import cats.Monad
import cats.effect.std.Console
import cats.syntax.all.*

def example[F[_]: {Monad, Console}](str: String): F[String] =
  val printer: F[Unit] = Console[F].println(str)
  (printer *> printer).as(str)
```

**Example breakdown:**

- `F[_]` -- abstract effect type
- `Monad[F]` -- ability to use `flatMap`, `map`, `*>`
- `Console[F]` -- ability to print to console (abstraction over `IO.println`)
- `printer` -- print effect in abstract context
- `printer *> printer` -- two sequential print effects
- `.as(str)` -- replace the result with the original string

### Using a polymorphic function

```dotty
// With concrete IO
val ioResult: IO[String] = example[IO]("Hello, World")

// With a test effect (e.g., for verification)
val testResult: TestEffect[String] = example[TestEffect]("Test")
```

### Why is this powerful?

1. **Library composition** -- different libraries (fs2, http4s, doobie) work with the same abstract `F`
2. **Testability** -- you can substitute `IO` or a special test effect
3. **Flexibility** -- you can change the execution strategy without changing code

## Best practices for working with effects

### Rule 1: Always wrap side effects

```dotty
// BAD: direct side effect
def readConfig(): String =
  println("Reading config...") // Side effect!
  ...

// GOOD: effectful function
def readConfig: IO[String] =
  for
    _      <- IO.println("Reading config...")
    config <- IO.blocking(...)
  yield config
```

### Rule 2: Use the right constructor

```dotty
// Fast computations
val fast: IO[Int] = IO(2 + 2)

// Disk I/O
val diskIO: IO[String] = IO.blocking(readFile())

// Network calls (async libraries)
val network: IO[Response] = IO.async_ { cb => httpClient.get(url, cb) }
```

### Rule 3: Do not mix effects and side effects

```dotty
// BAD: side effect inside map
val bad: IO[Int] =
  IO(42).map: x =>
    println(x) // Side effect in map!
    x + 1

// GOOD: wrap in IO
val good: IO[Int] =
  for
    x <- IO(42)
    _ <- IO.println(x)
  yield x + 1
```

### Rule 4: Remember the difference between val and def

```dotty
// One and the same description, reused multiple times
val sharedEffect: IO[Unit] = IO.println("Shared")

// Each call -- a new description
def freshEffect: IO[Unit] = IO.println("Fresh")

// sharedEffect can be used many times -- the description is the same
val program1 = sharedEffect *> sharedEffect // Will print twice

// freshEffect creates a new description on each use
val program2 = freshEffect *> freshEffect // Will also print twice
```

**When to use `val` vs `def`:**

- `val` -- when the effect should always do the same thing
- `def` -- when the effect is parameterized or should be recreated

## Summary

| Concept                  | Definition                                      | Example                                  |
|--------------------------|-------------------------------------------------|------------------------------------------|
| **Effect**               | Description of an action                        | `val print = IO.println("Hi")`           |
| **Side Effect**          | Action mutating state outside of return         | `println("Hi")` directly                 |
| **Effectful Function**   | Function returning an effect                    | `def foo: IO[String]`                    |
| **Effect Constructor**   | Way to wrap a side effect                       | `IO(...)`, `IO.blocking`, `IO.async`     |

**Key takeaways:**

1. Effects are **descriptions**, not executions
2. Side effects must always be **wrapped** in effect constructors
3. `IO` is not `Future`: `IO` describes, `Future` starts
4. Use the **right constructor** depending on the nature of the operation
5. Polymorphic effects (`F[_]`) give maximum flexibility and reuse

Following these principles allows writing predictable, testable, composable code, 
fully controlling the timing and manner of effect execution.

---

**References:**

- [Cats effect documentation][doc]
- [Cats effect Concepts](https://typelevel.org/cats-effect/docs/concepts)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts
