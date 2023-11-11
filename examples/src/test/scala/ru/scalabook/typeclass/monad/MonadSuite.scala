package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object MonadSuite extends SimpleIOSuite, Checkers, MonadLaw:
  test("idMonad must satisfy the Monad laws"):
    forall: (x: Int) =>
      whenSuccess(checkMonadLaw[Id, Int, String, Boolean](x)): _ =>
        success
