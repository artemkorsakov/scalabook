package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import cats.Id
import weaver.*
import weaver.scalacheck.*
import ru.scalabook.typeclass.common.Functions.given

object InvariantFunctorSuite extends SimpleIOSuite, Checkers:
  test("InvariantFunctor[Id] must satisfy the InvariantFunctor laws"):
    forall: (x: Int) =>
      expect(checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x)).isValid)
