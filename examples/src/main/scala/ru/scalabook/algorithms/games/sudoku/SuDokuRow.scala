package ru.scalabook.algorithms.games.sudoku

import cats.Show

enum SuDokuRow:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth

object SuDokuRow:
  given Show[SuDokuRow] = Show.show[SuDokuRow]: (row: SuDokuRow) =>
    (row.ordinal + 1).toString
