package typeclass.monad

import typeclass.common.*

trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

object InvariantFunctor:
  given InvariantFunctor[Id] with
    extension [A](fa: Id[A]) override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))

  given InvariantFunctor[Codec] with
    extension [A](fa: Codec[A])
      override def xmap[B](f: A => B, g: B => A): Codec[B] =
        new Codec[B]:
          def encode(value: B): String = fa.encode(g(value))

          def decode(value: String): B = f(fa.decode(value))

  def xmap[F[_], A, B](fa: F[A], f: A => B, g: B => A)(using functor: InvariantFunctor[F]): F[B] =
    functor.xmap(fa)(f, g)
