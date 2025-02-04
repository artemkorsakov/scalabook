package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.arrow.ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ArrowSuite extends SimpleIOSuite, Checkers:
  private val ad: Int => String     = given_Conversion_Int_String
  private val ab: Int => String     = given_Conversion_Int_String
  private val aa: Int => Int        = identity
  private val bc: String => Int     = given_Conversion_String_Int
  private val be: String => Boolean = given_Conversion_String_Boolean
  private val cd: Int => String     = given_Conversion_Int_String
  private val df: String => Char    = given_Conversion_String_Char
  private val ef: Boolean => Char   = given_Conversion_Boolean_Char

  test("Arrow[Function1] must satisfy the laws of Arrow"):
    forall { (a: Int, c: Int, d: String, e: Boolean) =>
      expect(
        checkArrowLaw[Function1, Int, String, Int, String, Boolean, Char](
          ad,
          ab,
          aa,
          bc,
          be,
          cd,
          df,
          ef
        )(a, c, d, e).isValid
      )
    }
end ArrowSuite
