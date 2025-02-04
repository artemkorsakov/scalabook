package ru.scalabook.typeclass.bifunctor

import ru.scalabook.typeclass.bifunctor.BifunctorLaws.*
import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object BifunctorSuite extends SimpleIOSuite, Checkers:
  test("Bifunctor[Either] must satisfy the laws of Bifunctor"):
    forall:
      (
          fab: Either[Int, String],
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        expect(
          checkBifunctorLaw[Either, String, Char, Int, String, Boolean](
            fab,
            far,
            fla,
            faa
          ).isValid
        )
end BifunctorSuite
