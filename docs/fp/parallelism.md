# Параллелизм

Параллелизм с помощью чистых функций.

Рассмотрим операцию суммирования чисел:

```dotty
def sum(ints: Seq[Int]): Int =
  ints.foldLeft(0)((a, b) => a + b)
```

Здесь используется последовательное сворачивание, когда мы проходим по всей коллекции элемент за элементом,
каждый раз складывая текущий элемент к итоговому результату.

Вместо последовательного сворачивания можно было бы использовать алгоритм "разделяй и властвуй":

```dotty
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

Для параллельных вычислений с помощью чистых функций можно определить следующий API:

```dotty
def unit[A](a: A): Par[A]
def fork[A](a: => Par[A]): Par[A]
def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

extension [A](pa: Par[A]) 
  def run: A
  def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C]
```

- `unit` "поднимает" константу до параллельных вычислений
- `fork` помечает для параллельного вычисления.
  На самом деле вычисление не произойдет, пока не будет принудительно запущена с помощью `run`.
- `lazyUnit` заключает свой невычисленный аргумент в `Par` и помечает его для параллельного вычисления.
- `run` извлекает значение из `Par`, фактически выполняя вычисление
- `map2` объединяет результаты двух параллельных вычислений с помощью бинарной функции

Тогда сумму можно реализовать так:

```dotty
def sum(ints: IndexedSeq[Int]): Par[Int] =
  if ints.size <= 1 then
    Par.unit(ints.headOption.getOrElse(0))
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.map2(sum(l), sum(r))(_ + _)
```

При этом для параллельных вычислений должен выполняться следующий закон:

```dotty
unit(x).map(f) == unit(f(x))
```


---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 7](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

