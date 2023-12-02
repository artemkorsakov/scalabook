package ru.scalabook.typeclass.bifunctor

import cats.Id
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object BitraverseSuite extends SimpleIOSuite, Checkers, BitraverseLaw:
  test("Bitraverse[Either] must satisfy the laws of Bitraverse"):
    forall:
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        whenSuccess(
          checkBitraverseLaw[
            Either,
            Id,
            Option,
            String,
            Char,
            Int,
            String,
            Boolean
          ](far, fla, faa)
        ): _ =>
          success
end BitraverseSuite
