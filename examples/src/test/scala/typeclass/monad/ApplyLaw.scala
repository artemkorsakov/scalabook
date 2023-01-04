package typeclass.monad

import typeclass.common.Runner1

trait ApplyLaw extends FunctorLaw, SemigroupalLaw:
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
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(
      Apply[F].apply(fbc)(Apply[F].apply(fab)(fa)),
      Apply[F].apply(
        Apply[F]
          .apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab)
      )(fa),
      "composition"
    )

  def checkApplyLawWithRunner[F[_]: Apply: Runner1, A, B, C](
      fa: F[A],
      fab: F[A => B],
      fbc: F[B => C]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkFunctorLawWithRunner[F, A, B, C](fa)
    val fb = Apply[F].apply(fab)(fa)
    val fc = Apply[F].apply(fbc)(fb)
    checkSemigroupalLawWithRunner[F, A, B, C](fa, fb, fc)
    assertEquals(
      Runner1[F].run(Apply[F].apply(fbc)(Apply[F].apply(fab)(fa))),
      Runner1[F].run(
        Apply[F].apply(
          Apply[F].apply(
            fbc.map((bc: B => C) => (ab: A => B) => bc compose ab)
          )(fab)
        )(fa)
      ),
      "composition"
    )
