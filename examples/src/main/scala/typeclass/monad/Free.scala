package typeclass.monad

import scala.annotation.tailrec

sealed trait Free[F[_], A]
final case class Unit[F[_], A](a: A) extends Free[F, A]
final case class FlatMap[F[_], A, B](a: Free[F, A], fx: A => Free[F, B]) extends Free[F, B]
final case class LiftF[F[_], A](fn: F[A]) extends Free[F, A]

trait Natural[F[_], G[_]]:
  def transform[A](a: F[A]): G[A]

object Free:
  given freeFunctor[F[_]]: Functor[[X] =>> Free[F, X]] with
    extension [A](as: Free[F, A])
      override def map[B](f: A => B): Free[F, B] =
        FlatMap(as, a => Unit(f(a)))

  given freeMonad[F[_]]: Monad[[X] =>> Free[F, X]] with
    override def unit[A](a: => A): Free[F, A] = Unit(a)

    extension [A](fa: Free[F, A]) override def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap(fa, f)

  def liftF[F[_], A](a: F[A]): Free[F, A] = LiftF[F, A](a)

  def foldF[F[_], M[_], A](fa: Free[F, A])(trans: Natural[F, M])(using monad: Monad[M]): M[A] =
    fa match
      case Unit(a)                   => monad.unit(a)
      case LiftF(fn)                 => trans.transform(fn)
      case FlatMap(Unit(a), f)       => foldF(f(a))(trans)
      case FlatMap(LiftF(a), f)      => monad.flatMap(trans.transform(a)) { aa => foldF(f(aa))(trans) }
      case FlatMap(FlatMap(a, f), g) => foldF(FlatMap(a, aa => FlatMap(f(aa), g)))(trans)
