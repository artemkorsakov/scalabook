package typeclass.monad

import typeclass.common.{Env, Id}

trait CoMonad[F[_]]:
  def coUnit[A](fa: F[A]): A
  def coFlatMap[A, B](fa: F[A])(f: F[A] => B): F[B]

object CoMonad:
  given CoMonad[Id] with
    override def coUnit[A](fa: Id[A]): A = fa.value

    override def coFlatMap[A, B](fa: Id[A])(f: Id[A] => B): Id[B] = Id(f(fa))

  given envCoMonad[R]: CoMonad[[X] =>> Env[X, R]] with
    override def coUnit[A](fa: Env[A, R]): A = fa.a

    override def coFlatMap[A, B](fa: Env[A, R])(f: Env[A, R] => B): Env[B, R] = Env(f(fa), fa.r)

  def coUnit[F[_], A](fa: F[A])(using cm: CoMonad[F]): A = cm.coUnit(fa)

  def coFlatMap[F[_], A, B](fa: F[A])(f: F[A] => B)(using cm: CoMonad[F]): F[B] = cm.coFlatMap(fa)(f)
