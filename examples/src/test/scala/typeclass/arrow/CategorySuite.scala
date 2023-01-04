package typeclass.arrow

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.{*, given}

class CategorySuite extends ScalaCheckSuite, CategoryLaw:
  private val ab: Int => String     = given_Conversion_Int_String
  private val bc: String => Boolean = given_Conversion_String_Boolean
  private val cd: Boolean => Char   = given_Conversion_Boolean_Char

  property("given Category[Function1] должен удовлетворять законам Category") {
    forAll { (x: Int) =>
      checkCategoryLaw[Function1, Int, String, Boolean, Char](
        ab,
        bc,
        cd,
        intToInt1,
        intToInt2,
        intToInt3
      )(x)
    }
  }
