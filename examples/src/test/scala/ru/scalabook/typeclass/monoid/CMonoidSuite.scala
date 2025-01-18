package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import weaver.*
import weaver.scalacheck.*

object CMonoidSuite extends SimpleIOSuite, Checkers:
  test("Int is a commutative monoid with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkCMonoid(x, y, z).isValid)
