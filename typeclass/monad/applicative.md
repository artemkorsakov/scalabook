# Applicative

`Applicative` расширяет `Apply` (и `InvariantApplicative`) и позволяет работать с несколькими «ящиками».
`Applicative`, дополнительно к операциям `Apply`, реализует операцию `unit` (другие названия: `point`, `pure`),
оборачивающую значение произвольного типа `A` в `Applicative`.

Для `Applicative` должны соблюдаться следующие законы (помимо законов родительских типовых классов):
- Identity: `apply(unit(identity))(fa) == fa`
- `unit(x).map(f) == unit(f(x))`
- `fa.map(f) == apply(unit(f))(fa)`
- Homomorphism: `apply(unit(f))(unit(x)) == unit(f(x))`
- Interchange: `apply(f)(unit(a)) == apply(unit((f: A => B) => f(a)))(f)`


### Примеры

#### Описание 

```scala
trait Applicative[F[_]] extends Apply[F] with InvariantApplicative[F] :
  def unit[A](a: => A): F[A]
  override def xunit0[A](a: => A): F[A] = unit(a)

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)
```

#### "Обертка"

```scala
case class Id[A](value: A)

given idApplicative: Applicative[Id] with
  override def unit[A](a: => A): Id[A] = Id(a)

  override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab.value(fa.value))
```

#### [Option](../../scala/fp/functional-error-handling)

```scala
given optionApplicative: Applicative[Option] with
  override def unit[A](a: => A): Option[A] = Some(a)

  override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
    (fab, fa) match
      case (Some(aToB), Some(a)) => Some(aToB(a))
      case _                     => None
```

#### [Последовательность](../../scala/collections)

```scala
given listApplicative: Applicative[List] with
  override def unit[A](a: => A): List[A] = List(a)

  override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
    fab.flatMap { aToB => fa.map(aToB) }
```

#### [Either](../../fp/handling-errors)

```scala
given eitherApplicative[E]: Applicative[[x] =>> Either[E, x]] with
  override def unit[A](a: => A): Either[E, A] = Right(a)

  override def apply[A, B](fab: Either[E, A => B])(fa: Either[E, A]): Either[E, B] =
    (fab, fa) match
      case (Right(fx), Right(a)) => Right(fx(a))
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
```

#### [Writer](../../fp/writer) - функциональный журнал

```scala
case class Writer[W, A](run: () => (W, A))

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

given writerApplicative[W](using monoid: Monoid[W]): Applicative[[x] =>> Writer[W, x]] with
  override def unit[A](a: => A): Writer[W, A] =
    Writer[W, A](() => (monoid.empty, a))

  override def apply[A, B](fab: Writer[W, A => B])(fa: Writer[W, A]): Writer[W, B] =
    Writer { () =>
      val (w0, aToB) = fab.run()
      val (w1, a) = fa.run()
      (monoid.combine(w0, w1), aToB(a))
    }
```

#### [State](../../fp/state) - функциональное состояние

```scala
case class State[S, +A](run: S => (S, A))

given stateApplicative[S]: Applicative[[x] =>> State[S, x]] with
  override def unit[A](a: => A): State[S, A] =
    State[S, A](s => (s, a))

  override def apply[A, B](fab: State[S, A => B])(fa: State[S, A]): State[S, B] =
    State { s =>
      val (s0, aToB) = fab.run(s)
      val (s1, a) = fa.run(s0)
      (s1, aToB(a))
    }
```

#### Tuple applicative

Как и монады, аппликативные функторы замкнуты относительно произведений; 
поэтому два независимых идиоматических эффекта обычно могут быть слиты в один, их продукт.

```scala
given tupleApplicative[F[_]: Applicative, G[_]: Applicative]: Applicative[[X] =>> (F[X], G[X])] with
  type FG[A] = (F[A], G[A])

  override def unit[A](a: => A): FG[A] = (summon[Applicative[F]].unit(a), summon[Applicative[G]].unit(a))

  override def apply[A, B](fab: FG[A => B])(fa: FG[A]): FG[B] =
    (summon[Applicative[F]].apply(fab._1)(fa._1), summon[Applicative[G]].apply(fab._2)(fa._2))
```


#### Composite Applicative

В отличие от монад, аппликативные функторы также закрыты по композиции; 
поэтому два последовательно зависимых аппликативных эффекта обычно могут быть объединены в один, их состав. 
Это называется композицией в `Applicative`:

```scala
given compositeApplicative[F[_]: Applicative, G[_]: Applicative]: Applicative[[X] =>> F[G[X]]] with
  override def unit[A](a: => A): F[G[A]] = summon[Applicative[F]].unit(summon[Applicative[G]].unit(a))

  override def apply[A, B](fab: F[G[A => B]])(fa: F[G[A]]): F[G[B]] =
    val applicativeF = summon[Applicative[F]]
    val applicativeG = summon[Applicative[G]]
    val tmp: F[G[A] => G[B]] = applicativeF.map(fab)(ga2b => applicativeG.apply(ga2b))
    applicativeF.apply(tmp)(fa)
```

#### Nested

```scala
final case class Nested[F[_], G[_], A](value: F[G[A]])

given nestedApplicative[F[_], G[_]](using
    applF: Applicative[F],
    applG: Applicative[G],
    functorF: Functor[F]
): Applicative[[X] =>> Nested[F, G, X]] with
  override def unit[A](a: => A): Nested[F, G, A] = Nested(applF.unit(applG.unit(a)))

  override def apply[A, B](fab: Nested[F, G, A => B])(fa: Nested[F, G, A]): Nested[F, G, B] =
    val curriedFuncs: G[A => B] => G[A] => G[B] = gaTob => ga => applG.apply(gaTob)(ga)
    val fgaToB: F[G[A => B]] = fab.value
    val fGaToGb: F[G[A] => G[B]] = functorF.map(fgaToB)(curriedFuncs)
    val fga: F[G[A]] = fa.value
    val fgb: F[G[B]] = applF.apply(fGaToGb)(fga)
    Nested(fgb)
```

#### IO

```scala
final case class IO[R](run: () => R)

given ioApplicative: Applicative[IO] with
  override def unit[A](a: => A): IO[A] = IO(() => a)
  override def apply[A, B](fab: IO[A => B])(fa: IO[A]): IO[B] = IO(() => fab.run()(fa.run()))
```


[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FApplicative.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FApplicativeSuite.scala)


### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

// ... Все операции родителей

1.point[List]                                         // List(1)
1.η[List]                                             // List(1)
```


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/applicative)
- [Algebird](https://twitter.github.io/algebird/typeclasses/applicative.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Applicative.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Applicative.html)
- [Applicative Programming with Effects](https://www.staff.city.ac.uk/~ross/papers/Applicative.html)
