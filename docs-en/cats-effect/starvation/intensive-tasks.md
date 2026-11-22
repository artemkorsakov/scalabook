# CPU-Intensive Tasks

## The Problem: Not Only Blocking Causes Starvation

While hard thread blocking is the simplest and most obvious way to "captured" all threads in the compute pool
(as in the `Thread.sleep` example), this is far from the only way.
The following program will similarly deprive your application of compute resources, but without a single blocking call:

```dotty
import cats.effect.*
import cats.syntax.all.*

object StarveThyselfAgain extends IOApp.Simple:
  val run = (0 until 100).toList.parTraverse_ { i =>
    IO.println(s"running #$i") >> IO(while true do {}) // Infinite loop!
  }
```

In this example, just like with `Thread.sleep`, all compute threads will be fully captured.
However, the difference is fundamental: instead of waiting (`BLOCKED`), threads are in `RUNNABLE` state, endlessly executing code.
This is **not blocking**, but **CPU monopolization**.

In real applications, of course, there are no such obvious traps. Instead, problems may look like this:

- A series of cryptographic operations (especially asymmetric cryptography, which is orders of magnitude more expensive than symmetric).
- Intensive numerical computations (simulations, signal processing).
- Processing large collections within a single `IO` step.
- Or something subtly hidden inside an `map` or `flatMap` operation.

The key difference from blocking is that CPU-intensive operations often **have no side effects**.
They are simply pure functions. This is what makes them insidious: there is no explicit `IO.blocking`, no native call, no visible "offense".

## Diagnostics: Finding the "Hungry" Code

Unlike blocking tasks (where a single thread dump suffices), finding CPU-intensive tasks is more complex.
Threads won't be in `BLOCKED` state — they will always be `RUNNABLE`. We need to understand _what is consuming the time_.

### 1. Series of Thread Dumps (Classic)

Take **several thread dumps** (`kill -3 <pid>` or `Ctrl+\`) with a 1-2 second interval.

- **What to look for:** `io-compute-*` threads that are in `RUNNABLE` state in **every dump**
  and **each time** have a stack trace pointing to the same methods in your code.
- **Why this works:** If the same thread consistently executes the same code,
  this is a strong indication that the code runs for a very long time without yielding control.

### 2. Async Profiler (Async Profiler)

A more modern and precise method is using an async profiler
(e.g., `async-profiler`, built into IntelliJ IDEA Ultimate or available as a standalone tool).
It samples call stacks at a high frequency and builds a flame graph.

- **What to look for:** "Thick" bands in the flame graph corresponding to your code running in `io-compute-*` threads.
- **Advantage:** Provides an accurate picture of which code consumes the most CPU, without guesswork.

## Solution: The "Three Pillars" of Fairness

Unlike blocking tasks (where the solution is `IO.blocking`), there is no single simple answer for CPU-intensive tasks.
The problem lies in a fundamental contradiction:

- **Blocking (`BLOCKED`):** A thread progresses **if there is time** (the OS gives it CPU when data is ready).
- **Computing (`RUNNABLE`):** A thread progresses only **if there is a free physical CPU core**.

If all your compute threads are busy with heavy computation,
you **cannot** simply create new threads (as `IO.blocking` does),
because physical resources (CPU cores) are finite.
Additional threads would only worsen competition and the OS scheduler load.

The solution to ensure **fairness** (i.e., application responsiveness)
under high computational load usually consists of three steps, applied sequentially.

### 1. Increasing Step Granularity

The Cats Effect runtime assumes (for scheduling purposes)
that each `IO` step (e.g., `map`, `flatMap`, `delay`) has **approximately equal cost**.
If you have one enormous step, this assumption is violated.

**How to increase granularity:**

Split one heavy step into several smaller ones.

```dotty
// Problematic code: all computations inside a single map
data.map: d =>
  val x = computeX(d) // Long
  val y = computeY(d) // Also long
  x + y
```

**Fix:** Split it into two steps using `flatMap`.

```dotty
// Fixed code: two steps, between which Cats Effect can schedule
data.flatMap: d =>
  IO.pure(computeX(d)).map: x =>  // Step 1
    x + computeY(d)               // Step 2
```

**Warning:** Increasing granularity leads to **increased memory allocation** (more `IO` objects)
and consequently reduced **throughput**.
The sweet spot is a balance: each step should be large enough
that `IO` management overhead does not dominate, but small enough to avoid starvation.

### 2. Adding `IO.cede` (Yielding)

The Cats Effect runtime divides continuous work into **tasks** by default.
One task consists of 1024 `IO` steps (this value is configurable).
This is done to reduce scheduling overhead.

The problem arises when _one_ step (`IO(...)`, `map`) takes longer than the remaining 1023 steps combined.
The solution is to forcibly split this work into separate tasks using `IO.cede`.
`cede` is a cooperative yielding point that tells the scheduler:
"Pause the current task and give others a chance to work." It acts as a **natural task boundary**.

**Main pattern:**

```dotty
val expensiveThing: IO[A] = IO(veryLongComputation())

// Add cede BEFORE and AFTER the expensive operation
val fairThing = IO.cede *> expensiveThing.guarantee(IO.cede)
// or, if expensiveThing can be cancelled:
val fairThing = IO.cede *> expensiveThing <* IO.cede
```

**Diagnosis: Do you need `cede`?**

A simple experiment with `timed` will help you understand how expensive your step is compared to the baseline (`IO.unit`).

```dotty
def isTooExpensive[A](fa: IO[A]): IO[Boolean] =
  IO.unit.timed.flatMap { case (baseline, _) =>
    fa.timed.flatMap { case (cost, _) =>
      IO.println(s"Baseline: $baseline, fa cost: $cost").as(cost >
        baseline * 1024)
    }
  }
```

If `isTooExpensive(expensiveThing)` returns `true`, this is a clear sign that `cede` is needed.

## Fairness vs Throughput

In the world of task scheduling, there is a fundamental contradiction
that underlies any resource allocation decision:
the tension between **fairness** and **throughput**.
Understanding this trade-off is key to building responsive and efficient applications, especially in the Cats Effect runtime.

### Maximum Throughput

**What is it?** This is an approach where the scheduler **never preempts tasks**.
Each task, once it gets access to the CPU, runs to full completion.
Tasks are processed, perhaps in arrival order (FIFO), but without any preemptions.

**What this looks like in code (idealized):**

```dotty
// Imaginary preempt-free scheduler
def processQueue(queue: List[Task]): Unit = queue.foreach(_.run())
```

**Pros:**

- **Maximum efficiency:** No context-switching overhead
  (saving/restoring state, switching between tasks). CPU spends all its time on useful work.
- **Minimum execution time for a fixed set of tasks:**
  If you have exactly 100 tasks and nothing else arrives, this approach will complete them the fastest.

**Cons:**

- **Complete unresponsiveness:** While processing the initial batch of tasks,
  the application _will not respond_ to new events (new network connections, timers, user input).
  It "hangs" until all old tasks are complete.
- **"Performance trap":** At first glance, this seems like the fastest approach.
  But for interactive systems (web servers, GUIs, microservices), it is **unacceptable**.
  A user won't notice that the server processed 100 requests in 100ms if it responded only to the 101st request after 5 seconds.

### Maximum Fairness

**What is it?** This is an approach where the scheduler tries to provide **equal CPU time quanta** to all ready-to-run tasks.
It actively switches between tasks so that none of them "starves."
New tasks get CPU access almost instantly, often at the cost of _suspending_ old tasks.

**What this looks like:**
A time-quantum scheduler (round-robin): each task runs for 1ms, then switches to the next.

**Pros:**

- **Responsiveness:** The application responds to new requests almost instantly. The UI does not "hang."
- **Predictability:** Tail latency (p99, p999) is significantly lower, since no task can monopolize the CPU for long.
  All tasks make progress uniformly.

**Cons:**

- **Reduced overall throughput:** Frequent switching between tasks creates overhead.
  The same fixed set of tasks in a system with fair scheduling will run **longer**
  than in a system optimized for maximum throughput.
- **Less efficient CPU cache usage:** Constant switching causes CPU cache "cooling" (L1/L2), slowing execution.

> **«Fairness is a form of prioritization. You prioritize application responsiveness over raw performance.»**

## Practical Trade-off: Choosing a Strategy

Application and runtime designers (such as Cats Effect) constantly balance between these two extremes.

- **Batch processing:** Processing a terabyte of logs overnight. Here, _throughput_ matters.
  Fairness is not needed, since there are no interactive users waiting for responses.
- **Web server:** Here, _responsiveness_ (small p99) is critical. Users are waiting for their requests.
  Fairness (so that no request "starves") is more important than maximum throughput.

**Cats Effect is default-configured to balance in favor of fairness and responsiveness.**
The work-stealing scheduler and automatic `cede` points are designed to ensure that
even under heavy load, the application remains responsive.
But this may not be enough for extremely heavy tasks.

### Extreme Case: An Indivisible Heavy Task

Now imagine that `expensiveThing` is an **indivisible** CPU-intensive operation, such as signing a JWT,
verifying a complex proof, or running a simulation. It cannot be split into steps.
We added `IO.cede` before and after, but this doesn't solve the problem: while `expensiveThing` runs, it occupies the CPU.

If you have 8 CPU cores and you process 16 concurrent requests, each spawning `expensiveThing`,
then all 8 compute threads will be fully occupied. The application becomes **unresponsive**.
New connections won't be accepted, timers won't fire. **Starvation** will occur.

What to do? The only solution is to **artificially limit the parallelism** of this heavy task.

### Throttling Strategy: Global Semaphore

A semaphore can limit the number of fibers that can simultaneously acquire permission to execute `expensiveThing`.

```dotty
import cats.effect.IO
import cats.effect.std.Semaphore

def handleRequest(throttle: Semaphore[IO], request: Request): IO[Unit] =
  val fairWork = IO.cede *> expensiveThing(request).guarantee(IO.cede)
  throttle.permit.use(_ => fairWork)
```

**Why `throttle.permit.use`?**
It automatically releases the permit after `fairWork` completes, even if an error or cancellation occurs.

**Choose `n = computeWorkerThreadCount / 2`.** This is an empirical rule. It guarantees:

1. Some threads (half) remain available for accepting new connections,
   processing timers, and other "light" tasks.
2. The remaining threads perform heavy work. If there are several of them, they will compete for CPU.

**Consequences:**

- **Increased p50 (median latency):** Requests begin waiting in the semaphore queue.
- **Improved p99 (tail latency):** The server remains responsive,
  and the slowest requests complete significantly faster than in the case where all threads are tied up with heavy work.
  The latency distribution becomes _more predictable_.

In the microservices context, this leads to a trade-off:
you **intentionally** sacrifice peak performance (increasing p50)
to **guarantee** predictable responsiveness and low p99.

### Background Processing Strategy: Bounded Queue

If `expensiveThing` doesn't need to complete _before_ sending a response to the client, the best strategy is to not execute it synchronously at all.
Send the task to a queue and respond to the client immediately.

```dotty
object Backgrounding extends IOApp.Simple:
  val run =
    val rsrc =
      for
        workQueue <- Queue.bounded[IO, IO[Unit]](8096).toResource
        // One background worker
        _ <- workQueue.take.flatMap(_.voidError).foreverM.background
        _ <- acceptRequest.toResource.flatMap(r =>
          handleRequest(workQueue, r).background
        )
      yield ()
    rsrc.useForever

  def handleRequest(queue: Queue[IO, IO[Unit]], r: Request): IO[Unit] =
    val fairWork = IO.cede *> expensiveThing(r).guarantee(IO.cede)
    queue.offer(fairWork) *> respond(r) // Response sent immediately!
```

**Advantages:**

- The client response is not delayed at all (`respond` runs immediately).
- A bounded queue creates backpressure:
  if the queue is full, `queue.offer` will semantically block, slowing down `handleRequest`
  and thereby limiting the rate of accepting new requests.

**Improvement: Multiple Background Workers**

You can launch multiple workers to increase background processing throughput,
but again, not exceeding a reasonable limit.

```dotty
// Launch, for example, half the number of cores as workers
_ <- workQueue.take
  .flatMap(_.voidError)
  .foreverM
  .background
  .replicateA_(computeWorkerThreadCount / 2)
```

This is _application-level_ concurrency that you fully control.

## Conclusion and Next Steps

The choice between fairness and throughput is not a technical problem, but a **business requirement**.
Understanding the trade-off allows you to consciously adjust it.

- **For synchronous heavy APIs:** Use a **semaphore** to limit concurrency, trading `p50` for improved `p99`.
- **For background tasks:** Use a **bounded queue** to decouple processing from client responses and create controlled backpressure.

These patterns are so common that for their efficient implementation, it often makes sense to transition
from low-level Cats Effect primitives to [the Fs2 library](https://fs2.io/#/) (Functional Streams for Scala).
Fs2 provides high-level, composable, and safe abstractions for concurrent stream processing,
including ready-made tools for concurrency limiting (`parEvalMap`), buffering (`buffer`), and queue management.

Using Fs2 allows expressing complex concurrent scenarios more declaratively and with fewer errors
than manual management of semaphores and queues.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
