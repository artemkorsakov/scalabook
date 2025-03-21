# Очереди

_Очередь_ или _односторонняя очередь_ — это линейный список, 
в котором все операции вставки выполняются на одном из концов списка, 
а все операции удаления (и, как правило, операции доступа к данным) — на другом.

По отношению к очередям применяют понятия _начало_ (_front_) и _конец_ (_rear_) очереди.

Структура такого типа определяет набор правил: 

- Элемент, который вставляется первым, удаляется первым - _First in First Out (FIFO)_. 
- Элемент, вставленный последним, покидает очередь последним. 

Временная сложность вставки и удаления элемента в очередь составляет _O(1)_.

В Scala присутствует стандартная релизация очереди.

**Оценка:**

- _Первый элемент очереди_ (`frontOption`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Конец очереди_ (`rearOption`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Проверка очереди на пустоту_ (`isEmpty`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Добавление элемента в очередь_ (`enqueue`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Удаление элемента из очереди_ (`dequeue`)
    - **Время** - O(1)
    - **Память** - O(1)


**Код:**

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

**Пример:**

```dotty
val emptyQueue: Queue[Int]    = Queue.empty[Int]
val nonEmptyQueue: Queue[Int] = emptyQueue.enqueue(1).enqueue(2)

emptyQueue.frontOption    // None
nonEmptyQueue.frontOption // Some(1)

emptyQueue.rearOption     // None
nonEmptyQueue.rearOption  // Some(Queue(2))
 
emptyQueue.isEmpty)       // true
nonEmptyQueue.isEmpty)    // false

emptyQueue.enqueue(5))    // Queue(5)
nonEmptyQueue.enqueue(5)) // Queue(1,2,5)

emptyQueue.dequeue)       // None
nonEmptyQueue.dequeue)    // Some((1,Queue(2)))
```

---

**Ссылки:**

- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Scalacaster - Queue](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/Queue.scala)
