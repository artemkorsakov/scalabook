package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.CRing.given

class CRingSuite extends ScalaCheckSuite, CRingLaw:
  property("CRing[Int] должен удовлетворять законам коммутативного кольца") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkCRingLaw(x, y, z)
    }
  }
