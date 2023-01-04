package typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.bifunctor.Bifoldable.given
import typeclass.bifunctor.BifoldableLaw
import typeclass.common.*
import typeclass.monoid.Monoid.given

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

        val actual           =
          Bifoldable[Either].bifoldMap(far)(f)(g)(using stringMonoidInstance)
        val expected: String =
          far match
            case Right(value) => value.toString
            case Left(value)  => value.toString
        assertEquals(actual, expected)
    }
  }
