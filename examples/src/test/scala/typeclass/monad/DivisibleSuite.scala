package typeclass.monad

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import typeclass.monad.Divisible.{conquer, divide, given}
import typeclass.monoid.Monoid.sumMonoidInstance

class DivisibleSuite extends ScalaCheckSuite:
  private def delta[A]: A => (A, A) = a => (a, a)

  property("Унарная функция должна удовлетворять законам Divisible") {
    forAll { (x: Int) =>
      val fa1: Int => Int = _ + 1
      val fa2: Int => Int = _ - 1
      val fa3: Int => Int = _ * 2

      assertEquals(
        (divide[[X] =>> X => Int, Int, Int, Int](divide[[X] =>> X => Int, Int, Int, Int](fa1, fa2)(delta), fa3)(delta))(
          x
        ),
        (divide[[X] =>> X => Int, Int, Int, Int](fa1, divide[[X] =>> X => Int, Int, Int, Int](fa2, fa3)(delta))(delta))(
          x
        ),
        "composition"
      )
      assertEquals(
        divide[[X] =>> X => Int, Int, Int, Int](fa1, conquer[[X] =>> X => Int, Int])(delta)(x),
        fa1(x),
        "rightIdentity"
      )
      assertEquals(
        divide[[X] =>> X => Int, Int, Int, Int](conquer[[X] =>> X => Int, Int], fa1)(delta)(x),
        fa1(x),
        "leftIdentity"
      )
    }
  }
