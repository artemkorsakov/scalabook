package typeclass.monad

import typeclass.monad.CoMonad.{coFlatMap, coUnit}

trait CoMonadLaw extends CoBindLaw:
  def checkCoMonadLaw[F[_]: CoMonad, A, B, C](fa: F[A], fab: F[A] => B, fbc: F[B] => C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkCoBindLaw(fa, fab, fbc)
    assertEquals(coFlatMap(fa)(coUnit), fa, "left identity")
    assertEquals(coUnit(coFlatMap(fa)(fab)), fab(fa), "right identity")
