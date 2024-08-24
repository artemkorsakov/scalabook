package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDokuBlock.*
import weaver.SimpleIOSuite

object SuDokuBlockSuite extends SimpleIOSuite:
  pureTest(
    "getBlock should return the correct block for each row and column combination"
  ):
    forEach(
      List(
        (SuDokuRow.First, SuDokuColumn.First, SuDokuBlock.First),
        (SuDokuRow.First, SuDokuColumn.Second, SuDokuBlock.First),
        (SuDokuRow.First, SuDokuColumn.Third, SuDokuBlock.First),
        (SuDokuRow.First, SuDokuColumn.Fourth, SuDokuBlock.Second),
        (SuDokuRow.First, SuDokuColumn.Fifth, SuDokuBlock.Second),
        (SuDokuRow.First, SuDokuColumn.Sixth, SuDokuBlock.Second),
        (SuDokuRow.First, SuDokuColumn.Seventh, SuDokuBlock.Third),
        (SuDokuRow.First, SuDokuColumn.Eighth, SuDokuBlock.Third),
        (SuDokuRow.First, SuDokuColumn.Ninth, SuDokuBlock.Third),
        (SuDokuRow.Second, SuDokuColumn.First, SuDokuBlock.First),
        (SuDokuRow.Second, SuDokuColumn.Second, SuDokuBlock.First),
        (SuDokuRow.Second, SuDokuColumn.Third, SuDokuBlock.First),
        (SuDokuRow.Second, SuDokuColumn.Fourth, SuDokuBlock.Second),
        (SuDokuRow.Second, SuDokuColumn.Fifth, SuDokuBlock.Second),
        (SuDokuRow.Second, SuDokuColumn.Sixth, SuDokuBlock.Second),
        (SuDokuRow.Second, SuDokuColumn.Seventh, SuDokuBlock.Third),
        (SuDokuRow.Second, SuDokuColumn.Eighth, SuDokuBlock.Third),
        (SuDokuRow.Second, SuDokuColumn.Ninth, SuDokuBlock.Third),
        (SuDokuRow.Third, SuDokuColumn.First, SuDokuBlock.First),
        (SuDokuRow.Third, SuDokuColumn.Second, SuDokuBlock.First),
        (SuDokuRow.Third, SuDokuColumn.Third, SuDokuBlock.First),
        (SuDokuRow.Third, SuDokuColumn.Fourth, SuDokuBlock.Second),
        (SuDokuRow.Third, SuDokuColumn.Fifth, SuDokuBlock.Second),
        (SuDokuRow.Third, SuDokuColumn.Sixth, SuDokuBlock.Second),
        (SuDokuRow.Third, SuDokuColumn.Seventh, SuDokuBlock.Third),
        (SuDokuRow.Third, SuDokuColumn.Eighth, SuDokuBlock.Third),
        (SuDokuRow.Third, SuDokuColumn.Ninth, SuDokuBlock.Third),
        (SuDokuRow.Fourth, SuDokuColumn.First, SuDokuBlock.Fourth),
        (SuDokuRow.Fourth, SuDokuColumn.Second, SuDokuBlock.Fourth),
        (SuDokuRow.Fourth, SuDokuColumn.Third, SuDokuBlock.Fourth),
        (SuDokuRow.Fourth, SuDokuColumn.Fourth, SuDokuBlock.Fifth),
        (SuDokuRow.Fourth, SuDokuColumn.Fifth, SuDokuBlock.Fifth),
        (SuDokuRow.Fourth, SuDokuColumn.Sixth, SuDokuBlock.Fifth),
        (SuDokuRow.Fourth, SuDokuColumn.Seventh, SuDokuBlock.Sixth),
        (SuDokuRow.Fourth, SuDokuColumn.Eighth, SuDokuBlock.Sixth),
        (SuDokuRow.Fourth, SuDokuColumn.Ninth, SuDokuBlock.Sixth),
        (SuDokuRow.Fifth, SuDokuColumn.First, SuDokuBlock.Fourth),
        (SuDokuRow.Fifth, SuDokuColumn.Second, SuDokuBlock.Fourth),
        (SuDokuRow.Fifth, SuDokuColumn.Third, SuDokuBlock.Fourth),
        (SuDokuRow.Fifth, SuDokuColumn.Fourth, SuDokuBlock.Fifth),
        (SuDokuRow.Fifth, SuDokuColumn.Fifth, SuDokuBlock.Fifth),
        (SuDokuRow.Fifth, SuDokuColumn.Sixth, SuDokuBlock.Fifth),
        (SuDokuRow.Fifth, SuDokuColumn.Seventh, SuDokuBlock.Sixth),
        (SuDokuRow.Fifth, SuDokuColumn.Eighth, SuDokuBlock.Sixth),
        (SuDokuRow.Fifth, SuDokuColumn.Ninth, SuDokuBlock.Sixth),
        (SuDokuRow.Sixth, SuDokuColumn.First, SuDokuBlock.Fourth),
        (SuDokuRow.Sixth, SuDokuColumn.Second, SuDokuBlock.Fourth),
        (SuDokuRow.Sixth, SuDokuColumn.Third, SuDokuBlock.Fourth),
        (SuDokuRow.Sixth, SuDokuColumn.Fourth, SuDokuBlock.Fifth),
        (SuDokuRow.Sixth, SuDokuColumn.Fifth, SuDokuBlock.Fifth),
        (SuDokuRow.Sixth, SuDokuColumn.Sixth, SuDokuBlock.Fifth),
        (SuDokuRow.Sixth, SuDokuColumn.Seventh, SuDokuBlock.Sixth),
        (SuDokuRow.Sixth, SuDokuColumn.Eighth, SuDokuBlock.Sixth),
        (SuDokuRow.Sixth, SuDokuColumn.Ninth, SuDokuBlock.Sixth),
        (SuDokuRow.Seventh, SuDokuColumn.First, SuDokuBlock.Seventh),
        (SuDokuRow.Seventh, SuDokuColumn.Second, SuDokuBlock.Seventh),
        (SuDokuRow.Seventh, SuDokuColumn.Third, SuDokuBlock.Seventh),
        (SuDokuRow.Seventh, SuDokuColumn.Fourth, SuDokuBlock.Eighth),
        (SuDokuRow.Seventh, SuDokuColumn.Fifth, SuDokuBlock.Eighth),
        (SuDokuRow.Seventh, SuDokuColumn.Sixth, SuDokuBlock.Eighth),
        (SuDokuRow.Seventh, SuDokuColumn.Seventh, SuDokuBlock.Ninth),
        (SuDokuRow.Seventh, SuDokuColumn.Eighth, SuDokuBlock.Ninth),
        (SuDokuRow.Seventh, SuDokuColumn.Ninth, SuDokuBlock.Ninth),
        (SuDokuRow.Eighth, SuDokuColumn.First, SuDokuBlock.Seventh),
        (SuDokuRow.Eighth, SuDokuColumn.Second, SuDokuBlock.Seventh),
        (SuDokuRow.Eighth, SuDokuColumn.Third, SuDokuBlock.Seventh),
        (SuDokuRow.Eighth, SuDokuColumn.Fourth, SuDokuBlock.Eighth),
        (SuDokuRow.Eighth, SuDokuColumn.Fifth, SuDokuBlock.Eighth),
        (SuDokuRow.Eighth, SuDokuColumn.Sixth, SuDokuBlock.Eighth),
        (SuDokuRow.Eighth, SuDokuColumn.Seventh, SuDokuBlock.Ninth),
        (SuDokuRow.Eighth, SuDokuColumn.Eighth, SuDokuBlock.Ninth),
        (SuDokuRow.Eighth, SuDokuColumn.Ninth, SuDokuBlock.Ninth),
        (SuDokuRow.Ninth, SuDokuColumn.First, SuDokuBlock.Seventh),
        (SuDokuRow.Ninth, SuDokuColumn.Second, SuDokuBlock.Seventh),
        (SuDokuRow.Ninth, SuDokuColumn.Third, SuDokuBlock.Seventh),
        (SuDokuRow.Ninth, SuDokuColumn.Fourth, SuDokuBlock.Eighth),
        (SuDokuRow.Ninth, SuDokuColumn.Fifth, SuDokuBlock.Eighth),
        (SuDokuRow.Ninth, SuDokuColumn.Sixth, SuDokuBlock.Eighth),
        (SuDokuRow.Ninth, SuDokuColumn.Seventh, SuDokuBlock.Ninth),
        (SuDokuRow.Ninth, SuDokuColumn.Eighth, SuDokuBlock.Ninth),
        (SuDokuRow.Ninth, SuDokuColumn.Ninth, SuDokuBlock.Ninth)
      )
    ) { case (row, col, expectedBlock) =>
      expect(SuDokuBlock.getBlock(row, col) == expectedBlock)
    }

end SuDokuBlockSuite
