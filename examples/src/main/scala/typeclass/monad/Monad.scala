package typeclass.monad

import typeclass.common.*
import typeclass.monoid.Monoid

trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def map[B](f: A => B): F[B] =
      fa.flatMap(a => unit(f(a)))

object Monad:
  given idMonad: Monad[Id] with
    override def unit[A](a: => A): Id[A] = Id(a)
    extension [A](fa: Id[A]) override def flatMap[B](f: A => Id[B]): Id[B] = f(fa.value)

  given optionMonad: Monad[Option] with
    override def unit[A](a: => A): Option[A] = Some(a)

    extension [A](fa: Option[A])
      override def flatMap[B](f: A => Option[B]): Option[B] =
        fa match
          case Some(a) => f(a)
          case None    => None

  given listMonad: Monad[List] with
    override def unit[A](a: => A): List[A] = List(a)

    extension [A](fa: List[A]) override def flatMap[B](f: A => List[B]): List[B] = fa.flatMap(f)

  given eitherMonad[E]: Monad[[x] =>> Either[E, x]] with
    override def unit[A](a: => A): Either[E, A] = Right(a)

    extension [A](fa: Either[E, A])
      override def flatMap[B](f: A => Either[E, B]): Either[E, B] =
        fa match
          case Right(a) => f(a)
          case Left(e)  => Left(e)

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

  given stateMonad[S]: Monad[[x] =>> State[S, x]] with
    override def unit[A](a: => A): State[S, A] =
      State[S, A](s => (s, a))

    extension [A](fa: State[S, A])
      override def flatMap[B](f: A => State[S, B]): State[S, B] =
        State[S, B] { s =>
          val (s1, a) = fa.run(s)
          f(a).run(s1)
        }

  def unit[F[_], A](a: => A)(using monad: Monad[F]): F[A] =
    monad.unit(a)

  def map[F[_], A, B](fa: F[A], f: A => B)(using monad: Monad[F]): F[B] =
    monad.map(fa)(f)

  def flatMap[F[_], A, B](fa: F[A], f: A => F[B])(using monad: Monad[F]): F[B] =
    monad.flatMap(fa)(f)
