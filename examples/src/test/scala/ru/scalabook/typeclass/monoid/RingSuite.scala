package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.Ring.given

class RingSuite extends ScalaCheckSuite, RingLaw:
  property("Ring[Int] должен удовлетворять законам кольца") {
    forAll { (x: Int, y: Int, z: Int) =>
      checkRingLaw(x, y, z)
    }
  }
