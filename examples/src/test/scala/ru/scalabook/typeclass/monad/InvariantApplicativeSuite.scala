package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object InvariantApplicativeSuite extends SimpleIOSuite, Checkers:
  test("InvariantApplicative[Id] must satisfy the InvariantApplicative laws"):
    forall: (x: Int) =>
      expect(
        checkInvariantApplicativeLaw[Id, Int, String, Boolean](Id(x)).isValid
      )
