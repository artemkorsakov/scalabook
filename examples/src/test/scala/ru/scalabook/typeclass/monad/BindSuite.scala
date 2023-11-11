package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object BindSuite extends SimpleIOSuite, Checkers, BindLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idBind must satisfy the Bind laws"):
    forall: (x: Int) =>
      whenSuccess(
        checkBindLaw[Id, Int, String, Boolean](
          Id(x),
          Id(f),
          Id(g),
          i => Id(f(i)),
          s => Id(g(s))
        )
      ): _ =>
        success
