package typeclass.monad

trait ApplicativePlusLaw extends ApplicativeLaw, PlusEmptyLaw:
  def checkApplicativePlusLaw[F[_]: ApplicativePlus, A, B, C](x: A, f1: F[A], f2: F[A], f3: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    checkApplicativeLaw[F, A, B, C](x)
    checkPlusEmptyLaw[F, A](f1, f2, f3)
