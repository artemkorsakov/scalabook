package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object SemiringSuite extends SimpleIOSuite, Checkers:
  test("Int is a semiring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkSemiring(x, y, z).isValid)
