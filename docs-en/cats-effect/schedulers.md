# Schedulers

Since Cats Effect is a library providing its own runtime,
it eventually has to face the non-trivial task of how to **best execute programs**
defined using its specific `IO` implementation.
Fibers are an incredibly powerful model, but they don't map 1:1
or even m:n to any built-in JVM or JavaScript construct.
This means some interpretation and scheduling is necessary.
The way this interpretation is achieved has a profound impact on the performance
and flexibility of programs written using `IO`.

This holds true for both JVM and JavaScript, and while it may seem intuitive
that scheduling in JavaScript would be a simpler task (due to its single-threaded nature),
there are significant nuances that become critically important in real-world applications.

## Scheduling on the JVM

`IO` programs and fibers ultimately execute on JVM threads, which in turn
map to OS kernel threads (which, when scheduled, map to physical CPUs).
Determining the optimal method for mapping a real,
concurrently running application to kernel-level threads is an extremely complex task.

In brief, `IO` uses two main, independent thread pools for program execution:

1. **Work-Stealing Pool for _computations_**:
   the size of this pool is exactly equal to the number of hardware processors (minimum value — 2).
2. **Unbounded Blocking Pool**: by default, it has no dedicated threads.
   Threads are created as needed (with caching and subsequent size reduction)
   to satisfy demand for _blocking_ operations.
   Technically, this pool is built into the work-stealing pool and handled transparently.

**Important:** The _overwhelming majority_ of any program runs in the work-stealing pool.
Moving computations between these pools is transparent to the user,
and in most cases, you don't even need to know about their existence.

### Tokio's Work-Stealing Pool

The work-stealing pool in Cats Effect is an extremely high-performance implementation.
It is based on the Rust framework [Tokio](https://github.com/tokio-rs/tokio),
which, in turn, drew inspiration from early work on the Cats Effect library.

This pool accounts for the specifics of async continuations within a given thread,
as well as common data patterns arising from constructs like `start`.
It allows worker threads to reduce management overhead to nearly zero,
since the threads themselves essentially act as queues of pending tasks.
The scheduler also ensures that, as much as possible,
any particular fiber remains bound to the same OS thread
(though it may share time with other fibers).
This allows the OS to optimize CPU cache usage and avoid expensive rescheduling.

The work-stealing scheduling algorithm
is particularly useful for modern highly-concurrent applications,
as it **eliminates the single point of contention problem**.

**The single-queue problem:** In a simple thread pool implementation (e.g., `FixedThreadPool`),
every task is placed in a single shared queue, and all worker threads poll this queue
to grab the next task (if available).
Such an implementation works reasonably well for a small number of threads (1-10),
but as the number increases (e.g., to 100), contention for queue locks becomes a huge overhead.

**The work-stealing solution:** In the work-stealing implementation,
each worker thread has **its own local queue**.
For the most part, threads work with their own queues, never coordinating with other threads.
This provides an additional benefit of cache optimization and avoiding expensive memory barriers.
When a worker thread clears its queue, it looks for another thread to "steal" tasks from.
This means moving half of the tasks from _another_ worker thread's queue into its own local queue.
This is a small contention point, but only between those two threads (the rest of the pool remains untouched).

**Scalability:** Such a pool can scale vertically to practically any number of worker threads
without increasing overhead. Moreover, the pool becomes **more efficient** as load increases.
When every worker thread is fully saturated (and its own queue is full of tasks),
the time spent "stealing" tasks from others drops to zero.
This eliminates all memory barriers and scheduler contention exactly when
your application needs it most — **under maximum load**.

**Summary:** The `IO` task scheduler using work-stealing provides a **1.5–5x** performance increase
(compared to a fixed-size `ThreadPoolExecutor`) under high load on consumer hardware.
In compute instances with hundreds of cores, the effect will be even more noticeable.
Synthetic benchmarks don't use a number of the most important scheduler optimizations (e.g., fiber-to-thread binding),
so on real workloads, results should be much better.

## Handling Blocking on the JVM

Thread blocking is a serious issue in practically all asynchronous applications, and in fact, in all _JVM_ applications.
This is especially relevant for Cats Effect, given that
the default compute pool uses a **fixed number of threads** (equal to the number of cores).
Blocking one worker thread (e.g., on `Thread.sleep` or, more realistically, on a JDBC call)
will radically impact the throughput of the entire application.
If enough threads become blocked, the application may come to a complete stop.

### Why Blocking Is Inevitable

Blocking operations are practically unavoidable. Partly this is due to JVM architecture specifics,
which predate many modern async concepts.

**Example 1: `java.net.URL`**

The `URL` class performs DNS resolution on creation (yes, including network calls).
The JVM uses an OS library _native call inside `URL#equals` comparison_,
and this call blocks until DNS resolution completes.
This means that every time you call `==` on `URL` (or, worse, use them as `Map` keys),
you block the thread for as long as the DNS server takes to respond.

**Example 2: File I/O**

File systems are another example of very old software transported into the modern world.
File operations are almost universally blocking at the OS level.
Local NTFS mounts on Windows are a notable exception,
but all file system operations on macOS are blocking,
and (practically) almost all file system operations on Linux are blocking too.

### The Solution: `IO.blocking` and `IO.interruptible`

Cats Effect provides special functions to solve this problem:

- **`IO.blocking(thunk)`** and **`Sync[F].blocking(thunk)`**: signal to the runtime that the given effect **will block the thread**.
  `IO` will guaranteed move execution of this `thunk` to a **separate, dedicated blocking pool** of threads.
- **`IO.interruptible(thunk)`** and **`IO.interruptibleMany(thunk)`**: do the same,
  but additionally allow **interrupting** the blocking operation (via `Thread.interrupt()`) on fiber cancellation.

**How this works:**

1. Calling `IO.blocking(expensiveOp)` moves execution of `expensiveOp` from the compute pool to the **blocking pool**.
2. A worker thread in the blocking pool blocks (the OS kernel may remove it from the processor) for the duration of `expensiveOp`.
3. The compute pool (with a fixed number of threads) remains free to process other, non-blocking fibers.
4. When `expensiveOp` completes, the result returns to the compute pool, and execution continues.

**Example:**

```dotty
import cats.effect.IO

// Blocking DNS resolution inside URL#equals
def compareUrls(url1: URL, url2: URL): IO[Boolean] =
  IO.blocking(url1 == url2)
```

This function will not block a precious compute pool thread,
which is critical for application responsiveness and performance.

## Scheduling on JavaScript

Although JavaScript runtimes are single-threaded and therefore
lack the `m:n` mapping complexities (as in the JVM), their extreme heterogeneity creates its own problems.
Moreover, fibers are a form of concurrency,
and emulating multithreading on a single-threaded runtime is a surprisingly complex task.

The ultimate goal is to execute `IO` fibers alongside other async events in the browser (animations)
or on the server (network I/O), without interfering with them and without performance loss.

### Mechanism: Macrotasks

The primary mechanism for implementing fibers in `IO` on JavaScript is **yielding**.
In JavaScript terms, this means the ability to submit a callback to the event loop,
which will be invoked in the future when the event loop is ready.
Essentially, this is submitting a callback that "moves to the end of the queue",
allowing everything else to execute before it runs again.
This mechanism maps precisely to the `IO.cede` effect.

**Implementation:**

With `IO.cede`, fibers are scheduled in turn. Each fiber executes until
it reaches a "yield point" (usually an async call or `cede` itself),
after which it returns control to the event loop, allowing other fibers to execute.
Eventually, these fibers will return to the queue, and the process repeats.
Nothing actually runs _concurrently in parallel_,
but concurrency is expressed semantically, allowing libraries and app code
to be fully cross-compiled between JVM and JS despite platform differences.

However, the challenge is implementing this yield in a way that is **high-performance**,
compatible with **all JavaScript environments**, and non-interfering with **other JavaScript features** (e.g., DOM event handling).

For this purpose, the **[scala-js-macrotask-executor](https://github.com/scala-js/scala-js-macrotask-executor)** project was created.
It uses **macrotasks**, not microtasks (like `Promise`), ensuring proper integration with the event loop.
Optimal performance is achieved in environments supporting:

- **NodeJS (0.9.1+):** via `setImmediate`.
- **Browsers:** via `window.postMessage()` (Chrome 1+, IE 9+, Edge, Firefox 3+, Opera 9.5+, Safari 4+).
- **Web workers:** via `MessageChannel`.

In most cases, you don't need to worry about these details.
Just understand that Cats Effect `IO` schedules fibers in a way
that plays well with other async events, avoiding overhead.

## Scheduling on Scala Native

The [Scala Native](https://github.com/scala-native/scala-native) runtime is currently **single-threaded**
(see [documentation](https://scala-native.org/en/latest/user/lang.html#multithreading)), similar to Scala.js.
Because of this, `IOApp` for Native cannot use `unsafeRunSync` and has other specifics.

**Important:** Be careful with `IO.blocking(...)`. Since there's no dedicated blocking thread pool,
`blocking` on Native will block the single thread, which is highly undesirable.

For more detailed information about the Native runtime internals and its Cats Effect integration,
it is recommended to read the [Typelevel blog post](https://typelevel.org/blog/2022/09/19/typelevel-native.html#how-does-it-work).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Schedulers](https://typelevel.org/cats-effect/docs/schedulers)

[doc]: https://typelevel.org/cats-effect/docs/concepts
