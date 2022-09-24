# Monoid

Моноид (_monoid_) — это полугруппа с единичным элементом.
Более формально: `(M, +)` является моноидом для заданного множества `M` и операции `+`,
если удовлетворяет следующим свойствам для любых `x, y, z ∈ M`:
- Closure (замыкание): `x + y ∈ M`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Identity (тождественность): существует `e ∈ M` такое, что `e + x = x + e = x`

Также говорится, что _M — моноид относительно +_.

### Примеры моноидов

##### Описание моноида
```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

##### Натуральные числа N являются моноидами относительно сложения (`0` является identity элементом)

```scala
given sumMonoidInstance: Monoid[Int] with
  val empty = 0
  def combine(x: Int, y: Int): Int = x + y
```

##### Натуральные числа N являются моноидами относительно умножения (`1` является identity элементом)

```scala
given productMonoidInstance: Monoid[Int] with
  val empty = 1
  def combine(x: Int, y: Int): Int = x * y
```

##### Строки образуют моноид относительно конкатенации (`""` является identity элементом)

```scala
given stringMonoidInstance: Monoid[String] with
  val empty = ""
  def combine(x: String, y: String): String = x + y
```

##### [Последовательность](../../scala/collections) образует моноид относительно операции объединения (пустая последовательность является identity элементом)

```scala
given listMonoidInstance[T]: Monoid[List[T]] with
  val empty = List.empty[T]
  def combine(x: List[T], y: List[T]): List[T] = x ++ y
```

##### [Кортеж](../../scala/collections/tuple) от двух и более моноидов также является моноидом

```scala
given nestedMonoidInstance[A, B](using aMonoid: Monoid[A], bMonoid: Monoid[B]): Monoid[(A, B)] with
  lazy val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
  def combine(x: (A, B), y: (A, B)): (A, B) = (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))
```

[Пример кода](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroup.scala&plain=1)

[Пример кода проверки полугруппы](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroupSuite.scala)


### Реализации моноидов в различных библиотеках


---

**References:**
- [Algebird](https://twitter.github.io/algebird/typeclasses/monoid.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
