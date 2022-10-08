package typeclass.monad

import munit.Assertions
import typeclass.monad.InvariantFunctor.xmap

trait InvariantFunctorLaw extends Assertions:
  def checkInvariantFunctorLaw[F[_], A, B, C](fa: F[A], f1: A => B, g1: B => A, f2: B => C, g2: C => B)(using
      InvariantFunctor[F]
  ): Unit =
    assertEquals(xmap(fa, identity, identity), fa, "check identity")
    assertEquals(xmap(xmap(fa, f1, g1), f2, g2), xmap(fa, f2 compose f1, g1 compose g2), "check composition")
