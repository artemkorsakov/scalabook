package typeclass.monad

trait MonadPlusLaw extends MonadLaw, ApplicativePlusLaw:
  def checkMonadPlusLaw[F[_]: MonadPlus, A, B, C](x: A, f1: F[A], f2: F[A], f3: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val ins = summon[MonadPlus[F]]
    checkMonadLaw[F, A, B, C](x)
    checkApplicativePlusLaw[F, A, B, C](x, f1, f2, f3)
    assertEquals(ins.empty[A].map(_ => x), ins.empty[A], "`empty[A]` is a polymorphic value over `A`")
    assertEquals(ins.empty[A].flatMap(_ => f1), ins.empty[A], "`empty` short-circuits its right")
    assertEquals(f1.flatMap(_ => ins.empty[A]), ins.empty[A], "`empty` short-circuits throughout its `join` tree")
