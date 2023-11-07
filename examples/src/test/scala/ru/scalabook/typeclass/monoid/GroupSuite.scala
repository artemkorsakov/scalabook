package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object GroupSuite extends SimpleIOSuite, Checkers, GroupLaw:
  test("Int is a Group with addition and zero"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkGroupLaw(x, y, z)): _ =>
        success
