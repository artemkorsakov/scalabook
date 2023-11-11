package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object ApplicativeSuite extends SimpleIOSuite, Checkers, ApplicativeLaw:
  test("idApplicative must satisfy the Applicative laws"):
    forall: (x: Int) =>
      whenSuccess(checkApplicativeLaw[Id, Int, String, Boolean](x)): _ =>
        success
