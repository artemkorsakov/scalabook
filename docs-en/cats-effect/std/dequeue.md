# Dequeue — Asynchronous Bidirectional Queue

`Dequeue[F[_], A]` (Double-Ended Queue) is a **concurrent bidirectional queue**
that extends the functionality of a regular `Queue`,
allowing adding and removing elements from **both ends**: the front and the back.

Imagine a regular queue where elements enter at the back and are processed from the front (FIFO — First-In-First-Out).
`Dequeue` adds flexibility: you can:

- Add an element to the front (`offerFront`).
- Add an element to the back (`offerBack`).
- Remove an element from the front (`takeFront`).
- Remove an element from the back (`takeBack`).

### Basic Interface

```dotty
trait Dequeue[F[_], A] extends Queue[F, A]:
  // Operations on the back (rear end)
  def offerBack(a: A): F[Unit]
  def tryOfferBack(a: A): F[Boolean]
  def takeBack: F[A]
  def tryTakeBack: F[Option[A]]

  // Operations on the front (front end)
  def offerFront(a: A): F[Unit]
  def tryOfferFront(a: A): F[Boolean]
  def takeFront: F[A]
  def tryTakeFront: F[Option[A]]

  // Reverses the queue (swaps head and tail)
  def reverse: F[Unit]

  // Inherited Queue methods delegate (by default)
  override def offer(a: A): F[Unit]       = offerBack(a) // offer -> offerBack
  override def tryOffer(a: A): F[Boolean] = tryOfferBack(a)
  override def take: F[A]                 = takeFront    // take -> takeFront
  override def tryTake: F[Option[A]]      = tryTakeFront
end Dequeue
```

## Types of `Dequeue` and Their Policies

Like `Queue`, `Dequeue` can be created with different behavioral policies regarding capacity.

| Queue Type                              | `offer*` Behavior on Full                                                            | `take*` Behavior on Empty                                                                     |
|:----------------------------------------|:-------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|
| **Bounded                              | Blocks until space becomes available.                                                | Blocks until an element appears.                                                              |
| **Unbounded**                           | Never blocks. Always succeeds.                                                       | Blocks until an element appears.                                                              |
| **Synchronous**                          | Blocks until the opposite operation (`takeFront` or `takeBack`) takes the element.  | Blocks until the opposite operation (`offerFront` or `offerBack`) provides an element.       |
| **Dropping**                            | Never blocks. New element is dropped on overflow.                                    | Blocks until an element appears.                                                              |
| **Circular Buffer**                     | Never blocks. The oldest element is overwritten.                                     | Blocks until an element appears.                                                              |

> **Important:** The size of `Dequeue` is the total count of elements in the queue.
> The "bounded" limit refers to the total number of elements, regardless of which end they are added from.

## Why Bidirectionality?

`Dequeue` is invaluable in scenarios that require more complex logic than a simple FIFO queue.

### 1. Priority Processing (Stack + Queue)

You can use `Dequeue` as a **combination of queue and stack**.

- **Normal tasks** are sent to the back via `offerBack` and processed from the front via `takeFront`.
- **Urgent tasks** are sent to the front via `offerFront`. They will be processed **immediately after the current** task.

```dotty
import cats.effect.IO
import cats.effect.std.Dequeue

class TaskProcessor(queue: Dequeue[IO, String]):

  def addNormalTask(task: String): IO[Unit] = queue.offerBack(task)
  def addUrgentTask(task: String): IO[Unit] = queue.offerFront(task)

  def processNext: IO[Unit] =
    queue.takeFront.flatMap: task =>
      IO.println(s"Processing task: $task")
```

### 2. Limited Buffer Queue (Limited Deque)

A classic example — a **limited buffer** for data exchange between a producer and consumer,
where the consumer can "peek into the past" or re-read recent elements.

```dotty
class LimitedBuffer(queue: Dequeue[IO, Int]):
  def addToBuffer(value: Int): IO[Unit] = queue.offerBack(value)
  def readLast: IO[Int]  = queue.takeBack  // Read the newest element
  def readFirst: IO[Int] = queue.takeFront // Read the oldest element
```

### 3. Retry Queue

A message processing system: if processing a message is temporarily impossible,
you can send it **back to the front** of the queue to try again,
rather than placing it at the back where it would wait longer.

```dotty
def processMessage(msg: String): IO[Boolean] =
  ??? // true - success, false - needs retry

def processor(queue: Dequeue[IO, String]): IO[Unit] =
  queue.takeFront.flatMap { msg =>
    processMessage(msg).ifM(
      ifTrue = IO.unit,
      ifFalse = queue.offerFront(msg) // Send back to front
    )
  }.foreverM
```

## Example 1: Basic `Dequeue` Usage as a Bidirectional Queue

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Dequeue
import cats.syntax.all.*

object DequeueExample extends IOApp.Simple:
  val run: IO[Unit] =
    for
      dq <- Dequeue.bounded[IO, String](5)

      _ <- dq.offerBack("A")  // [A]
      _ <- dq.offerBack("B")  // [A, B]
      _ <- dq.offerFront("C") // [C, A, B]
      _ <- dq.offerFront("D") // [D, C, A, B]

      // Remove from both ends
      first <- dq.takeFront // Extract D
      last  <- dq.takeBack  // Extract B

      _ <- IO.println(
        s"First: $first, Last: $last"
      ) // First: D, Last: B
    yield ()
```

**Output:**

```
First: D, Last: B
```

## Example 2: `Dequeue` as a Priority Queue (Stack + Queue)

A more complex example — an order system with normal and VIP orders.

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Dequeue
import scala.concurrent.duration.*

object PriorityQueueExample extends IOApp.Simple:

  case class Order(id: Int, isVIP: Boolean, description: String)

  def run: IO[Unit] =
    for
      dq <- Dequeue.bounded[IO, Order](100)

      // Submit orders
      _ <- submitOrder(dq)(Order(1, false, "Regular item"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(2, true, "VIP urgent item"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(3, false, "Another regular item"))
      _ <- IO.sleep(100.millis)
      _ <- submitOrder(dq)(Order(4, true, "Another VIP order"))

      // Start processor in the background
      processorFiber <- processOrder(dq).start
      _              <- IO.sleep(1.second) // Give time for processing
      _              <- processorFiber.cancel
    yield ()

  // Function to submit an order to the queue
  private def submitOrder(dq: Dequeue[IO, Order])(order: Order): IO[Unit] =
    if order.isVIP then dq.offerFront(order)
    else dq.offerBack(order)

  // Order processor
  private def processOrder(dq: Dequeue[IO, Order]): IO[Unit] =
    dq.takeFront.flatMap { order =>
      IO.println(
        s"Processing order #${order.id} (VIP: ${order.isVIP}): ${order.description}"
      )
    }.foreverM
end PriorityQueueExample

// Processing order #4 (VIP: true): Another VIP
// Processing order #2 (VIP: true): VIP urgent item
// Processing order #1 (VIP: false): Regular item
// Processing order #3 (VIP: false): Another regular item
```

**Expected processing order:** VIP orders (2 and 4) will be processed **before** normal ones, despite being submitted later.

## Functoriality: `DequeueSource` and `DequeueSink`

Like `Queue`, `Dequeue` is split into two interfaces:

- **`DequeueSource[F[_], A]`** — handles element extraction only (`takeFront`, `takeBack`, `tryTakeFront`, `tryTakeBack`, `reverse`, `Functor`).
- **`DequeueSink[F[_], A]`** — handles element insertion only (`offerFront`, `offerBack`, `tryOfferFront`, `tryOfferBack`, `Contravariant`).

This allows you to flexibly transform data types for reading and writing.

### Example: Covariance (`map`) for Source

We have a `Dequeue[IO, Int]`, and we need a `DequeueSource[IO, String]` that reads strings.

```dotty
import cats.Functor
import cats.effect.std.{Dequeue, DequeueSource}
import cats.effect.IO

val intDequeue: Dequeue[IO, Int]            = ???
val stringSource: DequeueSource[IO, String] =
  Functor[[A] =>> DequeueSource[IO, A]].map(intDequeue)(_.toString)
// Now calling stringSource.takeFront returns IO[String]
```

### Example: Contravariance (`contramap`) for Sink

We have a `Dequeue[IO, Int]`, and we need a `DequeueSink[IO, Boolean]` that inserts `Boolean` values, converting `true` to `1` and `false` to `0`.

```dotty
import cats.Contravariant
import cats.effect.std.{Dequeue, DequeueSink}

val intDequeue: Dequeue[IO, Int]       = ???
val boolSink: DequeueSink[IO, Boolean] =
  Contravariant[[A] =>> DequeueSink[IO, A]]
    .contramap(intDequeue)(if _ then 1 else 0)

boolSink.offerFront(true) // inserted true, which became 1
```

## `reverse`: Queue Reversal

The `reverse` method swaps the head and tail of the queue in **O(1)**.
This is useful when you need to change the processing order to the opposite.

```dotty
import cats.effect.IO
import cats.effect.std.Dequeue

val dq: Dequeue[IO, Int] = ???
// Assume the queue contains: [1, 2, 3] (head=1, tail=3)
dq.reverse  // Now the queue is: [3, 2, 1] (head=3, tail=1)
```

This is very efficient since it doesn't require copying elements, just swapping pointers to the head and tail.

## `Dequeue` vs `Queue`

| Capability                            |   `Queue[F, A]`   |   `Dequeue[F, A]`   |
|:--------------------------------------|:-----------------:|:-------------------:|
| Add to back (rear)                    |     ✅ `offer`     |    ✅ `offerBack`    |
| Add to front                          |         ❌         |   ✅ `offerFront`    |
| Remove from front                     |     ✅ `take`      |    ✅ `takeFront`    |
| Remove from back                      |         ❌         |    ✅ `takeBack`     |
| Reverse queue                         |         ❌         |     ✅ `reverse`     |
| Functor (for source)                  |  ✅ `QueueSource`  |  ✅ `DequeueSource`  |
| Contravariant (for sink)              |   ✅ `QueueSink`   |   ✅ `DequeueSink`   |
| All policies (bounded, dropping, etc.)|         ✅         |          ✅          |

**Conclusion:** `Dequeue` is a superset of `Queue`. If you don't need front operations,
you can use `Queue`, but `Dequeue` provides more flexibility.

## Practical Recommendations

| Scenario                                 | Recommendation                                                                     |
|:-----------------------------------------|:-----------------------------------------------------------------------------------|
| Classic FIFO producer-consumer           | Use `Queue` (or `Dequeue` if flexibility may be needed in the future).             |
| LIFO (stack) or mixed strategy           | `Dequeue` with `offerFront` and `takeFront` (stack) or a combination.              |
| Need to process urgent tasks             | `Dequeue` with `offerFront` for priority tasks.                                    |
| Retry queue                              | `Dequeue` with `offerFront` for retry.                                             |
| Limited buffer for sliding window        | `Dequeue` + `bounded` + `offerBack` / `takeFront`.                                 |
| Need to iterate the queue from both ends | `Dequeue` with `takeFront` and `takeBack`.                                         |

## Summary

`Dequeue` is a powerful and flexible data structure that extends the capabilities of a regular queue.
It is essential when more complex processing logic is required than classic FIFO. Key advantages:

1. **Bidirectional access** — add and remove elements from any end.
2. **All `Queue` policies** — `bounded`, `unbounded`, `dropping`, `circularBuffer`, `synchronous`.
3. **Functoriality** — transform types for reading (`Functor`) and writing (`Contravariant`).
4. **Efficient reversal** — `reverse` in O(1).

Use `Dequeue` for building complex concurrent systems: priority queues, retry buffers, stacks, and hybrid structures.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Dequeue](https://typelevel.org/cats-effect/docs/std/dequeue)

[doc]: https://typelevel.org/cats-effect/docs/concepts
