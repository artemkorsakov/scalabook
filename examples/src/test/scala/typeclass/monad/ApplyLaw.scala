package typeclass.monad

import typeclass.monad.Apply.{apply, map}

trait ApplyLaw extends FunctorLaw:
  def checkApplyLaw[F[_], A, B, C](fa: F[A], ab: A => B, bc: B => C, fab: F[A => B], fbc: F[B => C])(using Apply[F]): Unit =
    // checkFunctorLaw(fa, ab, bc)
    assertEquals(
      apply(fbc)(apply(fab)(fa)),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)
    )
