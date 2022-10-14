package typeclass.monad

import munit.Assertions
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

  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](
      fa: F[A],
      run: F[A] | F[B] | F[C] => A | B | C
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    assertEquals(run(xmap(fa, identity, identity)), run(fa), "identity")
    assertEquals(
      run(xmap(xmap(fa, f, fReverse), g, gReverse)),
      run(xmap(fa, g compose f, fReverse compose gReverse)),
      "composition"
    )
