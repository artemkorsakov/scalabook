package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object SemiringWithUnitySuite
    extends SimpleIOSuite,
      Checkers,
      SemiringWithUnityLaw:
  test("Int is a semiring with unity with addition, 0 and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkSemiringWithUnityLaw(x, y, z)): _ =>
        success
