package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

import ArrowLaws.*

object CategorySuite extends SimpleIOSuite, Checkers:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char   = given_Conversion_Boolean_Char
  private val intToInt1: Int => Int = _ * 2

  test("Category[Function1] must satisfy the laws of Category"):
    forall: (x: Int) =>
      expect(
        checkCategoryLaw[Function1, Int, String, Boolean, Char](
          ab,
          bc,
          cd,
          intToInt1
        )(x).isValid
      )
