package ru.scalabook.algorithms.games.sudoku

final case class SuDokuOld(cells: Array[Array[Array[Int]]]):

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def result: Option[Array[Array[Int]]] =
    var hasToRemove = true
    var isValid     = true
    while isValid && isNotAnswer && hasToRemove
    do
      hasToRemove = false
      cells.indices.foreach(i =>
        cells(i).indices
          .withFilter(j => cells(i)(j).length != 1)
          .foreach(j =>
            cells(i)(j).withFilter(!setValueWithoutChanges(i, j, _)).foreach {
              value =>
                hasToRemove = true
                if !removeValue(i, j, value) then isValid = false
            }
          )
      )

    if isValid && !isNotAnswer then Some(cells.map(_.map(_.head)))
    else None
  end result

  private def isNotAnswer: Boolean =
    cells.exists(_.exists(_.length != 1))

  /** Set the specified value in the specified field.
    */
  private def setValue(i: Int, j: Int, value: Int): Boolean =
    val tempSuDoku = copySuDoku
    tempSuDoku.cells(i)(j) = Array(value)
    val result = tempSuDoku.removeValueFromRowColSqr(
      i,
      j,
      value
    ) && tempSuDoku.isSuDokuMatrixValid
    if result then
      tempSuDoku.cells.indices.foreach(i => cells(i) = tempSuDoku.cells(i))
    result

  /** Set the specified value in the specified field but then return to the
    * previous state of the cells regardless of the result.
    */
  private def setValueWithoutChanges(i: Int, j: Int, value: Int): Boolean =
    val tempSuDoku = copySuDoku
    tempSuDoku.cells(i)(j) = Array(value)
    tempSuDoku.removeValueFromRowColSqr(
      i,
      j,
      value
    ) && tempSuDoku.isSuDokuMatrixValid

  /** Check that each row contains digits from 1 to 9 at least once. Check that
    * each column contains digits from 1 to 9 at least once.
    */
  private def isSuDokuMatrixValid: Boolean =
    !(1 to 9).exists(n => cells.exists(_.forall(c => !c.contains(n)))) &&
      !(1 to 9).exists(n =>
        cells.head.indices.exists(j =>
          cells.map(_(j)).forall(c => !c.contains(n))
        )
      )

  /** Removes a given value from the row, column, and square to which the point
    * belongs.
    */
  private def removeValueFromRowColSqr(i: Int, j: Int, value: Int): Boolean =
    (0 to 8).forall(k =>
      k == j || removeValue(i, k, value)
    ) && // Removes a given value from the row
      (0 to 8).forall(k =>
        k == i || removeValue(k, j, value)
      ) &&                 // Removes a given value from the column
      (0 to 2).forall(k => // Removes a given value from the square
        (0 to 2).forall { l =>
          val r = (i / 3) * 3 + k
          val c = (j / 3) * 3 + l
          (r == i && c == j) || removeValue(r, c, value)
        }
      )

  /** Removes a given value from the cell (i, j). */
  private def removeValue(i: Int, j: Int, value: Int): Boolean =
    if cells(i)(j).length == 1 then cells(i)(j).head != value
    else
      cells(i)(j) = cells(i)(j).filter(_ != value)
      cells(i)(j).length != 1 || setValue(i, j, cells(i)(j).head)

  private def copySuDoku: SuDokuOld =
    SuDokuOld(cells.map(_.map(_.map(identity))))

end SuDokuOld

object SuDokuOld:
  def apply(): SuDokuOld =
    val defaultCells = (0 to 8)
      .map(_ => (0 to 8).map(_ => Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).toArray)
      .toArray
    SuDokuOld(defaultCells)

  def toSuDoku(source: String): Option[SuDokuOld] =
    val sudoku = SuDokuOld()
    val rows   = source.trim.split("\n")
    val isCorrect = (0 to 8).forall(i =>
      (0 to 8).forall { j =>
        val d = rows(i).charAt(j).getNumericValue
        d == 0 || sudoku.setValue(i, j, d)
      }
    )
    if isCorrect then Some(sudoku) else None

end SuDokuOld

/*
final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  self =>

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
end SuDoku

object SuDoku:
  def apply(): SuDoku =
    val defaultCells =
      (0 to 8).toVector.map(_ => (0 to 8).toVector.map(_ => SuDokuCell.initial))

    SuDoku(defaultCells)

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
