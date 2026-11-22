# Cats Effect

[**Cats Effect**][home] is a pure asynchronous runtime for Scala.
It provides excellent capabilities for writing highly reliable, fast, and concurrent programs
while remaining faithful to the principles of functional programming.

## Key Features

### **Asynchronous**

An [Monad](../typeclass/monad/monad.md) `IO` allows capturing and
controlling callback-driven asynchronous effects behind a clean synchronous interface.
While it may superficially resemble `Future`, `IO` elevates this concept to a new level,
providing full control over the semantics and behavior of computations.
Write programs that seamlessly blend synchronous and asynchronous code without sacrificing clarity or composability.

### **Concurrency**

`IO` can power highly concurrent applications, such as web services handling tens of thousands of requests per second.
Concurrency in `IO` is implemented through fibers — lightweight, cancellable threads fully managed by the runtime.
Fibers are much cheaper than native OS threads, so your application can launch tens of millions of fibers without much effort.

### **Tracing**

`IO` collects information at runtime, making it easy to trace the source of errors
or probe the program as it runs.
Tracing can be enabled in production without a noticeable impact on performance,
automatically unlocking powerful capabilities such as enhanced exceptions that simplify error diagnosis.

### **Safety**

Real-world applications often work with resources: network connections, file descriptors.
Resource management is an exceedingly complex task in concurrent applications.
A single small mistake can lead to memory leaks or even deadlocks.
`IO` manages the lifecycle of resources, ensuring their safe allocation
and release even in the presence of exceptions and cancellation.

### **Composability**

Cats Effect follows the principles of purely functional programming:
`IO` represents a _description_ of a program, not a running computation.
This gives you full control over how and when effects are evaluated.
Complex programs can be built from simpler ones while retaining the ability to reason about behavior and complexity.
Refactor with confidence!

### **Powerful Abstraction**

Cats Effect uniquely defines what it means to be a functional effect.
This gives it unparalleled expressive and compositional power, unlocking a whole ecosystem of extensibility.
You get the ability to override the runtime itself,
bringing the full power of the Cats ecosystem to elegantly solve complex problems:
distributed tracing, dependency injection, error fan-out, and much more!

The Cats Effect abstraction system is the result of years of research and experience
in applying functional programming concepts to real-world JVM and JavaScript applications.

## Introduction to the Cats Effect Library

### 1. Adding the Dependency

To get started with **Cats Effect**, you need to add it to your `build.sbt` build file.

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0"
```

**What does this give you?**

- `IO` — the primary data type representing a _description_ of an I/O effect.
- `IOApp` — an abstraction for creating safe, managed applications with correct resource handling and shutdown.
- The ability to create concurrent, parallel, asynchronous programs without low-level threading concerns.

### 2. Simplest Cats Effect Application

Create a file `HelloWorld.scala` with the following content:

```dotty
import cats.effect.{IO, IOApp}

object HelloWorld extends IOApp.Simple:
  val run: IO[Unit] = IO.println("Hello, World!")
```

**Explanation:**

- `IOApp.Simple` — a convenient trait requiring only the implementation of the `run: IO[Unit]` method.
- `IO.println("Hello, World!")` — does not print immediately, but _describes_ an action that will be executed at runtime.
- The entire program is a **pure value** of type `IO[Unit]` that can be passed, combined, and reused.

**Running:**

```bash
sbt run
```

You will see:

```
Hello, World!
```

### 3. Capabilities of a Full Application

Applications built on `IOApp` automatically get:

- **Timers** (`sleep`, `timeout`, `clock`) — via `cats.effect.Clock`.
- **Multithreading** — via `cats.effect.Async` and `cats.effect.std`.
- **Concurrency** — lightweight _fibers_, similar to virtual threads but much lighter than OS threads.
- **Structured concurrency** — automatic management of child fiber lifetimes.
- **Cancellability** and error resilience.

### 4. Example: FizzBuzz with Four Parallel Fibers

This example demonstrates how easy it is to describe a parallel program in Cats Effect.
Four fibers run in parallel:

```dotty
import cats.effect.{IO, IOApp}
import scala.concurrent.duration.*

object StupidFizzBuzz extends IOApp.Simple:
  val run: IO[Unit] =
    for
      ctr <- IO.ref(0)           // 1. Create an atomic counter

      wait = IO.sleep(1.second)  // 2. Wait program
      poll = wait *> ctr.get     // 3. Poll: wait, then read the value

      // Fiber 1: prints only the number
      _ <- poll.flatMap(IO.println(_)).foreverM.start

      // Fiber 2: prints "fizz" if the number is divisible by 3
      _ <- poll.map(_ % 3 == 0)
               .ifM(IO.println("fizz"), IO.unit)
               .foreverM.start

      // Fiber 3: prints "buzz" if the number is divisible by 5
      _ <- poll.map(_ % 5 == 0)
               .ifM(IO.println("buzz"), IO.unit)
               .foreverM.start

      // Fiber 4: increments the counter every second
      _ <- (wait *> ctr.update(_ + 1)).foreverM.void
    yield ()   
```

**Breakdown of key constructs:**

| Construct     | Meaning                                                                     |
|---------------|-----------------------------------------------------------------------------|
| `IO.ref(0)`   | Creates a thread-safe reference to a mutable value.                         |
| `*> (productR)`| Executes the left action, then the right one, returning the right result.   |
| `foreverM`    | Repeats the action infinitely.                                              |
| `.start`      | Starts the action in a separate fiber and immediately returns control.      |
| `void`        | Ignores the result of the action.                                           |

**Example output (excerpt):**

```text
0
buzz
fizz
2
fizz
3
4
buzz
5
fizz
6
7
8
fizz
9
buzz
10
```

**Note an important detail:**

The variables `wait` and `poll` are `IO` _values_.
They are used in four independent fibers, but their behavior remains the same.
This is possible because we are working with _descriptions of computations_, not the computations themselves.

- **Composition** — simple programs (`wait`, `poll`) combine easily into complex ones.
- **Reuse** — the same description is used multiple times without unexpected side effects.
- **Stress-free parallelism** — launching four independent fibers takes just a few `.start` calls.
- **Safety** — Cats Effect automatically handles cancellation and resource finalization.

> **Terminology:**
> Cats Effect uses the concept of a _fiber_ — a lightweight thread scheduled on a JVM thread.
> You can create thousands or even millions of fibers without issues.

## Conclusion

Cats Effect offers an expressive and safe way to build parallel systems in Scala. Key ideas:

- **Description, not execution** — `IO` describes an action, execution happens only through the runtime.
- **Composition** — small programs easily combine into large ones.
- **Structured concurrency** — fibers are managed automatically, resource leaks are impossible.

---

**Links:**

- [Link to exercises repository](https://gitverse.ru/scalabook/cats-effect)
- [Cats effect documentation][doc]
- ["Fire and Forget" in Cats Effect](https://habr.com/ru/company/otus/blog/589297/)
- [A practical guide to error handling in Scala Cats and Cats Effect](https://softwaremill.com/practical-guide-to-error-handling-in-scala-cats-and-cats-effect/)
- [Baeldung: Introduction to Cats Effects](https://www.baeldung.com/scala/cats-effects-intro)
- [Baeldung: Introduction to Concurrent Programming With Cats Effect Fibers](https://www.baeldung.com/scala/cats-effect-fibers-concurrent-programming)
- [Baeldung: Difference Between flatMap(), flatTap(), evalMap() and evalTap() in Cats Effect](https://www.baeldung.com/scala/cats-effect)
- [Cats by example – State](https://www.youtube.com/watch?v=T8fhAHw9GM0&amp;list=PLy4hTMY6erDBmvZiEDjtCTP8XCdhaXc2c)
- [Cats Effect 3 by Daniel Spiewak](https://www.youtube.com/watch?v=KZtVBtOrP50)
- [Cats Effect 3 for Beginners Part-1](https://yadukrishnan.live/cats-effect-3-for-beginners-part-1)
- [Cats Effect Fibers](https://blog.rockthejvm.com/cats-effect-fibers/)
- [Cats Effect vs ZIO](https://softwaremill.com/cats-effect-vs-zio/)
- [Common APIs: Cats Effect IO Part-2](https://yadukrishnan.live/common-apis-cats-effect-io-part-2)
- [Concurrency in Cats Effect 3](https://typelevel.org/blog/concurrency-in-ce3.html)
- [Concurrency In Scala with Cats-Effect](https://github.com/slouc/concurrency-in-scala-with-ce)
- [Concurrency with Cats-effect](https://www.youtube.com/watch?v=Gig-f_HXvLI)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Flavors of shared state in Cats Effect](https://blog.kubukoz.com/flavors-of-shared-state/)
- [Herding Cats](https://eed3si9n.com/herding-cats/)
- [Lazy and Eager Computations in Cats using Eval](https://yadukrishnan.live/lazy-and-eager-computations-in-cats-using-eval)
- [Retry Failing Tasks with Cats and Scala - Alexandru Nedelcu](https://alexn.org/blog/2020/08/03/on-error-retry-loop/)
- [Retrying Cats Effect Failures With Ease](https://yadukrishnan.live/retrying-cats-effect-failures-with-ease)
- [Traversing Cats Effect IOs Part-3](https://yadukrishnan.live/traversing-cats-effect-ios-part-3)

[home]: https://typelevel.org/cats-effect/
[doc]: https://typelevel.org/cats-effect/docs/getting-started
