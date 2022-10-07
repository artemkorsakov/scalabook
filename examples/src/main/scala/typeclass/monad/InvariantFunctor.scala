package typeclass.monad

import typeclass.common.*

trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

object InvariantFunctor:
  given idInvariantFunctor: InvariantFunctor[Id] with
    extension [A](fa: Id[A]) override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))

  def xmap[F[_], A, B](fa: F[A], f: A => B, g: B => A)(using functor: InvariantFunctor[F]): F[B] =
    functor.xmap(fa)(f, g)
