package typeclass.monad

import munit.Assertions
import typeclass.common.State
import typeclass.monad.InvariantFunctor.xmap

trait InvariantFunctorLaw extends Assertions:
  def checkInvariantFunctorLaw[F[_], A, B, C](fa: F[A])(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(
      using InvariantFunctor[F]
  ): Unit =
    assertEquals(xmap(fa, identity, identity), fa, "check identity")
    assertEquals(
      xmap(xmap(fa, f, fReverse), g, gReverse),
      xmap(fa, g compose f, fReverse compose gReverse),
      "check composition"
    )

  def checkInvariantFunctorLawForState[A, B, C](fa: State[String, A], s: String)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using
      Functor[[x] =>> State[String, x]]
  ): Unit =
    assertEquals(xmap(fa, identity, identity).run(s), fa.run(s), "check identity")
    assertEquals(
      xmap(xmap(fa, f, fReverse), g, gReverse),
      xmap(fa, g compose f, fReverse compose gReverse),
      "check composition"
    )
