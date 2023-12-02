package ru.scalabook.typeclass.bifunctor

import ru.scalabook.typeclass.common.Instancies.given
import weaver.*
import weaver.scalacheck.*

object BifoldableSuite extends SimpleIOSuite, Checkers, BifoldableLaw:
  private val f: Int => String  = _.toString
  private val g: Char => String = _.toString

  test("Bifoldable[Either] must satisfy the laws of Bifoldable"):
    forall:
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        whenSuccess(
          checkBifoldableLaw[Either, String, Char, Int](far, fla, faa) combine {
            val expected: String =
              far match
                case Right(value) => value.toString
                case Left(value)  => value.toString
            check(Bifoldable[Either].bifoldMap(far)(f)(g) == expected)
          }
        ): _ =>
          success
end BifoldableSuite
