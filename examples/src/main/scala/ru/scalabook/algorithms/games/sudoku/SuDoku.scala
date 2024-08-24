package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*

final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  val isCorrect: Boolean =
    cells.length == 9 &&
      cells.forall(_.length == 9) &&
      cells.forall(_.forall(_.possibleValues.nonEmpty)) &&
      cells.forall(row => hasUniqueValues(row)) &&
      (0 to 8).forall(j => hasUniqueValues(cells.map(row => row(j)))) && {
        val blocks =
          for
            i <- 0 to 6 by 3
            j <- 0 to 6 by 3
          yield getBlock(i, j)
        blocks.forall: block =>
          val blockCells = block.map((i, j) => cells(i.toInt)(j.toInt))
          hasUniqueValues(blockCells)
      }

  val hasSolution: Boolean =
    isCorrect && cells.forall(_.forall(_.maybeValue.isDefined))

object SuDoku:
  val initial: SuDoku =
    val defaultCells =
      (0 to 8)
        .toVector
        .map: _ =>
          (0 to 8).toVector.map: _ =>
            SuDokuCell.initial

    SuDoku(defaultCells)

  private def hasUniqueValues(cells: IndexedSeq[SuDokuCell]): Boolean =
    val values = cells.flatMap(_.maybeValue)
    values.distinct.size == values.size

  type Row = Int
  object Row:
    def isCorrect(row: Int): Boolean = 0 <= row && row < 9
  type Column = Int
  object Column:
    def isCorrect(col: Int): Boolean = 0 <= col && col < 9

  def getBlock(i: Row, j: Column): IndexedSeq[(Row, Column)] =
    if Row.isCorrect(i) && Column.isCorrect(j) then
      for
        k <- 0 to 2
        l <- 0 to 2
      yield
        val r = (i / 3) * 3 + k
        val c = (j / 3) * 3 + l
        (r, c)
    else IndexedSeq.empty
end SuDoku
