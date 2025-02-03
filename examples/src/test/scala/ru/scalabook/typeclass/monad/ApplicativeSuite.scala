package ru.scalabook.typeclass.monad

import MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object ApplicativeSuite extends SimpleIOSuite, Checkers:
  test("idApplicative must satisfy the Applicative laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(checkApplicativeLaw[Id, Int, String, Boolean](a, b, c).isValid)
