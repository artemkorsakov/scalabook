package typeclass.bifunctor

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.bifunctor.Bifunctor.given
import typeclass.bifunctor.BifunctorLaw
import typeclass.common.*

class BifunctorSuite extends ScalaCheckSuite, BifunctorLaw:
  private val f: String => Char = _.headOption.getOrElse('a')
  private val g: Int => Boolean = _ % 2 == 0

  property("Bifunctor[Either] должен удовлетворять законам Bifunctor") {
    forAll {
      (
          far: Either[Int, Char],
          fla: Either[String, Int],
          faa: Either[Int, Int]
      ) =>
        val expected: Either[Char, Boolean] =
          fla match
            case Right(value) => Right(g(value))
            case Left(value)  => Left(f(value))
        assertEquals(Bifunctor[Either].bimap(fla)(f, g), expected)
        checkBifunctorLaw[Either, String, Char, Int, String, Boolean](
          far,
          fla,
          faa
        )
    }
  }

  property("Bifunctor[Writer] должен удовлетворять законам Bifunctor") {
    forAll { (i0: Int, i1: Int, i2: Int, i3: Int, s: String, c: Char) =>
      val far: Writer[Int, Char]   = Writer(() => (i0, c))
      val fla: Writer[String, Int] = Writer(() => (s, i1))
      val faa: Writer[Int, Int]    = Writer(() => (i2, i3))
      checkBifunctorLaw[Writer, String, Char, Int, String, Boolean](
        far,
        fla,
        faa
      )

      val expected: Writer[Char, Boolean] =
        val (a, b) = fla.run()
        Writer { () => (f(a), g(b)) }
      assertEquals(Bifunctor[Writer].bimap(fla)(f, g), expected)
    }
  }
