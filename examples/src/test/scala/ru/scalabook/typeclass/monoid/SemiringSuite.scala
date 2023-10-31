package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.Semiring.given

class SemiringSuite extends ScalaCheckSuite, SemiringLaw:
  property("Semiring[Int] должен удовлетворять законам полукольца") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkSemiringLaw(x, y, z)
    }
  }
