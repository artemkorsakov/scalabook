# Asynchrony, Concurrency, and Parallelism

## Introduction

In most imperative programming languages (including Scala), the terms **"asynchronous"**, 
**"concurrent"**, and **"parallel"** are often used interchangeably. 
This leads to confusion, especially when working with frameworks like Cats Effect, 
where each of these concepts has a precisely defined meaning, 
and their distinction becomes critically important for proper system design.

Let's examine each concept in detail, their interrelations, and how Cats Effect implements the corresponding mechanisms.

## Asynchrony -- how a value is produced

### Definition

**Asynchrony** is the opposite of **synchrony**. 
It describes **the way** in which a given effect produces its value (or error).

It is important to understand: asynchrony is an **implementation detail** of a specific effect, not a model of organizing computations as a whole. 
It says nothing about whether the effect runs concurrently with others or sequentially.

### Synchronous effects

Synchronous effects in Cats Effect are created with methods:

- `IO.apply` (or equivalently `IO.delay`)
- `IO.blocking`
- `IO.interruptible`
- `IO.interruptibleMany`

They work in the familiar way: returning a result or throwing an error via `throw`.

```dotty
import cats.effect.IO

// Synchronous effect: blocks the current thread for 500ms
val syncDelay: IO[Unit] = IO {
  Thread.sleep(500) // Warning: blocks the OS thread!
}
```

**Problem with synchronous blocking**: `Thread.sleep` does not return control to the JVM thread until the delay has elapsed. 
This means that a **scarce resource** (an OS thread) is wasted for the entire wait duration. 
If you have 1,000 concurrent requests each doing such blocking, 
you will need 1,000 OS threads, which leads to enormous overhead.

### Asynchronous effects

Asynchronous effects are created via `IO.async` (or the simplified `IO.async_`). 
They produce a result through a **callback**, where:

- A successful result is passed as `Right(value)`
- An error is passed as `Left(exception)`

```dotty
import java.util.concurrent.{Executors, TimeUnit}
import cats.effect.IO

val scheduler = Executors.newScheduledThreadPool(1)

// Asynchronous effect: does not block the thread
val asyncDelay: IO[Unit] = IO.async_ { callback =>
  scheduler.schedule(
    new Runnable:
      def run(): Unit = callback(Right(()))
    ,
    500,
    TimeUnit.MILLISECONDS
  )
  () // Finalizer (optional)
}
```

**What's happening here?**

1. `IO.async_` registers a `callback`.
2. The scheduler gets a task to execute the callback after 500ms.
3. The `async_` method **returns control immediately**.
4. The OS thread running this code is freed and can do other work.
5. After 500ms, the scheduler calls `callback` on some thread (often from a pool).

**Key advantage**: The base OS thread is **not wasted** on waiting. 
It can be redirected to perform other work.

### Comparison of synchronous and asynchronous effects

| Characteristic          | Synchronous (Thread.sleep)      | Asynchronous (ScheduledExecutor)   |
|-------------------------|---------------------------------|------------------------------------|
| Return of control       | Only after completion           | Immediately                        |
| OS thread usage         | Blocks for the entire duration  | Released immediately               |
| Scalability             | Poor (one thread per task)      | Excellent (one thread for many tasks) |
| Writing complexity      | Simple (familiar code)          | Complex (manual callback management) |

### The role of fibers: eliminating asynchrony complexity

The main problem with asynchronous effects is the **complexity of working with callbacks**. 
In classical asynchronous programming (Node.js, Netty), code quickly turns into "callback hell".

**Cats Effect fibers completely eliminate this drawback.** 
To external code, both examples above look the same:

```dotty
// Both synchronous and asynchronous effects have the same type!
val delay: IO[Unit] = ???  // It doesn't matter how it's implemented internally

// Usage is identical for both
for
  _ <- delay
  _ <- IO.println("500ms elapsed")
yield ()
```

The difference between `return/throw` and a callback is **fully encapsulated** at the effect definition level. 
The rest of your application doesn't know and shouldn't know whether the effect is synchronous or asynchronous. 
This is the **immense power of fibers**.

### Important clarification

> **Nothing in the definition of "asynchronous" implies "concurrent" or "parallel".**

An asynchronous effect can be executed **sequentially** with other effects -- 
it's simply a detail of how it obtains its value. You can write:

```dotty
// Everything runs sequentially, despite internal asynchrony
for
  _ <- asyncDelay1   // Asynchronous
  _ <- asyncDelay2   // Also asynchronous
  _ <- IO.println("Done")
yield ()
```

Here `asyncDelay2` will start **only after** `asyncDelay1` completes. 
Asynchrony does not mean automatic parallelization.

## Concurrency -- independence of control flows

### Definition

In Cats Effect terminology, **concurrent execution** means 
that two or more actions are **independent in their control flow**. 
This is the opposite of **sequential execution**, where actions are strictly ordered.

The most important nuance: **concurrent actions do not have to run in parallel**. 
The runtime may decide to run them sequentially on a single core, 
and this will still be a correct implementation of "concurrency". 
Parallelism is just a possible optimization.

### How to express concurrency in Cats Effect

The simplest way is to use operators like `parTupled`, `parMapN`, `parTraverse`:

```dotty
// Two independent service calls
val serviceA: IO[Response] = callServiceA(params1)
val serviceB: IO[Response] = callServiceB(params2)

// Concurrent execution (the runtime may parallelize)
val both: IO[(Response, Response)] = (serviceA, serviceB).parTupled
```

`parTupled` is an **instruction to the runtime**: 
"these two effects are independent, they can be computed concurrently (possibly in parallel, if there are free cores)". 
Cats Effect **never assumes** parallel execution on its own -- 
you must explicitly indicate it with the appropriate operators.

### Low-level primitives: `start` and `join`

All high-level concurrency in Cats Effect is built on two fundamental primitives:

- `start` -- launches a fiber in the background, returns a `Fiber`
- `join` -- waits for a fiber to complete and returns its result

```dotty
val fiber: Fiber[IO, Response] = serviceA.start  // Launch in background
val resultA: IO[Response] = fiber.join           // Wait for the result

// The same as parTupled, but manual:
val bothManual: IO[(Response, Response)] = 
  for
    fiberA <- serviceA.start
    fiberB <- serviceB.start
    resultA <- fiberA.join
    resultB <- fiberB.join
  yield (resultA, resultB)
```

These primitives are significantly faster and safer than equivalent Java thread operations.

### Structured concurrency

**Structured concurrency** is an approach where all parallel operations form a **closed hierarchy**:

- Any operation launching parallel actions must guarantee their completion before moving forward.
- Results of a parallel operation are available **only after its full completion** and **only to the parent**.

**Example of structured concurrency:**

```dotty
// Result is not available until both services complete
val result: IO[(Response, Response)] = (serviceA, serviceB).parTupled

// Inside this block, responses A and B are already available
result.flatMap { case (respA, respB) =>
  processBoth(respA, respB)
}
```

**Advantages of structured concurrency:**

- No "leaked" fibers (launched but lost)
- Easier to reason about resource lifetimes
- Automatic cancellation on errors
- More predictable behavior

**Cats Effect tools for structured concurrency:**

- `parTupled`, `parMapN`, `parTraverse` -- main operators
- `background` -- for tasks that should outlive the parent, but with control
- `Supervisor` -- for managing a set of long-lived fibers
- `Dispatcher` -- for integration with callback code

### Unstructured concurrency

Sometimes structured concurrency is too restrictive. 
Cats Effect **does not forbid** unstructured concurrency when needed:

```dotty
// Start a fiber and don't wait for it
val fiber: Fiber[IO, Unit] = longRunningTask.start

// The parent completes, while the fiber continues to live (this may be needed)
// But this can lead to a "leaked" fiber if the reference is lost
```

**Risks of unstructured concurrency:**

- Fiber leaks (lost reference to `Fiber`)
- Resources not released
- Harder debugging and testing

**Recommendation**: Wherever possible, use structured tools (`background`, `Supervisor`) instead of raw `start`.

### Shared state: Ref and Deferred

`Ref` (atomic reference) and `Deferred` (single-shot promise) are **unstructured** tools for modelling shared state:

```dotty
val counter: IO[Ref[IO, Int]] = IO.ref(0)

// Unstructured: any fiber can change the counter at any time
counter.update(_ + 1)
```

These primitives **do not impose structure** and can lead to complex-to-understand logic. 
However, they are building blocks for powerful high-level abstractions:

- `Queue` -- a queue
- `Semaphore` -- a semaphore
- `Hotswap` -- hot resource swap
- `Resource` -- resource management

**Conclusion**: Cats Effect **encourages structured concurrency** 
and provides many flexible tools to achieve it, 
but does not prevent unstructured approaches when they are truly needed.

> The primitives described above will be analyzed in more detail later.

## Parallelism -- a detail of the runtime implementation

### Definition

**Parallel execution** means that two processes execute **simultaneously on different physical cores** (or processors). 
This is **exclusively a detail of the runtime and hardware implementation**.

Parallelism is closely related to concurrency, but they are not the same:

- **Concurrency** is **semantics** (independence of control flows)
- **Parallelism** is **physical reality** (simultaneous execution on different cores)

### When concurrent is not parallel

#### Scenario 1: JavaScript (single-threaded environment)

JavaScript runs in a single thread. Even if you define effects as concurrent:

```dotty
// On JVM this may run in parallel
// On JavaScript -- strictly sequentially on a single thread
(serviceA, serviceB).parTupled
```

This **does not mean** that `parTupled` is useless in JavaScript! 
The runtime still knows that `serviceA` and `serviceB` are independent, 
and can interleave their execution (cooperative multitasking), 
but there will be no physical parallelism.

#### Scenario 2: More fibers than threads

Cats Effect on JVM typically creates a number of threads 
matching the number of **physical cores** (e.g., 8 threads on an 8-core processor). 
There may be **millions of fibers**:

```dotty
// Launch 1,000,000 concurrent fibers
val fibers = (1 to 1_000_000).map(_ -> task.start)
```

Only **8 fibers** can execute simultaneously (physically) on 8 cores. The rest will wait in a queue. 
From a semantic perspective, all 1,000,000 fibers are considered "concurrent", 
but physically they run sequentially when it's their turn.

### Fairness and preventing thread starvation

Fibers are **prevented from "starving"** their carrier thread. 
Cats Effect guarantees that no single fiber can indefinitely occupy an OS thread, blocking others.

**Fairness mechanisms:**

1. **Asynchronous effects** -- when performing an async operation, the fiber voluntarily yields the thread.
2. **Artificial cooperativity** -- if a fiber executes too many sequential sync effects 
    without a "yield", the runtime **inserts an artificial yield point** (prevents starvation).

```dotty
// Bad: long chain of sync effects without yield
def badLoop: IO[Unit] = (1 to 1000000).foldLeft(IO.unit): (acc, i) =>
  acc *> IO(compute(i)) // May block other fibers

// Good: Cats Effect will automatically insert yield points
// But you can also do it explicitly:
def goodLoop: IO[Unit] = (1 to 1000000).foldLeft(IO.unit): (acc, i) =>
  acc *> IO(compute(i)) *> IO.cede // Explicitly yield the thread
```

`IO.cede` is an explicit instruction to "yield the thread to other fibers". 
Usually not needed, as Cats Effect inserts yields automatically.

## Summary table: comparison of concepts

| Concept              | What it determines?                            | Opposite                               | Who controls?                      | Example                  |
|----------------------|------------------------------------------------|----------------------------------------|-------------------------------------|--------------------------|
| **Asynchrony**       | How a value is obtained (callback vs return)   | Synchrony                              | Developer (at effect definition)   | `IO.async_`              |
| **Concurrency**      | Independence of control flows                  | Sequentiality                          | Developer (via `parTupled`, `start`) | `(a, b).parTupled`     |
| **Parallelism**      | Physical execution on different cores          | Sequential execution on a single core  | Runtime + OS + hardware            | Depends on core count    |

## Practical recommendations

### When to use asynchronous effects

- **Whenever possible** for I/O operations (network, disk, timers)
- Cats Effect provides async versions of many operations out of the box
- For blocking operations, use `IO.blocking`

### When to use concurrency

- **Whenever** tasks are independent (can speed up execution)
- Use high-level operators: `parTraverse`, `parSequence`, `parMapN`
- Avoid `start` without structured wrappers (`background`, `Supervisor`)

### Expectations about parallelism

- **Do not rely** on physical parallelism for correctness
- Code should work correctly even on a single core
- Consider parallelism as an **optimization**, not a guarantee

## Conclusion

Understanding the differences between asynchrony, concurrency, and parallelism is important for effective work with Cats Effect. 
These concepts, though interconnected, describe different aspects of program execution:

- **Asynchrony** -- how (callback vs return)
- **Concurrency** -- what (independence of tasks)
- **Parallelism** -- where (on which cores)

Cats Effect provides powerful and safe tools for working with all three aspects, 
enabling writing correct, performant, and scalable code.

---

**References:**

- [Cats effect documentation][doc]
- [Cats effect Concepts](https://typelevel.org/cats-effect/docs/concepts)
- [Concurrent Execution in Cats Effect using Fibers Part 4](https://yadukrishnan.live/concurrent-execution-in-cats-effect-using-fibers)
- [Coroutines, Event Loops - Build Your Own in Scala](https://rockthejvm.com/articles/build-your-own-async)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts
