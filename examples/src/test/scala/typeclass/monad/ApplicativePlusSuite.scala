package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.ApplicativePlus.given

class ApplicativePlusSuite extends ScalaCheckSuite, ApplicativePlusLaw:
  property("listApplicativePlusInstance должен удовлетворять законам ApplicativePlus") {
    forAll { (i: Int, x: List[Int], y: List[Int], z: List[Int]) =>
      checkApplicativePlusLaw(i, x, y, z)
    }
  }
