package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.BinomialCoefficient.*
import weaver.FunSuite

object BinomialCoefficientSuite extends FunSuite:
  test("Calculation of the binomial coefficient"):
    expect.all(
      binomialCoefficient(100, 0) == BigInt(1),
      binomialCoefficient(100, 1) == BigInt(100),
      binomialCoefficient(100, 32) == BigInt("143012501349174257560226775"),
      binomialCoefficient(100, 50) == BigInt("100891344545564193334812497256"),
      binomialCoefficient(100, 99) == BigInt(100),
      binomialCoefficient(100, 100) == BigInt(1)
    )
