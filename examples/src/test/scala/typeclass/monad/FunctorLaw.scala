package typeclass.monad

import typeclass.common.Runner1

trait FunctorLaw extends InvariantFunctorLaw:
  def checkFunctorLaw[F[_]: Functor, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    checkInvariantFunctorLaw[F, A, B, C](fa)
    assertEquals(Functor[F].map(fa)(identity), fa, "identity")
    assertEquals(Functor[F].map(Functor[F].map(fa)(f))(g), Functor[F].map(fa)(f.andThen(g)), "composition")

  def checkFunctorLawWithRunner[F[_]: Functor: Runner1, A, B, C](
      fa: F[A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B): Unit =
    checkInvariantFunctorLawWithRunner[F, A, B, C](fa)
    assertEquals(Runner1[F].run(Functor[F].map(fa)(identity)), Runner1[F].run(fa), "identity")
    assertEquals(
      Runner1[F].run(Functor[F].map(Functor[F].map(fa)(f))(g)),
      Runner1[F].run(Functor[F].map(fa)(f.andThen(g))),
      "composition"
    )
