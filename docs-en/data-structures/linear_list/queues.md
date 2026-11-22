# Queues

_A queue_ or _one-way queue_ is a linear list
in which all insertion operations are performed at one end of the list,
and all deletion operations (and, typically, data access operations) are performed at the other.

Queues use the concepts of _front_ and _rear_ of the queue.

This type of structure defines a set of rules:

- The first element inserted is the first one removed - _First In First Out (FIFO)_.
- The last element inserted leaves the queue last.

The time complexity of inserting and deleting elements from a queue is _O(1)_.

Scala has a standard implementation of queues.

**Complexity:**

- _First element of queue_ (`frontOption`)
    - **Time** - O(1)
    - **Space** - O(1)
- _End of queue_ (`rearOption`)
    - **Time** - O(1)
    - **Space** - O(1)
- _Check if queue is empty_ (`isEmpty`)
    - **Time** - O(1)
    - **Space** - O(1)
- _Add element to queue_ (`enqueue`)
    - **Time** - O(1)
    - **Space** - O(1)
- _Remove element from queue_ (`dequeue`)
    - **Time** - O(1)
    - **Space** - O(1)


**Code:**

```dotty
final class Queue[+A](in: List[A], out: List[A]):
  def frontOption: Option[A] = dequeue.map(_._1)

  def rearOption: Option[Queue[A]] = dequeue.map(_._2)

  lazy val isEmpty: Boolean = in.isEmpty && out.isEmpty

  def enqueue[B >: A](x: B): Queue[B] = Queue(x :: in, out)

  def dequeue: Option[(A, Queue[A])] = out match
    case hd :: tl => Some((hd, Queue(in, tl)))
    case Nil => in.reverse match
        case hd :: tl => Some((hd, Queue(Nil, tl)))
        case Nil      => None

object Queue:
  def empty[A]: Queue[A] = Queue(Nil, Nil)
```

**Example:**

```dotty
val emptyQueue: Queue[Int]    = Queue.empty[Int]
val nonEmptyQueue: Queue[Int] = emptyQueue.enqueue(1).enqueue(2)

emptyQueue.frontOption    // None
nonEmptyQueue.frontOption // Some(1)

emptyQueue.rearOption     // None
nonEmptyQueue.rearOption  // Some(Queue(2))
 
emptyQueue.isEmpty        // true
nonEmptyQueue.isEmpty     // false

emptyQueue.enqueue(5)     // Queue(5)
nonEmptyQueue.enqueue(5)  // Queue(1,2,5)

emptyQueue.dequeue        // None
nonEmptyQueue.dequeue     // Some((1,Queue(2)))
```

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Queue](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/Queue.scala)

[source]: https://gitverse.ru/scalabook/algorithms
