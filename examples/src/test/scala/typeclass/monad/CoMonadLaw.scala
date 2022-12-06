package typeclass.monad

trait CoMonadLaw extends CoBindLaw:
  def checkCoMonadLaw[F[_]: CoMonad, A, B, C](fa: F[A], fab: F[A] => B, fbc: F[B] => C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkCoBindLaw(fa, fab, fbc)
    assertEquals(CoMonad[F].coFlatMap(fa)(CoMonad[F].coUnit), fa, "left identity")
    assertEquals(CoMonad[F].coUnit(CoMonad[F].coFlatMap(fa)(fab)), fab(fa), "right identity")
