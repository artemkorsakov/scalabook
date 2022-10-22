package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given

class StrongSuite extends ScalaCheckSuite, StrongLaw:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  property("given Strong[Function1] должен удовлетворять законам Strong") {
    forAll { (a: Boolean, c: Int, d: String) =>
      checkStrongLaw[Function1, Boolean, String, Int, String, Boolean, Char](gad, gad)(a, c, d)
    }
  }
