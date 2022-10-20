package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.arrow.Profunctor
import typeclass.arrow.Profunctor.given

class StrongSuite extends ScalaCheckSuite, StrongLaw:
  private val gad: Boolean => String = given_Conversion_Boolean_String

  property("given Strong[Function1] должен удовлетворять законам Strong") {
    forAll { (a: Boolean, c: Int) =>
      checkStrongLaw[Function1, Boolean, String, Int, String, Boolean, Char](gad, gad)(
        f => f(a),
        f => f(c),
        f => f(a, c),
        f => f(c, a),
        f => f(a, c),
        f => f(c, a),
        f => f(a, c),
        f => f(c, a)
      )
    }
  }
