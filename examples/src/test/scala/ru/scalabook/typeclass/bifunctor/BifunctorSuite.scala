package ru.scalabook.typeclass.bifunctor

import ru.scalabook.typeclass.common.Functions.given
import weaver.*
import weaver.scalacheck.*

object BifunctorSuite extends SimpleIOSuite, Checkers, BifunctorLaw:
  private val f: String => Char = _.headOption.getOrElse('a')
  private val g: Int => Boolean = _ % 2 == 0

  test("Bifunctor[Either] must satisfy the laws of Bifunctor"):
    forall:
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        val expected: Either[Char, Boolean] =
          fla match
            case Right(value) => Right(g(value))
            case Left(value)  => Left(f(value))
        whenSuccess(
          check(Bifunctor[Either].bimap(fla)(f, g) == expected) combine
            checkBifunctorLaw[Either, String, Char, Int, String, Boolean](
              far,
              fla,
              faa
            )
        ): _ =>
          success
