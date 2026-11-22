# Starvation and Tuning

## Automatic Performance Monitoring

All Cats Effect applications created with `IOApp` have a built-in automatic mechanism
that **periodically checks whether the application runtime is experiencing CPU starvation**.
If you've ever seen warnings like the following, they are the result of this mechanism.
It triggers when it detects that your application's responsiveness rate
(time between an async event occurring and it being handled) exceeds a given threshold.

```
2023-01-28T00:16:24.101Z [WARNING] Your application's responsiveness to a new asynchronous
event (e.g. a new connection, an upstream response, or a timer) exceeded 40 milliseconds.
It is likely that your CPU is starving. Consider increasing the granularity of your delays
or adding more cedes. This may also be a sign that you are unintentionally performing
blocking I/O (e.g. filesystem or network calls) without the blocking combinator.
```

**Important to understand:** The timestamp in the warning is the **detection time** of starvation,
not exactly the time starvation began or the time of the task that led to it.

Although this warning implementation is very straightforward and reliable (it almost never gives false negatives),
**the root cause of the problem is often complex and hard to trace**.
Unfortunately, there is no single action that solves the problem in all cases.
This page is intended to:

1. Discuss scenarios that can lead to "starvation."
2. Propose a list of commonly useful diagnostic steps and typical root causes.

## What Is "Starvation" in the Context of Cats Effect?

The first and most important step — understanding what _starvation_ means in this context.

In general terms, "starvation" can refer to any scenario where a task cannot access a resource
it needs to continue working. The warning above is something of a logical paradox:
a _processor_ cannot experience a resource shortage, since it is itself a resource.
**Tasks** are what starve — they cannot access the processor.

In most cases, true starvation in the strictest sense (complete inability to access the resource)
is relatively rare. While you certainly can implement such scenarios in Cats Effect,
it's usually intuitively undesirable to do so:

```dotty
import cats.effect.*
import cats.syntax.all.*

object StarveThyself extends IOApp.Simple:
  val run = (0 until 100).toList.parTraverse_ { i =>
    IO {
      println(s"running #$i")
      Thread.sleep(10000) // ← Blocking OS thread!
    }
  }
```

This program will very quickly "starve" Cats Effect of access to the compute pool.
You can observe this by the fact that only the first `n` `println` calls will execute,
where `n` is the number of threads in the compute pool (defaulting to the number of physical cores).
In fact, this program _blocks_ all tasks so effectively
that even the **starvation detector itself won't be able to run**
and thus won't be able to detect this scenario!
Such cases are usually obvious and easily diagnosed.

Much more complex (and more common) are situations where tasks are _partially_ starved,
leading to high latencies but not a complete halt.

### Other Types of Starvation

It's important to remember that not only CPU-waiting tasks can starve. For example:

- **Memory (Heap) Starvation:** If a task cannot allocate memory, it will fail with an `OutOfMemoryError`.
- **Stack Starvation:** If a task runs out of stack frames, it will fail with a `StackOverflowError`.
- **External Resources:** If another OS process is leaking file descriptors,
  your process may fail when attempting to open a new connection or write a log.

Moreover, the problem may lie outside your application:

- **JVM Settings:** Too small a heap size will lead to `OutOfMemoryError` even without leaks.
- **OS Contention:** Another process may consume all CPU or all file descriptors.
- **Hardware:** Insufficient number of physical cores.

**Key takeaway:** The only factor precisely measured by the detector is that
your application _is experiencing a shortage of CPU access_.
It cannot determine the root cause of this starvation. Investigation is the developer's responsibility.

## Understanding How the Starvation Checker Works

Before moving on to fixing issues, it's worth understanding the mechanism by which Cats Effect measures this effect.
The code in question is surprisingly simple:

```dotty
import scala.concurrent.duration.*

val starvationThreshold = 0.1.seconds
val sleepInterval       = 1.second

val tick: IO[Unit] =
  IO.monotonic.flatMap { start =>
    // Sleep for a fixed interval
    IO.sleep(sleepInterval) *> IO.monotonic.map(_ - start).flatMap { delta =>
      // delta is the sleep interval + scheduler delay.
      // Delay is the time between requesting CPU access and actually getting it.
      IO.println("Starvation detected!").whenA((delta - sleepInterval) >
        starvationThreshold)
    }
  }

// Start the checker in a background fiber
val checker: IO[Unit] = (IO.sleep(1.second) *> tick.foreverM).start.void
```

**How this works:**

1. The current time is recorded (`start`).
2. The fiber sleeps for `sleepInterval` (default 1 second) via `IO.sleep`.
   This is a **semantic** block, which does not block the OS thread.
3. After waking up, time is measured again (`delta`).
4. The difference `(delta - sleepInterval)` is the **scheduler delay**.
   It shows how much later the fiber was actually woken up after the timer should have fired.
5. If this delay exceeds the threshold (`starvationThreshold`, default 100 ms), a warning is issued.

**Why this matters:** This formulation approximately matches the behavior of **any other async event**.
For example, if a new connection arrives at a server socket,
the time between the OS kernel registering this event and the moment the fiber serving that socket
receives the CPU to process it will approach `delta`.
A high scheduler delay directly means high latency in handling new connections, timers, or upstream responses.

> **Warning:** The starvation checker shows that any delay exceeding the threshold (default 100 ms) is an issue worthy of a warning.
> If you raise the threshold or disable the checker entirely, you are thereby confirming that a delay > 100 ms is acceptable for your application.
> For many applications, this is perfectly fine!
> Think carefully about your SLA agreements.

## Conclusion and Diagnostic Steps

If you encounter starvation warnings:

1. **Assess.** First assess whether this is actually a problem for your SLAs. Delays of 100 ms may be acceptable.
2. [Check for blocking calls](starvation/blocking.md). This is the most common cause.
   Review the code for un-wrapped JDBC calls, file I/O, `Thread.sleep`, `URL#equals`, etc.
3. **Profile CPU.** Run the application under a profiler (JProfiler, YourKit, Async Profiler)
   and see which methods consume the most CPU. If you see GC-related methods — the problem is GC.
   If you see your methods — [the problem is CPU intensity](starvation/intensive-tasks.md).
4. **Enable scheduler logging.** Add `-Dcats.effect.tracing.mode=full` and `-Dcats.effect.logger.enabled=true`
   to JVM parameters for more detailed logging from Cats Effect about fiber behavior.
5. **Check `IO.cede` usage.** Ensure that long computations have yield points.
6. [Limit concurrency](starvation/too-many-threads.md). Use `parTraverseN` instead of `parTraverse` for large collections.
7. [Check the external environment](starvation/others.md). Ensure there are no other "greedy" processes on the server,
   and GC is properly configured (especially for apps with large heaps).
8. [Consider raising the threshold or disabling the checker](starvation/others.md). If delays persist after all efforts
   but are acceptable, you can configure the threshold via `IOApp` or disable the checker entirely.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
