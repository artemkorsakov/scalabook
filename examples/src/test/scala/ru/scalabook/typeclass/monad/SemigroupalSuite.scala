package ru.scalabook.typeclass.monad

import MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object SemigroupalSuite extends SimpleIOSuite, Checkers:
  test("Semigroupal[Id] must satisfy the Semigroupal laws"):
    forall: (a: Int, b: String, c: Boolean) =>
      expect(
        checkSemigroupalLaw[Id, Int, String, Boolean](Id(a), Id(b), Id(c)).isValid
      )
