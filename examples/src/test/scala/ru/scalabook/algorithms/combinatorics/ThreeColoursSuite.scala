package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.ThreeColours.*
import weaver.FunSuite

object ThreeColoursSuite extends FunSuite:
  test("countThreeColoursRows"):
    expect.all(
      countABCRows(1, 1, 1) == 6L,
      countABCRows(2, 1, 1) == 12L,
      countABCRows(2, 2, 0) == 6L,
      countABCRows(2, 2, 1) == 30L,
      countABCRows(2, 2, 2) == 90L,
      countABCRows(0, 2, 2) == 6L,
      countABCRows(2, 0, 2) == 6L
    )
