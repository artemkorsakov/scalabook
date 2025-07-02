package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object GroupSuite extends SimpleIOSuite, Checkers:
  test("Числа относительно сложения с 0"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkGroupLaw(x, y, z).isValid)
