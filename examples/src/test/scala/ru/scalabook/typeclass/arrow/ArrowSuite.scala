package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Functions.{*, given}
import weaver.*
import weaver.scalacheck.*

object ArrowSuite extends SimpleIOSuite, Checkers, ArrowLaw:
  private val gbc: String => Int = given_Conversion_String_Int
  private val gcd: Int => String = given_Conversion_Int_String

  test("Arrow[Function1] must satisfy the laws of Arrow"):
    forall: (a: Int, c: Int, d: String) =>
      whenSuccess(
        checkArrowLaw[Function1, Int, String, Int, String, Boolean, Char](
          gcd,
          gcd,
          gbc,
          gcd,
          intToInt1,
          intToInt2,
          intToInt3
        )(a, c, d)
      ): _ =>
        success
