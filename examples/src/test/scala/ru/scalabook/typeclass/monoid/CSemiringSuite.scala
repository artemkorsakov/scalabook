package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.Laws.*
import weaver.*
import weaver.scalacheck.*

object CSemiringSuite extends SimpleIOSuite, Checkers:
  test("Int is a commutative semiring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkCSemiring(x, y, z).isValid)
