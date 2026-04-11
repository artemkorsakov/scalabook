package ru.scalabook.typeclass.bifunctor

import cats.Id
import ru.scalabook.typeclass.bifunctor.BifunctorLaws.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object BitraverseSuite extends SimpleIOSuite, Checkers:
  test("Bitraverse[Either] must satisfy the laws of Bitraverse"):
    forall:
      (
          fab: Either[Int, String],
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        expect(
          checkBitraverseLaw[
            Either,
            Id,
            Option,
            String,
            Char,
            Int,
            String,
            Boolean
          ](fab, far, fla, faa).isValid
        )
end BitraverseSuite
