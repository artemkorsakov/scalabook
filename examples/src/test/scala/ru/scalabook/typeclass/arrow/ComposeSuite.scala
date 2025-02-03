package ru.scalabook.typeclass.arrow

import ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ComposeSuite extends SimpleIOSuite, Checkers:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char   = given_Conversion_Boolean_Char

  test("Compose[Function1] must satisfy the laws of Compose"):
    forall: (x: Int) =>
      expect(
        checkComposeLaw[Function1, Int, String, Boolean, Char](ab, bc, cd)(x).isValid
      )
