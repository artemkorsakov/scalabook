package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuColumn.next
import weaver.SimpleIOSuite

object SuDokuColumnSuite extends SimpleIOSuite:

  pureTest("SuDokuColumn.next should return the next column"):
    expect(next(SuDokuColumn.First).contains(SuDokuColumn.Second)) and
      expect(next(SuDokuColumn.Second).contains(SuDokuColumn.Third)) and
      expect(next(SuDokuColumn.Third).contains(SuDokuColumn.Fourth)) and
      expect(next(SuDokuColumn.Fourth).contains(SuDokuColumn.Fifth)) and
      expect(next(SuDokuColumn.Fifth).contains(SuDokuColumn.Sixth)) and
      expect(next(SuDokuColumn.Sixth).contains(SuDokuColumn.Seventh)) and
      expect(next(SuDokuColumn.Seventh).contains(SuDokuColumn.Eighth)) and
      expect(next(SuDokuColumn.Eighth).contains(SuDokuColumn.Ninth))

  pureTest("SuDokuColumn.next should return None for the last column"):
    expect(next(SuDokuColumn.Ninth).isEmpty)
