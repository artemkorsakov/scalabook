# Arrow

`Arrow` - категория [`Category`](category), поддерживающая все обычные функции, а также объединяющая "стрелки" по продуктам. 
Каждая "стрелка" образует [`Contravariant`](../monad/contravariant-functor) в одном параметре типа 
и [`Applicative`](../monad/applicative) в другом, как и в случае с обычными функциями.
`Arrow` расширяет [`Split`](split), [`Strong`](strong) и [`Category`](category).

`Arrow` должен удовлетворять законам расширяемых трейтов.


## Описание

```scala
trait Compose[=>:[_, _]]:
  /** Ассоциативный `=>:` бинарный оператор. */
  def compose[A, B, C](f: B =>: C, g: A =>: B): A =>: C

  def plus: Plus[[A] =>> A =>: A] = new Plus[[A] =>> A =>: A]:
    def plus[A](f1: A =>: A, f2: => A =>: A): A =>: A = compose(f1, f2)

  def semigroup[A]: Semigroup[A =>: A] = (f1: A =>: A, f2: A =>: A) => compose(f1, f2)
```

## Примеры

### Функция от одной переменной

```scala
given Compose[Function1] with
  override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FArrow.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FArrowSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

val plus1 = (_: Int) + 1
val times2 = (_: Int) * 2
val rev = (_: String).reverse

plus1.first apply (7 -> "abc")     // (8,abc)
plus1.second apply ("def" -> 14)   // (def,15)
plus1 *** rev apply (7 -> "abc")   // (8,cba)
plus1 &&& times2 apply 7           // (8,14)
plus1.product apply (9 -> 99)      // (10,100)
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
