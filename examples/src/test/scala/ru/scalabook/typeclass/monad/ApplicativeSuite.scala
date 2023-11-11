package ru.scalabook.typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.typeclass.common.*
import ru.scalabook.typeclass.common.Functions.given
import ru.scalabook.typeclass.monad.Applicative.given

class ApplicativeSuite extends ScalaCheckSuite, ApplicativeLaw:
  property("idApplicative должен удовлетворять законам Applicative") {
    forAll { (x: Int) =>
      checkApplicativeLaw[Id, Int, String, Boolean](x)
    }
  }
