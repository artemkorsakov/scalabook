package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.InvariantFunctor.given

class InvariantFunctorSuite extends ScalaCheckSuite, InvariantFunctorLaw:
  property("InvariantFunctor[Id] должен удовлетворять законам InvariantFunctor") {
    forAll { (x: Int) =>
      checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x))
    }
  }
