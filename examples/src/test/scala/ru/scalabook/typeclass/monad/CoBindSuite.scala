package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Env
import weaver.*
import weaver.scalacheck.*
import ru.scalabook.typeclass.common.Functions.given

object CoBindSuite extends SimpleIOSuite, Checkers, CoBindLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idCoBind must satisfy the laws of CoBind"):
    forall: (x: Int) =>
      whenSuccess(
        checkCoBindLaw[Id, Int, String, Boolean](
          Id(x),
          idInt => f(idInt),
          idStr => g(idStr)
        )
      ): _ =>
        success

  test("envCoBind must satisfy the laws of CoBind"):
    forall: (x: Int, y: String) =>
      whenSuccess(
        checkCoBindLaw[[X] =>> Env[X, String], Int, String, Boolean](
          Env[Int, String](x, y),
          fa => f(fa.a),
          fb => g(fb.a)
        )
      ): _ =>
        success
end CoBindSuite
