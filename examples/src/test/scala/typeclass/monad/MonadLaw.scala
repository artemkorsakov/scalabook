package typeclass.monad

import typeclass.common.Runner1
import typeclass.common.Runner1.run
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

  def checkMonadLawWithRunner[F[_]: Monad: Runner1, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = unit(x)
    val afb: A => F[B] = a => unit(f(a))
    val bfc: B => F[C] = b => unit(g(b))
    checkApplicativeLawWithRunner[F, A, B, C](x)
    checkBindLawWithRunner[F, A, B, C](fa, unit(f), unit(g), afb, bfc)
    assertEquals(run(unit(x).flatMap(afb)), run(afb(x)))
    assertEquals(run(fa.flatMap(unit _)), run(fa))
