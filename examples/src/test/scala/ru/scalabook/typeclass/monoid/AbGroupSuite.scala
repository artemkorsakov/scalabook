package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object AbGroupSuite extends SimpleIOSuite, Checkers, AbGroupLaw:
  test("Int is a Abelian group with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkAbGroupLaw(x, y, z)): _ =>
        success
