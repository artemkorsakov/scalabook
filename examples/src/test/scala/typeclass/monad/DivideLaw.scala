package typeclass.monad

import typeclass.common.Runner1
import typeclass.common.Runner1.run
import typeclass.monad.Divide.divide

trait DivideLaw extends ContravariantFunctorLaw:
  protected def delta[A]: A => (A, A) = a => (a, a)

  def checkDivideLaw[F[_]: Divide: Runner1, A, B, C](
      fa1: F[A],
      fa2: F[A],
      fa3: F[A]
  )(using fba: B => A, fcb: C => B, fab: A => B, fbc: B => C): Unit =
    checkContravariantFunctorLaw[F, A, B, C](fa1)
    assertEquals(
      divide(divide(fa1, fa2)(delta), fa3)(delta),
      divide(fa1, divide(fa2, fa3)(delta))(delta),
      "composition"
    )
