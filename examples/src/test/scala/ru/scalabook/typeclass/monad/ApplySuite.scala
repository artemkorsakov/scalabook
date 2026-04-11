package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object ApplySuite extends SimpleIOSuite, Checkers:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idApply must satisfy the Apply laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(
        checkApplyLaw[Id, Int, String, Boolean](
          Id(a),
          Id(b),
          Id(c),
          Id(f),
          Id(g)
        ).isValid
      )
