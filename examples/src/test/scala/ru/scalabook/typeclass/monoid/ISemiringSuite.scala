package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object ISemiringSuite extends SimpleIOSuite, Checkers:
  test("Set is an idempotent semiring"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      expect(checkISemiring(x, y, z).isValid)
