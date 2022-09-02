# Queue

[Очередь (`Queue`)](https://scala-lang.org/api/3.x/scala/collection/immutable/Queue.html) - 
это последовательность с [FIFO (первым пришёл — первым ушёл)](https://ru.wikipedia.org/wiki/FIFO). 
Элемент добавляется в очередь с помощью метода `enqueue` (или `enqueueAll` - для добавления коллекции в очередь) 
и достается из очереди используя метод `dequeue`. 
Эти операции выполняются за постоянное время.

Вот как можно создать пустую неизменяемую очередь:

```scala
import scala.collection.immutable.Queue
val empty = Queue[Int]()
// empty: Queue[Int] = Queue()
val has1 = empty.enqueue(1)
// has1: Queue[Int] = Queue(1)
val has123 = has1.enqueueAll(List(2, 3))
// has123: Queue[Int] = Queue(1, 2, 3)
val (element, has23) = has123.dequeue
// element: Int = 1
// has23: Queue[Int] = Queue(2, 3)
```

Обратите внимание, что `dequeue` возвращает пару, состоящую из удаленного элемента и остальной части очереди.

[Изменяемая очередь](https://scala-lang.org/api/3.x/scala/collection/mutable/Queue.html)


---

**References:**
- [Scala, Immutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-immutable-collection-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
