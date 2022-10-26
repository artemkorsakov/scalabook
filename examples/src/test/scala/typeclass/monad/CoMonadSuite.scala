package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.common.{Env, Id}
import typeclass.monad.CoMonad.{coFlatMap, coUnit, given}

class CoMonadSuite extends ScalaCheckSuite:
  property("idCoMonad должен быть комонадой") {
    forAll { (x: Int, y: String) =>
      val fa = Id(x)
      assertEquals(coUnit(fa), x)
      assertEquals(coFlatMap(fa)(_.value.toString), Id(x.toString))
    }
  }

  property("envCoMonad должен быть комонадой") {
    forAll { (x: Int, y: String) =>
      val fa = Env(x, y)
      assertEquals(coUnit[[X] =>> Env[X, String], Int](fa), x)
      assertEquals(coFlatMap[[X] =>> Env[X, String], Int, String](fa)(_.a.toString), Env(x.toString, y))
    }
  }
