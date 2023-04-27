package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class IsEmptySuite extends ScalaCheckSuite, IsEmptyLaw:
  property("listIsEmptyInstance должен удовлетворять законам IsEmpty") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkIsEmptyLawLaw(x, y, z)
    }
  }
