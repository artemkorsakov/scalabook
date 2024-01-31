package ru.scalabook.algorithms.equations

import ru.scalabook.algorithms.equations.PolynomialEquation.*

@main def polynomialCoefficientsBench(): Array[Long] =
  polynomialCoefficients(3, Array(1, 683, 44287))
