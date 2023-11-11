package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object FunctorSuite extends SimpleIOSuite, Checkers, FunctorLaw:
  test("idFunctor must satisfy the Functor laws"):
    forall: (x: Int) =>
      whenSuccess(checkFunctorLaw[Id, Int, String, Boolean](Id(x))): _ =>
        success
