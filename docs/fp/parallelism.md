# Параллелизм

Параллелизм с помощью чистых функций.

Рассмотрим операцию суммирования чисел:

```scala
def sum(ints: Seq[Int]): Int =
  ints.foldLeft(0)((a, b) => a + b)
```

Здесь используется последовательное сворачивание, когда мы проходим по всей коллекции элемент за элементом,
каждый раз складывая текущий элемент к итоговому результату.

Вместо последовательного сворачивания можно было бы использовать алгоритм "разделяй и властвуй":

```scala
def sum(ints: IndexedSeq[Int]): Int =
  if ints.size <= 1 then
    ints.headOption.getOrElse(0)
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    sum(l) + sum(r)
```

Последовательность делится пополам с помощью функции `splitAt`, рекурсивно суммируются обе половины, 
а затем результаты объединяются. 
И в отличие от реализации на основе `foldLeft`, 
реализацию "разделяй и властвуй" можно распараллелить — две половины можно суммировать параллельно.

### Тип данных для параллельных вычислений

Для параллельных вычислений можно определить тип данных с двумя методами:

- `def unit[A](a: => A): Par[A]`, для получения неоцененного `A` и возврата вычисления,
  которое могло бы оценить его в отдельном потоке.
- `def get[A](a: Par[A]): A`, для извлечения результирующего значения из параллельного вычисления.

```scala
import java.util.concurrent.*

object Par:
  opaque type Par[A] = ExecutorService => Future[A]

  extension [A](pa: Par[A]) def run(s: ExecutorService): Future[A] = pa(s)

  def unit[A](a: A): Par[A] =
    es => UnitFuture(a)

  private case class UnitFuture[A](get: A) extends Future[A]:
    val isDone: Boolean      = true
    val isCancelled: Boolean = false

    def get(timeout: Long, units: TimeUnit): A  = this.get
    def cancel(evenIfRunning: Boolean): Boolean = false
```







---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 7](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

