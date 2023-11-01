package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.CRingWithUnity.given

class CRingWithUnitySuite extends ScalaCheckSuite, CRingWithUnityLaw:
  property(
    "CRingWithUnity[Int] должен удовлетворять законам коммутативного кольца с единицей"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkCRingWithUnityLaw(x, y, z)
    }
  }
