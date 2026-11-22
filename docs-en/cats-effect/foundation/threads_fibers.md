# Threads and Fibers

## The Chasm Between Needs and Capabilities

Imagine you are developing a modern microservice that must handle **10,000 concurrent requests**. 
Each request requires:

- Reading data from a database (10ms)
- Calling an external API (20ms)
- Computing the result (5ms)
- Sending the response (1ms)

**How many threads do you need?**

If you use the traditional "one thread per request" model, that's **10,000 threads**.

But JVM cannot work efficiently with 10,000 threads. 
This is a **fundamental mismatch** between what we need (concurrent actions) 
and what the operating system can provide (limited number of threads).

### The Cost of JVM Threads

#### Memory: the invisible killer

```dotty
// Each JVM thread consumes memory
class ThreadMemory:
  def threadCost(): Unit =
    // Default thread stack: 1 MB
    // 1,000 threads = 1 GB of memory just for stacks!
    // 10,000 threads = 10 GB of memory

    // And this doesn't count:
    // - ThreadLocal variables
    // - Contextual data
    // - JVM overhead (~1-2 KB per thread)
    ()
```

**Reality:**

- 1 thread ≈ 1 MB memory
- 10,000 threads ≈ 10 GB RAM
- 100,000 threads → OutOfMemoryError

#### Context switching: the hidden cost

```dotty
class ContextSwitch:
  def cost(): Unit =
    // Thread context switch: ~1-10 microseconds

    val threads = 10000
    val switchesPerSecond = 100000

    // On 10,000 threads the OS scheduler will
    // switch between them millions of times per second
    // This can consume UP TO 30% of CPU!
    ()
```

#### Garbage collection problems

```dotty
class GCProblems:
  def explain(): Unit = 
    // Each thread has:
    // - Stack (1 MB) -- root for GC
    // - ThreadLocal variables
    // - Object references

    // GC must scan the stacks of ALL threads
    // 10,000 threads → 10 GB of stacks → GC pauses of seconds!
    ()
```

### Scale Mismatch

#### Optimal thread count vs. Actual count

```dotty
trait ConcurrencyParadox:
  // Theory: optimal number of threads
  def optimalThreads(): Int =
    val cpuCores = Runtime.getRuntime.availableProcessors()
    // Usually: CPU cores + 1 or CPU cores * 2
    // For an 8-core server: 8-16 threads
    cpuCores + 1

  // Reality: need to serve concurrently
  def requiredConcurrency(): Int =
    // Web server: number of concurrent requests
    // API gateway: WebSocket connection count
    // Message system: number of subscribers

    100000 // Thousands, tens of thousands

  // Gap: 16 vs 100,000 → 6,250x!
```

#### Example: Traditional Tomcat server

```dotty
class TomcatLimitations:
  def explain(): Unit =
    // Tomcat uses a thread pool
    // maxThreads = 200 (typically)
    // maxConnections = 10,000

    // What happens when 10,000 requests arrive?
    // 200 requests are processed
    // 9,800 wait in the connection queue

    // Wait times can be enormous!
    // At 1 second per request: the last one waits ~49 seconds
    ()
```

**Problem:** We are forced to limit parallelism not due to business requirements, but due to OS and JVM technical constraints.

### Traditional solutions and their problems

#### Solution 1: Thread pool

```dotty
class ThreadPoolSolution:
  private val pool = Executors.newFixedThreadPool(200)

  def handleRequest(request: Request): Future[Response] =
    // Problems:
    // 1. Blocking operations hold threads
    // 2. Need to know in advance how many threads are needed
    // 3. Hard to scale
    // 4. False sharing

    pool.submit(() => process(request))
```

**Why this isn't ideal:**

| Problem        | Description                                                        |
|----------------|--------------------------------------------------------------------|
| **Blocking**   | If 200 threads wait on DB, new requests aren't processed           |
| **Complexity** | Need to tune pool size (too small → queue, too many → GC)          |
| **Latency**    | Request queues create unpredictable latency                        |
| **Resources**  | Each thread consumes memory, even if just waiting                  |

#### Solution 2: Async callbacks

```dotty
class CallbackHell:
  def handleRequest(request: Request, callback: Response => Unit): Unit =
    // "Callback hell" -- a classic problem
    db.query(
      request.id,
      (dbResult, error1) =>
        if error1 != null then
          callback(error1)
        else
          httpClient.call(
            dbResult.url,
            (httpResponse, error2) =>
              if error2 != null then
                callback(error2)
              else
                process(
                  httpResponse,
                  (result, error3) =>
                    // Another level...
                    callback(result)
                )
          )
    )
end CallbackHell
```

**Why this isn't ideal:**

| Problem               | Description                                       |
|-----------------------|---------------------------------------------------|
| **Callback hell**     | Nesting makes code unreadable                     |
| **Non-composable**    | Can't simply chain two callback calls             |
| **Resource management**| Hard to guarantee resource closure                |
| **Errors**            | Easy to forget to handle an error in the callback |

#### Solution 3: CompletableFuture

```dotty
class CompletableFutureExample:
  def handleRequest(request: Request): CompletableFuture[Response] =
    return db.queryAsync(request.id)
      .thenCompose(dbResult -> httpClient.callAsync(dbResult.url))
      .thenApply(httpResponse -> process(httpResponse))
      .exceptionally(error -> handleError(error))
```

**Why it's better, but still not ideal:**

| Problem                    | Description                                         |
|----------------------------|-----------------------------------------------------|
| **Cancellation**           | Hard to cancel a chain of operations                |
| **Resources**              | No automatic resource management                    |
| **Monad composition**      | Doesn't work with for-comprehension                 |
| **Type safety**            | Easy to get ClassCastException                      |

### Fibers as a solution

#### What is a fiber?

```dotty
trait Fiber[F[_], E, A]:
  def cancel: F[Unit]
  def join: F[Outcome[F, E, A]]

// A fiber is a "semantic" execution thread
// A lightweight abstraction that:
// 1. Is not bound to an OS thread
// 2. Can be suspended and resumed
// 3. Is scheduled automatically by the runtime
```

#### Fibers in action

```dotty
import cats.effect.IO

class FiberExample:
  def handleRequest(request: Request): IO[Response] =
    // Fibers are so lightweight, we can create them by the millions!

    IO.defer:
      // Schedule 100,000 parallel operations
      (1 to 100000).toList.parTraverse: id =>
        db.query(id).timeout(30.seconds)
```

#### Comparison: Threads vs Fibers

| Characteristic     | JVM Thread                | Cats Effect Fiber                  |
|--------------------|---------------------------|------------------------------------|
| **Memory**         | ~1 MB                     | ~128 bytes                         |
| **Creation time**  | ~1 ms                     | ~100 ns                            |
| **Max count**      | ~10,000                   | Unlimited (millions)               |
| **Cancellation**   | Complex (interrupt)       | Built-in                           |
| **Switching**      | System call (~1 µs)       | User mode (~10 ns)                 |
| **Scheduling**     | OS                        | User-level (work stealing)         |


### Real-world scaling example

#### Problem: WebSocket server with 1,000,000 connections

```dotty
// ❌ Thread-based solution -- impossible
class ThreadBasedWebSocket:
  // 1,000,000 threads = 1 TB of memory
  def onOpen(session: WebSocketSession): Unit =
    new Thread(() =>
      while session.isOpen do
        val message = session.receive() // Blocking call
        process(message)
    ).start()

// ✅ Fiber-based solution -- works!
import cats.effect.IO
import cats.effect.std.Queue

class FiberBasedWebSocket:
  def onOpen(session: WebSocketSession): IO[Unit] =
    def handleMessages: IO[Unit] =
      for
        message <- session.receiveAsync // Non-blocking
        _       <- process(message)
      yield ()

    // 1,000,000 fibers ≈ 128 MB of memory!
    handleMessages.foreverM.start.void
```

### Fibers and cancellation

#### Cancellation problem in threads

```dotty
// ❌ Java thread cancellation is a nightmare
class ThreadCancellationProblem:
  def downloadFile(url: String): Unit =
    val thread = new Thread(() =>
      val connection = url.openConnection()
      val stream     = connection.getInputStream()
      // How to cancel this?
      // Thread.interrupt() may not work
      // No guarantee that stream will be closed
    )
    thread.start()

    // Attempt to cancel after 5 seconds
    Thread.sleep(5000)
    thread.interrupt() // ??? Does not guarantee stop
```

#### Fibers and cancellation

```dotty
// ✅ Cats Effect fiber cancellation -- guaranteed
import cats.effect.IO
import scala.concurrent.duration.*

class FiberCancellation:
  def downloadFile(url: String): IO[Unit] =
    // A cancellable fiber
    val download = IO {
      val connection = url.openConnection()
      val stream     = connection.getInputStream()
      // Fiber automatically checks for cancellation
      // at each async operation
      readStream(stream)
    }

    // Cancellation is guaranteed to fire after 5 seconds
    download.timeout(5.seconds)
```

### Fibers and composition

#### Composition problem in the async world

```dotty
// Traditional async code can't be simply composed
def traditionalAsync(): Unit =
  // How to combine two async calls?
  asyncCall1((result1, err1) =>
    asyncCall2((result2, err2) => {
      // Already two levels of nesting...
    })
  )

  // How to parallelize two calls?
  asyncCall1(result1 =>
    asyncCall2(result2 => {
      // They run sequentially!
    })
  )
```

### Fibers solve the composition problem

```dotty
// With fibers the code looks synchronous!
def fiberComposition[F[_]: Spawn]: F[Unit] =
  for
    // Parallel execution
    fiber1 <- asyncCall1.start
    fiber2 <- asyncCall2.start

    // Wait for results
    result1 <- fiber1.joinWithNever
    result2 <- fiber2.joinWithNever

    // Compose results
    _ <- process(result1, result2)
  yield ()
```

## Key advantages

1. **Scalability**: Millions of fibers instead of thousands of threads
2. **Efficiency**: Nanosecond switching instead of microseconds
3. **Simplicity**: Code looks synchronous, runs asynchronously
4. **Safety**: Built-in cancellation and resource management
5. **Composability**: Easy to combine and reuse

When to use fibers:

| Scenario                           | Threads            | Fibers                     |
|------------------------------------|--------------------|----------------------------|
| **CPU-intensive tasks**            | ✅ Suitable         | ✅ Suitable                 |
| **IO-intensive tasks**             | ⚠️ Limited         | ✅ Ideal                    |
| **High concurrency (>10,000)**     | ❌ Not suitable     | ✅ Ideal                    |
| **Real-time**                      | ❌ Unpredictable    | ⚠️ Depends on runtime      |
| **Distributed systems**            | ⚠️ Complex         | ✅ Built-in support         |

**Fibers are not just an optimization, but a paradigm shift.** 
They allow writing concurrent code as simply as sequential code, 
but with performance and scalability unattainable for traditional threads.

## Real-world example: fiber-based HTTP server

Next we'll walk through a working example of an **asynchronous HTTP server** built on the concept of "fibers". 

```dotty
import cats.effect.{MonadCancel, Spawn}
import cats.effect.syntax.all.*
import cats.syntax.all.*

// Server and connection abstractions
trait Server[F[_]]:
  def accept: F[Connection[F]]

trait Connection[F[_]]:
  def read: F[Array[Byte]]
  def write(bytes: Array[Byte]): F[Unit]
  def close: F[Unit]

// Application endpoint
def endpoint[F[_]: Spawn](
    server: Server[F]
)(body: Array[Byte] => F[Array[Byte]]): F[Unit] =

  // Handle a single connection
  def handle(conn: Connection[F]): F[Unit] =
    for
      request  <- conn.read
      response <- body(request)
      _        <- conn.write(response)
    yield ()

  // Handler that accepts connections and starts fibers
  val handler = MonadCancel[F].uncancelable: poll =>
    poll(server.accept).flatMap: conn =>
      handle(conn)
        .guarantee(conn.close) // Guarantee closing
        .start                 // Start in a separate fiber!

  // Infinite processing of connections
  handler.foreverM
end endpoint
```

### Why this works: breakdown of the key fragment

```dotty
// Each request gets ITS OWN fiber
val handler = MonadCancel[F].uncancelable: poll =>
  poll(server.accept).flatMap: conn => // Wait for new connection
    handle(conn)
      .guarantee(conn.close) // Guarantee of closing
      .start                 // ← Fiber per request!
```

**What happens step by step:**

1. `server.accept` -- a blocking operation *in terms of effect*, but not blocking the OS thread. 
   It suspends the current fiber until a new TCP connection appears.
2. Once the connection is received, we pass it to `handle`, which reads the request, 
   passes it to business logic (`body`) and sends the response.
3. `.guarantee(conn.close)` -- ensures **resource release** (the connection) on any outcome: success, error, or cancellation.
4. `.start` -- **launches processing in a background fiber** without waiting for it to complete. The current fiber immediately returns to waiting for the next connection.

**Advantages of this approach:**

1. **Scalability** -- millions of concurrent connections (fibers take ~128 bytes vs ~1 MB for OS threads).
2. **Safety** -- `guarantee` closes the connection even on cancellation (e.g., client disconnected or server stopped).
3. **Simplicity** -- the code looks synchronous (sequential `for`), but runs asynchronously and concurrently inside.

### Detailed breakdown section by section (bottom up)

This example has **a lot** going on, but thanks to functional programming capabilities 
we can break it into tiny fragments. Let's start from the very end:

```dotty
handler.foreverM
```

#### What is `foreverM`?

`foreverM` is a method that **repeats an effect infinitely**. It is defined for any `F[A]` and works as follows:

- Executes `handler`.
- When `handler` completes (successfully or with an error), `foreverM` **starts it again**.
- The only way to exit `foreverM` is via a **fatal error** or **external cancellation** (e.g., when shutting down the entire program).

Thus, `handler.foreverM` means: "keep processing incoming connections forever, until the server is stopped".

### Analyzing `handler`

```dotty
val handler = MonadCancel[F].uncancelable: poll =>
  poll(server.accept).flatMap: conn =>
    handle(conn).guarantee(conn.close).start
```

#### Why is `uncancelable` needed?

`uncancelable` is a [protection mechanism](../typeclasses/monadcancel.md) for critical sections against **interruption**. 
`uncancelable` guarantees that the inner block `{ ... }` will not be interrupted from outside. 
Instead, cancellation will be "deferred" until exit from the block.

The `poll` argument is a function that temporarily allows cancellation inside the protected block. 
In this case, `poll(server.accept)` means: "the connection acceptance operation _may_ be cancelled, but everything else is not".

> Summary: `uncancelable` ensures that a connection will either enter `handle` 
> or be closed, but never get stuck in an intermediate state.

#### Breakdown of `poll(server.accept).flatMap`

- `server.accept` -- an effect that returns `Connection[F]`. This may take an indefinite amount of time (waiting for a client).
- `poll(...)` allows cancellation while waiting for the connection. This is important so the server can be stopped even if it's not accepting anything.
- After obtaining `conn`, we are already inside `flatMap` -- here cancellation is blocked (the `uncancelable` applies).

#### `handle(conn).guarantee(conn.close).start`

- `handle(conn)` -- reads the request, invokes business logic, writes the response. This is a **long-running** effect.
- `.guarantee(conn.close)` -- ensures closing the connection **after** `handle` completes, regardless of whether it succeeded or errored. 
  Even if `handle` throws an exception or is cancelled -- `close` will execute.
- `.start` -- the key moment: we **do not wait** for `handle` to complete. Instead, we:
    1. Launch `handle` in a new background fiber.
    2. Get back a `Fiber` object representing this running fiber.
    3. The current fiber (the one executing `handler`) continues and immediately moves to the next iteration of the `foreverM` loop.

Thus, each connection is processed in **its own fiber**, and the main connection-accepting loop never blocks.

### What happens if we don't use `.start`?

If we remove `.start`:

```dotty
val handler = MonadCancel[F].uncancelable: poll =>
  poll(server.accept).flatMap: conn =>
    handle(conn).guarantee(conn.close)  // No .start!
```

The server will **process connections sequentially**:

1. Accept a connection.
2. Read the request.
3. Execute business logic.
4. Send the response.
5. Close the connection.
6. Only then move to the next `accept`.

This is a **sequential server** -- it cannot serve a second client until it finishes the first. 
Obviously, this is not suitable for real-world tasks.

Thus, `.start` is the magic that turns sequential code into concurrent code without changing the logic inside `handle`.

---

**References:**

- [Cats effect documentation][doc]
- [Cats effect MonadCancel](https://typelevel.org/cats-effect/docs/typeclasses/monadcancel)

[doc]: https://typelevel.org/cats-effect/docs/concepts
