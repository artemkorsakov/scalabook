package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ApplySuite extends SimpleIOSuite, Checkers, ApplyLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idApply must satisfy the Apply laws"):
    forall: (x: Int) =>
      whenSuccess(checkApplyLaw[Id, Int, String, Boolean](Id(x), Id(f), Id(g))):
        _ => success
