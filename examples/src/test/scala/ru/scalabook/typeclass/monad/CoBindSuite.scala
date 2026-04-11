package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Env
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object CoBindSuite extends SimpleIOSuite, Checkers:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idCoBind must satisfy the laws of CoBind"):
    forall: (x: Int) =>
      expect(
        checkCoBindLaw[Id, Int, String, Boolean](
          Id(x),
          idInt => f(idInt),
          idStr => g(idStr)
        ).isValid
      )

  test("envCoBind must satisfy the laws of CoBind"):
    forall: (x: Int, y: String) =>
      expect(
        checkCoBindLaw[[X] =>> Env[X, String], Int, String, Boolean](
          Env[Int, String](x, y),
          fa => f(fa.a),
          fb => g(fb.a)
        ).isValid
      )
end CoBindSuite
