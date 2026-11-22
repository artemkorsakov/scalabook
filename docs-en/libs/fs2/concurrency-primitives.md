# Concurrency Primitives

In the [`fs2.concurrent`](https://github.com/typelevel/fs2/tree/main/core/shared/src/main/scala/fs2/concurrent) package 
you will find several useful concurrency primitives, built on top of primitives 
defined in the [`cats-effect`](../../cats-effect/index.md) library. 
Among them:

- `Topic[F, A]` — implements the publish-subscribe pattern with support for multiple subscriptions to the same message stream.
- `Channel[F, A]` — implements a publish-subscribe pattern, but with a single subscriber and multiple publishers, 
  making it ideal for single-consumer scenarios.
- `Signal[F, A]` — represents a mutable value that can be read and updated, 
  and subscribed to for changes, allowing it to be used as a signalling mechanism between streams.

Additionally, `Stream` provides functions for interacting with [queues (`Queue`)](../../cats-effect/std/queue.md) 
from the `cats-effect` library, 
making it easy to integrate them into stream computations.

## Simple Examples

### Channel

`Channel` implements a publish-subscribe pattern and is particularly useful in situations 
where we have **multiple publishers** and **a single subscriber**. 
Unlike `Topic`, which allows multiple subscribers to receive the same messages, 
`Channel` is designed to route messages from many producers to a single consumer.

In the following example, we have two Publisher streams: `pub1` publishes the string `"Hello"` every 1 second, 
while `pub2` publishes the string `"World"` every 2 seconds. 
We also have a subscriber `sub` that consumes and prints each element to the console. 
All three streams run concurrently and are interrupted after 6 seconds.

```dotty
import cats.effect.*
import fs2.Stream
import scala.concurrent.duration.*
import cats.effect.unsafe.implicits.global
import fs2.concurrent.Channel

Channel.unbounded[IO, String].flatMap { channel =>
  // Publisher 1: sends "Hello" every second
  val pub1 =
    Stream.repeatEval(IO("Hello")).evalMap(channel.send).metered(1.second)

  // Publisher 2: sends "World" every 2 seconds
  val pub2 =
    Stream.repeatEval(IO("World")).evalMap(channel.send).metered(2.seconds)

  // Subscriber: reads from the channel and prints each received message
  val sub = channel.stream.evalMap(IO.println)

  // Run all concurrently and interrupt after 6 seconds
  Stream(
    pub1,
    pub2,
    sub
  ).parJoinUnbounded.interruptAfter(6.seconds).compile.drain
}.unsafeRunSync()
// Hello
// World
// Hello
// Hello
// Hello
// World
// Hello
```

**How it works:**

- `Channel.unbounded[IO, String]` creates an unbounded channel for passing strings.
- `pub1` and `pub2` use `evalMap(channel.send)` to send each generated value into the channel.
- `channel.stream` creates a stream that reads all incoming messages from the channel in the order they arrive.
- `parJoinUnbounded` runs all three streams concurrently without limiting the number of simultaneously running streams.
- `interruptAfter(6.seconds)` ensures the program terminates after 6 seconds, regardless of whether the streams have finished their work.

### Topic

(Based on an example by [Pera Villega](https://perevillega.com/), available [here](https://underscore.io/blog/posts/2018/03/20/fs2.html))

`Topic` implements the classic publish-subscribe pattern with support for **multiple subscribers**. 
Each message published to a topic is delivered to **all active subscribers**. 
This is particularly useful for broadcast scenarios, 
where the same event needs to be processed by multiple independent consumers.

In the following example, the publisher and subscriber start simultaneously. 
The publisher continuously publishes the string `"1"` to the topic, while the subscriber consumes messages until 
it receives four elements, after which it terminates.

```dotty
import cats.effect.*
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.Topic

Topic[IO, String].flatMap { topic =>
  // Publisher: infinitely generates the string "1" and publishes it to the topic
  val publisher = Stream.constant("1").covary[IO].through(topic.publish)

  // Subscriber: subscribes to the topic with a buffer of 10 messages and takes the first 4
  val subscriber = topic.subscribe(10).take(4)

  // Run the subscriber concurrently with the publisher and collect results
  subscriber.concurrently(publisher).compile.toVector
}.unsafeRunSync()
```

**Key points:**

- `Topic[IO, String]` creates a topic for `String` messages.
- `topic.publish` is a `Pipe` that accepts a stream of elements and publishes them to the topic.
- `topic.subscribe(10)` creates a stream reading messages from the topic with a buffer size of 10. 
  The buffer helps smooth out peak loads and prevents message loss if the subscriber temporarily cannot keep up.
- `concurrently(publisher)` runs the publisher in the background concurrently with the subscriber, ignoring its output. 
  This ensures both streams work simultaneously.

### Signal

`Signal` can be used as a communication mechanism between two different streams. 
It represents a thread-safe mutable value that can be subscribed to for changes. 
`Signal` is particularly useful for implementing stop flags, completion signals, or state transfer between system components.

In the following example, `s1` is a stream that prints the current time every second and is interrupted when the signal becomes `true`. 
`s2` is a stream that sleeps for four seconds, then sets the signal to `true`, which causes `s1` to be interrupted.

(Note that `SignallingRef` extends `Signal`, providing additional capabilities for atomic value updates.)

```dotty
import cats.effect.*
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.SignallingRef

import scala.concurrent.duration.*

SignallingRef[IO, Boolean](false).flatMap { signal =>
  // Stream s1: prints time every second and interrupts on signal
  val s1 = Stream.awakeEvery[IO](1.second).interruptWhen(signal)

  // Stream s2: waits 4 seconds, then sets the signal to true
  val s2 = Stream.sleep[IO](4.seconds) >> Stream.eval(signal.set(true))

  // Run s1 and s2 concurrently
  s1.concurrently(s2).compile.toVector
}.unsafeRunSync()
```

**Implementation details:**

- `SignallingRef[IO, Boolean](false)` creates a signalling reference with an initial value of `false`.
- `interruptWhen(signal)` is a method that stops the stream as soon as `signal` becomes `true`. 
  It automatically subscribes to signal changes.
- `Stream.sleep[IO](4.seconds)` creates a stream that simply waits for the specified duration without producing any output.
- `>>` is a sequential composition operator: the left stream (delay) executes first, then the right one (setting the signal).
- `concurrently` runs `s2` in the background concurrently with `s1`, allowing `s2` to interrupt `s1` after four seconds.

### Queue

[Queues (`Queue`) from the `cats-effect` library](../../cats-effect/std/queue.md) 
are a fundamental building block for organizing asynchronous data exchange between different parts of a program. 
FS2 provides convenient methods for integrating queues into stream processing.

In the following example, `Stream.fromQueueNoneTerminated` is used to create a stream from a `cats.effect.std.Queue`. 
This fragment returns `List(1, 2, 3)`: the stream will emit the first three values 
returned by the queue (`Some(1)`, `Some(2)`, `Some(3)`), and will be interrupted after the queue returns `None` — 
this signal is interpreted as a stream termination marker.

```dotty
import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import fs2.Stream

val program =
  for
    // Create an unbounded queue for Option[Int] values
    queue <- Queue.unbounded[IO, Option[Int]]

    // Create a stream from the queue — it will terminate when it receives None
    streamFromQueue = Stream.fromQueueNoneTerminated(queue)

    // Fill the queue with values: three numbers and the None termination marker
    _ <- Seq(Some(1), Some(2), Some(3), None).map(queue.offer).sequence

    // Run the stream and collect all elements into a list
    result <- streamFromQueue.compile.toList
  yield result

program.unsafeRunSync()
```

**Important details:**

- `Stream.fromQueueNoneTerminated(queue)` creates a stream that reads from the queue until it encounters a `None` value. 
  Once `None` is received, the stream terminates.
- `queue.offer` adds an element to the queue. In this case, we sequentially add several values and the termination marker.
- `.sequence` transforms `List[IO[Unit]]` into `IO[List[Unit]]`, executing all operations in order. 
  This ensures all elements are placed into the queue before we start reading from it.

## Advanced Examples

These data structures can be very useful in more complex scenarios. 
Below are examples of their usage, originally taken from the [gvolpe examples repository](https://github.com/gvolpe/advanced-http4s/tree/master/src/main/scala/com/github/gvolpe/fs2).

### FIFO (First IN / First OUT)

A typical use case for `cats.effect.std.Queue[F, A]`, which is also very useful for interaction with the outside world, 
as shown in the [main guide](guide.md).

In this example, `q1` has a buffer size of 1, and `q2` has a buffer size of 100. 
You will notice the effect of buffering when extracting elements from `q2`: since the `q2` buffer is larger, 
it can accumulate more elements before the consumer starts processing them.

**Example description:**

- **First stream** generates numbers from 0 to 999 and puts them into `q1` (buffer 1). 
  If `q1` is full, the `offer` operation will block (or asynchronously wait) until space becomes available.
- **Second stream** constantly extracts elements from `q1` and puts them into `q2` (buffer 100). 
  This creates an intermediate buffer that smooths out peak loads.
- **Third stream** constantly extracts elements from `q2` and prints them to the console. 
  Thanks to the buffer of size 100, this consumer can work with some delay relative to the producer without losing data.

```dotty
import cats.effect.{Concurrent, IO, IOApp}
import cats.effect.std.{Console, Queue}
import fs2.Stream

import scala.concurrent.duration.*

class Buffering[F[_]: {Concurrent, Console}](
    q1: Queue[F, Int],
    q2: Queue[F, Int]
):

  def start: Stream[F, Unit] =
    Stream(
      // Producer stream: generates numbers from 0 to 999 and puts them into q1
      Stream.range(0, 1000).covary[F].foreach(q1.offer),

      // Intermediary stream: moves elements from q1 to q2
      Stream.repeatEval(q1.take).foreach(q2.offer),

      // Consumer stream: takes elements from q2 and prints them
      // Note: .map won't work here since we're trying to 
      // apply a side effect to a pure value.
      // Use .foreach instead of .map to execute effects.
      Stream.repeatEval(q2.take).foreach(n =>
        Console[F].println(s"Extracting $n from queue #2")
      )
    ).parJoin(3) // Run all three streams concurrently
end Buffering

object Fifo extends IOApp.Simple:
  def run: IO[Unit] =
    val stream =
      for
        // Create two queues with different buffer sizes
        q1 <- Stream.eval(Queue.bounded[IO, Int](1))   // small buffer
        q2 <- Stream.eval(Queue.bounded[IO, Int](100)) // large buffer
        bp = new Buffering[IO](q1, q2)
        // Start buffering and let it run for 5 seconds
        _ <- Stream.sleep[IO](5.seconds) concurrently bp.start.drain
      yield ()
    stream.compile.drain
```

**Observations:**

- With a buffer size of 1, the producer will often block, waiting for the intermediary to take an element.
- With a buffer size of 100, the intermediary can accumulate a significant number of elements 
  before the consumer starts processing them, allowing peak loads to be smoothed out and overall throughput to be increased.

### Single Publisher / Multiple Subscriber

Building a system like Kafka based on concurrency primitives is possible using `fs2.concurrent.Topic[F, A]`. 
In this more complex example, we will also show how to use `fs2.concurrent.Signal[F, A]` to interrupt a scheduled stream.

**Scenario:**

- **Publisher** publishes text events every second for 15 seconds, then sends one `Quit` event and continues publishing text events.
- **Three subscribers** subscribe to the topic at different times:
    - Subscriber #1 subscribes immediately and should receive 15 events.
    - Subscriber #2 subscribes after a 5-second delay and should receive 10 events.
    - Subscriber #3 subscribes after a 10-second delay and should receive 5 events.
- When a subscriber receives the `Quit` event, it sets an interrupt signal, which stops the publisher and terminates the program.

The program terminates after 15 seconds, when the signal interrupts the publication of new events, 
since the final stream merge (`parJoin`) stops when the left stream (publisher) completes.

**Expected result:**

- Subscriber #1 receives 15 events.
- Subscriber #2 receives 10 events.
- Subscriber #3 receives 5 events.
- Publisher sends a `Quit` event.
- Subscribers raise the interrupt signal upon receiving `Quit`.

```dotty
import scala.concurrent.duration.*
import cats.effect.std.Console
import cats.effect.{Clock, IO, IOApp, Temporal}
import cats.syntax.all.*
import fs2.{Pipe, Stream}
import fs2.concurrent.{SignallingRef, Topic}

// Define event types
sealed trait Event
case class Text(value: String)
    extends Event // text event with a timestamp
case object Quit extends Event // termination signal

class EventService[F[_]](
    eventsTopic: Topic[F, Event],
    interrupter: SignallingRef[F, Boolean]
)(
    implicit
    F: Temporal[F],
    console: Console[F]
):

  // Publisher: publishes 15 text events, then one Quit, 
  // then continues publishing
  def startPublisher: Stream[F, Unit] =
    // Generate text events every second with the current time
    val textEvents =
      Stream.awakeEvery[F](1.second)
        .zipRight(Stream.repeatEval(Clock[F].realTime.map(t =>
          Text(t.toString)
        )))

    // Create a Quit event and publish it
    val quitEvent = Stream.eval(eventsTopic.publish1(Quit).as(Quit))

    // Take the first 15 text events, then Quit, then continue publishing
    // Interrupt if the signal becomes true
    (textEvents.take(15) ++ quitEvent ++ textEvents)
      .through(eventsTopic.publish)
      .interruptWhen(interrupter)

  // Create 3 subscribers starting at different times, 
  // and merge them for concurrent execution
  def startSubscribers: Stream[F, Unit] =
    // ProcessEvent function for a specific subscriber
    def processEvent(subscriberNumber: Int): Pipe[F, Event, Nothing] =
      _.foreach {
        case e @ Text(_) =>
          console.println(
            s"Subscriber #$subscriberNumber processing event: $e"
          )
        case Quit =>
          // Upon receiving Quit, set the interrupt signal
          interrupter.set(true)
      }

    // Get the event stream from the topic with a buffer of 10
    val events: Stream[F, Event] =
      eventsTopic.subscribe(10)

    // Three subscribers start with different delays
    Stream(
      // subscriber #1 — immediately
      events.through(processEvent(1)), 
      // subscriber #2 — after 5 seconds
      events.delayBy(5.second).through(processEvent(2)), 
      // subscriber #3 — after 10 seconds
      events.delayBy(10.second).through(processEvent(3)) 
    ).parJoin(3) // run all concurrently
  end startSubscribers
end EventService

object PubSub extends IOApp.Simple:

  val program =
    for
      // create topic
      topic  <- Stream.eval(Topic[IO, Event]) 
      // create interrupt signal
      signal <- Stream.eval(SignallingRef[IO, Boolean](false)) 
      // create service
      service = new EventService[IO](topic, signal) 
      // start publisher and subscribers concurrently
      _ <- service.startPublisher.concurrently(
        service.startSubscribers
      ) 
    yield ()

  def run: IO[Unit] = program.compile.drain
```

**Key concepts:**

- `Topic` ensures delivery of each message to all active subscribers.
- `SignallingRef` is used as a "stop-signal" mechanism: any subscriber can stop the entire system by receiving a `Quit` event.
- `delayBy` adds a delay before starting stream processing, allowing subscribers to connect at different times.
- `parJoin(3)` merges the three subscriber streams into one executing concurrently.

### Shared Resource

When multiple processes try to access a valuable resource (e.g., a database, filesystem, or network connection), 
you may need to limit the number of simultaneous accesses. 
This is where `cats.effect.std.Semaphore[F]` comes in — a semaphore that limits concurrent access to a shared resource.

In this example, three processes simultaneously try to access a shared resource, 
but only one of them gets access at any given moment. 
The others are forced to wait until the resource becomes available again (availability is 1, as indicated by the semaphore counter).

**Possible execution flow:**

```
P1 >> Availability: 1
P2 >> Availability: 1
P2 >> Starting work | Availability: 0
P3 >> Availability: 0
--------------------------------
P1 >> Starting work | Availability: 0
P2 >> Finished | Availability: 0
--------------------------------
P3 >> Starting work | Availability: 0
P1 >> Finished | Availability: 0
--------------------------------
P3 >> Finished | Availability: 1
```

**Execution analysis:**

- When P1 and P2 requested availability, it was 1. P2 was faster and gained access to the resource, starting processing.
- P3 was the slowest and from the start saw that availability was 0.
- After P2 finished, P1 immediately started processing, and availability became 0 again.
- After P1 completed, P3 started processing.
- Finally, P3 finished, and availability became 1 again.

```dotty
import cats.effect.{IO, IOApp, Temporal}
import cats.effect.std.Semaphore
import cats.syntax.all.*
import fs2.Stream

import scala.concurrent.duration.*

class SlowProcess[F[_]: Temporal](name: String, s: Semaphore[F]):

  def execute: Stream[F, Unit] =
    for
      // Check current availability
      _ <-
        Stream.eval(s.available.map(a => println(s"$name >> Availability: $a")))

      // Request access (blocks if availability = 0)
      _ <- Stream.eval(s.acquire)

      // Confirm work started (availability should be 0 after acquire)
      _ <- Stream.eval(s.available.map(a =>
        println(s"$name >> Starting work | Availability: $a")
      ))

      // Simulate lengthy work (3 seconds)
      _ <- Stream.sleep(3.seconds)

      // Release the resource
      _ <- Stream.eval(s.release)

      // Confirm completion
      _ <- Stream.eval(s.available.map(a =>
        println(s"$name >> Finished | Availability: $a")
      ))
    yield ()
end SlowProcess

object Resources extends IOApp.Simple:

  def run: IO[Unit] =
    val stream =
      for
        // Create a semaphore with initial availability of 1 (only one process at a time)
        s <- Stream.eval(Semaphore[IO](1))

        // Create three processes competing for the resource
        p1 = new SlowProcess[IO]("P1", s)
        p2 = new SlowProcess[IO]("P2", s)
        p3 = new SlowProcess[IO]("P3", s)

        // Run all three processes concurrently
        _ <- Stream(p1.execute, p2.execute, p3.execute).parJoin(3)
      yield ()
    stream.compile.drain
```

**Semaphore details:**

- `Semaphore[IO](1)` creates a semaphore with one permit. This ensures that only one process can own the resource at any given time.
- `s.available` returns the current number of available permits (non-blocking operation).
- `s.acquire` requests a permit. If no permits are available, the operation blocks (asynchronously) until a permit becomes available.
- `s.release` releases a permit, increasing the availability counter.
- `parJoin(3)` runs all three processes concurrently, demonstrating fair competition for the resource.

**Real-world applications:**
Semaphores are widely used for:

- Limiting the number of simultaneous connections to a database.
- Regulating access to external APIs with rate limiting.
- Managing thread or connection pools.
- Implementing the "rate limiter" pattern.

## Conclusion

Concurrency primitives in FS2 and Cats Effect provide a powerful and safe set of tools for building concurrent applications. 
By using `Channel`, `Topic`, `Signal`, `Queue`, and `Semaphore`, you can implement complex interaction patterns between streams, such as:

- **Publish-subscribe** with one or multiple subscribers.
- **Interruption mechanisms** and stream lifecycle management.
- **Buffering and throttling** of data streams.
- **Access restriction** to shared resources.

All these primitives are thread-safe and guarantee correct behavior even under high load and concurrent access.

---

**References:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
