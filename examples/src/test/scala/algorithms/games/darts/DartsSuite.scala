package algorithms.games.darts

import munit.FunSuite
import algorithms.games.darts.Darts

class DartsSuite extends FunSuite:
  test("allDistinctWaysToCheckOut") {
    val result = Darts.allDistinctWaysToCheckOut
    assertEquals(result(6), 11)
    assertEquals(result.values.sum, 42336)
    assertEquals((2 to 99).map(result.getOrElse(_, 0)).sum, 38182)
  }
