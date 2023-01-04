package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given

class ProfunctorSuite extends ScalaCheckSuite, ProfunctorLaw:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  property(
    "given Profunctor[Function1] должен удовлетворять законам Profunctor"
  ) {
    forAll { (a: Boolean, c: Int) =>
      checkProfunctorLaw[
        Function1,
        Boolean,
        String,
        Int,
        String,
        Boolean,
        Char
      ](gad)(a, c)
    }
  }
