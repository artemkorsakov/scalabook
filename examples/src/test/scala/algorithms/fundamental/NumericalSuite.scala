package algorithms.fundamental

import algorithms.fundamental.Numerical.*
import munit.FunSuite

class NumericalSuite extends FunSuite:
  test("decToBinConv should convert decimal number to binary") {
    assertEquals(decToBinConv(1), "1")
    assertEquals(decToBinConv(2), "10")
    assertEquals(decToBinConv(8), "1000")
    assertEquals(decToBinConv(7), "111")
  }

  test("gcd should return the greatest common divisor") {
    assertEquals(gcdByEuclideanAlgorithm(4851, 3003), 231L)
    assertEquals(gcd(4851, 3003), 231L)
  }

  test("'power' should raise the number to a power") {
    assertEquals(power(7, 6), BigInt(117649))
  }
