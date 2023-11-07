package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object BandSuite extends SimpleIOSuite, Checkers, BandLaw:
  test("Set is an idempotent semigroup"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      whenSuccess(checkBandLaw(x, y, z)): _ =>
        success
