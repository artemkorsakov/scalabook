package typeclass.monad

import typeclass.monad.Apply.apply

trait ApplyLaw extends FunctorLaw:
  def checkApplyLaw[F[_], A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Apply[F]): Unit =
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(
      apply(fbc)(apply(fab)(fa)),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa),
      "composition"
    )

  def checkApplyLaw[F[_], A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C], run: F[A] | F[B] | F[C] => A | B | C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Apply[F]): Unit =
    checkFunctorLaw[F, A, B, C](fa, run)
    assertEquals(
      run(apply(fbc)(apply(fab)(fa))),
      run(apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)),
      "composition"
    )
