package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.{*, given}

class SplitSuite extends ScalaCheckSuite, SplitLaw:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char   = given_Conversion_Boolean_Char

  property("given Split[Function1] должен удовлетворять законам Split") {
    forAll { (x: Int) =>
      checkSplitLaw[Function1, Int, String, Boolean, Char](
        ab,
        bc,
        cd,
        intToInt1,
        intToInt2,
        intToInt3
      )(x)
    }
  }
