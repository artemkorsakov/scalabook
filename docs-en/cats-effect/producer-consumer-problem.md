# The Producer-Consumer Problem — Concurrency and Fibers

The producer-consumer pattern is one of the cornerstones of concurrent programming.
It arises whenever one part of a system generates data and another part processes it.

**Classic examples:**

- Web server: the producer (thread pool accepting connections) pushes HTTP requests into a queue.
  Consumers (workers) pull them and process them.
- Logger: the producer (application) pushes log messages into a log queue.
  The consumer (writing thread) periodically flushes them to disk.
- File parser: the producer reads lines from a file, the consumer parses them into objects.

**Key aspects of the problem:**

- **Asynchrony:** The producer and consumer run in parallel execution streams.
- **Different speeds:** The producer may generate data faster than the consumer can process it (or vice versa).
- **Coordination:** If the queue is empty, consumers must **block** and wait for data.
  If the queue is bounded and full, producers must **block**, waiting for space.
- **Safety:** Only one producer should add an element to the queue at a time.
  Only one consumer should extract an element at a time (so two consumers don't read the same element).

These requirements map perfectly to Cats Effect capabilities: lightweight fibers and concurrent data structures (especially `Queue`).

## Introduction to Fibers

### What Is a Fiber?

A [fiber](foundation/fibers.md) is the fundamental building block of concurrent execution in Cats Effect.
It's easiest to think of it as a **"lightweight thread"**:

- A fiber encapsulates an `F[A]` action (usually `IO[A]`) that it must execute.
- Fibers are created and managed by Cats Effect without OS intervention.

```dotty
import cats.effect.{IO, IOApp}

object FiberExample extends IOApp.Simple:
  def run: IO[Unit] =
    for
      fiber <- IO.println("Hello from a background fiber!").start
      // The main fiber continues working, not waiting for the background
      _ <- IO.println("The main fiber is running...")
      _ <- fiber.join // Wait for the background fiber to complete (optional)
    yield ()
    
// The main fiber is running...
// Hello from a background fiber! 
```

Let's return to the producer-consumer pattern.

### Simple Model with an Unbounded Queue

Suppose we have one producer, one consumer, and an unbounded queue.

```dotty
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}

object UnboundedProducerConsumer extends IOApp.Simple:
  def run: IO[Unit] =
    for
      queue <- Queue.unbounded[IO, String]

      producer =
        for
          _ <- IO.println("Producer: generating data...")
          _ <- queue.offer("result")
        yield ()

      consumer =
        for
          elem <- queue.take
          _    <- IO.println(s"Consumer: received '$elem'")
        yield ()

      // Run the producer and consumer in parallel fibers
      producerFiber <- producer.start
      consumerFiber <- consumer.start

      _ <- producerFiber.join
      _ <- consumerFiber.join
    yield ()
```

**What's happening here:**

1. An [unbounded queue](std/queue.md) is created.
2. `producer` and `consumer` are effect descriptions.
3. `.start` turns each description into a fiber that starts executing in parallel.
4. `queue.offer` (producer) never blocks (the queue is unbounded).
5. `queue.take` (consumer) blocks the fiber if the queue is empty — but the OS thread is NOT blocked!
   As soon as an element appears, the fiber is automatically woken up.

### Increasing the Complexity: Multiple Producers and Consumers

It's easy to scale to multiple producers and consumers by using `.start` for each.

```dotty
val producers = List.fill(10)(producer.start)
val consumers = List.fill(5)(consumer.start)

(producers ++ consumers).parSequence.void
```

Cats Effect guarantees that `Queue` operations are thread-safe:
concurrent `offer` calls from different producers will not corrupt the queue's internal state.

### Bounded Queue

If we limit the queue size (e.g., to prevent memory overflow), then producers must also be able to block.

```dotty
import cats.effect.std.Queue

for
  queue <- Queue.bounded[IO, String](10)  // max 10 elements
  // ... producers and consumers
yield ()
```

**Key difference:** If the queue is full, `queue.offer` blocks the **producer's fiber** until
the consumer removes an element and frees space.

## Coordinating Fibers with `Deferred` and `Ref`

In the simple case, `Queue` already provides safe coordination. But in more complex scenarios, other primitives may be needed:

- [Ref](typeclasses/concurrent/ref.md) — an atomic reference for shared state.
- [Deferred](typeclasses/concurrent/deferred.md) — a one-shot signal.
- [Semaphore](std/semaphore.md) — a counting semaphore for concurrency control.
- [Mutex](std/mutex.md) — mutual exclusion for fibers.
- [Backpressure](std/backpressure.md) — flow control under overload.
- [CountDownLatch](std/countdown-latch.md) — waiting for a given number of events.
- [CyclicBarrier](std/cyclic-barrier.md) — a multi-use synchronization barrier.

> Cats Effect allows modeling complex concurrent scenarios (like the producer-consumer problem)
> at a high level of abstraction, without worrying about low-level thread management and locks.
> Fibers provide resource-efficient concurrency,
> and synchronization through `Queue`, `Ref`, `Deferred` remains pure and composable.

## First (and Inefficient) Implementation

We need an intermediate structure where the producer(s) can insert data and the consumer(s) can extract it.
Let's assume a simple Queue. Initially, there will be only one producer and one consumer.
The producer will generate a sequence of integers (`1`, `2`, `3`...), and the consumer will simply read this sequence.
Our shared queue will be an instance of the immutable `Queue[Int]`.
This is a good choice because immutable data structures are safe for concurrent sharing —
the only issue is the atomicity of updating the reference to the current queue.

Access to the queue will be (and is!) concurrent, so we need a way to protect the queue
so that only one stream processes it at a time.
A good way to ensure ordered access to shared data is [`Ref`](typeclasses/concurrent/ref.md).
A `Ref` instance wraps given data and provides methods to manipulate this data in a safe way.

The object `Ref` wrapping our queue will be of type `Ref[F, Queue[Int]]` (for some `F[_]`).

### Producer Implementation (`producer`)

The producer will generate a sequence of integers and add them to the queue.
Every 10,000 elements, it will log a message so we know it's still alive.

```dotty
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

def producer[F[_]: {Sync, Console}](
    queueR: Ref[F, Queue[Int]],
    counter: Int
): F[Unit] =
  for
    // Log every 10000 elements
    _ <- Sync[F].whenA(counter % 10000 == 0)(
      Console[F].println(s"Produced $counter elements")
    )
    // Atomically update the queue: add the next element
    _ <- queueR.getAndUpdate(_.enqueue(counter + 1))
    // Recursive call for the next element
    _ <- producer(queueR, counter + 1)
  yield ()
```

**Code breakdown:**

1. **`whenA`** — conditional execution of an effect. If the condition is true, the effect executes; if false, returns `unit`.
   This is the idiomatic alternative to `if (...) then ... else Sync[F].unit`.
2. **`Console[F]`** — a type class providing [console methods](std/console.md) (`println`, `readLine`, etc.).
   `IO.println` simply calls `Console[IO].println` internally.
3. **`getAndUpdate`** — an atomic `Ref` operation that reads the current value, applies a function to it,
   and writes the result back. Here we take the current queue (`queue`),
   add a new value at the end (`counter + 1`), and store the updated queue.
   If another stream accesses `queueR` concurrently, it will be **semantically blocked** until this operation completes.
   Note: the **fiber** is blocked, not the OS thread.

### Consumer Implementation (`consumer`)

The consumer needs to extract elements from the queue. But the queue may be empty!
It needs to handle this correctly by returning `None`,
and continue trying later (unlike blocking, as in a real concurrent queue).

```dotty
def consumer[F[_]: {Sync, Console}](
    queueR: Ref[F, Queue[Int]]
): F[Unit] =
  for
    maybeI <- queueR.modify { queue =>
      queue.dequeueOption match
        case Some((i, newQueue)) => (newQueue, Some(i))
        case None                => (queue, None)
    }
    _ <- Sync[F].whenA(maybeI.exists(_ % 10000 == 0))(
      Console[F].println(s"Consumed ${maybeI.get} elements")
    )
    _ <- consumer(queueR)
  yield ()
```

**Code breakdown:**

1. **`modify`** — an atomic `Ref` operation that allows simultaneously reading the current value,
   computing the new state, and returning an arbitrary result. This is one step — no races between read and write.
2. **`dequeueOption`** — a method of the immutable queue that returns `Option[(Int, Queue[Int])]`.
   If the queue is not empty, returns `Some((head, tail))`, otherwise `None`.
3. On success: the new state is `newQueue` (tail), and the return value is `Some(i)` (head).
   If the queue is empty: the state doesn't change, the return value is `None`.
4. Then we log every 10,000 consumed elements. Note the check: `maybeI.exists(_ % 10000 == 0)`.
5. Finally, recursively call `consumer` again to process the next element.

Seems logical. The producer adds elements, the consumer pulls them. Let's see how to wire this all together.

### Running: Producer and Consumer in Parallel

We want to start the producer and consumer simultaneously and let them run
until the user interrupts the program (e.g., Ctrl+C).
We have two main ways to do this: **high-level** (`parMapN`) and **low-level** (manual fiber management).
Cats Effect recommends the high-level approach.

#### Solution 1: Using `parMapN` (Recommended)

```dotty
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

object InefficientProducerConsumer extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      result <- (consumer(queueR), producer(queueR, 0))
        .parMapN((_, _) => ExitCode.Success)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error: ${t.getMessage}").as(ExitCode.Error)
        }
    yield result

  private def producer[F[_]: {Sync, Console}](
      queueR: Ref[F, Queue[Int]],
      counter: Int
  ): F[Unit] = ??? // as above

  private def consumer[F[_]: {Sync, Console}](
      queueR: Ref[F, Queue[Int]]
  ): F[Unit] = ??? // as above
```

**What `parMapN` does:**

- Starts both effects (`consumer` and `producer`) in separate fibers.
- If both complete successfully (which will never happen in our program — they're infinite), applies the function and passes the result forward.
- If **any** effect completes with an error, the error is passed to `handleErrorWith`,
  and the second effect (still running) is **automatically cancelled**.
- If the user presses Ctrl+C, `IOApp` sends a cancellation signal that correctly stops both fibers.

#### Solution 2: Manual Fiber Management (Not Recommended, But Useful for Understanding)

As an alternative, we could use `start` to explicitly create fibers:

```dotty
override def run(args: List[String]): IO[ExitCode] =
  for
    queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
    producerFiber <- producer(queueR, 0).start
    consumerFiber <- consumer(queueR).start
    _ <- producerFiber.join
    _ <- consumerFiber.join
  yield ExitCode.Success
```

**Why this is bad in practice:**

1. **Errors are ignored.** `join` returns `Outcome[F, E, A]`, not just `A`.
   If a fiber completes with an error, `join` does **not** throw an exception. You need to manually analyze `Outcome`.
2. **An error in one fiber doesn't cancel the other.** If `consumer` crashes with an error,
   `producer` will continue running infinitely, consuming resources.
3. **Blocking `join`.** Calling `producerFiber.join` blocks the current fiber until `producer` completes.
   But `producer` never completes (infinite loop). As a result, `consumerFiber.join` is never reached,
   and the program cannot shut down correctly or handle errors.
4. **Error observation delayed.** If `consumerFiber` completes with an error, we won't see it
   until `producerFiber` completes. And it never will.

**Summary:** Manual fiber management is treacherous and error-prone.
Cats Effect provides high-level combinator (`parMapN`, `parSequence`, `race`)
that correctly handle cancellation and error propagation. Use them whenever possible.

## Why This Implementation Is Inefficient

It seems the code works. The producer generates numbers, the consumer reads them.
But this is an **extremely inefficient solution** for a real task. Let's figure out why.

### Problem 1: Constant Locking on `Ref`

Each producer operation (`getAndUpdate`) and consumer operation (`modify`) requires exclusive access to `Ref`.
This means that **at any given moment, only one fiber** (producer OR consumer) can work with the queue.

- While the producer adds an element, the consumer is forced to wait.
- While the consumer extracts an element, the producer is forced to wait.

In effect, the producer and consumer are working **sequentially**, not **in parallel**!
This defeats the entire purpose of multithreading.

### Problem 2: Busy-Waiting on an Empty Queue

What happens when the queue is empty?

```dotty
// consumer
val maybeI <- queueR.modify { queue =>
  queue.dequeueOption match
    case Some((i, newQueue)) => (newQueue, Some(i))
    case None                => (queue, None)   // ← Queue is empty, but we don't block!
}
```

If the queue is empty, `modify` still executes, returning `None`.
Then `consumer` **immediately** recursively calls itself, and the loop repeats.
This is an **infinite loop at maximum speed** that:

- Consumes 100% of one CPU core.
- Constantly locks `Ref`, preventing the producer from adding even one element.
- In reality, the producer will likely **never get access** to `Ref`, since the consumer holds the lock continuously.

This is classic **busy-waiting** — active polling, which is an antipattern in asynchronous programming.
A real queue should block the consumer **until data appears**, without consuming CPU.

### Problem 3: No Bounds

Our queue has no size limit. If the producer works faster than the consumer,
the queue will grow infinitely, which will eventually lead to an `OutOfMemoryError`.

### Problem 4: No Safe Shutdown

The current implementation has no way to "gracefully" stop the producer and consumer. They run forever.
Relying solely on Ctrl+C is not always acceptable.
In a real system, there needs to be a mechanism to signal completion (e.g., via `Deferred`).

### What's Next?

We got a first, working, but extremely inefficient implementation. This was necessary to understand:

- Correct structure (producer, consumer, shared queue).
- Synchronization mechanisms (`Ref`).
- Ways to launch parallel fibers (`parMapN` vs manual `start`/`join`).

But this code **should not be used in real projects**!

## A More Robust Producer/Consumer Implementation

In the previous inefficient implementation, we faced two fundamental problems:

1. **Busy-waiting:** The consumer constantly polled the queue in a loop, even when empty,
   wasting CPU and contending for `Ref`.
2. **Sequential execution:** Due to constant `Ref` contention, the producer and consumer hardly ran in parallel.

A real concurrent queue should **semantically block the consumer** when the queue is empty,
and **block the producer** when the queue is bounded and full.
The blocking should not affect OS threads — this is one of the superpowers of Cats Effect fibers.

To implement such blocking, we need another primitive — [Deferred](typeclasses/concurrent/deferred.md).

### New State Model

In our first implementation, the state contained only the queue of produced but not yet consumed elements.
Now we also need to track **waiting consumers** (`takers`) — fibers that came for an element,
found the queue empty, and are now waiting on `Deferred`.

```dotty
import cats.effect.Deferred
import scala.collection.immutable.Queue

case class State[F[_], A](
  queue: Queue[A],              // elements waiting to be processed
  takers: Queue[Deferred[F, A]] // consumers waiting for elements
)
```

**Logic of operation:**

- **Consumer (`take`):**
   1. If there are elements in `queue` → extract the first one, return it.
   2. If the queue is empty → create a new `Deferred`, add it to the `takers` queue, and call `taker.get` (block).
- **Producer (`offer`):**
   1. If there are waiting consumers in `takers` → take the first one and "wake it up" via `taker.complete(i)`, passing the new element.
   2. If there are no waiting consumers → add the new element to `queue`.

This scheme guarantees that:

- The consumer doesn't waste CPU — it blocks and waits.
- The producer can directly pass an element to a waiting consumer, bypassing the queue.
- State access is always atomic via `Ref.modify`.

### Consumer Implementation (`consumer`)

The consumer now works through a helper function `take`,
which encapsulates the logic of extracting an element from the shared state.

```dotty
def consumer[F[_]: {Async, Console}](
    id: Int,
    stateR: Ref[F, State[F, Int]]
): F[Unit] =
  // Function that atomically extracts an element from the state
  def take: F[Int] =
    Deferred[F, Int].flatMap: taker =>
      stateR.modify {
        // Case 1: There are elements in the queue
        case State(queue, takers) if queue.nonEmpty =>
          val (i, rest) = queue.dequeue
          State(rest, takers) -> Async[F].pure(i)

        // Case 2: Queue is empty — register a waiting consumer
        case State(queue, takers) =>
          State(queue, takers.enqueue(taker)) -> taker.get
      }.flatten

  for
    i <- take
    _ <- Async[F].whenA(i % 10000 == 0)(
      Console[F].println(s"Consumer $id processed $i elements")
    )
    _ <- consumer(id, stateR) // recursively continue consuming
  yield ()
end consumer
```

#### Detailed Breakdown of `take`

1. **Creating `Deferred`:** `Deferred[F, Int].flatMap { taker => ... }` — each `take` call creates its own `Deferred`,
   which will be used if the queue is empty.
2. **`stateR.modify`** — atomic read and update of state.
3. **First case (`queue.nonEmpty`):** If there are elements — extract the head (`i`) and tail (`rest`).
   New state: `State(rest, takers)`. Return `Async[F].pure(i)` — the element is already there, no need to block.
4. **Second case (`queue.isEmpty`):** If the queue is empty, add the created `taker` to the `takers` queue.
   New state: `State(queue, takers.enqueue(taker))`. Return `taker.get` — this is the blocking.
   The fiber will suspend until some producer calls `taker.complete(i)`.
5. **`.flatten`** — since `modify` returns `F[F[Int]]` (outer layer — the state change effect itself,
   inner layer — the result from the selected branch), `flatten` combines them into a single `F[Int]`.

### Producer Implementation (`producer`)

The producer also gets a helper function `offer`, which atomically decides
where to direct the new element: to a waiting consumer or to the queue.

```dotty
def producer[F[_]: {Async, Console}](
    id: Int,
    counterR: Ref[F, Int],
    stateR: Ref[F, State[F, Int]]
): F[Unit] =

  // Function that atomically adds an element to the state
  def offer(i: Int): F[Unit] =
    stateR.modify {
      // Case 1: There are waiting consumers
      case State(queue, takers) if takers.nonEmpty =>
        val (taker, rest) = takers.dequeue
        State(queue, rest) -> taker.complete(i).void

      // Case 2: No waiting consumers
      case State(queue, takers) =>
        State(queue.enqueue(i), takers) -> Async[F].unit
    }.flatten

  for
    // Get the current counter and increment
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- Async[F].whenA(i % 100000 == 0)(
      Console[F].println(s"Producer $id produced $i elements")
    )
    // Small delay for balancing
    _ <- Async[F].sleep(1.microsecond)
    // Recursively continue producing
    _ <- producer(id, counterR, stateR)
  yield ()
```

#### Detailed Breakdown of `offer`

1. **`stateR.modify`** — atomically read and update state.
2. **First case (`takers.nonEmpty`):** If there are waiting consumers, take the first from the queue (`taker`),
   remove it from the queue (`rest`).
   Return `taker.complete(i).void` — this action wakes up the consumer, passing it element `i`.
   Note: the element **does not enter** the `queue`!
   A direct transfer from producer to consumer occurs.
3. **Second case (`takers.isEmpty`):** If there are no waiting consumers, add the element to the end of `queue`.
   Return `unit` — no additional action needed.
4. **`.flatten`** — like the consumer, combines effect layers.

#### Artificial Delay

Note the line:

```dotty
_ <- Async[F].sleep(1.microsecond)
```

This is **not** part of the solution, but an artificial delay to prevent the producer from overwhelming consumers in a demo.
In a real application, it's not needed — if consumers keep up, no delay is needed;
if they don't, the queue will fill up, and producers should block (we'll look at this later, with a bounded queue).

### Running: Multiple Producers and Consumers

Now we can easily launch multiple producers and multiple consumers in parallel.
Each will run in its own fiber, and they will all share the same `stateR` state.

```dotty
import cats.effect.*
import cats.effect.std.Console
import cats.instances.list.*
import cats.syntax.all.*

import scala.collection.immutable.Queue

object ProducerConsumer extends IOApp:

  // State (as defined above)
    case class State[F[_], A](
      queue: Queue[A],              // elements waiting to be processed
      takers: Queue[Deferred[F, A]] // consumers waiting for elements
  )
  object State:
    def empty[F[_], A]: State[F, A] = State(Queue.empty, Queue.empty)

  override def run(args: List[String]): IO[ExitCode] =
    for
      stateR   <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int])
      counterR <- Ref.of[IO, Int](1) // shared counter for all producers

      // 10 producers and 10 consumers
      producers = List.range(1, 11).map(producer(_, counterR, stateR))
      consumers = List.range(1, 11).map(consumer(_, stateR))

      res <- (producers ++ consumers)
        .parSequence
        .as(ExitCode.Success)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error: ${t.getMessage}").as(ExitCode.Error)
        }
    yield res
    
  // consumer and producer defined above  
```

**What's happening here:**

1. `List.range(1, 11).map(producer(_, counterR, stateR))` creates a **list of descriptions** for 10 producers.
   The producers themselves are not yet running — these are just `IO[Unit]`.
2. Same for consumers.
3. `(producers ++ consumers).parSequence` — `parSequence` runs **all** items in the list (all 20 fibers) in parallel.
   It returns control only when **all** of them have completed.
   But our producers and consumers run infinitely (recursive calls).
   So the program will run until the user interrupts it (Ctrl+C).
4. `as(ExitCode.Success)` — when `parSequence` completes (which won't happen during normal operation), return `ExitCode.Success`.
5. `handleErrorWith` — if any of the 20 fibers encounters an error, it will be caught here,
   and **all other fibers will be automatically cancelled**.

### Analysis: Why This Solution Is Efficient

| Feature                         | First Implementation (Inefficient)                           | Second Implementation (with `Deferred`)                                   |
|:--------------------------------|:-------------------------------------------------------------|:---------------------------------------------------------------------------|
| **Behavior on empty queue**     | Busy-waiting, 100% CPU                                       | Semantic blocking, 0% CPU                                                 |
| **Contending for `Ref`**        | High (every operation requires locking)                      | Low (`modify` runs only on actual state changes)                          |
| **Direct transfer**             | No (always via queue)                                        | Yes (producer → consumer, bypassing the queue)                            |
| **Scalability**                 | Poor (producer and consumer are nearly sequential)           | Excellent (multiple producers and consumers run in parallel)              |
| **CPU usage**                   | High (constant loops and locks)                              | Low (fibers sleep when there's nothing to do)                             |

## Producer-Consumer with a Bounded Queue

In the unbounded queue implementation, we have a problem:
if producers consistently outpace consumers,
the `queue` will grow infinitely, eventually leading to an `OutOfMemoryError`.
This is unacceptable for any serious system running 24/7.

**Solution:** Introduce a **queue size limit** (`capacity`). Now:

- If the queue is **not full**, the producer can add an element.
- If the queue is **full**, the producer must **block** (semantically) and wait for the consumer to free space.

For this, we need to track not only waiting consumers (`takers`) but also waiting producers (`offerers`).
Let's add them to our state.

### Extended State Model

```dotty
case class State[F[_], A](
    queue: Queue[A],                        // elements waiting to be processed
    capacity: Int,                          // maximum queue size
    takers: Queue[Deferred[F, A]],          // consumers waiting for an element
    offerers: Queue[(A, Deferred[F, Unit])] // producers waiting for space
)
```

**What's stored in `offerers`:**

- Each item in the `offerers` queue is a pair `(element, deferred)`.
- `deferred` is of type `Deferred[F, Unit]` — this is the channel on which the producer will block.
- When space becomes available, we'll extract this producer,
  put its element into the queue (or pass it directly to a consumer), and complete the `deferred`, unblocking the producer.

**Symmetry:** Note the symmetry between `takers` and `offerers`:

- `takers` (waiting consumers): wait on `Deferred[F, A]` — they need an element of type `A`.
- `offerers` (waiting producers): wait on `Deferred[F, Unit]` — they just need a "space available" signal; they already have the element.

### Consumer Implementation with a Bounded Queue

Now the consumer has **four possible scenarios** when extracting an element from the state,
depending on the combination of "is the queue empty" and "are there waiting producers."

#### Breakdown of All Four Scenarios

| Scenario | `queue` | `offerers` | Action                                                                                                                                                          |
|:---------|:--------|:-----------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **1**    | Not empty | Empty     | Simply extract an element from `queue` and return it.                                                                                                           |
| **2**    | Not empty | Not empty | Extract an element from `queue` (for the consumer) **AND** wake up the first waiting producer by adding its element to the end of `queue`.                    |
| **3**    | Empty   | Not empty | No elements in `queue`, but there are producers waiting. Take the first producer, return its element to the consumer (bypassing the queue), wake up the producer.|
| **4**    | Empty   | Empty     | No elements and no waiting producers. Create a new `taker`, add to the waiting queue, and block the consumer.                                                   |

```dotty
def consumer[F[_]: {Async, Console}](
    id: Int,
    stateR: Ref[F, State[F, Int]]
): F[Unit] =
  val take: F[Int] =
    Deferred[F, Int].flatMap { taker =>
      stateR.modify {
        // Scenario 1: Queue not empty, no waiting producers
        case State(queue, capacity, takers, offerers)
            if queue.nonEmpty && offerers.isEmpty =>
          val (i, rest) = queue.dequeue
          State(rest, capacity, takers, offerers) -> Async[F].pure(i)

        // Scenario 2: Queue not empty, there are waiting producers
        case State(queue, capacity, takers, offerers)
            if queue.nonEmpty =>
          val (i, rest)               = queue.dequeue
          val ((move, release), tail) = offerers.dequeue
          // Add the element from the woken producer to the queue
          State(rest.enqueue(move), capacity, takers, tail) ->
            release.complete(()).as(i)

        // Scenario 3: Queue empty, but there are waiting producers
        case State(queue, capacity, takers, offerers)
            if offerers.nonEmpty =>
          val ((i, release), rest) = offerers.dequeue
          State(queue, capacity, takers, rest) ->
            release.complete(()).as(i)

        // Scenario 4: Queue empty and no waiting producers
        case State(queue, capacity, takers, offerers) =>
          State(queue, capacity, takers.enqueue(taker), offerers) -> taker.get
      }.flatten
    }

  for
    i <- take
    _ <- Async[F].whenA(i % 100000 == 0)(
      Console[F].println(s"Consumer $id processed $i elements")
    )
    _ <- consumer(id, stateR)
  yield ()
end consumer
```

#### Detailed Breakdown of Complex Scenarios

**Scenario 2 (`queue.nonEmpty && offerers.nonEmpty`):**

```dotty
val (i, rest) = queue.dequeue           // i — for the current consumer
val ((move, release), tail) = offerers.dequeue  // move — element from the waiting producer
State(rest.enqueue(move), capacity, takers, tail) -> release.complete(()).as(i)
```

- The consumer gets element `i` from the head of the queue.
- We wake up the first waiting producer (`release.complete(())`).
- The element `move` from this producer is **placed at the end of the queue**, not passed directly to the consumer.
  Why? Because we already have an element for the current consumer;
  the transfer must preserve order (FIFO) for elements passing through the queue.
- Note the `.as(i)` — after `release.complete(())` executes, we return `i` to the consumer.

**Scenario 3 (`offerers.nonEmpty`):**

```dotty
val ((i, release), rest) = offerers.dequeue
State(queue, capacity, takers, rest) -> release.complete(()).as(i)
```

- The queue is empty, but there are producers waiting.
- We take the first one, return its element `i` **directly** to the consumer (bypassing the queue).
- Simultaneously, we wake up the producer (`release.complete(())`).
- This is optimal — no need to put the element in the queue just to immediately extract it.

### Producer Implementation with a Bounded Queue

The producer now has **three scenarios:**

| Scenario | Condition                                                                            | Action                                                                                             |
|:---------|:-------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------|
| **1**    | There are waiting consumers (`takers.nonEmpty`)                                      | Pass the element directly to the first consumer, bypassing the queue.                              |
| **2**    | No waiting consumers, and queue is **not full** (`queue.size < capacity`)            | Add the element to the end of the queue.                                                           |
| **3**    | No waiting consumers, and queue is **full**                                          | Create an `offerer` (with `Deferred[F, Unit]`) and block until space becomes available.            |

```dotty
def producer[F[_]: {Async, Console}](
    id: Int,
    counterR: Ref[F, Int],
    stateR: Ref[F, State[F, Int]]
): F[Unit] =
  def offer(i: Int): F[Unit] =
    Deferred[F, Unit].flatMap { offerer =>
      stateR.modify {
        // Scenario 1: There are waiting consumers
        case State(queue, capacity, takers, offerers)
            if takers.nonEmpty =>
          val (taker, rest) = takers.dequeue
          State(queue, capacity, rest, offerers) ->
            taker.complete(i).void

        // Scenario 2: No waiting consumers, queue not full
        case State(queue, capacity, takers, offerers)
            if queue.size < capacity =>
          State(queue.enqueue(i), capacity, takers, offerers) ->
            Async[F].unit

        // Scenario 3: No waiting consumers, queue full
        case State(queue, capacity, takers, offerers) =>
          State(queue, capacity, takers, offerers.enqueue((i, offerer))) ->
            offerer.get
      }.flatten
    }

  for
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- Async[F].whenA(i % 100000 == 0)(
      Console[F].println(s"Producer $id produced $i elements")
    )
    _ <- producer(id, counterR, stateR)
  yield ()
end producer
```

**Important changes compared to the unbounded version:**

1. **We removed the `Async[F].sleep(1.microsecond)` call.** The artificial delay is no longer needed,
   because now when the queue is full, producers will **block automatically**, waiting for space to free up.
   This is a natural backpressure mechanism.
2. In Scenario 3, the producer creates an `offerer` — a pair of its element and a `Deferred[F, Unit]`,
   on which it blocks via `offerer.get`. The consumer will wake it up when space is available.

### Running the Program with a Bounded Queue

The final program differs from the previous one only by specifying `capacity` when creating the state.

```dotty
object ProducerConsumerBounded extends IOApp:
  case class State[F[_], A](
      queue: Queue[A],
      capacity: Int,
      takers: Queue[Deferred[F, A]],
      offerers: Queue[(A, Deferred[F, Unit])]
  )

  object State:
    def empty[F[_], A](capacity: Int): State[F, A] =
      State(Queue.empty, capacity, Queue.empty, Queue.empty)

  override def run(args: List[String]): IO[ExitCode] =
    for
      // Create state with max queue size of 100
      stateR <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int](capacity = 100))
      counterR <- Ref.of[IO, Int](1)

      producers = List.range(1, 11).map(producer(_, counterR, stateR))
      consumers = List.range(1, 11).map(consumer(_, stateR))

      res <- (producers ++ consumers)
        .parSequence
        .as(ExitCode.Success)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error: ${t.getMessage}").as(ExitCode.Error)
        }
    yield res
    
  // producer and consumer defined above (with capacity taken into account)
```

**What will happen on execution:**

1. All 10 producers and 10 consumers start in parallel.
2. Initially, while the queue is empty, producers will quickly fill it to the limit (100 elements).
3. Once the queue is full, producers trying to add the 101st element will **block** (semantically) and join the `offerers` queue.
4. Consumers pull elements from the queue. As soon as the queue is no longer full,
   the first waiting producer is woken up and adds its element (or passes it directly if there's a waiting consumer).
5. The system reaches equilibrium: producers and consumers work at a common pace,
   determined by the consumer throughput.
   The queue stays filled to about 100 elements, but doesn't overflow.

### Analysis of the Current Solution

| Feature                             | Unbounded Queue                                       | Bounded Queue (Final Solution)                              |
|:------------------------------------|:------------------------------------------------------|:------------------------------------------------------------|
| **Memory control**                  | None (OOM risk)                                       | Full (fixed queue size)                                     |
| **Backpressure**                    | None (producers run at max speed)                     | Yes (producers block when queue is full)                    |
| **Code complexity**                 | Moderate                                              | Higher (but all complexity is encapsulated in `modify`)     |
| **Producer blocking**               | No (only consumers)                                   | Yes (symmetric with consumers)                              |
| **Direct transfer**                 | Only producer → consumer (bypassing queue)            | Also possible, but considering the size limit               |
| **CPU usage**                       | Efficient (blocking via `Deferred`)                   | Efficient (producers also don't waste CPU)                  |

## Cancellation Handling

Let's ask ourselves: **is our implementation cancellation-safe?**
That is, what happens if the fiber running the consumer or producer
is cancelled (e.g., due to a timeout, an error in another fiber, or via `fiber.cancel`)?
Will the state become inconsistent?

This is a critically important question. In Cats Effect, cancellation is a **coordinated mechanism**:
a fiber cannot be stopped at an arbitrary point in execution.
Cancellation happens only at **cancellation points** — usually between `flatMap` steps (within for-comprehensions).
Our task is to ensure that on cancellation:

1. Resources do not leak.
2. The shared state (`stateR`) remains consistent.
3. Blocking operations can be interrupted.

### Analyzing the Producer (`producer`)

Let's focus on the internal `offer` function. Let's rewrite it as a for-comprehension for clarity:

```dotty
def offer[F[_]: Async](i: Int): F[Unit] =
  for
    offerer <- Deferred[F, Unit]    // Step 1: create Deferred
    op      <- stateR.modify{ ... } // Step 2: atomically update state, get `op` effect
    _       <- op                   // Step 3: execute `op`
  yield ()
```

**What can go wrong on cancellation?**

- **Cancellation before step 1 or after step 1 but before step 2:** The created `offerer` has no strong references (except the local variable).
  It will simply be garbage collected. State has not been modified — all good.
- **Cancellation after step 2 but before step 3:** This is the most dangerous situation! We've already modified the state
  (either added `offerer` to the `offerers` queue, or removed `taker`), but the `op` effect has not yet been executed.
  The state has become **inconsistent**:
    - If `op` was supposed to be `offerer.get` (blocking), then `offerer` is in `State`,
      but the producer will not block on it (because we haven't reached `.get` yet).
    - If `op` was supposed to be `taker.complete(i).void`, then `taker` is already removed from the queue, but the consumer won't receive the element.
- **Cancellation during `op` execution:** If `op` is `offerer.get` (blocking call),
  cancellation may occur while the fiber waits on `Deferred`.
  This is fine, but we need to correctly remove `offerer` from the state.

### Solution: `F.uncancelable` and `Poll[F]`

Cats Effect [provides the `uncancelable` mechanism](typeclasses/monadcancel.md) to protect critical sections from cancellation,
with the ability to "propagate" cancellation to specific places.

```dotty
F.uncancelable { poll =>
  // Code inside this block CANNOT be cancelled, except at places
  // where `poll(...)` is explicitly used
}
```

**How it works:**

- The entire `uncancelable` block is protected from cancellation.
- Inside the block, you can use `poll(someEffect)` to make `someEffect` **cancellable** — cancellation can occur inside that sub-effect.
- This allows, for example, protecting state modification, but making a `Deferred.get` blocking call cancellable.

### Fixed Producer with Cancellation Support

```dotty
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import cats.effect.syntax.all.*  // for .onCancel
import scala.collection.immutable.Queue

def producer[F[_]: {Async, Console}](
    id: Int,
    counterR: Ref[F, Int],
    stateR: Ref[F, State[F, Int]]
): F[Unit] =
  def offer(i: Int): F[Unit] =
    Deferred[F, Unit].flatMap { offerer =>
      // Protect the critical section from cancellation
      Async[F].uncancelable { poll =>
        stateR.modify {
          // Scenario 1: There are waiting consumers
          case State(queue, capacity, takers, offerers) if takers.nonEmpty =>
            val (taker, rest) = takers.dequeue
            State(queue, capacity, rest, offerers) -> taker.complete(i).void

          // Scenario 2: No waiting consumers, queue not full
          case State(queue, capacity, takers, offerers)
              if queue.size < capacity =>
            State(queue.enqueue(i), capacity, takers, offerers) ->
              Async[F].unit

          // Scenario 3: No waiting consumers, queue full
          case State(queue, capacity, takers, offerers) =>
            // Cleanup function: removes offerer from the queue if cancellation occurs
            val cleanup = stateR.update { s =>
              s.copy(offerers = s.offerers.filter(_._2 ne offerer))
            }
            // Return the blocking effect wrapped in poll (cancellable)
            State(queue, capacity, takers, offerers.enqueue(i -> offerer)) ->
              poll(offerer.get).onCancel(cleanup)
        }.flatten
      }
    }

  for
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- Async[F].whenA(i % 100000 == 0)(
      Console[F].println(s"Producer $id produced $i elements")
    )
    _ <- producer(id, counterR, stateR)
  yield ()
end producer
```

**What changed?**

1. **`Async[F].uncancelable { poll => ... }`** — the entire critical section (reading and modifying state) is protected from cancellation.
   Cancellation cannot occur between `modify` and executing `op`.
2. **In Scenario 3 ( producer blocking):**
     - Instead of `offerer.get`, we use `poll(offerer.get)`. This makes the blocking itself **cancellable**.
     - A `cleanup` function is added — it removes `offerer` from the `offerers` queue if cancellation does occur during blocking.
     - `.onCancel(cleanup)` means: "If this effect (`poll(offerer.get)`) is cancelled, execute `cleanup`".

**Guarantees:**

- State is modified **atomically** and with no possibility of cancellation at the wrong moment.
- Blocking can be interrupted (e.g., by timeout).
- On blocking cancellation, the state is correctly cleaned up.

### Fixed Consumer with Cancellation Support

The consumer requires similar handling. The blocking call here is `taker.get`,
and cleanup should remove `taker` from the `takers` queue.

```dotty
def consumer[F[_]: {Async, Console}](
    id: Int,
    stateR: Ref[F, State[F, Int]]
): F[Unit] =
  val take: F[Int] =
    Deferred[F, Int].flatMap { taker =>
      Async[F].uncancelable { poll =>
        stateR.modify {
          // Scenario 1: Queue not empty, no waiting producers
          case State(queue, capacity, takers, offerers)
              if queue.nonEmpty && offerers.isEmpty =>
            val (i, rest) = queue.dequeue
            State(rest, capacity, takers, offerers) -> Async[F].pure(i)

          // Scenario 2: Queue not empty, there are waiting producers
          case State(queue, capacity, takers, offerers) if queue.nonEmpty =>
            val (i, rest)               = queue.dequeue
            val ((move, release), tail) = offerers.dequeue
            State(rest.enqueue(move), capacity, takers, tail) ->
              release.complete(()).as(i)

          // Scenario 3: Queue empty, but there are waiting producers
          case State(queue, capacity, takers, offerers)
              if offerers.nonEmpty =>
            val ((i, release), rest) = offerers.dequeue
            State(queue, capacity, takers, rest) -> release.complete(()).as(i)

          // Scenario 4: Queue empty and no waiting producers — block
          case State(queue, capacity, takers, offerers) =>
            // Cleanup function: removes taker from the queue on cancellation
            val cleanup = stateR.update { s =>
              s.copy(takers = s.takers.filter(_ ne taker))
            }
            State(queue, capacity, takers.enqueue(taker), offerers) ->
              poll(taker.get).onCancel(cleanup)
        }.flatten
      }
    }

  for
    i <- take
    _ <- Async[F].whenA(i % 100000 == 0)(
      Console[F].println(s"Consumer $id processed $i elements")
    )
    _ <- consumer(id, stateR)
  yield ()
```

**Symmetry:** The producer and consumer code is now structurally identical:

- `uncancelable` protects state modification.
- `poll` makes the `Deferred.get` blocking cancellable.
- `onCancel(cleanup)` ensures state consistency on cancellation.

### Why Is This Safe?

| Scenario                                                                      | Guarantee                                                                                       |
|:------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------|
| **Cancellation before `modify`**                                              | State was not modified — nothing to clean up.                                                   |
| **Cancellation during `modify`**                                              | `modify` is an atomic `Ref` operation and cannot be cancelled.                                  |
| **Cancellation immediately after `modify`, but before executing `op`**        | Protected by `uncancelable` — this situation does not arise.                                    |
| **Cancellation during `op`, if `op` is non-blocking (`unit` or `complete`)**  | These operations execute instantly; cancellation doesn't have time to occur.                    |
| **Cancellation during `op`, if `op` is blocking (`get`)**                     | `poll` makes the blocking cancellable. On cancellation, `cleanup` is executed, removing the `Deferred` from state. |

**Key takeaway:** The implementation became slightly more complex, but gained a **critically important property** — cancellation resistance.
In real applications (where timeouts, graceful shutdown, and error handling are the norm), such protection is necessary.

The full implementation with cancellation support is available [here][source].

## Conclusion

| Stage                                 | Key Takeaway                                                                        |
|:--------------------------------------|:------------------------------------------------------------------------------------|
| **Motivation**                        | Functional effects (`F[_]`) encapsulate side effects.                               |
| **Type classes**                      | Restricting capabilities via type classes is documentation and safety.              |
| **Resource**                          | `Resource` guarantees resource release even on cancellation.                        |
| **Inefficient queue**                | Don't do this (100% CPU, sequential execution).                                     |
| **Efficient queue (unbounded)**       | Semantic blocking without CPU consumption.                                          |
| **Bounded queue**                    | Backpressure and OOM protection.                                                    |
| **Cancellability**                    | Code is resistant to cancellation, timeouts, and graceful shutdown.                 |

---

**Links:**

- [Source code][source]
- [Cats effect documentation](https://typelevel.org/cats-effect/docs/concepts)
- [Cats effect Producer-Consumer](https://typelevel.org/cats-effect/docs/tutorial)

[source]: https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/main/scala/ru/scalabook/libs/cats/effect/ProducerConsumer.scala
