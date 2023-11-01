package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.Generators.smallNumbersGen
import ru.scalabook.typeclass.monoid.GCDRing.given

class GCDRingSuite extends ScalaCheckSuite, GCDRingLaw:
  property("GCDRing[Int] должен удовлетворять законам кольца НОД") {
    forAll(smallNumbersGen) { (x: Int, y: Int, z: Int) =>
      checkGCDRingLaw(x, y, z)
    }
  }
