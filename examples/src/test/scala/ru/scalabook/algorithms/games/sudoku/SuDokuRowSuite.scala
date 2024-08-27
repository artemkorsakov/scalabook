package ru.scalabook.algorithms.games.sudoku

import weaver.SimpleIOSuite
import SuDokuRow.next

object SuDokuRowSuite extends SimpleIOSuite:

  pureTest("SuDokuRow.next should return the next row"):
    expect(next(SuDokuRow.First).contains(SuDokuRow.Second)) and
      expect(next(SuDokuRow.Second).contains(SuDokuRow.Third)) and
      expect(next(SuDokuRow.Third).contains(SuDokuRow.Fourth)) and
      expect(next(SuDokuRow.Fourth).contains(SuDokuRow.Fifth)) and
      expect(next(SuDokuRow.Fifth).contains(SuDokuRow.Sixth)) and
      expect(next(SuDokuRow.Sixth).contains(SuDokuRow.Seventh)) and
      expect(next(SuDokuRow.Seventh).contains(SuDokuRow.Eighth)) and
      expect(next(SuDokuRow.Eighth).contains(SuDokuRow.Ninth))

  pureTest("SuDokuRow.next should return None for the last row"):
    expect(next(SuDokuRow.Ninth).isEmpty)
