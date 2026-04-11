package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object ApplicativePlusSuite extends SimpleIOSuite, Checkers:
  test("listApplicativePlusInstance must satisfy the ApplicativePlus laws"):
    forall:
      (
          a: Int,
          b: String,
          c: Boolean,
          x: List[Int],
          y: List[Int],
          z: List[Int]
      ) =>
        expect(
          checkApplicativePlusLaw[List, Int, String, Boolean](
            a,
            b,
            c,
            a :: x,
            a :: y,
            a :: z
          ).isValid
        )
end ApplicativePlusSuite
