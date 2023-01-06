# ApplicativePlus

`ApplicativePlus` - это моноидальный аппликативный функтор, 
[`Applicative`](applicative) комбинированный с [`PlusEmpty`](plus-empty).

`ApplicativePlus` должен удовлетворять законам своих родителей.


## Описание

```scala
trait ApplicativePlus[F[_]] extends Applicative[F] with PlusEmpty[F]
```

## Примеры

### Связанный список

```scala
given ApplicativePlus[List] with
  override def unit[A](a: => A): List[A] = List(a)

  override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
    fab.flatMap { aToB => fa.map(aToB) }

  override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2

  override def empty[A]: List[A] = List.empty[A]
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FApplicativePlus.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FApplicativePlusSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```


---

## References

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/MonadPlus.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/ApplicativePlus.html)
