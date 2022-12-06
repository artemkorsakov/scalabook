package typeclass.monad

import typeclass.common.Runner1

trait DivideLaw extends ContravariantFunctorLaw:
  protected def delta[A]: A => (A, A) = a => (a, a)

  def checkDivideLaw[F[_]: Divide: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using fba: B => A, fcb: C => B, fab: A => B, fbc: B => C): Unit =
    checkContravariantFunctorLaw[F, A, B, C](fa1)
    assertEquals(
      Divide[F].divide(Divide[F].divide(fa1, fa2)(delta), fa3)(delta),
      Divide[F].divide(fa1, Divide[F].divide(fa2, fa3)(delta))(delta),
      "composition"
    )
