package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Monad.given

class MonadSuite extends ScalaCheckSuite, MonadLaw:
  property("idMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[Id, Int, String, Boolean](x)
    }
  }

  property("optionMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[Option, Int, String, Boolean](x)
    }
  }

  property("listMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[List, Int, String, Boolean](x)
    }
  }

  property("eitherMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[[X] =>> Either[String, X], Int, String, Boolean](x)
    }
  }

  property("writerMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[[X] =>> Writer[String, X], Int, String, Boolean](x)
    }
  }

  property("stateMonad должен удовлетворять законам монады") {
    forAll { (x: Int, s: String) =>
      checkMonadLaw[[X] =>> State[String, X], Int, String, Boolean](x, _.run(s)._2)
    }
  }

  property("ioMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[IO, Int, String, Boolean](x)
    }
  }
