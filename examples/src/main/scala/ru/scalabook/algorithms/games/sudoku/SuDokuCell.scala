package ru.scalabook.algorithms.games.sudoku

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  def apply(cellValue: SuDokuCellValue): SuDokuCell = SuDokuCell(Set(cellValue))
