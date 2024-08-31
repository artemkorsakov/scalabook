package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import ru.scalabook.fp.state.State

import scala.annotation.tailrec
import scala.util.Try

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
  def crossOutLoop(
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
        if currentValues.size == 1 then
          // Точное значение определено, переходим к следующей ячейке
          crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
        else
          // Ищем значение в ячейке, которое можно вычеркнуть
          val invalidValues = currentValues.filter: value =>
            !setValue(row, col, value).run(sudoku)._1
          if invalidValues.isEmpty then
            // Таких значений нет, переходим к следующей ячейке
            crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
          else
            val input: Seq[SuDokuInnerInput] =
              invalidValues.map: invalidValue =>
                (SuDokuInnerOperation.RemoveFromCell, row, col, invalidValue)
              .toSeq
            val (result, newSudoku) = SuDokuInner.execute(input*).run(sudoku)
            // Если результат вычеркивания успешен,
            // то ищем в заданной ячейке следующий кандидат на вычеркивание
            if result then
              crossOutLoop(newSudoku, Some(row), SuDokuColumn.next(col))
            // Если не успешен, значит решения головоломки не существует
            else (false, sudoku)
        end if
    end match
  end crossOutLoop

  /** Находим такое значение в ячейке, которое не встречается в других ячейках
    * блока, но при этом это значение не единственное в своей ячейке.
    *
    * Такие значения можно установить единственными в своих ячейках, тем самым
    * уменьшив возможные варианты для других значений.
    *
    * В каждой ячейке возможно максимум одно такое значение, иначе Су Доку
    * составлено некорректно.
    */
  def getSingleValues(
      sudoku: SuDoku,
      block: SuDokuBlock
  ): List[(SuDokuRow, SuDokuColumn, SuDokuCellValue)] =
    val blockCells = sudoku.cells.filter { case ((_, _, blk), cell) =>
      blk == block
    }
    def notInOtherCells(index: SuDokuIndex, value: SuDokuCellValue): Boolean =
      blockCells.forall { (otherIndex, otherCell) =>
        otherIndex == index || !otherCell.possibleValues.contains(value)
      }
    blockCells.foldLeft(List.empty[SuDokuInput]) { case (acc, (index, cell)) =>
      if cell.maybeValue.isDefined then acc
      else
        cell
          .possibleValues
          .collectFirst:
            case value if notInOtherCells(index, value) =>
              (index._1, index._2, value) :: acc
          .getOrElse(acc)
    }
  end getSingleValues

  /** Устанавливает значения-одичночки. */
  @tailrec
  def blockSingleValueLoop(
      sudoku: SuDoku,
      blocks: List[SuDokuBlock]
  ): SuDokuOutput =
    blocks match
      case Nil => (true, sudoku) // Обошли все блоки
      case head :: tail =>
        val singleValues = getSingleValues(sudoku, head)
        if singleValues.isEmpty then
          // Если значений-одиночек нет, переходим к следующим блокам
          blockSingleValueLoop(sudoku, tail)
        else
          // Если есть, устанавливаем такие значения
          val (result, sudoku2) = setValues(singleValues*).run(sudoku)
          // Если установка закончилась успешно, переходим к следующему блоку
          if result then blockSingleValueLoop(sudoku2, tail)
          else (false, sudoku)

  private val blockSingleValueState = State[SuDoku, Boolean](
    blockSingleValueLoop(_, SuDokuBlock.values.toList)
  )
  private val crossOutState = State[SuDoku, Boolean](
    crossOutLoop(_, Some(SuDokuRow.First), Some(SuDokuColumn.First))
  )
  private val loopState: State[SuDoku, Boolean] =
    State[SuDoku, Boolean] { sudoku =>
      val (res, newSudoku) = blockSingleValueState.run(sudoku)
      if res then crossOutState.run(newSudoku)
      else (false, sudoku)
    }

  @tailrec
  private def solutionLoop(sudoku: SuDoku): SuDokuOutput =
    val (result, newSudoku) = loopState.run(sudoku)
    if !result then (false, sudoku)
    else if sudoku != newSudoku then solutionLoop(newSudoku)
    else (result, sudoku)

  /** Головоломка решается до тех пор, пока:
    *
    *   - не найдено решение
    *   - если есть значения, которые можно вычеркнуть
    *   - если есть значения, которые можно вставить только в одну ячейку
    *   - если есть значения, которые можно вычеркнуть из строки или столбца
    *     других блоков
    */
  val solutionState: SuDokuState = State[SuDoku, Boolean](solutionLoop)

  def toSuDoku(source: String): SuDokuOutput =
    val rows = source.trim.split("\n")

    def getValue(row: SuDokuRow, col: SuDokuColumn): Option[SuDokuCellValue] =
      val i = row.ordinal
      val j = col.ordinal
      if 0 <= i && i < rows.length && 0 <= j && j < rows(i).length then
        val value = rows(i).charAt(j).getNumericValue
        if 1 <= value && value <= 9 then
          Try(SuDokuCellValue.fromOrdinal(value - 1)).toOption
        else None
      else None

    val inputs: Vector[SuDokuInput] =
      for
        row   <- SuDokuRow.values.toVector
        col   <- SuDokuColumn.values.toVector
        value <- getValue(row, col)
      yield (row, col, value)

    setValues(inputs*).run(SuDoku.initial)

  end toSuDoku
end SuDokuSolver
