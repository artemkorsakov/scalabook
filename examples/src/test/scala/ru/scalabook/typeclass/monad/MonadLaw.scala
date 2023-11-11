package ru.scalabook.typeclass.monad

trait MonadLaw extends ApplicativeLaw, BindLaw:
  def checkMonadLaw[F[_]: Monad, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa             = Monad[F].unit(x)
    val afb: A => F[B] = a => Monad[F].unit(f(a))
    val bfc: B => F[C] = b => Monad[F].unit(g(b))
    checkApplicativeLaw[F, A, B, C](x)
    checkBindLaw[F, A, B, C](fa, Monad[F].unit(f), Monad[F].unit(g), afb, bfc)
    assertEquals(Monad[F].unit(x).flatMap(afb), afb(x))
    assertEquals(fa.flatMap(Monad[F].unit _), fa)
