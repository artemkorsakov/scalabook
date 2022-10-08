package typeclass.monad

import typeclass.common.*

trait Apply[F[_]] extends Functor[F]:
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    apply(fa.map(f.curried))(fb)

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] =
    apply2(fa, fb)((_, _))

  def lift2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    apply2(_, _)(f)

object Apply:
  given Apply[Id] with
    override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab.value(fa.value))

    extension [A](as: Id[A]) override def map[B](f: A => B): Id[B] = Id(f(as.value))

  given Apply[Option] with
    override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
      (fab, fa) match
        case (Some(aToB), Some(a)) => Some(aToB(a))
        case _                     => None

    extension [A](as: Option[A])
      override def map[B](f: A => B): Option[B] =
        as match
          case Some(a) => Some(f(a))
          case None    => None

  def apply[F[_], A, B](fab: F[A => B])(fa: F[A])(using app: Apply[F]): F[B] =
    app.apply(fab)(fa)

  def map[F[_], A, B](fa: F[A], f: A => B)(using app: Apply[F]): F[B] =
    app.map(fa)(f)
