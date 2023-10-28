package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.CMonoid.given

class CMonoidSuite extends ScalaCheckSuite, CMonoidLaw:
  property(
    "CMonoid[Int] должен удовлетворять законам коммутативного моноида"
  ) {
    forAll { (x: Int, y: Int, z: Int) =>
      checkCMonoidLaw(x, y, z)
    }
  }
