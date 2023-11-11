package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object RingWithUnitySuite extends SimpleIOSuite, Checkers, RingWithUnityLaw:
  test("Int is a ring with unity with addition, 0, multiplication and 1"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkRingWithUnityLaw(x, y, z)): _ =>
        success
