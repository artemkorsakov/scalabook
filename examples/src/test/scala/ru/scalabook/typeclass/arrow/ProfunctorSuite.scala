package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.ArrowLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ProfunctorSuite extends SimpleIOSuite, Checkers:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  test("Profunctor[Function1] must satisfy the laws of Profunctor"):
    forall: (a: Boolean, c: Int) =>
      expect(
        checkProfunctorLaw[
          Function1,
          Boolean,
          String,
          Int,
          String,
          Boolean,
          Char
        ](gad)(a, c).isValid
      )
