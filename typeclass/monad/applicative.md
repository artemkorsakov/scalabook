# Applicative

`Applicative` расширяет `Functor` и позволяет работать с несколькими «ящиками».
Он реализует операцию `applicate` 
(также встречаются названия `join`, `sequence`, `joinWith`, `ap`, `apply` - названия взаимозаменяемы), 
которая объединяет `F[A => B]` и `F[A]` в `F[B]`.

Для `Applicative` должны соблюдаться следующие законы:
- `map(unit(x))(f) == unit(f(x))`
- `join(unit(x), unit(y)) == unit((x, y))`

### Примеры Applicative

##### Описание Applicative

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A]) def map[B](f: A => B): F[B]

trait Applicative[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)
```

##### "Обертка"

```scala
case class Id[A](value: A)

given idApplicative: Applicative[Id] with
  override def unit[A](a: => A): Id[A] = Id(a)

  override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab.value(fa.value))
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given optionApplicative: Applicative[Option] with
  override def unit[A](a: => A): Option[A] = Some(a)

  override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
    (fab, fa) match
      case (Some(aToB), Some(a)) => Some(aToB(a))
      case _                     => None
```

##### [Последовательность](../../scala/collections)

```scala
given listApplicative: Applicative[List] with
  override def unit[A](a: => A): List[A] = List(a)

  override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
    fab.flatMap { aToB => fa.map(aToB) }
```

##### [Either](../../fp/handling-errors)

```scala
given eitherApplicative[E]: Applicative[[x] =>> Either[E, x]] with
  override def unit[A](a: => A): Either[E, A] = Right(a)

  override def apply[A, B](fab: Either[E, A => B])(fa: Either[E, A]): Either[E, B] =
    (fab, fa) match
      case (Right(fx), Right(a)) => Right(fx(a))
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
```

##### Writer

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

##### [State](../../fp/state) - функциональное состояние

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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FApplicative.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FApplicativeSuite.scala)


### Реализации Applicative в различных библиотеках


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/applicative)
- [Algebird](https://twitter.github.io/algebird/typeclasses/applicative.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
