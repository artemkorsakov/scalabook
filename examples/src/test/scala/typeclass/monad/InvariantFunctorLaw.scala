package typeclass.monad

import munit.Assertions
import typeclass.common.Runner1

trait InvariantFunctorLaw extends Assertions:
  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    assertEquals(
      InvariantFunctor[F].xmap(fa)(identity, identity),
      fa,
      "identity"
    )
    assertEquals(
      InvariantFunctor[F]
        .xmap(InvariantFunctor[F].xmap(fa)(f, fReverse))(g, gReverse),
      InvariantFunctor[F].xmap(fa)(g compose f, fReverse compose gReverse),
      "composition"
    )

  def checkInvariantFunctorLawWithRunner[F[
      _
  ]: InvariantFunctor: Runner1, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    assertEquals(
      Runner1[F].run(InvariantFunctor[F].xmap(fa)(identity, identity)),
      Runner1[F].run(fa),
      "identity"
    )
    assertEquals(
      Runner1[F].run(
        InvariantFunctor[F]
          .xmap(InvariantFunctor[F].xmap(fa)(f, fReverse))(g, gReverse)
      ),
      Runner1[F].run(
        InvariantFunctor[F].xmap(fa)(g compose f, fReverse compose gReverse)
      ),
      "composition"
    )
