package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.TypeLaws.*
import weaver.*
import weaver.scalacheck.*

object CSemigroupSuite extends SimpleIOSuite, Checkers:
  test("Int is a commutative semigroup with addition"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkCSemigroup(x, y, z).isValid)
