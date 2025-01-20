package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object SemiringWithUnitySuite extends SimpleIOSuite, Checkers:
  test("Int is a semiring with unity with addition, 0 and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkSemiringWithUnity(x, y, z).isValid)
