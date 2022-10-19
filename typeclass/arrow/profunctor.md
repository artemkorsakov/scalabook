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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FProfunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FProfunctorSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

// ... Все операции родителей
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Profunctor.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
