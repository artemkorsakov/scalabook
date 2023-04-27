package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.Functions.given
import ru.scalabook.typeclass.common.*
import ru.scalabook.typeclass.monad.InvariantFunctor.given

class InvariantFunctorSuite extends ScalaCheckSuite, InvariantFunctorLaw:
  property(
    "InvariantFunctor[Id] должен удовлетворять законам InvariantFunctor"
  ) {
    forAll { (x: Int) =>
      checkInvariantFunctorLaw[Id, Int, String, Boolean](Id(x))
    }
  }
