package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ApplicativePlusSuite extends SimpleIOSuite, Checkers, ApplicativePlusLaw:
  test("listApplicativePlusInstance must satisfy the ApplicativePlus laws"):
    forall: (i: Int, x: List[Int], y: List[Int], z: List[Int]) =>
      whenSuccess(
        checkApplicativePlusLaw[List, Int, String, Boolean](i, x, y, z)
      ): _ =>
        success
