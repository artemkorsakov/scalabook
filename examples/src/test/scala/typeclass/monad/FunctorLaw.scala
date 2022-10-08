package typeclass.monad

import typeclass.common.State
import typeclass.monad.Functor.map

trait FunctorLaw extends InvariantFunctorLaw:
  def checkFunctorLaw[F[_], A, B, C](fa: F[A])(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      Functor[F]
  ): Unit =
    checkInvariantFunctorLaw[F, A, B, C](fa)
    assertEquals(map(fa, identity), fa, "identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "composition")

  def checkFunctorLaw[F[_], A, B, C](
      fa: F[A],
      run: F[A] | F[B] | F[C] => A | B | C
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      Functor[F]
  ): Unit =
    checkInvariantFunctorLaw[F, A, B, C](fa, run)
    assertEquals(run(map(fa, identity)), run(fa), "identity")
    assertEquals(run(map(map(fa, f), g)), run(map(fa, f.andThen(g))), "composition")
