package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.{*, given}
import weaver.*
import weaver.scalacheck.*

object ArrowSuite extends SimpleIOSuite, Checkers:
  private val gbc: String => Int = given_Conversion_String_Int
  private val gcd: Int => String = given_Conversion_Int_String

  test("Arrow[Function1] must satisfy the laws of Arrow"):
    forall: (a: Int, c: Int, d: String) =>
      expect(
        checkArrowLaw[Function1, Int, String, Int, String, Boolean, Char](
          gcd,
          gcd,
          gbc,
          gcd,
          intToInt1
        )(a, c, d).isValid
      )
