package algorithms.fundamental

import algorithms.fundamental.ChineseRemainderTheorem.*
import munit.FunSuite

class ChineseRemainderTheoremSuite extends FunSuite:
  test("Китайская теорема об остатках") {
    assertEquals(solution(Array(2, 3), Array(1, 2)), BigInt(5))
    assertEquals(solution(Array(707, 527), Array(0, 5)), BigInt(258762))
    assertEquals(
      solution(
        Array(1504170715041707L, 4503599627370517L),
        Array(0L, 8912517754604L)
      ),
      BigInt(1504170715041707L * 3)
    )
  }
