package ru.scalabook.typeclass.monad

import cats.Id

trait InvariantApplicative[F[_]] extends InvariantFunctor[F]:
  def xunit0[A](a: => A): F[A]

  extension [A](fa: => F[A])
    def xunit1[B](f: A => B, g: B => A): F[B] =
      fa.xmap(f, g)

object InvariantApplicative:
  def apply[F[_]: InvariantApplicative]: InvariantApplicative[F] =
    summon[InvariantApplicative[F]]

  given InvariantApplicative[Id] with
    def xunit0[A](a: => A): Id[A] = Id(a)
    extension [A](fa: Id[A])
      override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa))
