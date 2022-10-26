package typeclass.monad

import typeclass.common.{Env, Id}

trait CoBind[F[_]] extends Functor[F]:
  extension [A](fa: F[A])
    /** Также известно как `extend` */
    def cobind[B](f: F[A] => B): F[B]

    final def extend[B](f: F[A] => B): F[B] = fa.cobind(f)

    final def coFlatMap[B](f: F[A] => B): F[B] = fa.cobind(f)

    /** Также известно как `duplicate` */
    final def cojoin: F[F[A]] = fa.cobind(fa => fa)

object CoBind:
  given CoBind[Id] with
    extension [A](as: Id[A])
      override def map[B](f: A => B): Id[B] = Id(f(as.value))

      override def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))

  given envCoBind[R]: CoBind[[X] =>> Env[X, R]] with
    extension [A](as: Env[A, R])
      override def map[B](f: A => B): Env[B, R] =
        val Env(a, r) = as
        Env(f(a), r)

      override def cobind[B](f: Env[A, R] => B): Env[B, R] =
        val Env(_, r) = as
        Env(f(as), r)

  def cobind[F[_], A, B](fa: F[A])(f: F[A] => B)(using cm: CoBind[F]): F[B] = fa.cobind(f)
