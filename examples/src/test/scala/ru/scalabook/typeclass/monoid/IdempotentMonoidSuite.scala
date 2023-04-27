package ru.scalabook.typeclass.monoid

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.monoid.IdempotentMonoid.given

class IdempotentMonoidSuite extends ScalaCheckSuite, IdempotentMonoidLaw:
  property(
    "setIdempotentMonoidInstance должен удовлетворять законам идемпотентного моноида"
  ) {
    forAll { (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      checkIdempotentMonoidLaw(x, y, z)
    }
  }
