package ru.scalabook.algorithms.games.sudoku

import cats.Show

enum SuDokuColumn:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth

object SuDokuColumn:
  given Show[SuDokuColumn] = Show.show[SuDokuColumn]: (column: SuDokuColumn) =>
    (column.ordinal + 1).toString

  def next(row: SuDokuColumn): Option[SuDokuColumn] =
    val next   = row.ordinal + 1
    val values = SuDokuColumn.values
    if next >= values.length then None else Some(values(next))
