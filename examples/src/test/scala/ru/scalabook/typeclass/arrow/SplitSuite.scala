package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.{*, given}
import weaver.*
import weaver.scalacheck.*

object SplitSuite extends SimpleIOSuite, Checkers:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char   = given_Conversion_Boolean_Char

  test("Split[Function1] must satisfy the laws of Split"):
    forall: (x: Int) =>
      expect(
        checkSplitLaw[Function1, Int, String, Boolean, Char](ab, bc, cd)(x).isValid
      )
