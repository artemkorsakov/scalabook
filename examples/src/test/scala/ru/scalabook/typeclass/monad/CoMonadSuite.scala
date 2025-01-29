package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.MonadLaws.*
import ru.scalabook.typeclass.common.Env
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object CoMonadSuite extends SimpleIOSuite, Checkers:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idCoMonad must satisfy the laws of CoMonad"):
    forall: (x: Int) =>
      expect(
        checkCoMonadLaw[Id, Int, String, Boolean](
          Id(x),
          idInt => f(idInt),
          idStr => g(idStr)
        ).isValid
      )

  test("envCoMonad must satisfy the laws of  CoMonad"):
    forall: (x: Int, y: String) =>
      expect(
        checkCoMonadLaw[[X] =>> Env[X, String], Int, String, Boolean](
          Env[Int, String](x, y),
          fa => f(fa.a),
          fb => g(fb.a)
        ).isValid
      )
end CoMonadSuite
