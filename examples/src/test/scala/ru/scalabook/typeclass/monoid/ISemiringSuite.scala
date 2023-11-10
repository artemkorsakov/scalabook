package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object ISemiringSuite extends SimpleIOSuite, Checkers, ISemiringLaw:
  test("Set is an idempotent semiring"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      whenSuccess(checkISemiringLaw(x, y, z)): _ =>
        success
