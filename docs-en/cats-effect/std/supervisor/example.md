# Shared State and Usage Regions

## What Is Shared State in Functional Concurrency?

In concurrent functional programming, **shared state** —
is any data that is simultaneously used by multiple fibers.
Examples of shared state include:

- [Ref](../../typeclasses/concurrent/ref.md) — shared variable
- [Semaphore](../semaphore.md) — permit counter
- [Deferred](../../typeclasses/concurrent/deferred.md) — a signal to wait for
- [Queue](../queue.md) — message queue
- [Mutex](../mutex.md) — lock

However, unlike imperative languages where shared state is often **global** (e.g., `static` variables),
in Cats Effect shared state is created **locally**, and its scope is explicitly controlled via `flatMap`.

## Key Idea: Usage Regions

In Cats Effect, a **usage region** is defined by the boundaries of `flatMap` (or syntactic sugar for-comprehension).
When you create shared state inside `flatMap`, you clearly define where it will be accessible.

```dotty
for
  sem <- Semaphore[IO](1) // ← start of the shared usage region
  // sem is accessible here
  _ <- something(sem)
// sem is still accessible
yield ()
// sem is NO LONGER accessible — the region ended
```

This is similar to the **principle of least privilege**: a resource exists only where it is needed.

## Example 1: Correct Sharing via `flatMap`

In this example, two infinite processes `p1` and `p2` share one semaphore that limits access to an "expensive" operation.
The semaphore is created in the `flatMap` scope and is **explicitly passed** to both functions.

```dotty
import scala.concurrent.duration.*
import cats.effect.*
import cats.effect.std.{Semaphore, Supervisor}

object Regions extends IOApp.Simple:

  // Simulate random delay (700–800 ms)
  val randomSleep: IO[Unit] =
    IO(scala.util.Random.nextInt(100)).flatMap { ms =>
      IO.sleep((ms + 700).millis)
    }.void

  // Process 1: acquires a permit, prints, then sleeps
  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P1")) >> randomSleep

  // Process 2: same
  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P2")) >> randomSleep

  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      // Create a semaphore with 1 permit INSIDE the Supervisor scope
      Semaphore[IO](1).flatMap { sem =>
        // Launch both processes under the supervisor
        supervisor.supervise(p1(sem).foreverM).void *>
          supervisor.supervise(p2(sem).foreverM).void *>
          // Let them work for 5 seconds
          IO.sleep(5.seconds).void
      }
    }
end Regions
```

### What Is Important Here?

| Element               | Role                                                                                    |
|:----------------------|:----------------------------------------------------------------------------------------|
| `Semaphore[IO](1)`    | Created **inside** `flatMap`, its scope is limited to the block.                        |
| `p1(sem)`, `p2(sem)`  | The semaphore is **explicitly passed** as a parameter. No global access.                |
| `Supervisor`          | Long-lived processes are launched, but it doesn't affect the visibility scope of state. |

**Result:** `p1` and `p2` compete for one permit.
The execution order is unpredictable, but they never work simultaneously.
At the same time, the semaphore is **not visible** outside `flatMap`.

## Why Do Data Structures Return `F[Something]`?

You may have noticed: `Semaphore[IO](1)` returns not `Semaphore[IO]`, but `IO[Semaphore[IO]]`. This is **intentional**.

```dotty
// Creating a semaphore is an effect!
val create: IO[Semaphore[IO]] = Semaphore[IO](1)

// Using it
create.flatMap { sem => // only here is sem accessible
  // ...
}
```

**Reasons:**

1. **Atomic initialization:** Creating a semaphore may include internal state (`Ref`) that needs to be created atomically.
2. **Explicit scope:** You can't "accidentally" make a semaphore global without performing the effect.
3. **Resource safety:** If resource creation is an effect, it can be embedded in `Resource` or `bracket`.
4. **Referential transparency:** Everything remains a pure function.

> **OO analogy:** This is as if a class constructor could only be called inside `try-with-resources`
> or `using` — you can't create an object outside its context.

## Example 2: Leaky State — WHAT NOT TO DO

Now consider a **bad** example where shared state becomes global and uncontrolled.

```dotty
import scala.concurrent.duration.*
import cats.effect.*
import cats.effect.std.{Semaphore, Supervisor}
import cats.effect.unsafe.implicits.global

object LeakyState extends IOApp.Simple:

  // ❌ GLOBAL SEMAPHORE — created once outside control
  lazy val sem: Semaphore[IO] =
    Semaphore[IO](1).unsafeRunSync() // anti-pattern!

  def launchMissiles: IO[Unit] =
    sem.permit.surround {
      IO.println("Launching missiles") >>
        IO.sleep(3.seconds) >>
        IO.println("Missiles launched, lock released")
    }

  def randomSleep: IO[Unit] =
    IO(scala.util.Random.nextInt(100)).flatMap { ms =>
      IO.sleep((ms + 700).millis)
    }.void

  def p1: IO[Unit] =
    sem.permit.surround(IO.println("Running P1")) >> randomSleep

  def p2: IO[Unit] =
    sem.permit.surround(IO.println("Running P2")) >> randomSleep

  def run: IO[Unit] =
    Supervisor[IO].use { supervisor =>
      supervisor.supervise(launchMissiles) *>
        supervisor.supervise(p1.foreverM).void *>
        supervisor.supervise(p2.foreverM).void *>
        IO.sleep(5.seconds).void
    }
end LeakyState

// Running P1
// Launching missiles
// Missiles launched, lock released
// Running P2
// Running P1
// Running P2
// Running P1
// Running P2
// Running P1
```

### Why Is This Dangerous?

1. **Global access:** `sem` is accessible from any point in the program, including `launchMissiles`, `p1`, `p2`.
2. **Implicit sharing:** You can't see where and how the semaphore is used just by looking at the function signature.
3. **Deadlock risk:** If `launchMissiles` grabs the permit and never releases it
   (e.g., due to an error or infinite loop), `p1` and `p2` will be permanently blocked.
4. **Testing:** You can't easily replace the semaphore in tests — it is hard-coded.
5. **Undefined behavior:** `unsafeRunSync()` outside `IOApp` can lead to hangs, especially on platforms like Scala.js.

### What Happens When Running `LeakyState`?

A scenario like this is possible:

- `launchMissiles` grabs the permit.
- `p1` and `p2` try to grab the permit → block.
- After 3 seconds, `launchMissiles` completes and releases the permit.
- `p1` or `p2` grabs it, but the process has already lost 3 seconds.
- In a large program, it is hard to predict who will grab the permit and when, and whether one "greedy" process will cause starvation of others.

## Comparison: Correct State vs Leak

| Aspect                    | ✅ Correct (Regions)           | ❌ Leak (LeakyState)           |
|:--------------------------|:-------------------------------|:-------------------------------|
| **Creation**              | Inside `flatMap`, explicit effect | Global, `unsafeRunSync()`     |
| **Scope**                 | Local, limited to the block    | Global, accessible everywhere  |
| **Passing to functions**  | Explicit parameter             | Implicit (via closure)         |
| **Testability**           | Easy to replace the semaphore  | Impossible without rewriting   |
| **Deadlock risk**         | Low (visible who uses it)      | High (hidden dependencies)     |
| **Referential transparency** | Yes                          | No (due to `unsafeRunSync`)    |

## Main Idea: Shared Region = `flatMap`

In Cats Effect, the **shared region** is a **block of code** where shared state is created and used.

```dotty
// Shared region starts here
Semaphore[IO](1).flatMap { sem =>
  // Everything here can see sem
  p1(sem) *> p2(sem)
}
// Here sem is no longer available
```

**How does this help?**

- You can see **all users** of shared state by simply looking at the block.
- No hidden dependencies via global variables.
- Easy refactoring and testing.

## Practical Takeaways

### ✅ What to Do:

1. Always create shared state inside `flatMap` or `for`.
2. Pass it **explicitly** as a parameter where it is needed.
3. Use `Resource` for long-lived state (e.g., connection pools).
4. Test by creating state locally inside the test.

```dotty
test("semaphore works") {
  Semaphore[IO](1).flatMap { sem =>
    // test sem
  }
}
```

### ❌ What to Avoid:

1. **Global variables** with shared state (`lazy val` + `unsafeRunSync`).
2. **`unsafeRunSync()`** outside `IOApp` (this is a "loophole" only for REPL and demonstration examples).
3. Creating state without an explicit scope (e.g., inside an `object`).
4. Passing state **implicitly** via closures (makes understanding dependencies difficult).

## Summary

- **Shared state in Cats Effect** is not magic, but ordinary values (`Ref`, `Semaphore`, etc.),
  which are **created in an effect** and **have an explicit scope** via `flatMap`.
- **Shared region** is a block of code, delimited by `flatMap`, within which the state is accessible.
- **State leak** occurs when you create shared state **globally**, outside `flatMap`,
  and it becomes accessible uncontrollably.
- Following the principle "create inside `flatMap`, pass explicitly"
  gives you **predictable, testable, and safe** concurrent code.

This is one of the fundamental reasons why Cats Effect (and other functional effect systems)
designs its data structures to return `F[Thing]`, not just `Thing`.
They force you to **think about shared regions**.

---

**Links:**

- [Volpe Gabriel - Practical FP in Scala](https://leanpub.com/pfp-scala)

[doc]: https://typelevel.org/cats-effect/docs/concepts
