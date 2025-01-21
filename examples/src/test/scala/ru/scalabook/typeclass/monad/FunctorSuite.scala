package ru.scalabook.typeclass.monad

import ru.scalabook.typeclass.common.MonadLaws.*
import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object FunctorSuite extends SimpleIOSuite, Checkers:
  test("idFunctor must satisfy the Functor laws"):
    forall: (x: Int) =>
      expect(checkFunctorLaw[Id, Int, String, Boolean](Id(x)).isValid)
