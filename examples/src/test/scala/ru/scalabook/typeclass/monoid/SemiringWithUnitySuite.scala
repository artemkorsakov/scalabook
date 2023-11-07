package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.SemiringWithUnity.given

class SemiringWithUnitySuite extends ScalaCheckSuite, SemiringWithUnityLaw:
  property(
    "SemiringWithUnity[Int] должен удовлетворять законам полукольца с единицей"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkSemiringWithUnityLaw(x, y, z)
    }
  }
