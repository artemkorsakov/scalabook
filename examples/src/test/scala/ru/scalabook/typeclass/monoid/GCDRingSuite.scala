package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object GCDRingSuite extends SimpleIOSuite, Checkers, GCDRingLaw:
  test("Int is a GCD ring with unity with addition, 0, multiplication and 1"):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      whenSuccess(checkGCDRingLaw(x, y, z)): _ =>
        success
