package ru.scalabook.algorithms.others

import org.scalacheck.Gen
import ru.scalabook.algorithms.others.NewtonsMethod.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object NewtonsMethodSuite extends SimpleIOSuite with Checkers:
  test("The derivative of x cubed must be 3*x*x"):
    val g: Double => Double = x => x * x * x

    forall(Gen.choose(0, 100)): x =>
      expect.all(math.abs(derivative(x, g) - 3 * x * x) <= 1e-3)

  test("Solving the equation cos x = x * x * x by Newton's method"):
    val g: Double => Double = x => math.cos(x) - x * x * x

    forall(Gen.const(0.5)): guess =>
      expect.all(math.abs(newtonsMethod(g, guess) - 0.865474) <= 1e-6)
