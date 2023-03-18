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

Тогда сумму можно реализовать так:

```scala
def sum(ints: IndexedSeq[Int]): Int =
  if ints.size <= 1 then
    ints.headOption.getOrElse(0)
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    val sumL: Par[Int] = Par.unit(sum(l))
    val sumR: Par[Int] = Par.unit(sum(r))
    Par.get(sumL) + Par.get(sumR)
```

Здесь возникает проблема в том, что вычисления в реальности не параллелятся: в момент вызова `Par.get(sumL)`
мы будем вынуждены ждать окончания вычисления `sumL`.

Необходимо объединять параллельные вычисления и отложить вызов `get` до финального этапа:

```scala
def sum(ints: IndexedSeq[Int]): Par[Int] =
  if ints.size <= 1 then
    Par.unit(ints.headOption.getOrElse(0))
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.map2(sum(l), sum(r))(_ + _)
```

Здесь возникает необходимость в определении функции `map2`: 

```scala
// Через метод расширения
extension [A](pa: Par[A]) 
  def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C]

// Стандартная функция
def map2[A, B, C](pa: Par[A], pb: Par[B])(f: (A, B) => C): Par[C]
```






---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 7](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

