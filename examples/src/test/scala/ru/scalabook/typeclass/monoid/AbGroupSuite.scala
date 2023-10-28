package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class AbGroupSuite extends ScalaCheckSuite, AbGroupLaw:
  property("AbGroup[Int] должен удовлетворять законам абелевой группы") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkAbGroupLaw(x, y, z)
    }
  }
