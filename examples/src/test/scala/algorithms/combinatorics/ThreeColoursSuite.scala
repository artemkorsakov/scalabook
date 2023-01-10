package algorithms.combinatorics

import munit.FunSuite
import algorithms.combinatorics.ThreeColours.*

class ThreeColoursSuite extends FunSuite:
  test("countThreeColoursRows") {
    assertEquals(countABCRows(1, 1, 1), 6L)
    assertEquals(countABCRows(2, 1, 1), 12L)
    assertEquals(countABCRows(2, 2, 0), 6L)
    assertEquals(countABCRows(2, 2, 1), 30L)
    assertEquals(countABCRows(2, 2, 2), 90L)
    assertEquals(countABCRows(0, 2, 2), 6L)
    assertEquals(countABCRows(2, 0, 2), 6L)
  }
