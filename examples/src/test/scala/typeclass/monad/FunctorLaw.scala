package typeclass.monad

import typeclass.common.Runner1
import typeclass.common.Runner1.run
import typeclass.monad.Functor.map

trait FunctorLaw extends InvariantFunctorLaw:
  def checkFunctorLaw[F[_]: Functor, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    checkInvariantFunctorLaw[F, A, B, C](fa)
    assertEquals(map(fa, identity), fa, "identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "composition")

  def checkFunctorLawWithRunner[F[_]: Functor: Runner1, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    checkInvariantFunctorLawWithRunner[F, A, B, C](fa)
    assertEquals(run(map(fa, identity)), run(fa), "identity")
    assertEquals(run(map(map(fa, f), g)), run(map(fa, f.andThen(g))), "composition")
