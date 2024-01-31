package ru.scalabook.algorithms.equations

import ru.scalabook.algorithms.equations.PolynomialEquation.*

private val goodPolynomialArrayTemp: Array[Long] =
  (1L to 10L).map(i => i * i * i).toArray

@main def polynomialCoefficientsBench(): Unit =
  println(polynomialCoefficients(3, goodPolynomialArrayTemp))
