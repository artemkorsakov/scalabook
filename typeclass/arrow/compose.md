# Compose

`Compose` объединяет две функции в одну.
Функция `compose` позволяет реализовать [Semigroup](../monoid/semigroup) и [Plus](../monad/plus) в терминах объединения функций.

`Compose` должен удовлетворять следующим законам:
- Associativity (ассоциативность): `compose(compose(f, g), h) = compose(f, compose(g, h))`


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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FCompose.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FComposeSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

val f1 = (_:Int) + 1
val f2 = (_:Int) * 100

(f1 >>> f2)(2)   // 300
(f1 <<< f2)(2)   // 201
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Compose.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
