package ru.scalabook.algorithms.games.sudoku

import weaver.SimpleIOSuite

object SuDokuCellValueSuite extends SimpleIOSuite:
  pureTest("SuDokuCellValue should have correct integer values"):
    expect.all(
      SuDokuCellValue.fromOrdinal(1 - 1) == SuDokuCellValue.One,
      SuDokuCellValue.fromOrdinal(2 - 1) == SuDokuCellValue.Two,
      SuDokuCellValue.fromOrdinal(3 - 1) == SuDokuCellValue.Three,
      SuDokuCellValue.fromOrdinal(4 - 1) == SuDokuCellValue.Four,
      SuDokuCellValue.fromOrdinal(5 - 1) == SuDokuCellValue.Five,
      SuDokuCellValue.fromOrdinal(6 - 1) == SuDokuCellValue.Six,
      SuDokuCellValue.fromOrdinal(7 - 1) == SuDokuCellValue.Seven,
      SuDokuCellValue.fromOrdinal(8 - 1) == SuDokuCellValue.Eight,
      SuDokuCellValue.fromOrdinal(9 - 1) == SuDokuCellValue.Nine
    )
