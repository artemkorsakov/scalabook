package typeclass.monad

import typeclass.common.Runner1
import typeclass.monad.Apply.apply

trait ApplyLaw extends FunctorLaw, SemigroupalLaw:
  def checkApplyLaw[F[_]: Apply, A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(
      apply(fbc)(apply(fab)(fa)),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa),
      "composition"
    )

  def checkApplyLawWithRunner[F[_]: Apply: Runner1, A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkFunctorLawWithRunner[F, A, B, C](fa)
    val fb = apply(fab)(fa)
    val fc = apply(fbc)(fb)
    checkSemigroupalLawWithRunner[F, A, B, C](fa, fb, fc)
    assertEquals(
      Runner1[F].run(apply(fbc)(apply(fab)(fa))),
      Runner1[F].run(apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)),
      "composition"
    )
