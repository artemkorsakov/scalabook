package algorithms.others

import algorithms.others.NewtonsMethod.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class NewtonsMethodSuite extends ScalaCheckSuite:
  property("Производная x в кубе должна равняться 3*x*x") {
    val g: Double => Double = x => x * x * x

    forAll(Gen.choose(0, 100)) { x =>
      math.abs(derivative(x, g) - 3 * x * x) <= 1e-3
    }
  }

  property("Решение уравнения cos x = x * x * x методом Ньютона") {
    val g: Double => Double = x => math.cos(x) - x * x * x

    forAll(Gen.const(0.5)) { guess =>
      math.abs(newtonsMethod(g, guess) - 0.865474) <= 1e-6
    }
  }
