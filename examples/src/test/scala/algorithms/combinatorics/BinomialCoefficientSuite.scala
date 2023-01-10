package algorithms.combinatorics

import munit.FunSuite
import algorithms.combinatorics.BinomialCoefficient.*

class BinomialCoefficientSuite extends FunSuite:
  test("Вычисление биномиального коэффициента") {
    assertEquals(binomialCoefficient(100, 0), BigInt(1))
    assertEquals(binomialCoefficient(100, 1), BigInt(100))
    assertEquals(
      binomialCoefficient(100, 32),
      BigInt("143012501349174257560226775")
    )
    assertEquals(
      binomialCoefficient(100, 50),
      BigInt("100891344545564193334812497256")
    )
    assertEquals(binomialCoefficient(100, 99), BigInt(100))
    assertEquals(binomialCoefficient(100, 100), BigInt(1))
  }
