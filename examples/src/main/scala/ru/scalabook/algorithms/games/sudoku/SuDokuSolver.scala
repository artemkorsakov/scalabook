package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import ru.scalabook.fp.state.State

import scala.annotation.tailrec

object SuDokuSolver:
  type SuDokuState  = State[SuDoku, Boolean]
  type SuDokuOutput = (Boolean, SuDoku)

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
          (result, _) <- State.get[SuDokuOutput]
        yield result

      val (_, output) = state.run((true, sudoku))
      output
    }

  type SuDokuInput =
    (SuDokuRow, SuDokuColumn, SuDokuCellValue)

  private val updateSuDoku: SuDokuInput => SuDokuOutput => SuDokuOutput =
    (i, j, value) =>
      (result, sudoku) =>
        val inputSet: SuDokuInnerInput =
          (SuDokuInnerOperation.SetToCell, i, j, value)
        val (resultSet, sudokuSet) =
          SuDokuInner.execute(inputSet).run(sudoku)

        val inputsRemove: List[SuDokuComplexInnerInput] =
          List(
            (SuDokuInnerComplexOperation.RemoveFromRow, i, j, value),
            (SuDokuInnerComplexOperation.RemoveFromColumn, i, j, value),
            (SuDokuInnerComplexOperation.RemoveFromBlock, i, j, value)
          )
        val (resultRemove, sudokuRemove) =
          SuDokuComplexInner.execute(inputsRemove).run(sudokuSet)

        if result && resultSet && resultRemove then (true, sudokuRemove)
        else (false, sudoku)

  def setValue(
      i: SuDokuRow,
      j: SuDokuColumn,
      value: SuDokuCellValue
  ): SuDokuState =
    setValues((i, j, value))

  def setValues(inputs: SuDokuInput*): SuDokuState =
    executeInnerOperation(inputs.toList, updateSuDoku)

  /** Поиск значений, которые можно вычеркнуть:
    *
    *   - проходимся по каждой строчке и по каждому столбцу
    *   - в каждой ячейке ищем среди списка возможных такие значения, которые
    *     при вставке в заданное поле приводят к ошибке - это означает, что в
    *     данную ячейку такое значение вставлять нельзя и его можно вычеркнуть
    *   - вычеркиваем значение
    *   - пытаемся найти следующий кандидат на вычеркивание в той же ячейке
    *   - если в ячейке не осталось значений, которые можно вычеркнуть, то
    *     переходим к следующей ячейке
    */
  @tailrec
  private def crossOutLoop(
      sudoku: SuDoku,
      maybeRow: Option[SuDokuRow],
      maybeColumn: Option[SuDokuColumn]
  ): SuDokuOutput =
    val maybeCell =
      for
        row  <- maybeRow
        col  <- maybeColumn
        cell <- sudoku.getCell(row, col)
      yield cell
    (maybeRow, maybeColumn, maybeCell) match
      case (None, _, _) => // Дошли до конца поля
        (true, sudoku)
      case (Some(row), None, _) => // Дошли до конца строки
        crossOutLoop(sudoku, SuDokuRow.next(row), Some(SuDokuColumn.First))
      case (Some(row), Some(col), None) => // Некорректная ячейка
        (false, sudoku)
      case (Some(row), Some(col), Some(sudokuCell)) =>
        val currentValues = sudokuCell.possibleValues
        // Точное значение определено, переходим к следующей ячейке
        if currentValues.size == 1 then
          crossOutLoop(sudoku, maybeRow, SuDokuColumn.next(col))
        else // Ищем значение в ячейке, которое можно вычеркнуть
          val maybeInvalidValue = currentValues.find: value =>
            !setValue(row, col, value).run(sudoku)._1
          maybeInvalidValue match
            // Такого значения нет, переходим к следующей ячейке
            case None => crossOutLoop(sudoku, maybeRow, SuDokuColumn.next(col))
            case Some(invalidValue) =>
              val input: SuDokuInnerInput =
                (SuDokuInnerOperation.RemoveFromCell, row, col, invalidValue)
              val (result, newSudoku) =
                SuDokuInner.execute(input).run(sudoku)
              // Если результат вычеркивания успешен,
              // то ищем в заданной ячейке следующий кандидат на вычеркивание
              if result then crossOutLoop(sudoku, maybeRow, maybeColumn)
              // Если не успешен, значит решения головоломки не существует
              else (false, sudoku)
    end match
  end crossOutLoop

//  /** Находим такое значение в ячейке, которое не встречается в других ячейках
//    * блока, но при этом это значение не единственное в своей ячейке.
//    *
//    * Такие значения можно установить единственными в своих ячейках, тем самым
//    * уменьшив возможные варианты для других значений.
//    *
//    * В каждой ячейке возможно максимум одно такое значение, иначе Су Доку
//    * составлено некорректно.
//    */
//  def getSingleValues(
//      sudoku: SuDoku,
//      block: IndexedSeq[(Row, Column)]
//  ): List[(Row, Column, SuDokuCellValue)] =
//    block.foldLeft(List.empty[SuDokuInput]) {
//      case (acc, (i, j)) =>
//        val cell = sudoku.cells(i)(j)
//        if cell.maybeValue.isDefined then acc
//        else
//          val values = cell.possibleValues
//          values
//            .find: value =>
//              block.forall: (k, l) =>
//                (k == i && l == j) ||
//                  !sudoku.cells(k)(l).possibleValues.contains(value)
//            .map: value =>
//              (i, j, value) :: acc
//            .getOrElse(acc)
//    }
//
//  /** Устанавливает значения-одичночки. */
//  @tailrec
//  def blockSingleValueLoop(
//      sudoku: SuDoku,
//      i: Int,
//      j: Int
//  ): SuDokuOutput =
//    // Дошли до конца поля
//    if i >= 9 then (true, sudoku)
//    // Дошли до конца столбца, переходим к следующему блоку
//    else if j >= 9 then blockSingleValueLoop(sudoku, i + 3, 0)
//    else
//      val block        = getBlock(i, j)
//      val singleValues = getSingleValues(sudoku, block)
//      if singleValues.isEmpty then
//        // Если значений-одиночек нет, переходим к следующему блоку
//        blockSingleValueLoop(sudoku, i, j + 3)
//      else
//        // Если есть устанавливаем такие значения
//        val (result, sudoku2) = setValues(singleValues*).run(sudoku)
//        // Если установка закончилась успешно, переходим к следующему блоку
//        if result then blockSingleValueLoop(sudoku2, i, j + 3)
//        else (false, sudoku)
//
//  private val crossOutState = State[SuDoku, Boolean](crossOutLoop(_, 0, 0))
//  private val blockSingleValueState =
//    State[SuDoku, Boolean](blockSingleValueLoop(_, 0, 0))
//  private val loopState =
//    crossOutState.map2(blockSingleValueState)(_ && _)
//
//  @tailrec
//  private def solutionLoop(sudoku: SuDoku): SuDokuOutput =
//    val (result, newSudoku) = loopState.run(sudoku)
//    if !result then (false, sudoku)
//    else if sudoku != newSudoku then solutionLoop(newSudoku)
//    else (result, sudoku)
//
//  /** Головоломка решается до тех пор, пока:
//    *
//    *   - не найдено решение
//    *   - если есть значения, которые можно вычеркнуть
//    *   - если есть значения, которые можно вставить только в одну ячейку
//    */
//  val solutionState: SuDokuState = State[SuDoku, Boolean](solutionLoop)
//
//  def toSuDoku(source: String): SuDokuOutput =
//    val rows = source.trim.split("\n")
//
//    def getValue(i: Int, j: Int): Option[SuDokuCellValue] =
//      if 0 <= i && i < rows.length && 0 <= j && j < rows(i).length then
//        val value = rows(i).charAt(j).getNumericValue
//        if 1 <= value && value <= 9 then
//          Try(SuDokuCellValue.fromOrdinal(value - 1)).toOption
//        else None
//      else None
//
//    val inputs: IndexedSeq[SuDokuInput] =
//      for
//        i     <- 0 to 8
//        j     <- 0 to 8
//        value <- getValue(i, j)
//      yield (i, j, value)
//
//    setValues(inputs*).run(SuDoku.initial)
//
//  end toSuDoku
end SuDokuSolver
