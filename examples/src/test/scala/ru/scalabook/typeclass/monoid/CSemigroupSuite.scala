package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.CSemigroup.given

class CSemigroupSuite extends ScalaCheckSuite, CSemigroupLaw:
  property(
    "CSemigroup[Int] должен удовлетворять законам коммутативной полугруппы"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkCSemigroupLaw(x, y, z)
    }
  }
