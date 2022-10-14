package typeclass.monad

import typeclass.monad.Monad.unit

trait MonadLaw extends ApplicativeLaw, BindLaw:
  def checkMonadLaw[F[_]: Monad, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = unit(x)
    val afb: A => F[B] = a => unit(f(a))
    val bfc: B => F[C] = b => unit(g(b))
    checkApplicativeLaw[F, A, B, C](x)
    checkBindLaw[F, A, B, C](fa, unit(f), unit(g), afb, bfc)
    assertEquals(unit(x).flatMap(afb), afb(x))
    assertEquals(fa.flatMap(unit _), fa)

  def checkMonadLaw[F[_]: Monad, A, B, C](x: A, run: F[A] | F[B] | F[C] => A | B | C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = unit(x)
    val afb: A => F[B] = a => unit(f(a))
    val bfc: B => F[C] = b => unit(g(b))
    checkApplicativeLaw[F, A, B, C](x, run)
    checkBindLaw[F, A, B, C](fa, unit(f), unit(g), afb, bfc, run)
    assertEquals(run(unit(x).flatMap(afb)), run(afb(x)))
    assertEquals(run(fa.flatMap(unit _)), run(fa))
