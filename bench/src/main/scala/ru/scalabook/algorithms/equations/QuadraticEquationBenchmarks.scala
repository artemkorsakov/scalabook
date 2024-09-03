package ru.scalabook.algorithms.equations

import spire.math.Complex

@main def solutionsInIntegersBench(): Array[Long] =
  QuadraticEquation(1, -5, 6).solutionsInIntegers

@main def solutionsBench(): Array[Double] =
  QuadraticEquation(3, -4, 1).solutions

@main def solutionsInComplexNumbersBench(): Array[Complex[Double]] =
  QuadraticEquation(1, -2, 2).solutionsInComplexNumbers
