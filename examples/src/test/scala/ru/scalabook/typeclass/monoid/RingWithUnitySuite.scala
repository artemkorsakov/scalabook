package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.RingWithUnity.given

class RingWithUnitySuite extends ScalaCheckSuite, RingWithUnityLaw:
  property(
    "RingWithUnity[Int] должен удовлетворять законам кольца с единицей"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkRingWithUnityLaw(x, y, z)
    }
  }
