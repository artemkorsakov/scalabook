package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Apply.given

class ApplySuite extends ScalaCheckSuite, ApplyLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  property("idApply должен удовлетворять законам Apply") {
    forAll { (x: Int) =>
      checkApplyLaw[Id, Int, String, Boolean](Id(x), Id(f), Id(g))
    }
  }

  property("optionApply должен удовлетворять законам Apply") {
    forAll { (x: Int) =>
      checkApplyLaw[Option, Int, String, Boolean](Some(x), Some(f), Some(g))
    }
  }
