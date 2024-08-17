package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInnerComplex.*
import ru.scalabook.fp.state.State

final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  val isCorrect: Boolean =
    cells.length == 9 && cells.forall(_.length == 9)

object SuDoku:
  val initial: SuDoku =
    val defaultCells =
      (0 to 8)
        .toVector
        .map: _ =>
          (0 to 8).toVector.map: _ =>
            SuDokuCell.initial

    SuDoku(defaultCells)

  type SuDokuState  = State[SuDoku, Boolean]
  type SuDokuOutput = (Boolean, SuDoku)
  type Row          = Int
  object Row:
    def isCorrect(row: Int): Boolean = 0 <= row && row < 9
  type Column = Int
  object Column:
    def isCorrect(col: Int): Boolean = 0 <= col && col < 9

  def executeInnerOperation[A](
      inputs: List[A],
      update: A => SuDokuOutput => SuDokuOutput
  ): SuDokuState =
    State[SuDoku, Boolean] { sudoku =>
      val state =
        for
          _ <- State.traverse(inputs)(input =>
            State.modify[SuDokuOutput](update(input))
          )
          output <- State.get
        yield output._1

      state.run((true, sudoku))._2
    }
end SuDoku

object SuDokuInnerComplex:
  enum SuDokuInnerComplexOperation:
    case RemoveFromRow, RemoveFromColumn, RemoveFromBlock

  /*
    /** Удаление значения из заданной ячейки. */
  def removeValue(i: Int, j: Int, value: SuDokuCellValue): Option[SuDoku] =
    if doesCellExist(i, j) then
      cells(i)(j).removeValue(value).map: updatedCell =>
        SuDoku(
          cells.updated(i, cells(i).updated(j, updatedCell))
        )
    else None

  private def doesCellExist(i: Int, j: Int): Boolean =
    0 <= i && i < cells.length && 0 <= j && j < cells(i).length
   */
end SuDokuInnerComplex

/*
  /** Устанавливает заданное значение в ячейку. */
  def setValue(i: Int, j: Int, value: SuDokuCellValue): Option[SuDoku] = ???

  //  private def setValue(i: Int, j: Int, value: Int): Boolean =
  //    val tempSuDoku = copySuDoku
  //    tempSuDoku.cells(i)(j) = Array(value)
  //    val result =
  //      tempSuDoku.removeValueFromRowColSqr(i, j, value) &&
  //        tempSuDoku.isSuDokuMatrixValid
  //    if result then
  //      tempSuDoku.cells.indices.foreach(i => cells(i) = tempSuDoku.cells(i))
  //    result

  //  /** Check that each row contains digits from 1 to 9 at least once. Check that
  //    * each column contains digits from 1 to 9 at least once.
  //    */
  //  private def isSuDokuMatrixValid: Boolean =
  //    !(1 to 9).exists(n => cells.exists(_.forall(c => !c.contains(n)))) &&
  //      !(1 to 9).exists(n =>
  //        cells.head.indices.exists(j =>
  //          cells.map(_(j)).forall(c => !c.contains(n))
  //        )
  //      )

  /** Удаление значения из строки, столбца, а также - из клетки 3*3, которой
 * принадлежит ячейка, но только не из ячейки (i, j).
 */
  def removeValueFromRowColSqr(
      i: Int,
      j: Int,
      value: SuDokuCellValue
  ): Option[SuDoku] =
    // Удаление из строки i, за исключением ячейки (i,j)
    val suDokuAfterRemoveFromRow = (0 to 8).foldLeft(Option(self)):
      (maybeSuDoku, k) =>
        if k == j then maybeSuDoku
        else removeValue(i, k, value)

    // Удаление из столбца j, за исключением ячейки (i,j)
    val suDokuAfterRemoveFromCol = (0 to 8).foldLeft(suDokuAfterRemoveFromRow):
      (maybeSuDoku, k) =>
        if k == i then maybeSuDoku
        else removeValue(k, j, value)
    // Удаление из клетки 3*3, которой принадлежит ячейка, но только не из ячейки (i,j)
    (0 to 2).foldLeft(suDokuAfterRemoveFromCol): (maybeSuDoku, k) =>
      (0 to 2).foldLeft(maybeSuDoku): (maybeSuDoku, l) =>
        val r = (i / 3) * 3 + k
        val c = (j / 3) * 3 + l
        if r == i && c == k then maybeSuDoku
        else removeValue(r, c, value)
  end removeValueFromRowColSqr

  /** Удаление из строки i, за исключением ячейки (i,j). */
  def removeValueFromRow(
      i: Int,
      j: Int,
      value: SuDokuCellValue
  ): Option[SuDoku] =
    (0 to 8).foldLeft(Option(self)): (maybeSuDoku, k) =>
      if k == j then maybeSuDoku
      else removeValue(i, k, value)
end SuDoku

 */

/*
object SuDoku:
  def toSuDoku(source: String): Option[SuDoku] =
    val sudoku = SuDoku()
    val rows   = source.trim.split("\n")

    (0 to 8).foldLeft(Option(sudoku)) { case (maybeSudoku, i) =>
      (0 to 8).foldLeft(maybeSudoku) { case (maybeSudoku, j) =>
        getValue(rows, i, j) match
          case Some(value) => maybeSudoku.flatMap(_.setValue(i, j, value))
          case None        => None
      }
    }

  end toSuDoku

  private def getValue(
      rows: Array[String],
      i: Int,
      j: Int
  ): Option[SuDokuCellValue] =
    if 0 <= i && i < rows.length && 0 <= j && j < rows(i).length then None
    else
      val value = rows(i).charAt(j).getNumericValue
      if 1 <= value && value <= 9 then
        Try(SuDokuCellValue.fromOrdinal(value - 1)).toOption
      else None

end SuDoku

//  @SuppressWarnings(
//    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
//  )
//  def result: Option[Array[Array[Int]]] =
//    var hasToRemove = true
//    var isValid     = true
//    while isValid && isNotAnswer && hasToRemove
//    do
//      hasToRemove = false
//      cells.indices.foreach(i =>
//        cells(i).indices
//          .withFilter(j => cells(i)(j).length != 1)
//          .foreach(j =>
//            cells(i)(j).withFilter(!setValueWithoutChanges(i, j, _)).foreach {
//              value =>
//                hasToRemove = true
//                if !removeValue(i, j, value) then isValid = false
//            }
//          )
//      )
//
//    if isValid && !isNotAnswer then Some(cells.map(_.map(_.head)))
//    else None
//  end result

//  private def isNotAnswer: Boolean =
//    cells.exists(_.exists(_.length != 1))

//  /** Set the specified value in the specified field but then return to the
//    * previous state of the cells regardless of the result.
//    */
//  private def setValueWithoutChanges(i: Int, j: Int, value: Int): Boolean =
//    val tempSuDoku = copySuDoku
//    tempSuDoku.cells(i)(j) = Array(value)
//    tempSuDoku.removeValueFromRowColSqr(i, j, value) &&
//      tempSuDoku.isSuDokuMatrixValid
//

//@main def problem96(): Unit =
//  lazy val bigSudoku =
//    Source.fromResource("p096_sudoku.txt").getLines.toSeq.mkString("\n")
//  lazy val sources: Array[String] =
//    bigSudoku.split("Grid \\d{1,2}").filter(_.trim.nonEmpty)
//
//  lazy val sum =
//    sources
//      .flatMap(source =>
//        for
//          sudoku <- toSuDoku(source)
//          res    <- sudoku.result
//        yield res.head.take(3).mkString.toInt
//      )
//      .sum
//
//  require(sum == 24702)
 */
