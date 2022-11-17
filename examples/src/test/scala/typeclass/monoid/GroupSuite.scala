package typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class GroupSuite extends ScalaCheckSuite, GroupLaw:
  property("Group[Int] должен удовлетворять законам Group") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkGroupLaw(x, y, z)
    }
  }
