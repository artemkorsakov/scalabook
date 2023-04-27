package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class PlusEmptySuite extends ScalaCheckSuite, PlusEmptyLaw:
  property("listPlusEmptyInstance должен удовлетворять законам PlusEmpty") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkPlusEmptyLaw(x, y, z)
    }
  }
