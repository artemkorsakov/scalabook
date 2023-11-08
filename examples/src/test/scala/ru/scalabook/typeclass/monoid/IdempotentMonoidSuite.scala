package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object IdempotentMonoidSuite
    extends SimpleIOSuite,
      Checkers,
      IdempotentMonoidLaw:
  test("Set is an idempotent monoid"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      whenSuccess(checkIdempotentMonoidLaw(x, y, z)): _ =>
        success
