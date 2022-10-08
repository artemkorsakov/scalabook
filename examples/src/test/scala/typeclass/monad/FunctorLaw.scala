package typeclass.monad

import typeclass.common.State
import typeclass.monad.Functor.map

trait FunctorLaw extends InvariantFunctorLaw:
  def checkFunctorLaw[F[_], A, B, C](fa: F[A])(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      Functor[F]
  ): Unit =
    checkInvariantFunctorLaw[F, A, B, C](fa)
    assertEquals(map(fa, identity), fa, "check identity")
    assertEquals(map(map(fa, f), g), map(fa, f.andThen(g)), "check composition")

  def checkFunctorLawForState[A, B, C](fa: State[String, A], s: String)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using
      Functor[[x] =>> State[String, x]]
  ): Unit =
    checkInvariantFunctorLawForState[A, B, C](fa, s)
    assertEquals(map(fa, identity).run(s), fa.run(s), "check identity")
    assertEquals(map(map(fa, f), g).run(s), map(fa, f.andThen(g)).run(s), "check composition")
