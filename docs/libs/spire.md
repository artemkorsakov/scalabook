# Spire

Spire — это числовая библиотека для Scala, которая должна быть универсальной, быстрой и точной.

Используя такие функции, как специализация, макросы, классы типов и неявные выражения, Spire прилагает все усилия, 
чтобы бросить вызов общепринятым представлениям о компромиссах между производительностью и точностью. 
Основная цель — позволить разработчикам писать эффективный числовой код 
без необходимости "встраивать" определенные числовые представления. 
В большинстве случаев универсальные реализации, использующие специализированные классы типов Spire, 
работают идентично соответствующим прямым реализациям.

Пример использования библиотеки Spire:

```dotty
import spire.*
import spire.implicits.*
import spire.math.*

Complex(3.0,5.0).sin
// val res0: spire.math.Complex[Double] = (10.472508533940392 + -73.46062169567367i)
```

## Числовые типы

Помимо поддержки всех встроенных числовых типов Scala, 
Spire представляет несколько новых, все из которых можно найти в `spire.math`:

- `Natural` - беззнаковое, неизменяемое целое число произвольной точности
- `Rational` - дроби целых чисел с идеальной точностью
- `Algebraic` - лениво вычисляемые алгебраические числа произвольной точности
- `Real` - реализация вычислимых действительных чисел
- `Complex[A]` - комплексные числа, точки на комплексной плоскости
- `Jet[A]` - N-мерные двойные числа для автоматического дифференцирования
- `Quaternion[A]`- расширение комплексных чисел в четырехмерное пространство
- от `UByte` до `ULong` - классы значений, поддерживающие беззнаковые операции
- `SafeLong` - быстрый целочисленный тип с защитой от переполнения
- `Number` - запечатанный тип с поддержкой традиционной числовой опоры
- `Interval[A]` - арифметика на открытых, закрытых и несвязанных интервалах
- `Polynomial[A]` - одномерные (с одной переменной) полиномиальные выражения
- `Trilean` - класс значений, поддерживающий трехзначную логику
- `FixedPoint` - дроби с `Long` числителем и неявным знаменателем (в доп.)

## Классы типов

Spire предоставляет классы типов для поддержки широкого спектра унарных и двоичных операций с числами. 
Классы типов являются специализированными, не выполняют упаковку и используют неявные выражения 
для обеспечения удобного инфиксного синтаксиса.

Классы типов общего назначения можно найти в пакете `spire.math`:

- `Numeric[A]` - все типы чисел, прилагает все усилия для поддержки операторов
- `Fractional[A]` - типы дробных чисел, где `/` - честное деление
- `Integral[A]` - типы целых чисел, где `/` - деление по уровням

Некоторые классы типов общего назначения построены на основе набора более фундаментальных классов типов, 
определенных в `spire.algebra`. 
Многие из них соответствуют понятиям абстрактной алгебры:

- `Eq[A]` - типы, которые можно сравнивать на равенство
- `Order[A]` - типы, которые можно сравнивать и упорядочивать
- `PartialOrder[A]` - типы, которые можно сравнивать на равенство и для которых упорядочены определенные пары
- `Semigroup[A]` - типы с ассоциативным бинарным оператором `|+|`
- `Monoid[A]` - полугруппы, имеющие единичный элемент
- `Group[A]` - моноиды, имеющие обратный оператор
- `(Left/Right/)Action[P, G]` - действия влево/вправо для полугрупп/моноидов/групп
- `Semiring[A]` - типы, образующие полугруппы под `+` и `*`
- `Rng[A]` - типы, которые образуют группу `+` и полугруппу под `*`
- `Rig[A]` - типы, образующие моноиды под `+` и `*`
- `Ring[A]` - типы, образующие группу под `+` и моноид под `*`
- `EuclideanRing[A]` - кольца с частными и остатками (евклидово деление)
- `Field[A]` - евклидовы кольца с мультипликативными инверсиями (взаимными)
- `Signed[A]` - типы, имеющие знак (отрицательный, ноль, положительный)
- `NRoot[A]` - типы, поддерживающие k-корни, журналы и дробные степени
- `Module[V,R]` - типы, образующие левый R-модуль
- `VectorSpace[V,F]` - типы, образующие векторное пространство
- `NormedVectorSpace[V,F]` - типы с соответствующей нормой
- `InnerProductSpace[V,F]` - типы с внутренним произведением
- `MetricSpace[V,R]` - типы со связанной метрикой
- `Trig[A]` - типы, поддерживающие тригонометрические функции
- `Bool[A]` - типы, образующие булеву алгебру
- `Heyting[A]` - типы, образующие алгебру Гейтинга

Варианты `Semigroup`/`Monoid`/`Group`/`Action` с частичными операциями определены в подпакете `spire.algebra.partial`.

Помимо самих классов типов, `spire.implicits` определяет множество неявных функций, 
которые предоставляют унарные и инфиксные операторы для классов типов. 
Самый простой способ использовать их — импортировать файлы `spire.implicits.*`.

## С чего начать?

Spire содержит множество типов, а также другие механизмы, обеспечивающие удобство использования. 
Самый простой способ использовать Spire — импортировать зависимости:

```dotty
import spire.algebra.*   // предоставляет алгебраические классы типов
import spire.implicits.* // обеспечивает инфиксные операторы, инстансы и конверсию
import spire.math.*      // обеспечивает функции, типа и классы типов
```

## Операции, отсортированные по классам типов

Ниже приводится более подробное описание классов типов, предоставляемых Spire, а также операторов, которые они используют. 
Хотя Spire по возможности избегает введения новых операторов, в некоторых случаях это было неизбежно.

- [Eq, Order и PartialOrder](spire/eq.md)
- [Semigroup, Monoid и Group](spire/semigroup.md)
- [Ring и компания](spire/rings.md)
- [Vector и компания](spire/vector.md)
- [Числа](spire/numeric.md)
- [Bool](spire/bool.md)
- [Тригонометрия](spire/trig.md)
- [Алгебра Гейтинга](spire/heyting.md)

## Синтаксис

Используя строковую интерполяцию и макросы, Spire обеспечивает удобный синтаксис для числовых типов. 
Эти макросы оцениваются во время компиляции, и любые ошибки, с которыми они сталкиваются, возникают во время компиляции.

Например:

```dotty
object LiteralsDemo:
  import spire.syntax.literals.*

  // byte-ы и short-ы
  val x    = b"100" // без аннотации типа!
  val y    = h"999"
  val mask = b"255" // беззнаковая константа сконвертированная в знаковую (-1)

  // дроби
  val n1 = r"1/3"
  val n2 = r"1599/115866" // упроститься при компиляции до 13/942
```

```dotty
object SIDemo:
  // SI нотация для больших чисел
  import spire.syntax.literals.si.* // также доступны `.us` и `.eu`
  
  val w = i"1 944 234 123" // Int
  val x = j"89 234 614 123 234 772" // Long
  val y = big"123 234 435 456 567 678 234 123 112 234 345" // BigInt
  val z = dec"1 234 456 789.123456789098765" // BigDecimal
```

Spire также предоставляет макрос цикла `cfor`, синтаксис которого немного похож на традиционный цикл `for` из C или Java. 
Этот макрос расширяется до хвостовой рекурсивной функции, которая встраивает литеральные аргументы функции.

Макрос может быть вложен сам в себя и выгодно отличается от других конструкций цикла в Scala, таких как `for` и `while`:

```dotty
import spire.syntax.cfor.*

// печатает числа от 0 до 9
cfor(0)(_ < 10, _ + 1): i =>
  println(i)

// простой алгоритм сортировки
def selectionSort(ns: Array[Int]): Unit =
  val limit = ns.length - 1
  cfor(0)(_ < limit, _ + 1): i =>
    var k = i
    val n = ns(i)
    cfor(i + 1)(_ <= limit, _ + 1): j =>
      if ns(j) < ns(k) then k = j
    ns(i) = ns(k)
    ns(k) = n
```

## Остальные операции

- [Сортировка, выбор и поиск](spire/sorting.md)


### Pseudo-Random Number Generators

Spire comes with many different PRNG implementations, which extends
the `spire.random.Generator` interface. Generators are mutable RNGs
that support basic operations like `nextInt`. Unlike Java, generators
are not threadsafe by default; synchronous instances can be attained
by calling the `.sync` method.

Spire supports generating random instances of arbitrary types using
the `spire.random.Dist[A]` type class. These instances represent a
strategy for getting random values using a `Generator` instance. For
instance:

```scala mdoc:silent
object DistDemo {
  import spire.random.Dist
  
  val rng = spire.random.rng.Cmwc5()
  
  // produces a map with ~10-20 entries
  implicit val nextmap: Dist[Map[Int, Complex[Double]]] = Dist.map[Int, Complex[Double]](10, 20)
  val m = rng.next[Map[Int, Complex[Double]]]

  // produces a double in [0.0, 1.0)
  val n = rng.next[Double]
  
  // produces a complex number, with real and imaginary parts in [0.0, 1.0)
  val q = rng.next[Complex[Double]]
}
```

Unlike generators, `Dist[A]` instances are immutable and composable,
supporting operations like `map`, `flatMap`, and `filter`. Many default
instances are provided, and it's easy to create custom instances for
user-defined types.

### Miscellany

In addition, Spire provides many other methods which are "missing" from
`java.Math` (and `scala.math`), such as:

* `log(BigDecimal): BigDecimal`
* `exp(BigDecimal): BigDecimal`
* `pow(BigDecimal): BigDecimal`
* `pow(Long): Long`
* `gcd(Long, Long): Long`
* *and so on...*

### Benchmarks

In addition to unit tests, Spire comes with a relatively fleshed-out set of
[JMH](https://openjdk.java.net/projects/code-tools/jmh) micro-benchmarks. To
run the benchmarks from within SBT, change to the `benchmark` subproject
and then `jmh:run -l` to see a list of benchmarks:

```
$ sbt
> project benchmark
> jmh:run -l

[info] Benchmarks:
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateGeneric
[info] spire.benchmark.AddBenchmarks.addComplexFloatStateDirect
...
```

To run all available benchmarks:
```
> jmh:run
```

To run a specific benchmark method:
```
> jmh:run spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
```

To run all benchmarks in a specific class:
```
> jmh:run spire.benchmark.AddBenchmarks
```

To see all available JMH usage options:
```
> jmh:run -h
```

If you plan to contribute to Spire, please make sure to run the relevant
benchmarks to be sure that your changes don't impact performance. Benchmarks
usually include comparisons against equivalent Scala or Java classes to try to
measure relative as well as absolute performance.


---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
