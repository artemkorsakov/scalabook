package typeclass.monad

import typeclass.common.*

trait Bind[F[_]] extends Apply[F]:
  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def bind[B](f: A => F[B]): F[B] = flatMap(f)

  def join[A](ffa: F[F[A]]): F[A] = flatMap(ffa)(a => a)

  override def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    flatMap(fab)(x => fa.map(x))

  override def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    flatMap(fa)(a => fb.map(b => f(a, b)))

object Bind:
  given Bind[Id] with
    extension [A](fa: Id[A])
      override def map[B](f: A => B): Id[B] =
        Id(f(fa.value))

      override def flatMap[B](f: A => Id[B]): Id[B] =
        f(fa.value)

  given Bind[Option] with
    extension [A](fa: Option[A])
      override def map[B](f: A => B): Option[B] =
        fa match
          case Some(a) => Some(f(a))
          case None    => None

      override def flatMap[B](f: A => Option[B]): Option[B] =
        fa match
          case Some(a) => f(a)
          case None    => None
