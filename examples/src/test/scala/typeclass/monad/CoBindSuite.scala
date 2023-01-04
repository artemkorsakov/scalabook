package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.*
import typeclass.monad.Bind.given

class CoBindSuite extends ScalaCheckSuite, CoBindLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  property("idCoBind должен удовлетворять законам CoBind") {
    forAll { (x: Int) =>
      checkCoBindLaw[Id, Int, String, Boolean](
        Id(x),
        idInt => f(idInt.value),
        idStr => g(idStr.value)
      )
    }
  }

  property("envCoBind должен удовлетворять законам CoBind") {
    forAll { (x: Int, y: String) =>
      checkCoBindLaw[[X] =>> Env[X, String], Int, String, Boolean](
        Env[Int, String](x, y),
        fa => f(fa.a),
        fb => g(fb.a)
      )
    }
  }
