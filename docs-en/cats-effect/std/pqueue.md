# PQueue — Asynchronous Priority Queue

`PQueue[F[_], A: Order]` is a **concurrent priority queue** where each element has its own priority,
defined by the `Order[A]` instance from the Cats library.
Unlike a regular `Queue` (FIFO), `PQueue` always retrieves the **smallest** element according to the given order.

Imagine a queue at a hospital or airport: patients with more serious conditions (higher priority)
are served first, regardless of when they arrived. `PQueue` automatically maintains this order.

### Basic Interface

```dotty
trait PQueue[F[_], A: Order]:
  /** Retrieves the element with the highest priority (smallest according to Order).
    * Blocks the fiber if the queue is empty.
    */
  def take: F[A]

  /** Attempts to retrieve an element without blocking. */
  def tryTake: F[Option[A]]

  /** Adds an element to the queue considering its priority. Blocks only in a
    * bounded queue on overflow.
    */
  def offer(a: A): F[Unit]

  /** Attempts to add an element without blocking. */
  def tryOffer(a: A): F[Boolean]
```

### Key Feature: `Order`

`PQueue` uses `Order[A]` from Cats — a type class for comparing elements.

```dotty
import cats.Order

// Define order: smaller number = higher priority
given Order[Int] = Order.fromLessThan(_ < _)

// Or for strings: shorter = higher priority
given Order[String] = Order.by(_.length)
```

## Types of `PQueue` and Their Policies

Like `Queue`, `PQueue` can be created with different behavioral policies regarding capacity.

| Queue Type                             | `offer` Behavior on Full                                       | `take` Behavior on Empty           | Creation                       |
|:---------------------------------------|:---------------------------------------------------------------|:-----------------------------------|:-------------------------------|
| **Bounded                             | **Blocks** until space becomes available.                      | **Blocks** until an element appears.  | `PQueue.bounded(capacity)`     |
| **Unbounded**                          | Never blocks. Always succeeds.                                 | **Blocks** until an element appears.  | `PQueue.unbounded`             |

> **Important:** Unlike `Queue`, `PQueue` **does not have a synchronous (synchronous)** variant,
> because a priority queue cannot work in a "handoff" mode without losing priority semantics.

## Priority: What Does "Highest" Mean?

All `PQueue` operations use the **smallest** element according to `Order[A]` as the element with the highest priority.

```dotty
import cats.Order
import cats.effect.std.PQueue
import cats.effect.IO

// Example with numbers: Order[Int] by default — numeric order
// Highest priority for the smallest number (1 > 2)
val numberQueue: IO[PQueue[IO, Int]] = PQueue.bounded[IO, Int](10)

for
  pq    <- numberQueue
  _     <- pq.offer(5)
  _     <- pq.offer(1)
  _     <- pq.offer(3)
  first <- pq.take
  _     <- IO.println(s"First: $first") // First: 1
yield ()
```

**What happens "under the hood":**

1. Internally, `PQueue` uses a binary heap to store elements.
2. On `offer`, the element is inserted into the heap — complexity **O(log n)**.
3. On `take`, the root (minimum element) is extracted — complexity **O(log n)**.

## Example 1: Sorting

An ironic but clear demonstration: you can sort a list
by simply dumping all elements into a `PQueue` and then extracting them one by one.

```dotty
import cats.Order
import cats.effect.IO
import cats.effect.std.PQueue
import cats.effect.unsafe.implicits.global

val list = List(1, 4, 3, 7, 5, 2, 6, 9, 8)

// Define order: from smallest to largest
given Order[Int] = Order.fromLessThan(_ < _)

def sortWithPriorityQueue(list: List[Int]): IO[List[Int]] =
  for
    pq     <- PQueue.bounded[IO, Int](10)
    _      <- list.traverse(pq.offer) // Add all elements
    sorted <- List.fill(list.length)(()).traverse(_ => pq.take)
  yield sorted

sortWithPriorityQueue(list).flatMap(IO.println).unsafeRunSync()
// Output: List(1, 2, 3, 4, 5, 6, 7, 8, 9)
```

**Key takeaway:** `PQueue` always returns elements in ascending priority order (smallest first),
making it the natural choice for any tasks requiring ordered processing.

## Example 2: Order Processing System with Priorities

A real-world example: an online store where VIP customer orders should be processed before normal ones.

```dotty
import cats.Order
import cats.effect.std.PQueue
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.*

object OrderProcessing extends IOApp.Simple:

  // Order with a priority level (smaller number = higher priority)
  case class UserOrder(id: Int, priority: Int, description: String)

  // Define order for orders: first compare priority, then id
  given Order[UserOrder] = Order.by(o => (o.priority, o.id))

  val run: IO[Unit] =
    for
      pq <- PQueue.bounded[IO, UserOrder](100)

      // Send orders with different priorities
      _ <- pq.offer(UserOrder(1, 10, "Regular item"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(2, 1, "VIP urgent order"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(3, 5, "Premium order"))
      _ <- IO.sleep(100.millis)
      _ <- pq.offer(UserOrder(4, 1, "Another VIP"))

      // Order processor: always takes the highest priority
      processor <- processOrders(pq).start

      _ <- IO.sleep(3.seconds) // Give time for processing
      _ <- processor.cancel
    yield ()

  private def processOrders(pq: PQueue[IO, UserOrder]): IO[Unit] =
    pq.take.flatMap { order =>
      IO.println(s"[${System.currentTimeMillis()}] Processing: $order") *>
        IO.sleep(500.millis) // simulate processing
    }.foreverM
end OrderProcessing
```

**Expected processing order:** VIP orders (priority=1) will be processed first,
regardless of the fact that the regular order (priority=10) arrived earlier.

## Functoriality: `PQueueSource` and `PQueueSink`

Like other queues, `PQueue` is split into two interfaces:

- **`PQueueSource[F[_], A]`** — read-only (`take`, `tryTake`), has a `Functor` instance.
- **`PQueueSink[F[_], A]`** — write-only (`offer`, `tryOffer`), has a `Contravariant` instance.

### Covariance (`map`) for Source

We have a `PQueue[IO, Int]`, and we need a `PQueueSource[IO, String]` that reads strings.

```dotty
import cats.Functor
import cats.effect.std.{PQueue, PQueueSource}
import cats.effect.IO

val intQueue: PQueue[IO, Int] = ??? // created somewhere
val stringSource: PQueueSource[IO, String] =
  Functor[[A] =>> PQueueSource[IO, A]].map(intQueue)(_.toString)
```

### Contravariance (`contramap`) for Sink

We have a `PQueue[IO, Int]`, and we need a `PQueueSink[IO, Boolean]` that inserts `Boolean` values,
converting `true` to `1`, and `false` to `5` (smaller number = higher priority).

```dotty
import cats.Contravariant
import cats.effect.std.{PQueue, PQueueSink}
import cats.effect.IO

val intQueue: PQueue[IO, Int] = ???
val boolSink: PQueueSink[IO, Boolean] =
  Contravariant[[A] =>> PQueueSink[IO, A]]
    .contramap(intQueue)(if _ then 1 else 5)

boolSink.offer(true)  // inserted true → 1 (high priority)
boolSink.offer(false) // inserted false → 5 (low priority)
```

## `PQueue` vs `Queue` vs `Dequeue`

| Capability                    | `Queue`  | `Dequeue`  | `PQueue`  |
|:------------------------------|:--------:|:----------:|:---------:|
| **FIFO**                      |    ✅     |     ✅      |     ❌     |
| **Bidirectional**             |    ❌     |     ✅      |     ❌     |
| **Priority (by Order)**       |    ❌     |     ❌      |     ✅     |
| **Bounded / Unbounded**       |    ✅     |     ✅      |     ✅     |
| **Dropping / Circular**       |    ✅     |     ✅      |     ❌     |
| **Functor** (for reading)     |    ✅     |     ✅      |     ✅     |
| **Contravariant** (for writing)|  ✅     |     ✅      |     ✅     |

## Important Limitations and Features

### 1. `Order` Must Be Consistent

```dotty
// ❌ Bad: inconsistent order
given Order[Int] = Order.fromLessThan((x, y) => x != y)

// ✅ Good: total and transitive order
given Order[Int] = Order.fromLessThan((x, y) => x < y) // standard
```

### 2. Priority Is Determined at Insertion

If you change fields of an object affecting `Order`, the queue won't re-sort automatically.
The priority is fixed at the moment of `offer`.

### 3. No "Change Priority" Operation

In `PQueue`, you can't simply increase the priority of an element already in the queue.
For this, you need to remove it and insert it again.

## Practical Recommendations

| Scenario                                  | Recommendation                                                    |
|:------------------------------------------|:------------------------------------------------------------------|
| **Sorting data**                          | Use `unbounded` or `bounded` with a large margin.                 |
| **Processing orders with different priorities** | `bounded` with a reasonable limit.                    |
| **Task scheduler with deadlines**         | `bounded` + `Order.by(_.deadline)`.                                |
| **Unbounded stream of events**            | `unbounded` — but watch memory!                                    |

## Summary

`PQueue` is a powerful structure for **priority-ordered** data processing in a concurrent environment.
Key advantages:

1.  **Automatic sorting** — elements are always extracted in ascending priority order.
2.  **All familiar policies** — `bounded`, `unbounded`.
3.  **Functoriality** — easily transform types for reading and writing.
4.  **Cats integration** — uses `Order` for flexible definition of "what matters more".

Use `PQueue` wherever elements need to be serviced in order of importance, not arrival order:
ticketing systems, task schedulers, real-time event processing, medical or emergency systems.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Priority Queue](https://typelevel.org/cats-effect/docs/std/pqueue)

[doc]: https://typelevel.org/cats-effect/docs/concepts
