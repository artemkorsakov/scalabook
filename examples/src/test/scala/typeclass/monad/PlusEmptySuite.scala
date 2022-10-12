package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.PlusEmpty.given

class PlusEmptySuite extends ScalaCheckSuite, PlusEmptyLaw:
  property("listPlusEmptyInstance должен удовлетворять законам PlusEmpty") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkPlusEmptyLaw(x, y, z)
    }
  }
