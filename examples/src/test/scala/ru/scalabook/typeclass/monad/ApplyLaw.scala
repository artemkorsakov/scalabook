package ru.scalabook.typeclass.monad

trait ApplyLaw extends FunctorLaw, SemigroupalLaw, munit.Assertions:
  def checkApplyLaw[F[_]: Apply, A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkFunctorLaw[F, A, B, C](fa): Unit
    assertEquals(
      Apply[F].apply(fbc)(Apply[F].apply(fab)(fa)),
      Apply[F].apply(
        Apply[F]
          .apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab)
      )(fa),
      "composition"
    )
