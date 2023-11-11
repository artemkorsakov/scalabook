package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object StrongSuite extends SimpleIOSuite, Checkers, StrongLaw:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  test("Strong[Function1] must satisfy the laws of Strong"):
    forall: (a: Boolean, c: Int, d: String) =>
      whenSuccess(
        checkStrongLaw[Function1, Boolean, String, Int, String, Boolean, Char](
          gad,
          gad
        )(a, c, d)
      ): _ =>
        success
