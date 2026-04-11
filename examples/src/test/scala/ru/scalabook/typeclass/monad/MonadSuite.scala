package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

import MonadLaws.*

object MonadSuite extends SimpleIOSuite, Checkers:
  test("idMonad must satisfy the Monad laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(checkMonadLaw[Id, Int, String, Boolean](a, b, c).isValid)
