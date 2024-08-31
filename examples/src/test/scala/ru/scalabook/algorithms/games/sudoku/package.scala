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

  given Ordering[(SuDokuRow, SuDokuColumn, SuDokuCellValue)] =
    (
        a: (SuDokuRow, SuDokuColumn, SuDokuCellValue),
        b: (SuDokuRow, SuDokuColumn, SuDokuCellValue)
    ) =>
      val ac = a._1.ordinal.compareTo(b._1.ordinal)
      val bc = a._2.ordinal.compareTo(b._2.ordinal)
      val cc = ac.compareTo(bc)
      if ac != 0 then ac
      else if bc != 0 then bc
      else cc

  given Ordering[(SuDokuRow, SuDokuColumn, SuDokuBlock)] =
    (
        a: (SuDokuRow, SuDokuColumn, SuDokuBlock),
        b: (SuDokuRow, SuDokuColumn, SuDokuBlock)
    ) =>
      val ac = a._1.ordinal.compareTo(b._1.ordinal)
      val bc = a._2.ordinal.compareTo(b._2.ordinal)
      val cc = ac.compareTo(bc)
      if ac != 0 then ac
      else if bc != 0 then bc
      else cc
end sudoku
