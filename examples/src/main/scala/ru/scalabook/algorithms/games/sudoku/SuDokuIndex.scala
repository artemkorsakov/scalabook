package ru.scalabook.algorithms.games.sudoku

import cats.Show
import cats.syntax.show.*

type SuDokuIndex = (SuDokuRow, SuDokuColumn, SuDokuBlock)

object SuDokuIndex:
  given Show[SuDokuIndex] = Show.show[SuDokuIndex]: (index: SuDokuIndex) =>
    s"(${index._1.show}${index._2.show})"

  def apply(row: SuDokuRow, column: SuDokuColumn): SuDokuIndex =
    (row, column, SuDokuBlock.getBlock(row, column))
