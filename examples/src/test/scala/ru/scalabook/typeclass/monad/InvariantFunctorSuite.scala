package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object InvariantFunctorSuite extends SimpleIOSuite, Checkers:
  test("InvariantFunctor[Id] must satisfy the InvariantFunctor laws"):
    forall: (x: Int) =>
      expect(checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x)).isValid)
