package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.{*, given}
import typeclass.arrow.Compose
import typeclass.arrow.Compose.given

class ComposeSuite extends ScalaCheckSuite, ComposeLaw:
  private val ab: Int => String = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char = given_Conversion_Boolean_Char

  property("given Compose[Function1] должен удовлетворять законам Compose") {
    forAll { (x: Int) =>
      checkComposeLaw[Function1, Int, String, Boolean, Char](ab, bc, cd, intToInt1, intToInt2, intToInt3)(
        f => f(x),
        f => f(x)
      )
    }
  }
