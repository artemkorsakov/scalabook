package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.HammingNumber.*

class HammingNumberSuite extends FunSuite:
  test("isHammingNumber") {
    assertEquals(isHammingNumber(2125764000, 5), true)
    assertEquals(isHammingNumber(2125764000, 7), true)
    assertEquals(isHammingNumber(2125764000, 3), false)
  }
