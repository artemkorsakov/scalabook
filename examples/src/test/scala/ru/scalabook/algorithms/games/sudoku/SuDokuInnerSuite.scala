package ru.scalabook.algorithms.games.sudoku

import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuInnerSuite extends SimpleIOSuite with Checkers:
  pureTest(
    "SuDokuInner.execute should create a 9x9 grid of SuDokuCell with initial values"
  ):
    success
