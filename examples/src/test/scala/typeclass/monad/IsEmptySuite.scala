package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.IsEmpty.given

class IsEmptySuite extends ScalaCheckSuite, IsEmptyLaw:
  property("listIsEmptyInstance должен удовлетворять законам IsEmpty") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkIsEmptyLawLaw(x, y, z)
    }
  }
