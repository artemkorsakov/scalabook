package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object PlusEmptySuite extends SimpleIOSuite, Checkers, PlusEmptyLaw:
  test("listPlusEmptyInstance must satisfy the PlusEmpty laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      whenSuccess(checkPlusEmptyLaw(x, y, z)): _ =>
        success
