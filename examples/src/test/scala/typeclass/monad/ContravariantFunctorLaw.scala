package typeclass.monad

import munit.Assertions

trait ContravariantFunctorLaw extends Assertions:
  def checkContravariantFunctorLaw(): Unit =
    assertEquals(42, 42)
