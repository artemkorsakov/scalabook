package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object InvariantApplicativeSuite
    extends SimpleIOSuite,
      Checkers,
      InvariantApplicativeLaw:
  test("InvariantApplicative[Id] must satisfy the InvariantApplicative laws"):
    forall: (x: Int) =>
      whenSuccess(
        checkInvariantApplicativeLaw[Id, Int, String, Boolean](Id(x))
      ): _ =>
        success
