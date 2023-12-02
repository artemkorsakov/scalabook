package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.Runner1

trait DivisibleLaw extends DivideLaw:
  def checkDivideLaw[F[_]: Divisible: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using
      fba: B => A,
      fcb: C => B,
      fab: A => B,
      fbc: B => C
  ): ValidatedNel[String, Unit] =
    checkDivideLaw[F, A, B, C](fa1, fa2, fa3) combine
      check(
        Divisible[F].divide(fa1, Divisible[F].conquer)(delta) ==
          fa1,
        "rightIdentity"
      ) combine
      check(
        Divisible[F].divide(Divisible[F].conquer, fa1)(delta) ==
          fa1,
        "leftIdentity"
      )
end DivisibleLaw
