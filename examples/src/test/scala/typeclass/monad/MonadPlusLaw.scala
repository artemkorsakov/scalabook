package typeclass.monad

trait MonadPlusLaw extends MonadLaw, ApplicativePlusLaw:
  def checkMonadPlusLaw[F[_], A, B, C](x: A, f1: F[A], f2: F[A], f3: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using mp: MonadPlus[F]): Unit =
    checkMonadLaw[F, A, B, C](x)
    checkApplicativePlusLaw[F, A, B, C](x, f1, f2, f3)
    assertEquals(mp.empty[A].map(_ => x), mp.empty[A], "`empty[A]` is a polymorphic value over `A`")
    assertEquals(mp.empty[A].flatMap(_ => f1), mp.empty[A], "`empty` short-circuits its right")
    assertEquals(f1.flatMap(_ => mp.empty[A]), mp.empty[A], "`empty` short-circuits throughout its `join` tree")
