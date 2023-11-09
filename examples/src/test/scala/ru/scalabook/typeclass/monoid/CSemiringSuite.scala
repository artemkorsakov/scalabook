package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object CSemiringSuite extends SimpleIOSuite, Checkers, CSemiringLaw:
  test("Int is a commutative semiring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkCSemiringLaw(x, y, z)): _ =>
        success
