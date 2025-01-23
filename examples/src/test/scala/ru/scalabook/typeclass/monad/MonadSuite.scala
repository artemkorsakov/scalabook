package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Instancies.given
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object MonadSuite extends SimpleIOSuite, Checkers:
  test("idMonad must satisfy the Monad laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(checkMonadLaw[Id, Int, String, Boolean](a, b, c).isValid)
