# Plus

Универсальная количественная [полугруппа](https://scalabook.gitflic.space/docs/typeclass/monoid/semigroup). 

`Plus` должен удовлетворять законам полугруппы:

- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`


## Описание

```scala
trait Plus[F[_]]:
  def plus[A](fa1: F[A], fa2: => F[A]): F[A]

  def semigroup[A]: Semigroup[F[A]] = (f1: F[A], f2: F[A]) => plus(f1, f2)
```

## Примеры

### Связанный список

```scala
given Plus[List] with
  def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
```


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

List(1, 2) <+> List(3, 4)                   // List(1, 2, 3, 4)
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FPlus.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FPlusSuite.scala)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/MonadPlus.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Plus.html)
