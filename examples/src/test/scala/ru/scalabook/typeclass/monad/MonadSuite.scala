package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.common.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.monad.Monad.given

class MonadSuite extends ScalaCheckSuite, MonadLaw:
  property("idMonad должен удовлетворять законам монады") {
    forAll { (x: Int) =>
      checkMonadLaw[Id, Int, String, Boolean](x)
    }
  }
