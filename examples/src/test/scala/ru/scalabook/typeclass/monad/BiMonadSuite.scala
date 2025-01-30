package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import ru.scalabook.typeclass.common.MonadLaws.*
import weaver.*
import weaver.scalacheck.*

object BiMonadSuite extends SimpleIOSuite, Checkers:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idBiMonad must satisfy the laws of BiMonad"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(
        checkBimonadLaw[Id, Int, String, Boolean](
          a,
          b,
          c,
          Id(a),
          idInt => f(idInt),
          idStr => g(idStr)
        ).isValid
      )
end BiMonadSuite
