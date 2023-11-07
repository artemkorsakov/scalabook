package ru.scalabook.typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.bifunctor.Bifoldable.given
import ru.scalabook.typeclass.common.Instancies.given

class BifoldableSuite extends ScalaCheckSuite, BifoldableLaw:
  private val f: Int => String  = _.toString
  private val g: Char => String = _.toString

  property("Bifoldable[Either] должен удовлетворять законам Bifoldable") {
    forAll {
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        checkBifoldableLaw[Either, String, Char, Int](far, fla, faa)

        val actual =
          Bifoldable[Either].bifoldMap(far)(f)(g)
        val expected: String =
          far match
            case Right(value) => value.toString
            case Left(value)  => value.toString
        assertEquals(actual, expected)
    }
  }
