package ru.scalabook.algorithms.games.nim

import weaver.FunSuite

object NimSuite extends FunSuite:
  test("Nim.getX"):
    expect.all(
      Nim.getX(0, 0, 0) == 0L,
      Nim.getX(0, 0, 3) > 0,
      Nim.getX(0, 5, 5) == 0L,
      Nim.getX(0, 5, 6) > 0,
      Nim.getX(1, 2, 3) == 0L,
      Nim.getX(1, 2, 6) > 0,
      (1L to math.pow(2, 10).toLong).count(i =>
        Nim.getX(i, 2 * i, 3 * i) == 0
      ) == 144
    )
