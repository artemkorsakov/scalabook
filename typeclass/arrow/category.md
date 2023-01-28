# Category

`Category` - это [`Compose`](compose) с операцией `identity`.
`Category` можно трансформировать в [Monoid](../monoid/monoid) и [PlusEmpty](../monad/plus-empty).

`Category` должен удовлетворять следующим законам (помимо законов родительских типовых классов):
- Identity (тождественность): существует `e ∈ M` такое, что `e + x = x + e = x`


## Описание

```scala
trait Category[=>:[_, _]] extends Compose[=>:]:
  def id[A]: A =>: A

  def empty: PlusEmpty[[A] =>> A =>: A] = new PlusEmpty[[A] =>> A =>: A] with ComposePlus:
    def empty[A]: A =>: A = id

  def monoid[A]: Monoid[A =>: A] = new Monoid[A =>: A] with ComposeSemigroup[A]:
    def empty: A =>: A = id
```

## Примеры

### Функция от одной переменной

```scala
given Category[Function1] with
  override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f

  override def id[A]: A => A = a => a
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FCategory.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FCategorySuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```


---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Category.html)
