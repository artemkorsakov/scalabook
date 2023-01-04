package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monoid.Band.given

class BandSuite extends ScalaCheckSuite, BandLaw:
  property(
    "setBandInstance должен удовлетворять законам идемпотентной полугруппы"
  ) {
    forAll { (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      checkBandLaw(x, y, z)
    }
  }
