# Group

Моноид (_monoid_) — это [полугруппа](semigroup) с единичным элементом.

Более формально: `(M, +)` является моноидом для заданного множества `M` и операции `+` (`combine` с типом `(A, A) => A`),
если удовлетворяет следующим свойствам для любых `x, y, z ∈ M`:
- Closure (замыкание): `x + y ∈ M`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)` или 
  `m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)`
- Identity (тождественность): существует `e ∈ M` (`empty: A`) такой, что `e + x = x + e = x` 
  или `(m.combine(x, m.empty) == x) && (m.combine(m.empty, x) == x)`

Также говорится, что _M — моноид относительно +_.

## Описание 

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

## Примеры

### Числа относительно сложения с 0

```scala
given sumMonoidInstance: Monoid[Int] with
  val empty = 0
  def combine(x: Int, y: Int): Int = x + y
```

### Числа относительно умножения с 1

```scala
given productMonoidInstance: Monoid[Int] with
  val empty = 1
  def combine(x: Int, y: Int): Int = x * y
```

### Строки

```scala
given stringMonoidInstance: Monoid[String] with
  val empty = ""
  def combine(x: String, y: String): String = x + y
```

### [Последовательность](../../scala/collections)

```scala
given listMonoidInstance[T]: Monoid[List[T]] with
  val empty = List.empty[T]
  def combine(x: List[T], y: List[T]): List[T] = x ++ y
```

### [Кортеж](../../scala/collections/tuple) от двух и более моноидов

```scala
given nestedMonoidInstance[A, B](using aMonoid: Monoid[A], bMonoid: Monoid[B]): Monoid[(A, B)] with
  lazy val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
  def combine(x: (A, B), y: (A, B)): (A, B) = (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))
```

### [Option](../../scala/fp/functional-error-handling)

`Option` является моноидом, если его параметр типа - полугруппа, например:

```scala
given optionMonoidInstance[A: Semigroup]: Monoid[Option[A]] with
  val empty: Option[A] = None

  def combine(x: Option[A], y: Option[A]): Option[A] =
    (x, y) match
      case (Some(a1), Some(a2)) => Some(summon[Semigroup[A]].combine(a1, a2))
      case (Some(_), None)      => x
      case (None, Some(_))      => y
      case (None, None)         => None
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FMonoid.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FMonoidSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

mzero[List[Int]]             // List()
```

## Реализация в Cats

```scala
import cats.*
import cats.implicits.*

Monoid[Int].combine(1, Monoid[Int].empty)   // 1
"Hi " |+| "there" |+| Monoid[String].empty  // Hi there
```


---

## References

- [Algebird](https://twitter.github.io/algebird/typeclasses/monoid.html)
- [Cats](https://typelevel.org/cats/typeclasses/monoid.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Monoid.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monoid.html#Monoid)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-monoid)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Monoid.html)
