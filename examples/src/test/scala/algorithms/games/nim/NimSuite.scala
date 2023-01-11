package algorithms.games.nim

import munit.FunSuite
import algorithms.games.nim.Nim

class NimSuite extends FunSuite:
  test("Nim.getX") {
    assertEquals(Nim.getX(0, 0, 0), 0L)
    assert(Nim.getX(0, 0, 3) > 0)
    assertEquals(Nim.getX(0, 5, 5), 0L)
    assert(Nim.getX(0, 5, 6) > 0)
    assertEquals(Nim.getX(1, 2, 3), 0L)
    assert(Nim.getX(1, 2, 6) > 0)
    assertEquals(
      (1L to math.pow(2, 10).toLong).count(i => Nim.getX(i, 2 * i, 3 * i) == 0),
      144
    )
  }
