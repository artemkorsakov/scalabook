package ru.scalabook.algorithms.games.sudoku

import cats.Show
import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDoku.*

final case class SuDoku(cells: Map[SuDokuIndex, SuDokuCell]):
  def getCell(row: SuDokuRow, column: SuDokuColumn): Option[SuDokuCell] =
    val block = SuDokuBlock.getBlock(row, column)
    getCell((row, column, block))

  def getCell(index: SuDokuIndex): Option[SuDokuCell] = cells.get(index)

  val isCorrect: Boolean =
    cells.groupBy(_._1._1).forall((_, c) => hasUniqueValues(c.values)) &&
      cells.groupBy(_._1._2).forall((_, c) => hasUniqueValues(c.values)) &&
      cells.groupBy(_._1._3).forall((_, c) => hasUniqueValues(c.values))

  val hasSolution: Boolean =
    isCorrect && cells.forall(_._2.maybeValue.isDefined)

object SuDoku:
  given Show[SuDoku] = Show.show[SuDoku]: (sudoku: SuDoku) =>
    SuDokuRow.values.map: row =>
      SuDokuColumn.values.map: col =>
        val block              = SuDokuBlock.getBlock(row, col)
        val index: SuDokuIndex = (row, col, block)
        sudoku.getCell(row, col).map(_.show).getOrElse("()").padTo(20, ' ')
      .mkString("|", "|", "|")
    .mkString("", "\n", "")

  val initial: SuDoku =
    val defaultCells =
      for
        row <- SuDokuRow.values
        col <- SuDokuColumn.values
      yield
        val block = SuDokuBlock.getBlock(row, col)
        (row, col, block) -> SuDokuCell.initial

    SuDoku(defaultCells.toMap)

  private def hasUniqueValues(cells: Iterable[SuDokuCell]): Boolean =
    val values = cells.flatMap(_.maybeValue)
    values.toSet.size == values.size
end SuDoku
