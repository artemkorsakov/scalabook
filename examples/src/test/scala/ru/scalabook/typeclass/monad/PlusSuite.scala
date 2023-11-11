package ru.scalabook.typeclass.monad

import weaver.*
import weaver.scalacheck.*

object PlusSuite extends SimpleIOSuite, Checkers, PlusLaw:
  test("listPlusInstance must satisfy the Plus laws"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      whenSuccess(checkPlusLaw(x, y, z)): _ =>
        success
