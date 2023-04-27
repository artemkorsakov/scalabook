package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class PlusSuite extends ScalaCheckSuite, PlusLaw:
  property("listPlusInstance должен удовлетворять законам Plus") {
    forAll { (x: List[Int], y: List[Int], z: List[Int]) =>
      checkPlusLaw(x, y, z)
    }
  }
