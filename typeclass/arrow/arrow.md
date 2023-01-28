# Arrow

`Arrow` ("стрелка") — это термин, используемый в теории категорий как абстрактное понятие вещи, 
которая ведет себя как функция. "Стрелки", могут быть полезны, если нужно добавить некоторый контекст к функциям и парам.
Каждая "стрелка" образует [`Contravariant`](../monad/contravariant-functor) в одном параметре типа 
и [`Applicative`](../monad/applicative) в другом, как и в случае с обычными функциями.
`Arrow` расширяет [`Split`](split), [`Strong`](strong) и [`Category`](category).

`Arrow` должен удовлетворять законам расширяемых трейтов.


## Описание

```scala
trait Arrow[=>:[_, _]] extends Split[=>:], Strong[=>:], Category[=>:]:
  self =>

  /** 'Поднятие' обычной функции до Arrow. */
  def arr[A, B](f: A => B): A =>: B

  /** Псевдоним для `compose`. */
  final def <<<[A, B, C](fbc: B =>: C, fab: A =>: B): A =>: C =
    compose(fbc, fab)

  /** Обратный `<<<`. */
  final def >>>[A, B, C](fab: A =>: B, fbc: B =>: C): A =>: C =
    compose(fbc, fab)

  /** Меняет пару местами */
  def swap[X, Y]: (X, Y) =>: (Y, X) = arr[(X, Y), (Y, X)] { case (x, y) => (y, x) }

  /** Пропустить `C` нетронутым. */
  override def second[A, B, C](f: A =>: B): (C, A) =>: (C, B) =
    >>>(<<<(first[A, B, C](f), swap), swap)

  /** Запустить `fab` и `fcd` рядом друг с другом. Иногда обозначается как `***`. */
  override def split[A, B, C, D](fab: A =>: B, fcd: C =>: D): (A, C) =>: (B, D) =
    >>>(first[A, B, C](fab), second[C, D, B](fcd))

  /** Запустить `fab` и `fac` на одном и том же `A`. Иногда обозначается как `&&&`. */
  def combine[A, B, C](fab: A =>: B, fac: A =>: C): A =>: (B, C) =
    >>>(arr((a: A) => (a, a)), split(fab, fac))

  /** Contramap on `A`. */
  override def mapfst[A, B, C](fab: A =>: B)(f: C => A): C =>: B =
    >>>[C, A, B](arr(f), fab)

  /** Functor map on `B`. */
  override def mapsnd[A, B, C](fab: A =>: B)(f: B => C): A =>: C =
    <<<[A, B, C](arr(f), fab)

  override def covariantInstance[C]: Applicative[[X] =>> =>:[C, X]] =
    new Applicative[[X] =>> =>:[C, X]]:
      override def unit[A](a: => A): C =>: A = arr(_ => a)

      override def apply[A, B](fab: C =>: (A => B))(fa: C =>: A): C =>: B =
        <<<(arr((y: (A => B, A)) => y._1(y._2)), combine(fab, fa))

  /** Запустить два `fab` рядом друг с другом */
  def product[A, B](fab: A =>: B): (A, A) =>: (B, B) =
    split(fab, fab)
```

## Примеры

### Функция от одной переменной

```scala
given Arrow[Function1] with
  override def arr[A, B](f: A => B): A => B = f
  override def id[A]: A => A = a => a
  override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
  override def first[A, B, C](fa: A => B): ((A, C)) => (B, C) = (a, c) => (fa(a), c)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FArrow.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FArrowSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

val plus1 = (_: Int) + 1
val times2 = (_: Int) * 2
val rev = (_: String).reverse

plus1.first apply (7 -> "abc")     // (8,abc)
plus1.second apply ("def" -> 14)   // (def,15)
plus1 *** rev apply (7 -> "abc")   // (8,cba)
plus1 &&& times2 apply 7           // (8,14)
plus1.product apply (9 -> 99)      // (10,100)

val f1 = (_:Int) + 1
val f2 = (_:Int) * 100

(f1 >>> f2)(2)                     // 300
(f1 <<< f2)(2)                     // 201
```


---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Arrow.html)
