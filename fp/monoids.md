# Monoids

`(M, +)` является [моноидом](../typeclass/monoid/monoid) для заданного множества `M` и операции `+`,
если удовлетворяет следующим свойствам для любых `x, y, z ∈ M`:
- Closure (замыкание): `x + y ∈ M`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Identity (тождественность): существует `e ∈ M` такое, что `e + x = x + e = x`

Также говорится, что _M — моноид относительно +_.

Законы _associativity_ и _identity_ в совокупности называются моноидными законами (_monoid laws_).

Monoid может быть выражен с помощью `trait`:

```scala
trait Monoid[A]:
 def combine(a1: A, a2: A): A
 def empty: A 
```

Тогда законы примут вид:
- _associativity_: `combine(combine(x, y), z) == combine(x, combine(y, z))`
- _identity_: существует `e ∈ M` такое, что `combine(x, empty) == combine(empty, x) == x`

## Примеры моноидов

### Множество - `String`, операция - конкатенация, пустой элемент - пустая строка

```scala
val stringMonoid: Monoid[String] = new:
  def combine(a1: String, a2: String) = a1 + a2
  val empty = ""
stringMonoid.combine("ab", "cd")
// res0: String = "abcd"
```

### Множество - `List`, операция - конкатенация, пустой элемент - `Nil`

```scala
def listMonoid[A]: Monoid[List[A]] = new:
  def combine(a1: List[A], a2: List[A]): List[A] = a1 ++ a2
  val empty: List[A] = Nil
listMonoid.combine(List(1, 2), List(3, 4))
// res1: List[Int] = List(1, 2, 3, 4)
```

### Множество - `Int`, операция - умножение, пустой элемент - 1

```scala
val intMultiplication: Monoid[Int] = new:
  def combine(a1: Int, a2: Int): Int = a1 * a2
  val empty: Int = 1
intMultiplication.combine(5, 6)
// res2: Int = 30 
```

### Множество - `Boolean`, операция - логическое И (`&&`), пустой элемент - `true`

```scala
val booleanAnd: Monoid[Boolean] = new:
  def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2
  val empty: Boolean = true
booleanAnd.combine(true, false)
// res3: Boolean = false
```

## Использование моноидов

Моноиды тесно связаны со списками.
Например, вот как можно свернуть список, используя моноид:

```scala
def combineAll[A](as: List[A], m: Monoid[A]): A =
  as.foldLeft(m.empty)(m.combine)
combineAll(List(1, 2, 3, 4), intMultiplication)
// res4: Int = 24
```

Тот факт, что операция `combine` моноида является ассоциативной, означает, что можно выбирать, 
как сворачивать структуру данных, такую как список. 
Если есть моноид, можно уменьшить список, используя сбалансированное сворачивание (_balanced fold_), 
которое может быть более эффективным для некоторых операций, а также допускает параллелизм. 

В качестве примера предположим, что есть последовательность `a`, `b`, `c`, `d`, 
которую необходимо сократить, используя некоторый моноид.
_Balanced fold_ выглядит так:

```scala
combine(combine(a, b), combine(c, d))
```

Обратите внимание, что _balanced fold_ допускает параллелизм, 
потому что два внутренних вызова независимы друг от друга и могут выполняться одновременно. 
Но помимо этого, более сбалансированная древовидная структура может быть более эффективной в тех случаях, 
когда стоимость каждого из них пропорциональна размеру его комбинированных аргументов.

Например, рассмотрим производительность этого выражения во время выполнения:

```scala
List("lorem", "ipsum", "dolor", "sit").foldLeft("")(_ + _)
```

На каждом этапе сворачивания (_fold_) выделяется полная промежуточная строка только лишь для того, 
чтобы быть отброшенной, а `String` выделяет более крупную строку на следующем шаге. 
Напомним, что значения являются неизменяемыми, и что `String` вычисляется для строк 
и требует выделения нового массива символов и копирования `a + b` для строк `a` и `b` в этот новый массив. 
Это занимает время, пропорциональное `a.length + b.length`. 

Вот трассировка вычисляемого предыдущего выражения:

```scala
List("lorem", "ipsum", "dolor", "sit").foldLeft("")(_ + _)
List("ipsum", "dolor", "sit").foldLeft("lorem")(_ + _)
List("dolor", "sit").foldLeft("loremipsum")(_ + _)
List("sit").foldLeft("loremipsumdolor")(_ + _)
List().foldLeft("loremipsumdolorsit")(_ + _)
"loremipsumdolorsit"
```

Обратите внимание, что промежуточные строки создаются, а затем сразу же отбрасываются. 
Более эффективной стратегией было бы объединение половинок последовательности, 
что называется сбалансированным сворачиванием (_balanced fold_) — 
сначала строятся `"loremipsum"` и `"dolorsit"`, а затем они складываются вместе.

## Foldable

Операция _fold_ позволяет агрегировать.
Она берет начальный элемент и объединяет его с типом [_Foldable_](../typeclass/monad/foldable), 
следуя способу, предоставленному методом `f`.

_Foldable_ может быть выражен так:

```scala
trait Foldable[F[_]]:
  extension [A](as: F[A])
    def foldRight[B](acc: B)(f: (A, B) => B): B
    def foldLeft[B](acc: B)(f: (B, A) => B): B
    def foldMap[B](f: A => B)(using m: Monoid[B]): B
    def combineAll(using m: Monoid[A]): A
```

При этом каждую операцию можно выразить через другие:

```scala
trait Monoid[A]:
  def combine(a1: A, a2: A): A
  def empty: A

object Monoid:
  def dual[A](m: Monoid[A]): Monoid[A] = new:
    def combine(x: A, y: A): A = m.combine(y, x)
    val empty: A = m.empty

  def endoMonoid[A]: Monoid[A => A] = new:
    def combine(a1: A => A, a2: A => A): A => A = a1 andThen a2
    val empty: A => A = identity

trait Foldable[F[_]]:
  import Monoid.{endoMonoid, dual}

  extension [A](as: F[A])
    def foldRight[B](acc: B)(f: (A, B) => B): B =
      foldMap(f.curried)(using endoMonoid[B])(acc)

    def foldLeft[B](acc: B)(f: (B, A) => B): B =
      foldMap(a => b => f(b, a))(using dual(endoMonoid[B]))(acc)

    def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
      foldRight(mb.empty)((a, b) => mb.combine(f(a), b))

    def combineAll(using ma: Monoid[A]): A =
      foldLeft(ma.empty)(ma.combine)
```

Поэтому при реализации `Foldable` достаточно переопределить, как минимум, один метод.

## Операции с моноидами

Моноиды можно объединять: 
если `A` и `B` - это моноиды, то кортеж `(A, B)` также является моноидом (и называется _продуктом_)

```scala
given productMonoid[A, B](using ma: Monoid[A], mb: Monoid[B]): Monoid[(A, B)] with
  def combine(x: (A, B), y: (A, B)): (A, B) =
    (ma.combine(x(0), y(0)), mb.combine(x(1), y(1)))

  val empty: (A, B) = (ma.empty, mb.empty)
```

Моноидом также является `Map`, если доступен моноид для его значений:

```scala
given mapMergeMonoid[K, V](using mv: Monoid[V]): Monoid[Map[K, V]] with
  def combine(a: Map[K, V], b: Map[K, V]): Map[K, V] =
    (a.keySet ++ b.keySet).foldLeft(empty) { (acc, k) =>
      acc.updated(k, mv.combine(a.getOrElse(k, mv.empty), b.getOrElse(k, mv.empty)))
    }
    
  val empty: Map[K, V] = Map()
```

Моноидом является функция, результаты которой являются моноидами.

```scala
given functionMonoid[A, B](using mb: Monoid[B]): Monoid[A => B] with
  def combine(f: A => B, g: A => B): A => B = a => mb.combine(f(a), g(a))

  val empty: A => B = _ => mb.empty
```

Тот факт, что несколько моноидов могут быть объединены в один, означает, 
что можно одновременно выполнять несколько вычислений при свертывании структуры данных. 
Например, можно взять длину и сумму списка одновременно, чтобы вычислить среднее значение:

```scala
val p = List(1, 2, 3, 4).foldMap(a => (1, a))
// p: Tuple2[Int, Int] = (4, 10)
val mean = p(0) / p(1).toDouble
// mean: Double = 0.4
```


---

## Ссылки

- [Functional Programming in Scala, Second Edition, Chapter 10](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
