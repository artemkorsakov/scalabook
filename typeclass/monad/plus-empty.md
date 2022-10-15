# PlusEmpty

Универсальный количественный [моноид](../monoid/monoid).

`PlusEmpty` должен удовлетворять законам моноида:
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Identity (тождественность): существует `e ∈ M` такое, что `e + x = x + e = x`


## Описание

```scala
trait PlusEmpty[F[_]] extends Plus[F]:
  self =>

  def empty[A]: F[A]

  def monoid[A]: Monoid[F[A]] =
    new Monoid[F[A]]:
      override def combine(f1: F[A], f2: F[A]): F[A] = plus(f1, f2)
      override def empty: F[A] = self.empty[A]
```

## Примеры

### Связанный список

```scala
given PlusEmpty[List] with
  def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
  def empty[A]: List[A] = List.empty[A]
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FPlusEmpty.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FPlusEmptySuite.scala)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

// ... Все операции родителей

(PlusEmpty[List].empty: List[Int])                  // List()
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/PlusEmpty.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/MonadPlus.html)
