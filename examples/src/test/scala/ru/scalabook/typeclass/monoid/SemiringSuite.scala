package ru.scalabook.typeclass.monoid

import weaver.*
import weaver.scalacheck.*

object SemiringSuite extends SimpleIOSuite, Checkers, SemiringLaw:
  test("Int is a semiring with addition and multiplication"):
    forall: (x: Int, y: Int, z: Int) =>
      whenSuccess(checkSemiringLaw(x, y, z)): _ =>
        success
