# Band

_Band_ — это полугруппа, которая также является идемпотентной, 
т.е. добавление значения к самому себе приводит к тому же значению.

Band должна удовлетворять следующим законам:
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Idempotency (идемпотентность): для любых `a ∈ M` такое, что `a + a = a`

### Примеры

##### Описание
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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FBand.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FBandSuite.scala)


---

**References:**
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Band.html)
