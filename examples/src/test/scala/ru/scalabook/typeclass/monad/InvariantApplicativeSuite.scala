package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object InvariantApplicativeSuite extends SimpleIOSuite, Checkers:
  test("InvariantApplicative[Id] must satisfy the InvariantApplicative laws"):
    forall: (x: Int) =>
      expect(
        checkInvariantApplicativeLaw[Id, Int, String, Boolean](Id(x)).isValid
      )
