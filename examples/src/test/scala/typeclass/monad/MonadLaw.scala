package typeclass.monad

import typeclass.monad.Monad.{flatMap, unit}

trait MonadLaw extends FunctorLaw:
  def checkMonadLaw[F[_], A, B, C](x: A, fa: F[A], afb: A => F[B], bfc: B => F[C])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Monad[F]): Unit =
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(flatMap(unit(x), afb), afb(x))
    assertEquals(flatMap(fa, unit _), fa)
    assertEquals(
      flatMap(flatMap(fa, afb), bfc),
      flatMap(fa, x => flatMap(afb(x), bfc))
    )

  def checkMonadLaw[F[_], A, B, C](
      x: A,
      fa: F[A],
      afb: A => F[B],
      bfc: B => F[C],
      run: F[A] | F[B] | F[C] => A | B | C
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Monad[F]): Unit =
    checkFunctorLaw[F, A, B, C](fa, run)
    assertEquals(run(flatMap(unit(x), afb)), run(afb(x)))
    assertEquals(run(flatMap(fa, unit _)), run(fa))
    assertEquals(
      run(flatMap(flatMap(fa, afb), bfc)),
      run(flatMap(fa, x => flatMap(afb(x), bfc)))
    )
