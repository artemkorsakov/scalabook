package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object RingSuite extends SimpleIOSuite, Checkers, RingLaw:
  test("Int is a ring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkRingLaw(x, y, z)): _ =>
        success
