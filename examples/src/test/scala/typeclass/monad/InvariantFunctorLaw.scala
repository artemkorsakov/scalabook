package typeclass.monad

import munit.Assertions
import typeclass.common.Runner1
import typeclass.monad.InvariantFunctor.xmap

trait InvariantFunctorLaw extends Assertions:
  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    assertEquals(xmap(fa, identity, identity), fa, "identity")
    assertEquals(
      xmap(xmap(fa, f, fReverse), g, gReverse),
      xmap(fa, g compose f, fReverse compose gReverse),
      "composition"
    )

  def checkInvariantFunctorLawWithRunner[F[_]: InvariantFunctor: Runner1, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    assertEquals(Runner1[F].run(xmap(fa, identity, identity)), Runner1[F].run(fa), "identity")
    assertEquals(
      Runner1[F].run(xmap(xmap(fa, f, fReverse), g, gReverse)),
      Runner1[F].run(xmap(fa, g compose f, fReverse compose gReverse)),
      "composition"
    )
