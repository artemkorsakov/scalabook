package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Applicative.{apply, map, unit, given}
import typeclass.monad.FunctorSuite.checkFunctor

class ApplicativeSuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")

  property("Applicative[Id] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[Id, Int, String, Boolean](x, f, g)
    }
  }

  property("Applicative[Option] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[Id, Int, String, Boolean](x, f, g)
    }
  }

  property("Applicative[List] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[List, Int, String, Boolean](x, f, g)
    }
  }

  property("Applicative[Either] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[x] =>> Either[String, x], Int, String, Boolean](x, f, g)
    }
  }

  property("Applicative[Writer] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[x] =>> Writer[String, x], Int, String, Boolean](x, f, g)
    }
  }

  property("Applicative[State] должна удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[x] =>> State[String, x], Int, String, Boolean](x, f, g)
    }
  }

  private def checkApplicative[F[_], A, B, C](x: A, f: A => B, g: B => C)(using applicative: Applicative[F]): Unit =
    assertEquals(map(unit(x), f), unit(f(x)))
    assertEquals(apply(unit(f))(unit(x)), unit(f(x)))
    checkFunctor(unit(x), f, g)(using applicative)
