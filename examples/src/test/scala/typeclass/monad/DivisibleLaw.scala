package typeclass.monad

import typeclass.common.Runner1

trait DivisibleLaw extends DivideLaw:
  def checkDivideLaw[F[_]: Divisible: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using fba: B => A, fcb: C => B, fab: A => B, fbc: B => C): Unit =
    checkDivideLaw[F, A, B, C](fa1, fa2, fa3)
    assertEquals(
      Divisible[F].divide(fa1, Divisible[F].conquer)(delta),
      fa1,
      "rightIdentity"
    )
    assertEquals(
      Divisible[F].divide(Divisible[F].conquer, fa1)(delta),
      fa1,
      "leftIdentity"
    )
