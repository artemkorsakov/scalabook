package ru.scalabook.algorithms.games.sudoku

import cats.Show

enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine

object SuDokuCellValue:
  given Show[SuDokuCellValue] = Show.show[SuDokuCellValue]:
    (value: SuDokuCellValue) =>
      (value.ordinal + 1).toString
