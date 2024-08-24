package ru.scalabook.algorithms.games

import org.scalacheck.{Arbitrary, Gen}
import ru.scalabook.algorithms.games.sudoku.*

package object sudoku:
  private val suDokuCellValueGen: Gen[SuDokuCellValue] =
    Gen.oneOf(SuDokuCellValue.values.toSeq)
  given Arbitrary[SuDokuCellValue] = Arbitrary(suDokuCellValueGen)
  private val suDokuRowGen: Gen[SuDokuRow] =
    Gen.oneOf(SuDokuRow.values.toSeq)
  given Arbitrary[SuDokuRow] = Arbitrary(suDokuRowGen)
  private val suDokuColumnGen: Gen[SuDokuColumn] =
    Gen.oneOf(SuDokuColumn.values.toSeq)
  given Arbitrary[SuDokuColumn] = Arbitrary(suDokuColumnGen)
  private val suDokuBlockGen: Gen[SuDokuBlock] =
    Gen.oneOf(SuDokuBlock.values.toSeq)
  given Arbitrary[SuDokuBlock] = Arbitrary(suDokuBlockGen)
  private val suDokuIndexGen: Gen[SuDokuIndex] =
    for
      row    <- suDokuRowGen
      column <- suDokuColumnGen
    yield (row, column, SuDokuBlock.getBlock(row, column))
  given Arbitrary[SuDokuIndex] = Arbitrary(suDokuIndexGen)
end sudoku
