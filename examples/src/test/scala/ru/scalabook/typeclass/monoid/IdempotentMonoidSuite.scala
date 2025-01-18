package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import weaver.*
import weaver.scalacheck.*

object IdempotentMonoidSuite extends SimpleIOSuite, Checkers:
  test("Set is an idempotent monoid"):
    forall: (x: Set[Int], y: Set[Int], z: Set[Int]) =>
      expect(checkIdempotentMonoid(x, y, z).isValid)
