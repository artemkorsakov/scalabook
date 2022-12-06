package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.Functions.given
import typeclass.common.{Env, Id}
import typeclass.monad.CoMonad.given

class CoMonadSuite extends ScalaCheckSuite, CoMonadLaw:
  private val f: Int => String     = given_Conversion_Int_String.apply
  private val g: String => Boolean = given_Conversion_String_Boolean.apply

  property("idCoMonad должен удовлетворять законам CoMonad") {
    forAll { (x: Int) =>
      checkCoMonadLaw[Id, Int, String, Boolean](Id(x), idInt => f(idInt.value), idStr => g(idStr.value))
    }
  }

  property("envCoMonad должен удовлетворять законам CoMonad") {
    forAll { (x: Int, y: String) =>
      checkCoMonadLaw[[X] =>> Env[X, String], Int, String, Boolean](
        Env[Int, String](x, y),
        fa => f(fa.a),
        fb => g(fb.a)
      )
    }
  }
