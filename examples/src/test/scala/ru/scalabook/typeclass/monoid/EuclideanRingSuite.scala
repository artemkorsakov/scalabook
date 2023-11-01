package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.Generators.smallNumbersGen
import ru.scalabook.typeclass.monoid.EuclideanRing.given

class EuclideanRingSuite extends ScalaCheckSuite, EuclideanRingLaw:
  property("EuclideanRing[Int] должен удовлетворять законам евклидова кольца") {
    forAll(smallNumbersGen) { (x: Int, y: Int, z: Int) =>
      checkEuclideanRingLaw(x, y, z)
    }
  }
