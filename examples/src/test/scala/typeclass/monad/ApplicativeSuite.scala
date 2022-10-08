package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.Applicative.{apply, map, unit, given}
import typeclass.monad.Functor.given

class ApplicativeSuite extends ScalaCheckSuite:
  private val f: Int => String = _.toString
  private val g: String => Boolean = _.startsWith("1")

  property("idApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[Id, Int, String, Boolean](x, f, g)
    }
  }

  property("optionApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[Id, Int, String, Boolean](x, f, g)
    }
  }

  property("listApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[List, Int, String, Boolean](x, f, g)
    }
  }

  property("eitherApplicative[E] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[x] =>> Either[String, x], Int, String, Boolean](x, f, g)
    }
  }

  property("writerApplicative[W] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[x] =>> Writer[String, x], Int, String, Boolean](x, f, g)
    }
  }

  property("stateApplicative[S] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      assertEquals(
        map[[x] =>> State[String, x], Int, String](unit(x), f).run("state"),
        unit[[x] =>> State[String, x], String](f(x)).run("state")
      )
      assertEquals(
        apply[[x] =>> State[String, x], Int, String](unit(f))(unit(x)).run("state"),
        unit[[x] =>> State[String, x], String](f(x)).run("state")
      )
      // checkStateFunctor(unit(x), f, g)(using stateApplicative[String])
    }
  }

  property("nestedApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[[X] =>> Nested[Id, Option, X], Int, String, Boolean](x, f, g)
    }
  }

  property("ioApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicative[IO, Int, String, Boolean](x, f, g)
    }
  }

  private def checkApplicative[F[_], A, B, C](x: A, f: A => B, g: B => C)(using applicative: Applicative[F]): Unit =
    assertEquals(map(unit(x), f), unit(f(x)))
    assertEquals(apply(unit(f))(unit(x)), unit(f(x)))
    // checkFunctor(unit(x), f, g)(using applicative)
    
    /*
    - Identity: `apply(unit(identity))(fa) == fa`
    - `unit(x).map(f) == unit(f(x))`
    - `fa.map(f) == apply(unit(f))(fa)`
    - Homomorphism: `apply(unit(f))(unit(x)) == unit(f(x))`
    - Interchange: `apply(f)(unit(a)) == apply(unit((f: A => B) => f(a)))(f)`
    */
