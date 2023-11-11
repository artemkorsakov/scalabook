package ru.scalabook.typeclass.monad

import cats.Id

trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

object InvariantFunctor:
  def apply[F[_]: InvariantFunctor]: InvariantFunctor[F] =
    summon[InvariantFunctor[F]]

  given InvariantFunctor[Id] with
    extension [A](fa: Id[A])
      override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa))
