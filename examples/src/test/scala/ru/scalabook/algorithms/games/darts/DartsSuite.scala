package ru.scalabook.algorithms.games.darts

import weaver.FunSuite

object DartsSuite extends FunSuite:
  test("allDistinctWaysToCheckOut"):
    val result = Darts.allDistinctWaysToCheckOut
    expect.all(
      result(6) == 11,
      result.values.sum == 42336,
      (2 to 99).map(result.getOrElse(_, 0)).sum == 38182
    )
