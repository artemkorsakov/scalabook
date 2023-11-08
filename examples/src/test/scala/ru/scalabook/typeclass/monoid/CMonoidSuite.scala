package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object CMonoidSuite extends SimpleIOSuite, Checkers, CMonoidLaw:
  test("Int is a commutative monoid with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkCMonoidLaw(x, y, z)): _ =>
        success
