package ru.scalabook.algorithms.equations

import spire.math.Complex
import weaver.FunSuite

object QuadraticEquationSuite extends FunSuite:
  test("solutionsInIntegers"):
    expect.all(
      QuadraticEquation(1, -5, 6).solutionsInIntegers sameElements Array(
        3L,
        2L
      ),
      QuadraticEquation(1, -5, -6).solutionsInIntegers sameElements Array(
        6L,
        -1L
      ),
      QuadraticEquation(4, -8, 4).solutionsInIntegers sameElements Array(1L),
      QuadraticEquation(1, -5, 7).solutionsInIntegers.isEmpty,
      QuadraticEquation(1, -5, 5).solutionsInIntegers.isEmpty,
      QuadraticEquation(3, -4, 1).solutionsInIntegers sameElements Array(1L),
      QuadraticEquation(3, 4, 1).solutionsInIntegers sameElements Array(-1L)
    )

  test("solutions"):
    expect.all(
      QuadraticEquation(1, -5, 6).solutions sameElements Array(3.0, 2.0),
      QuadraticEquation(1, -5, -6).solutions sameElements Array(6.0, -1.0),
      QuadraticEquation(1, -2, 1).solutions sameElements Array(1.0),
      QuadraticEquation(1, -5, 7).solutions.isEmpty,
      QuadraticEquation(1, -5, 5).solutions sameElements Array(
        (5.0 + math.sqrt(5.0)) / 2.0,
        (5.0 - math.sqrt(5.0)) / 2.0
      ),
      QuadraticEquation(3, -4, 1).solutions sameElements Array(1.0, 1.0 / 3.0),
      QuadraticEquation(3, 4, 1).solutions sameElements Array(-1.0 / 3.0, -1)
    )

  test("solutionsInComplexNumbers"):
    expect.all(
      QuadraticEquation(1, -2, 2).solutionsInComplexNumbers sameElements Array(
        Complex(1, 1),
        Complex(1, -1)
      ),
      QuadraticEquation(3, 4, 1).solutionsInComplexNumbers sameElements Array(
        Complex(-1.0 / 3.0, 0.0),
        Complex(-1, 0.0)
      )
    )
end QuadraticEquationSuite
