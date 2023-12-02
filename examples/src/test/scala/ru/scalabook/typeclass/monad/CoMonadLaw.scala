package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel

trait CoMonadLaw extends CoBindLaw:
  def checkCoMonadLaw[F[_]: CoMonad, A, B, C](
      fa: F[A],
      fab: F[A] => B,
      fbc: F[B] => C
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkCoBindLaw(fa, fab, fbc) combine
      check(
        CoMonad[F].coFlatMap(fa)(CoMonad[F].coUnit) ==
          fa,
        "left identity"
      ) combine
      check(
        CoMonad[F].coUnit(CoMonad[F].coFlatMap(fa)(fab)) ==
          fab(fa),
        "right identity"
      )
end CoMonadLaw
