package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Applicative.given
import typeclass.monad.Functor.given

class ApplicativeSuite extends ScalaCheckSuite, ApplicativeLaw:
  property("idApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[Id, Int, String, Boolean](x)
    }
  }

  property("optionApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[Option, Int, String, Boolean](x)
    }
  }

  property("listApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[List, Int, String, Boolean](x)
    }
  }

  property("eitherApplicative[E] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[x] =>> Either[String, x], Int, String, Boolean](x)
    }
  }

  property("writerApplicative[W] должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[x] =>> Writer[String, x], Int, String, Boolean](x)
    }
  }

  property("stateApplicative[S] должен удовлетворять законам Applicative") {
    forAll { (x: Int, s: String) =>
      checkApplicativeLaw[[x] =>> State[String, x], Int, String, Boolean](x, _.run(s)._2)
    }
  }

  property("nestedApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[[X] =>> Nested[Id, Option, X], Int, String, Boolean](x)
    }
  }

  property("ioApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[IO, Int, String, Boolean](x)
    }
  }
