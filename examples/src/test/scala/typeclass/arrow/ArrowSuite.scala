package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.{*, given}

class ArrowSuite extends ScalaCheckSuite, ArrowLaw:
  private val gbc: String => Int = given_Conversion_String_Int
  private val gcd: Int => String = given_Conversion_Int_String

  property("given Arrow[Function1] должен удовлетворять законам Arrow") {
    forAll { (a: Int, c: Int, d: String) =>
      checkArrowLaw[Function1, Int, String, Int, String, Boolean, Char](gcd, gcd, gbc, gcd, intToInt1, intToInt2, intToInt3)(a, c, d)
    }
  }
