package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.*
import typeclass.monad.InvariantFunctor.given

class InvariantFunctorSuite extends ScalaCheckSuite, InvariantFunctorLaw:
  private val f1: Int => String = _.toString
  private val g1: String => Int = _.toInt
  private val f2: String => Boolean = _.startsWith("1")
  private val g2: Boolean => String = _.toString

  property("idFunctor должен удовлетворять законам инвариантного функтора") {
    forAll { (x: Int) =>
      checkInvariantFunctorLaw(Id(x), f1, g1, f2, g2)
    }
  }
