package ru.scalabook.algorithms.equations

import ru.scalabook.algorithms.numbers.*
import weaver.FunSuite

object QuadraticEquationSuite extends FunSuite:
  test("solutionsInIntegers"):
    expect.all(
      QuadraticEquation(1, -5, 6).solutionsInIntegers == Seq(3L, 2L),
      QuadraticEquation(1, -5, -6).solutionsInIntegers == Seq(6L, -1L),
      QuadraticEquation(4, -8, 4).solutionsInIntegers == Seq(1L),
      QuadraticEquation(1, -5, 7).solutionsInIntegers == Seq(),
      QuadraticEquation(1, -5, 5).solutionsInIntegers == Seq(),
      QuadraticEquation(3, -4, 1).solutionsInIntegers == Seq(1L),
      QuadraticEquation(3, 4, 1).solutionsInIntegers == Seq(-1L)
    )

  test("solutions"):
    expect.all(
      QuadraticEquation(1, -5, 6).solutions == Seq(3.0, 2.0),
      QuadraticEquation(1, -5, -6).solutions == Seq(6.0, -1.0),
      QuadraticEquation(1, -2, 1).solutions == Seq(1.0),
      QuadraticEquation(1, -5, 7).solutions == Seq(),
      QuadraticEquation(1, -5, 5).solutions == Seq(
        (5.0 + math.sqrt(5.0)) / 2.0,
        (5.0 - math.sqrt(5.0)) / 2.0
      ),
      QuadraticEquation(3, -4, 1).solutions == Seq(1.0, 1.0 / 3.0),
      QuadraticEquation(3, 4, 1).solutions == Seq(-1.0 / 3.0, -1)
    )

  test("solutionsInComplexNumbers"):
    expect.all(
      QuadraticEquation(1, -2, 2).solutionsInComplexNumbers == Seq(
        ComplexNumber(1, 1),
        ComplexNumber(1, -1)
      ),
      QuadraticEquation(3, 4, 1).solutionsInComplexNumbers == Seq(
        ComplexNumber(-1.0 / 3.0, 0.0),
        ComplexNumber(-1, 0.0)
      )
    )
