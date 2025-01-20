package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object CRingSuite extends SimpleIOSuite, Checkers:
  test("Int is a commutative ring with addition, 0, multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkCRing(x, y, z).isValid)
