package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object CSemigroupSuite extends SimpleIOSuite, Checkers, CSemigroupLaw:
  test("Int is a commutative monoid with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkCSemigroupLaw(x, y, z)): _ =>
        success
