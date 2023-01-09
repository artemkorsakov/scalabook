# Split

`Split` - полугруппоид ([`Compose`](compose)), допускающий произведения.

`Split` должен удовлетворять законам `Compose`.


## Описание

```scala
trait Split[=>:[_, _]] extends Compose[=>:]:
  def split[A, B, C, D](f: A =>: B, g: C =>: D): (A, C) =>: (B, D)
```

## Примеры

### Функция от одной переменной

```scala
object Split:
  given Split[Function1] with
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f

    override def split[A, B, C, D](f: A => B, g: C => D): ((A, C)) => (B, D) =
      (a, c) => (f(a), g(c))
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FSplit.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FSplitSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```


## Реализация в Cats

```scala
import cats.*, cats.data.*, cats.syntax.all.*

lazy val f = (_:Int) + 1
lazy val g = (_:Int) * 100

(f split g)((1, 1))
// res0: (Int, Int) = (2, 100)
```


---

## Ссылки

- [Herding Cats](http://eed3si9n.com/herding-cats/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Split.html)
