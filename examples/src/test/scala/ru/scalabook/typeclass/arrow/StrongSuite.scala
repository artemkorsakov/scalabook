package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

import ArrowLaws.*

object StrongSuite extends SimpleIOSuite, Checkers:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  test("Strong[Function1] must satisfy the laws of Strong"):
    forall: (a: Boolean, c: Int, d: String) =>
      expect(
        checkStrongLaw[Function1, Boolean, String, Int, String, Boolean, Char](
          gad,
          gad
        )(a, c, d).isValid
      )
