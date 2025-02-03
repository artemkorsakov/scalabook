package ru.scalabook.typeclass.arrow

import ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object SplitSuite extends SimpleIOSuite, Checkers:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Int     = given_Conversion_String_Int
  private val cd: Int => String     = given_Conversion_Int_String
  private val ef: Boolean => Char   = given_Conversion_Boolean_Char
  private val be: String => Boolean = given_Conversion_String_Boolean
  private val df: String => Char    = given_Conversion_String_Char

  test("Split[Function1] must satisfy the laws of Split"):
    forall: (a: Int, c: Int, e: Boolean) =>
      expect(
        checkSplitLaw[Function1, Int, String, Int, String, Boolean, Char](
          ab,
          bc,
          cd,
          ef,
          be,
          df
        )(a, c, e).isValid
      )
end SplitSuite
