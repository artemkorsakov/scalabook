package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Bind.given

class BindSuite extends ScalaCheckSuite, BindLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  property("idBind должен удовлетворять законам Bind") {
    forAll { (x: Int) =>
      checkBindLaw[Id, Int, String, Boolean](
        Id(x),
        Id(f),
        Id(g),
        i => Id(f(i)),
        s => Id(g(s))
      )
    }
  }

  property("optionBind должен удовлетворять законам Bind") {
    forAll { (x: Int) =>
      checkBindLaw[Option, Int, String, Boolean](
        Some(x),
        Some(f),
        Some(g),
        i => Some(f(i)),
        s => Some(g(s))
      )
    }
  }
