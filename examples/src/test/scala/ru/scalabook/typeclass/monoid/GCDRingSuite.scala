package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Generators.smallNumbersGen
import weaver.*
import weaver.scalacheck.*

object GCDRingSuite extends SimpleIOSuite, Checkers, GCDRingLaw:
  test("GCDRing[Int] должен удовлетворять законам кольца НОД"):
    forall(smallNumbersGen): (x: Int, y: Int, z: Int) =>
      whenSuccess(checkGCDRingLaw(x, y, z)): _ =>
        success
