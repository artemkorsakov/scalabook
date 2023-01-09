# Profunctor

`Profunctor` ковариантен справа и контравариантен слева.
`Profunctor` позволяет реализовать [InvariantFunctor](../monad/invariant-functor), [Functor](../monad/functor) 
и [ContravariantFunctor](../monad/contravariant-functor).

`Profunctor` должен удовлетворять следующим законам:
- _identity_: если определен метод идентификации `identity` такой, что: `identity(a) == a`, то при отображении этой функции
  на профунктор и слева, и справа, то получим тот же профунктор: `dimap(gad)(identity)(identity) == gad`
- `composite`: при последовательном отображении двух пар функции на профунктор слева и справа - это тоже самое,
  что отображение профунктора на композицию этих функций:  
  `dimap(dimap(gad)(fba)(fde))(fcb)(fef) == dimap(gad)(fba compose fcb)(fef compose fde)`, где 
  `gad: A =>: D, fcb: C => B, fba: B => A, fde: D => E, fef: E => F`

  
## Описание

```scala
trait Profunctor[=>:[_, _]]:
  /** Contramap on `A`. */
  def mapfst[A, B, C](fab: A =>: B)(f: C => A): C =>: B

  /** Functor map on `B`. */
  def mapsnd[A, B, C](fab: A =>: B)(f: B => C): A =>: C

  /** Functor map on `A` and `B`. */
  def dimap[A, B, C, D](fab: A =>: B)(f: C => A)(g: B => D): C =>: D =
    mapsnd(mapfst(fab)(f))(g)

  protected[this] trait SndCovariant[C] extends Functor[[X] =>> =>:[C, X]]:
    extension [A](fa: C =>: A) override def map[B](f: A => B): C =>: B = mapsnd(fa)(f)

  def invariantFunctor: InvariantFunctor[[X] =>> X =>: X] =
    new InvariantFunctor[[X] =>> X =>: X]:
      extension [A](fa: A =>: A)
        override def xmap[B](f: A => B, g: B => A): B =>: B = mapsnd(mapfst(fa)(g))(f)

  def covariantInstance[C]: Functor[[X] =>> =>:[C, X]] =
    new SndCovariant[C] {}

  def contravariantInstance[C]: ContravariantFunctor[[X] =>> =>:[X, C]] =
    new ContravariantFunctor[[X] =>> =>:[X, C]]:
      override def cmap[A, B](fa: B =>: C)(f: A => B): A =>: C = mapfst(fa)(f)
```

## Примеры

### Функция от одной переменной

```scala
given Profunctor[Function1] with
  override def mapfst[A, B, C](fab: A => B)(f: C => A): C => B = f andThen fab

  override def mapsnd[A, B, C](fab: A => B)(f: B => C): A => C = fab andThen f
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FProfunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FProfunctorSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```


---

## Ссылки

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Profunctor.html)
