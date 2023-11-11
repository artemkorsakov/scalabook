package ru.scalabook.typeclass.monad

import cats.Id
import weaver.*
import weaver.scalacheck.*
import ru.scalabook.typeclass.common.Functions.given

object InvariantFunctorSuite
    extends SimpleIOSuite,
      Checkers,
      InvariantFunctorLaw:
  test("InvariantFunctor[Id] must satisfy the InvariantFunctor laws"):
    forall: (x: Int) =>
      whenSuccess(checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x))):
        _ => success
