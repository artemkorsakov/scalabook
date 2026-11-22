# Fiber Dumps

## Problem: Async Deadlocks

Async deadlocks are one of the most difficult problems to diagnose in asynchronous applications.
The classic scenario: fiber A waits for fiber B, which in turn waits for fiber A (the simplest case of cyclic waiting).

In the JVM synchronous world, we have **thread dumps** — a powerful tool
showing the state of each thread and its call stack.
In a deadlock, you see the exact lines of code where each thread is blocked.

In the async Cats Effect world, things are more complex. When a fiber blocks (e.g., on `Deferred.get`),
the runtime removes all references to it from active scheduling.
The fiber "falls asleep" until it is woken up. As a result, classic JVM thread dumps are **useless**:

- You'll see `io-compute-*` threads just spinning in a wait loop, but you won't see _which_ fiber fell asleep and on what.
- The only debugging option is to add `IO.println` to the code in the hope of reproducing the issue. This is painful and often useless in production.

Starting with Cats Effect 3.3.0, it's possible to take **fiber dumps**.

## What Is a Fiber Dump?

A fiber dump is a **snapshot of the state of all fibers** known to the Cats Effect runtime at the current moment. It shows:

- The **status** of each fiber (`WAITING`, `YIELDING`, `RUNNING`, `QUEUED`).
- The **stack trace** with information about combinators (`flatMap`, `map`, `deferred`, etc.) and exact locations in user code.
- Summary statistics by pools and global fibers.

Here's what a dump looks like for a simple deadlock (see the example below):

```
cats.effect.IOFiber@56824a14 WAITING
  ├ flatMap @ Deadlock$.$anonfun$run$2(Deadlock.scala:26)
  ├ flatMap @ Deadlock$.$anonfun$run$1(Deadlock.scala:25)
  ├ deferred @ Deadlock$.<clinit>(Deadlock.scala:22)
  ╰ flatMap @ Deadlock$.<clinit>(Deadlock.scala:22)

cats.effect.IOFiber@6194c61c WAITING
  ├ get @ Deadlock$.$anonfun$run$1(Deadlock.scala:24)
  ╰ get @ Deadlock$.$anonfun$run$1(Deadlock.scala:24)

Global: enqueued 0, waiting 2
```

### Detailed Breakdown of the Deadlock Example

Let's look at the code that leads to this deadlock:

```dotty
import cats.effect.{IO, IOApp}

object Deadlock extends IOApp.Simple:
  val run: IO[Unit] =
    for
      latch <- IO.deferred[Unit]  // Create a Deferred (like a semaphore)
      body = latch.get            // Fiber that will wait for the signal
      fiber <- body.start         // Launch this fiber in the background
      _     <- fiber.join         // Main fiber waits for the background fiber
      _     <- latch.complete(()) // Never executes!
    yield ()
```

**What's happening here (classic deadlock):**

1. A `Deferred[Unit]` is created (empty).
2. The background fiber (`body`) is launched and blocks on `latch.get` — it waits for a signal from the main fiber.
3. The main fiber calls `fiber.join` — it waits for the background fiber to complete.
4. Deadlock: the main fiber waits for the background, and the background waits for a signal from the main. No one can make progress.

**How a fiber dump helps:**

- **First fiber (`@56824a14`):** Is in `WAITING` state. The trace shows it's waiting on `fiber.join` (line 26).
- **Second fiber (`@6194c61c`):** Is in `WAITING` state. The trace shows it's waiting on `latch.get` (line 24).

Now we have a **clear, direct piece of evidence** pointing to the cause of the deadlock.
We see both fibers and the exact code lines where they are blocked.

## How to Trigger a Fiber Dump?

### POSIX Signals (Linux, macOS, BSD)

The way to trigger a dump depends on the platform and available signals.
Cats Effect tries to register handlers for **two signals**: `INFO` (macOS/BSD) and `USR1` (Linux). In practice:

- **macOS / BSD:** Press **`Ctrl + T`** in the terminal where the app is running. You'll see the fiber dump right in stdout/stderr.
  This works because the terminal sends the `SIGINFO` signal.
- **Linux:** Use the command `kill -USR1 <pid>`, where `<pid>` is the process ID of your application.
- **Node.js:** Use **`kill -USR2 <pid>`**.

> **Important:** Fiber dumps **do not work** on regular Windows (without WSL), as it lacks POSIX signals.
> They are also disabled in **JDK 8** due to the lack of free signals.

### JMX (Java Management Extensions)

If POSIX signals are unavailable or you prefer a programmatic approach,
Cats Effect registers an **MBean** named `cats.effect.unsafe.metrics.LiveFiberSnapshotTriggerMBean`.
By calling its single method `liveFiberSnapshot`, you get an array of strings — the fiber dump.

**Example of programmatic dump retrieval via JMX:**

```dotty
import java.lang.management.ManagementFactory
import javax.management.ObjectName

// Get the MBeanServer
val server    = ManagementFactory.getPlatformMBeanServer
val mbeanName =
  new ObjectName("cats.effect.unsafe.metrics:type=LiveFiberSnapshotTrigger-0")

// Call the liveFiberSnapshot method
val fibers = server
  .invoke(mbeanName, "liveFiberSnapshot", Array.empty, Array.empty)
  .asInstanceOf[Array[String]]
  .filter(_.trim.nonEmpty)

fibers.foreach(println)
```

### Programmatic Dump Retrieval

> **⚠️ WARNING:** Programmatic dump retrieval creates a **strong reference** to each fiber.
> This prevents these fibers from being garbage collected as long as the snapshot object is in scope.
> **This can lead to memory leaks** if used in a production loop.
> Use only for debugging!

The most direct way to get a dump is to use the `runtime.liveFiberSnapshot()` method:

```dotty
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

object Main extends IOApp.Simple:
  val run: IO[Unit] =
    for
      snapshot <- IO.delay(runtime.liveFiberSnapshot())
      // Print fibers bound to specific worker threads (JVM/Native only)
      _ <- snapshot.workers.toList.traverse_ { case (worker, fibers) =>
        IO.println(s"Worker ${worker.thread} #${worker.index}") *>
          fibers.traverse_(fiber => IO.println(fiber.pretty))
      }
      // Print global fibers (not bound to threads, e.g., blocked on Deferred)
      _ <- snapshot.external.traverse_(fiber => IO.println(fiber.pretty))
    yield ()
```

## Fiber Statuses

During a dump, each fiber has one of the following statuses:

| Status          | Description                                                                                                                                               | Typical Causes                                              |
|:----------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------|
| **`WAITING`**   | Fiber is semantically blocked. It's waiting for an external event: `Deferred.get`, `Semaphore.acquire`, `IO.sleep`, `IO.async`, etc.                      | Deadlocks, long timeouts, resource waiting.                 |
| **`YIELDING`**  | Fiber is suspended cooperatively (yield). It's ready to execute, but the scheduler hasn't given it the CPU yet.                                          | High CPU load, large number of active fibers.               |
| **`RUNNING`**   | Fiber is currently executing on some worker thread.                                                                                                       | Normal working state.                                       |
| **`QUEUED`**    | Fiber is in the queue (e.g., blocked on `Mutex` or `Semaphore`). It's waiting its turn.                                                                | High concurrency, limited resources.                        |

## Fiber Stack Trace (`pretty` print)

For each fiber, a chain of combinators (stack trace) is printed:

```
cats.effect.IOFiber@6194c61c WAITING
  ├ get @ Deadlock$.$anonfun$run$1(Deadlock.scala:24)
  ╰ get @ Deadlock$.$anonfun$run$1(Deadlock.scala:24)
```

- **`get @ Deadlock$.$anonfun$run$1`:** Shows the combinator (`get`) and an approximate location in the code (anonymous function).
- The number in parentheses (`(Deadlock.scala:24)`) — the line in the source file where the call is located (if available).
- Characters `├`, `╰` — just a visualization of the call tree (no syntactic significance).

## Platform-Specific Notes

### JVM

- Full support: all statuses, stack traces with line numbers (with debug symbols enabled), signal integration.
- MBean support for programmatic access.
- Thread names (`io-compute-*`) are printed in the statistics.

### Node.js (JavaScript)

- The mechanism works! Use `kill -USR2 <pid>`.
- Traces may be less detailed due to aggressive optimizer inlining.
- No thread name binding (single thread).
- **Browser:** Currently not supported.

### Scala Native

- Limited support. Some features (e.g., worker thread binding) may be unavailable.
- Requires native signal support.

## Additional Statistics

After the fiber list, summary statistics are printed:

```
Global: enqueued 0, waiting 2
```

- **`enqueued`:** Number of fibers queued for execution (waiting for CPU).
- **`waiting`:** Number of fibers in `WAITING` state (semantically blocked).

## Configuration

Fiber dumps are enabled **by default**.
However, collecting trace information for dumps can add overhead.
Research has shown that this overhead is less than 1% in realistic applications,
but for extreme performance-sensitive systems, you can disable this feature via [the `IORuntime` configuration](io-runtime-config.md).

## Summary

Fiber dumps are a powerful and indispensable tool for debugging complex concurrent programs in Cats Effect.
They turn solving the "async deadlock" problem from agonizing guessing into a structured process:
trigger the dump → see blocked fibers → fix the bug.

Remember the main ways to trigger:

1. **macOS:** Press `Ctrl+T`.
2. **Linux:** `kill -USR1 <pid>`.
3. **Node.js:** `kill -USR2 <pid>`.
4. **JMX:** Call `liveFiberSnapshot` on the MBean.
5. **Programmatically:** Use `runtime.liveFiberSnapshot()` (only for debugging due to leak risk).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Fiber Dumps](https://typelevel.org/cats-effect/docs/core/fiber-dumps)

[doc]: https://typelevel.org/cats-effect/docs/concepts
