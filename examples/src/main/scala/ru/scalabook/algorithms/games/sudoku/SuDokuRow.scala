package ru.scalabook.algorithms.games.sudoku

import cats.Show

enum SuDokuRow:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth

object SuDokuRow:
  given Show[SuDokuRow] = Show.show[SuDokuRow]: (row: SuDokuRow) =>
    (row.ordinal + 1).toString
    
  def next(row: SuDokuRow): Option[SuDokuRow] =
    val next   = row.ordinal + 1
    val values = SuDokuRow.values
    if next >= values.length then None else Some(values(next))
