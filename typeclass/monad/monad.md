# Monad

Монада (_monad_) - это `Functor` и `Applicative` с дополнительной функцией: `flatten` (сведение: `F[F[A]] -> F[A]`). 
Что позволяет определить `flatMap` — `map`, за которой следует `flatten`.

Для `Monad` должны соблюдаться следующие законы:
- identities:
  - `flatMap(apply(x))(fn) == fn(x)`
  - `flatMap(m)(apply _) == m`
- associativity на flatMap:
  - `flatMap(flatMap(m)(f))(g) == flatMap(m) { x => flatMap(f(x))(g) }`


### Примеры монад

##### Описание монады

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A]) def map[B](f: A => B): F[B]

trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))
```

##### "Обертка"

```scala
case class Id[A](value: A)

given idMonad: Monad[Id] with
  override def unit[A](a: => A): Id[A] = Id(a)

  extension [A](fa: Id[A]) override def flatMap[B](f: A => Id[B]): Id[B] = f(fa.value)
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given optionMonad: Monad[Option] with
  override def unit[A](a: => A): Option[A] = Some(a)

  extension [A](fa: Option[A])
    override def flatMap[B](f: A => Option[B]): Option[B] =
      fa match
        case Some(a) => f(a)
        case None    => None
```

##### [Последовательность](../../scala/collections)

```scala
given listMonad: Monad[List] with
  override def unit[A](a: => A): List[A] = List(a)

  extension [A](fa: List[A]) override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)
```

##### [Either](../../fp/handling-errors)

```scala
given eitherMonad[E]: Monad[[x] =>> Either[E, x]] with
  override def unit[A](a: => A): Either[E, A] = Right(a)

  extension [A](fa: Either[E, A])
    override def flatMap[B](f: A => Either[E, B]): Either[E, B] =
      fa match
        case Right(a) => f(a)
        case Left(e)  => Left(e)
```

##### Writer

```scala
case class Writer[W, A](run: () => (W, A))

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

given writerMonad[W](using monoid: Monoid[W]): Monad[[x] =>> Writer[W, x]] with
  override def unit[A](a: => A): Writer[W, A] =
    Writer[W, A](() => (monoid.empty, a))

  extension [A](fa: Writer[W, A])
    override def flatMap[B](f: A => Writer[W, B]): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = fa.run()
        val (w2, b) = f(a).run()
        (monoid.combine(w1, w2), b)
      }
```

##### [State](../../fp/state) - функциональное состояние

```scala
case class State[S, +A](run: S => (S, A))

given stateMonad[S]: Monad[[x] =>> State[S, x]] with
  override def unit[A](a: => A): State[S, A] =
    State[S, A](s => (s, a))

  extension [A](fa: State[S, A])
    override def flatMap[B](f: A => State[S, B]): State[S, B] =
      State[S, B] { s =>
        val (s1, a) = fa.run(s)
        f(a).run(s1)
      }
```


### Реализации монад в различных библиотеках


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/monad)
- [Algebird](https://twitter.github.io/algebird/typeclasses/monad.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
