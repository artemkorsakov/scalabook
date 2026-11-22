# Too Many Threads — The Other Side of the Coin

In a sense, this scenario is the mirror image of the "not enough CPU" problem,
and it is surprisingly common in ordinary JVM applications.
If in the previous section we discussed a shortage of compute resources,
here the topic is their **overload** due to an excessive number of threads.

Consider a typical thread list for an 8-CPU application (see the previous section):

- 8 compute worker threads (`io-compute-*`)
- 1 timer dispatch thread
- 1 async I/O dispatch thread
- **Unlimited number of blocking threads (`io-blocking-*`)**
- 1 JIT thread
- 1+ GC thread
- 1 main thread
- **Undefined number of "unauthorized" threads** (from third-party libraries)

The two bolded items deserve special attention.

1. **Blocking threads (`io-blocking-*`):** Cats Effect uses an _unlimited_ cached pool for operations wrapped in `IO.blocking`.
   This means the number of such threads can theoretically grow into the thousands
   if thousands of fibers simultaneously execute blocking calls.
   While this is better than blocking compute threads, thousands of threads place a huge load on the OS scheduler.
2. **"Unauthorized" threads:** This is a realm outside Cats Effect's control, but one capable of destroying any performance.
   Most JVM libraries, even those written in an async style, often create their own thread pools "just in case."
   AWS SDK v2 is a classic example. The Blaze backend for http4s also creates its own threads.
   The JVM ecosystem in general tends toward excessive thread usage.

The problem with a large number of threads — whether active (executing CPU work)
or inactive (merely existing in memory) — leads to two serious consequences.

### Consequence 1: Time-Slice Granularity

The OS scheduler works on a round-robin principle:
each ready-to-run thread is allocated a small time slice.
If we have **8 CPUs** and **80 active threads**, each thread gets on average
only `1/10` of available CPU time (80 / 8 = 10 threads per core, each with a time-slice 10× smaller).
This means your application's useful work will be constantly interrupted for context-switching between threads,
even if they spend most of their time simply "waiting."

### Consequence 2: Cache Thrashing

This is the most destructive factor. Modern CPUs have a multi-level cache architecture (L1/L2/L3).
Caches store the "working set" of a thread's data for maximum access speed.

When the OS kernel switches from one thread to another on the same physical CPU core,
the incoming thread must **completely flush the L1 and L2 caches** of data used by the previous thread,
and load its own working set. This operation is extremely costly.
With a large number of active threads, the CPU spends more time flushing and loading caches
than executing useful work. This state is called **cache thrashing**.
It can slow down an application by tens or hundreds of times.

## Diagnostics: Active or Inactive?

Before taking any action, you need to understand the nature of the "extra" threads.
**Active** threads are constantly in `RUNNABLE` state and performing real computation.
**Inactive** threads spend most of their time in `BLOCKED`, `WAITING`, or `TIMED_WAITING` state
(e.g., `io-blocking-*`, waiting for data).

- **Inactive threads are bad,** but not catastrophic. They reduce performance through time-slice fragmentation
  and serve as GC roots, but do not cause cache thrashing.
- **Active "foreign" threads are a catastrophe.** They directly compete with your application's compute threads for CPU and cache.

### Diagnostic Tools

1. **Thread dumps (`Ctrl+\` / `kill -3`):** Check the state (`RUNNABLE` vs `BLOCKED`) and stack traces of threads.
   Take several dumps with a 1-2 second interval. A thread that is constantly `RUNNABLE`
   and in every dump is inside the same method — is active.
2. **Async Profiler:** Shows where CPU is spent.
   If you see methods from AWS SDK or another library there, that is an active "foreign" thread.
3. **JMX bean `ComputePoolSamplerMBean`:** Useful for monitoring `getBlockedWorkerThreadCount` — the count of allocated blocking threads.

## Fixing Strategies

### Step 1: Limiting Active Foreign Threads

Your goal is to ensure that the sum of **active "foreign" threads + Cats Effect compute threads** does not exceed the number of CPUs.
Ideally, there should be 0 active foreign threads.

1. **Try merging pools.** If a library creating foreign threads
   provides a way to pass it an `ExecutionContext` (e.g., via configuration), **always** pass `IO.executionContext`.
   This will make it use your compute threads rather than creating its own.
     - _This is safe if the library never blocks on its threads._
     - _For libraries from the Akka ecosystem that wrap blocking in `scala.concurrent.blocking(...)`, this is also safe (analogous to `IO.blocking`)._
2. **Configure internal library pools.** If merging pools is not possible (e.g., the library uses blocking),
   at least try to reduce its pool size via configuration.
   Look for parameters like `thread-pool-size`, `max-concurrency`, `num-threads` in the documentation.
3. **Reduce the number of Cats Effect compute threads.** If you have 8 CPUs, 8 compute threads,
   and 2 active foreign threads that cannot be merged or reduced, you simply have too many threads. You need to make room.

```dotty
object MyApp extends IOApp.Simple:
  // Reduce compute threads by 2 to make room for 2 foreign ones.
  override def computeWorkerThreadCount: Int =
    // At least 2 threads even for a single core, to avoid deadlocks.
    Math.max(2, super.computeWorkerThreadCount - 2)

  val run: IO[Unit] = ???
```

### Step 2: Minimizing Inactive Threads

Inactive threads are mostly `io-blocking-*` (from `IO.blocking`).
Each such thread is allocated memory (stack ~1 MB) and an extra GC root.

- **Logging:** This is the most frequent source of uncontrolled blocking.
  Most logging systems (even slf4j-logback) eventually write to a file (`FileOutputStream`),
  and file I/O is a blocking operation. If you log many messages, you create many small blocking calls,
  leading to the allocation of many `io-blocking-*` threads.

  **Solution:**
    - **Use an async logger:** Configure logback with `AsyncAppender`.
      This is `<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">`,
      which uses a ring buffer and a single background thread.
    - **Use `Queue.circularBuffer`:** As shown in the Backpressure section,
      you can implement async logging on Cats Effect by sending log messages to a queue
      and processing them in a single dedicated blocking thread, dropping old messages on overflow.

- **Limiting `IO.blocking` with a semaphore:** If the source of blocking calls is
  your own code (e.g., queries to an old JDBC database), do not allow "endless" fibers to simultaneously execute `IO.blocking`.
  Assume that each such call will create a new thread in the pool.
  Limit the concurrency of these calls using a `Semaphore`.

```dotty
// Good empirical rule: semaphore limit = computeWorkerThreadCount * 2 or * 8
val blockingSemaphore = Semaphore[IO](computeWorkerThreadCount * 2)

def safeBlocking[A](thunk: => A): IO[A] =
  blockingSemaphore.permit.use(_ => IO.blocking(thunk))
```

## Conclusion

A large number of threads kills Cats Effect performance just as effectively as too few.
The work-stealing scheduler is designed to manage compute threads specifically,
not to compete for CPU with dozens of other threads.

Always start with diagnostics (thread dumps, profiler). Identify active and inactive threads.

1. **Active foreign threads:** Merge pools or configure their size. Reduce `computeWorkerThreadCount` if necessary.
2. **Inactive (blocking) threads:** Switch to async logging.
   Limit `IO.blocking` with semaphores. Use queues to manage bursts of blocking operations.

Only after bringing the total number of active threads into alignment with the number of CPUs
can you get the maximum benefit from Cats Effect.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
