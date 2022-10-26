package typeclass.bifunctor

import munit.{Assertions, ScalaCheckSuite}
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.common.Runner1.*
import typeclass.monad.Functor.{map, given}
import typeclass.monad.FunctorLaw

class BifunctorSuite extends ScalaCheckSuite, FunctorLaw:
  property("idFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      assertEquals(map(Id(x), _ * 2), Id(x * 2))
      checkFunctorLaw[Id, Int, String, Boolean](Id(x))
    }
  }

  property("optionFunctor должен удовлетворять законам функтора") {
    forAll { (maybeInt: Option[Int]) =>
      checkFunctorLaw[Option, Int, String, Boolean](maybeInt)
    }
  }

  property("listFunctor должен удовлетворять законам функтора") {
    forAll { (list: List[Int]) =>
      checkFunctorLaw[List, Int, String, Boolean](list)
    }
  }

  property("eitherFunctor должен удовлетворять законам функтора") {
    forAll { (either: Either[String, Int]) =>
      checkFunctorLaw[[X] =>> Either[String, X], Int, String, Boolean](either)
    }
  }

  property("writerFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      val writer = Writer(() => ("state", x))
      checkFunctorLaw[[X] =>> Writer[String, X], Int, String, Boolean](writer)
    }
  }

  property("stateFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int, s: String) =>
      val state = State[String, Int](s => (s, x))
      given Runner1[[X] =>> State[String, X]] = stateRunner[String](s)
      checkFunctorLawWithRunner[[X] =>> State[String, X], Int, String, Boolean](state)
    }
  }

  property("nestedFunctor должен удовлетворять законам функтора") {
    forAll { (maybeInt: Option[Int]) =>
      val nested = Nested[Id, Option, Int](Id(maybeInt))
      checkFunctorLaw[[X] =>> Nested[Id, Option, X], Int, String, Boolean](nested)
    }
  }

  property("ioFunctor должен удовлетворять законам функтора") {
    forAll { (x: Int) =>
      checkFunctorLaw[IO, Int, String, Boolean](IO(() => x))
    }
  }
