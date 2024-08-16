package ru.scalabook.algorithms.games

import cats.Show
import org.scalacheck.{Arbitrary, Gen}

package object sudoku:
  given Show[SuDokuCellValue] = Show.show[SuDokuCellValue]:
    (value: SuDokuCellValue) =>
      (value.ordinal + 1).toString
  private val suDokuCellValueGen: Gen[SuDokuCellValue] =
    Gen.oneOf(SuDokuCellValue.values.toSeq)
  given Arbitrary[SuDokuCellValue] = Arbitrary(suDokuCellValueGen)

  given Show[SuDokuCell] = Show.show[SuDokuCell]: (cell: SuDokuCell) =>
    cell.possibleValues.map(v => Show[SuDokuCellValue].show(v)).toList.sorted
      .mkString("(", ",", ")")
