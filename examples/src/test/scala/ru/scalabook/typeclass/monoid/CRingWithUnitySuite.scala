package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object CRingWithUnitySuite extends SimpleIOSuite, Checkers, CRingWithUnityLaw:
  test(
    "Int is a commutative ring with unity with addition, 0, multiplication and 1"
  ):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkCRingWithUnityLaw(x, y, z)): _ =>
        success
