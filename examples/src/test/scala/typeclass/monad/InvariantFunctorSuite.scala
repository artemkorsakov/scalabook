package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.InvariantFunctor.given

class InvariantFunctorSuite extends ScalaCheckSuite, InvariantFunctorLaw:
  property("idFunctor должен удовлетворять законам инвариантного функтора") {
    forAll { (x: Int) =>
      checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x))
    }
  }
