package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.Divide.given
import typeclass.monoid.Monoid.sumMonoidInstance

class DivideSuite extends ScalaCheckSuite:
  private def delta[A]: A => (A, A) = a => (a, a)

  property("Унарная функция должна удовлетворять законам Divide") {
    forAll { (x: Int) =>
      val fa1: Int => Int = _ + 1
      val fa2: Int => Int = _ - 1
      val fa3: Int => Int = _ * 2

      assertEquals(
        (Divide[[X] =>> X => Int]
          .divide[Int, Int, Int](Divide[[X] =>> X => Int].divide[Int, Int, Int](fa1, fa2)(delta), fa3)(delta))(
          x
        ),
        (Divide[[X] =>> X => Int]
          .divide[Int, Int, Int](fa1, Divide[[X] =>> X => Int].divide[Int, Int, Int](fa2, fa3)(delta))(delta))(
          x
        ),
        "composition"
      )
    }
  }
