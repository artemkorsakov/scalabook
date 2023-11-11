package ru.scalabook.typeclass.monad

import cats.data.*
trait InvariantApplicativeLaw extends InvariantFunctorLaw:
  def checkInvariantApplicativeLaw[F[_]: InvariantApplicative, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa)
