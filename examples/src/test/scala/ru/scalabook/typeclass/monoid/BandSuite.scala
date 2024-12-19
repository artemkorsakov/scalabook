package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Laws.checkBandLaw
import weaver.*
import weaver.scalacheck.*

object BandSuite extends SimpleIOSuite, Checkers:
  test("Set is an idempotent semigroup"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      expect(checkBandLaw(x, y, z).isValid)
