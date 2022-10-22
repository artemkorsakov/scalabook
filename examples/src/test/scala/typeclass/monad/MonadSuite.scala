package typeclass.monad

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.common.Runner1.stateRunner
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
      given Runner1[[X] =>> State[String, X]] = stateRunner[String](s)
      checkMonadLawWithRunner[[X] =>> State[String, X], Int, String, Boolean](x)
    }
  }

  property("ioMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[IO, Int, String, Boolean](x)
    }
  }
