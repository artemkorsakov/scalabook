package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.arrow.Profunctor
import typeclass.arrow.Profunctor.given

class ProfunctorSuite extends ScalaCheckSuite, ProfunctorLaw:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  property("given Profunctor[Function1] должен удовлетворять законам Profunctor") {
    forAll { (a: Boolean, c: Int) =>
      checkProfunctorLaw[Function1, Boolean, String, Int, String, Boolean, Char](gad)(f => f(a), f => f(c))
    }
  }
