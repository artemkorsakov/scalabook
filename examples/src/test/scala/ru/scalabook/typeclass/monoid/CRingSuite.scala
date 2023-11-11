package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object CRingSuite extends SimpleIOSuite, Checkers, CRingLaw:
  test("Int is a commutative ring with addition, 0, multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkCRingLaw(x, y, z)): _ =>
        success
