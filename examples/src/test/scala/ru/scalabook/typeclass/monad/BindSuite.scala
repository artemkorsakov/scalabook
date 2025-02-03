package ru.scalabook.typeclass.monad

import MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object BindSuite extends SimpleIOSuite, Checkers:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  test("idBind must satisfy the Bind laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(
        checkBindLaw[Id, Int, String, Boolean](
          Id(a),
          Id(b),
          Id(c),
          Id(f),
          Id(g),
          i => Id(f(i)),
          s => Id(g(s))
        )
          .isValid
      )
