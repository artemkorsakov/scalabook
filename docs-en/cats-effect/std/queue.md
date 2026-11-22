# Queue — Asynchronous Queue for Data Exchange

`Queue[F[_], A]` is a **purely functional, concurrent queue**,
designed for safe data exchange between fibers.
It is the analog of `java.util.concurrent.BlockingQueue`, but fully integrated with Cats Effect effects
and uses **semantic blocking**, without stopping system threads.

Fibers can:

- **Put** elements into the queue (`offer`).
- **Retrieve** elements from the queue (`take`).

The main advantage is **safe communication** between producers and consumers in a concurrent environment.

### Basic Interface

```dotty
trait Queue[F[_], A]:
  /** Retrieves an element from the queue. Blocks the fiber if the queue is empty. */
  def take: F[A]

  /** Attempts to retrieve an element without blocking. */
  def tryTake: F[Option[A]]

  /** Adds an element to the queue. Blocks the fiber if the queue is full
    * (for bounded queues).
    */
  def offer(a: A): F[Unit]

  /** Attempts to add an element without blocking. Returns `true` if the element
    * was added.
    */
  def tryOffer(a: A): F[Boolean]
```

## Types of Queues and Their Policies

`Queue` can be created with different behavioral policies, which determine
how `offer` and `take` operations behave upon reaching maximum capacity.

| Queue Type                             | `offer` Behavior on Full                                                            | `take` Behavior on Empty                                              | Creation                                       |
|:---------------------------------------|:------------------------------------------------------------------------------------|:----------------------------------------------------------------------|:-----------------------------------------------|
| **Bounded                             | **Blocks** until space becomes available.                                            | **Blocks** until an element appears.                                   | `Queue.bounded(capacity)`                      |
| **Unbounded**                           | Never blocks. Always succeeds.                                                       | **Blocks** until an element appears.                                   | `Queue.unbounded`                              |
| **Synchronous**                           | Blocks until another `take` call retrieves the element.                             | Blocks until another `offer` call provides an element.                | `Queue.synchronous` (equivalent to `bounded(0)`)  |
| **Dropping**                              | Never blocks. **New element is dropped** if the queue is full.                       | Blocks until an element appears.                                       | `Queue.dropping(capacity)`                     |
| **Circular Buffer**                     | Never blocks. **The oldest element is overwritten** if the queue is full.            | Blocks until an element appears.                                       | `Queue.circularBuffer(capacity)`               |

### Choosing the Right Queue: Examples

- **`bounded`**: Classic producer-consumer. Ideal when you need to limit resource consumption
  or normalize load between a fast producer and a slow consumer.
- **`unbounded`**: Convenient when the producer always keeps up, or data loss is unacceptable.
  **Caution**: can lead to memory overflow with a constantly growing queue.
- **`synchronous`**: For direct "handoff" message exchange. Each `offer` waits for its `take`.
- **`dropping`**: For telemetry or logs, where only the freshest data matters, and older data can be discarded.
- **`circularBuffer`**: For a cache or sliding window of events, where you always need the latest `N` elements.

## Deep Dive: Queue Operations

### `take` — Waiting for an Element

- **If the queue is not empty**: the element is extracted and returned **immediately**.
- **If the queue is empty**: the fiber is **semantically blocked**, i.e., it is suspended and stops consuming an OS thread.
  As soon as another fiber calls `offer`, the waiting fiber is woken up and receives the element.
- **Cancellability**: waiting in `take` is fully cancelable. If the fiber is cancelled, it is correctly removed from the wait queue.

### `offer` — Adding an Element

- **In `unbounded`**: the element is always added instantly. No blocking.
- **In `bounded` and `synchronous`**:
  - If the queue is **not full**, the element is added, and if there are waiting `take`s — they instantly receive the element. The operation succeeds.
  - If the queue is **full**, the fiber blocks and enters the `offer` wait queue until some `take` frees up space.
- **In `dropping` and `circularBuffer`**: the operation **never blocks**,
  but the element may be dropped (dropping) or overwrite an old one (circular buffer).

### `tryTake` / `tryOffer` — Non-Blocking Versions

Useful when you need to poll the queue or try to add an element without waiting.

- `tryTake`: returns `Some(a)` if an element was immediately available, and `None` if the queue is empty.
- `tryOffer`: returns `true` if the element was successfully added (considering the queue policy!),
  and `false` otherwise (e.g., queue is full in `dropping`).

## Example 1: Producer-Consumer with a `bounded` Queue

This is the most classic scenario. One or more producers create data, one or more consumers process it.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Queue
import cats.syntax.all.*

object ProducerConsumer extends IOApp.Simple:
  val run: IO[Unit] =
    for
      queue <- Queue.bounded[IO, String](5) // Queue holding 5 messages

      producer = (id: Int) =>
        (1 to 10).toList.traverse_ { i =>
          val msg = s"Producer_$id: Msg_$i"
          queue.offer(msg) >> IO.println(s"SENT: $msg")
        }

      consumer = (id: Int) =>
        queue.take.flatMap { msg =>
          IO.println(s"Consumer_$id: RECEIVED: $msg")
        }.foreverM // Consume infinitely

      // Start 2 producers and 3 consumers in parallel
      _ <- List(
        producer(1).start,
        producer(2).start,
        consumer(1).start,
        consumer(2).start,
        consumer(3).start
      ).parSequence
    yield ()
```

**How it works:**

1.  Producers send messages quickly. If the 5-message queue becomes full, the next `offer` **blocks** the producer.
2.  Consumers wait (`take`) and process messages.
3.  As soon as a consumer retrieves a message, space opens in the queue, and the blocked producer wakes up and sends the next message.

```text
SENT: Producer_2: Msg_1
SENT: Producer_1: Msg_1
Consumer_1: RECEIVED: Producer_2: Msg_1
SENT: Producer_2: Msg_2
SENT: Producer_2: Msg_3
SENT: Producer_2: Msg_4
...
SENT: Producer_1: Msg_9
Consumer_2: RECEIVED: Producer_1: Msg_9
SENT: Producer_1: Msg_10
Consumer_3: RECEIVED: Producer_1: Msg_10
```

## Example 2: Using `dropping` for Telemetry

Imagine collecting metrics from millions of sensors, but you can only process the 100 newest per second.

```dotty
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}

object Telemetry extends IOApp.Simple:
  val run: IO[Unit] =
    for
      metricsQueue <-
        Queue.dropping[IO, Metric](100) // Queue for 100, drop the rest
      // Processing stream: retrieves and saves metrics
      _ <- metricsQueue.take.flatMap(saveMetric).foreverM.start
      // Simulate a sensor stream
      _ <- (1 to 10000).toList.traverse_ { i =>
        metricsQueue.offer(Metric(i, System.currentTimeMillis()))
      }
    yield ()
```

**Advantage:** The system won't choke and won't block the sensors,
but we risk losing some data if the throughput exceeds the processing capacity.

## Functoriality: `QueueSource` and `QueueSink`

This is a powerful and elegant feature, rarely found in other languages. `Queue` is split into two interfaces:

```dotty
trait QueueSource[F[_], A]:
  def take: F[A]
  def tryTake: F[Option[A]]

object QueueSource:
  implicit def catsFunctorForQueueSource[F[_]: Functor]
      : Functor[[A] =>> QueueSource[F, A]] =
    new Functor[[A] =>> QueueSource[F, A]]:
      def map[A, B](fa: QueueSource[F, A])(f: A => B): QueueSource[F, B] =
        ???
        
trait QueueSink[F[_], A]:
  def offer(a: A): F[Unit]
  def tryOffer(a: A): F[Boolean]

object QueueSink:
  implicit def catsContravariantForQueueSink[F[_]]
      : Contravariant[[A] =>> QueueSink[F, A]] =
    new Contravariant[[A] =>> QueueSink[F, A]]:
      def contramap[A, B](fa: QueueSink[F, A])(f: B => A)
          : QueueSink[F, B] = ???
```

This allows you to:

1. **Transform the queue for reading (`map`):** E.g., have a byte queue, but read from it as a string queue (`String`),
   applying a transformation function on the fly.
2. **Transform the queue for writing (`contramap`):** E.g., have a number queue, but send boolean values (`Boolean`) to it,
   converting `true` to `1` and `false` to `0`.

### Example: Covariance (`map`)

Suppose you have an `Int` queue, but you need a `Long` source.

```dotty
import cats.Functor
import cats.effect.std.{Queue, QueueSource}

val intQueue: IO[Queue[IO, Int]] = Queue.bounded[IO, Int](10)

intQueue.flatMap: queue =>
  // Transform the Int source queue into a Long source queue
  val longSource: QueueSource[IO, Long] =
    Functor[[A] =>> QueueSource[IO, A]].map(queue)(_.toLong) // Int -> Long

  // Now you can put Ints and retrieve Longs
  queue.offer(42) >> longSource.take.map(println) // Prints: 42L
```

### Example: Contravariance (`contramap`)

Suppose you have an `Int` queue, but you need a `Boolean` sink.

```dotty
import cats.Contravariant
import cats.effect.std.{Queue, QueueSink}

val intQueue: IO[Queue[IO, Int]] = Queue.bounded[IO, Int](10)

intQueue.flatMap: queue =>
  // Transform the Int sink queue into a Boolean sink queue
  val boolSink: QueueSink[IO, Boolean] =
    Contravariant[[A] =>> QueueSink[IO, A]]
      .contramap(queue)(b => if b then 1 else 0)

  // Now you can send Booleans and retrieve Ints
  boolSink.offer(true) >> queue.take.map(println)  // Prints: 1
  boolSink.offer(false) >> queue.take.map(println) // Prints: 0
```

**Where is this useful?** When you have a complex system of several queues,
and you want to connect them without creating intermediate data structures.

## Summary: Which Queue to Choose?

| Scenario                                                                            | Recommended Queue Type       |
|:------------------------------------------------------------------------------------|:-----------------------------|
| **Load balancing between differently-speeded producer/consumer**                      | `bounded`                    |
| **Need to guarantee all messages are processed, producer is very fast**              | `unbounded` (watch memory)   |
| **Direct message exchange (handoff), no buffer needed**                              | `synchronous`                |
| **Only the freshest data matters (logs, metrics, sensor readings)**                  | `dropping`                   |
| **Need to keep the last N elements (chat history, sliding window)**                  | `circularBuffer`             |
| **Working with different data types in the same stream**                             | `map` / `contramap`          |

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Queue](https://typelevel.org/cats-effect/docs/std/queue)

[doc]: https://typelevel.org/cats-effect/docs/concepts
