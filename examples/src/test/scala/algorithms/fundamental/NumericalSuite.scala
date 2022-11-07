package algorithms.fundamental

import algorithms.fundamental.Numerical.decToBinConv
import munit.FunSuite

class NumericalSuite extends FunSuite:
  test("decToBinConv should convert decimal number to binary") {
    assertEquals(decToBinConv(1), "1")
    assertEquals(decToBinConv(2), "10")
    assertEquals(decToBinConv(8), "1000")
    assertEquals(decToBinConv(7), "111")
  }
