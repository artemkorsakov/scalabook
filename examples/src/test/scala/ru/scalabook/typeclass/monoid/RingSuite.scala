package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object RingSuite extends SimpleIOSuite, Checkers:
  test("Int is a ring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkRing(x, y, z).isValid)
