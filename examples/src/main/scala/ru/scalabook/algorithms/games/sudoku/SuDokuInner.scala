package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuCell.SuDokuCellOperation
import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.SuDokuInnerOperation.*
import ru.scalabook.algorithms.games.sudoku.SuDokuSolver.{
  SuDokuOutput,
  SuDokuState
}
import ru.scalabook.fp.state.State

object SuDokuInner:
  enum SuDokuInnerOperation:
    case SetToCell, RemoveFromCell

  type SuDokuInnerInput = (SuDokuInnerOperation, Row, Column, SuDokuCellValue)

  private val updateSuDoku: SuDokuInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        // Выполняем операцию только если поле, строка и столбец корректные,
        // а также если предыдущая операция завершилась успешно
        val isCorrect =
          sudoku.isCorrect && Row.isCorrect(i) && Column.isCorrect(j) && result

        if isCorrect then
          operation match
            case SetToCell =>
              updateCell(SuDokuCellOperation.Set, sudoku, i, j, value)
            case RemoveFromCell =>
              updateCell(SuDokuCellOperation.Remove, sudoku, i, j, value)
        else (false, sudoku)

  // Определяем текущую ячейку, выполняем операцию, в случае успеха обновляем ячейку
  private def updateCell(
      oper: SuDokuCellOperation,
      sudoku: SuDoku,
      i: Int,
      j: Int,
      value: SuDokuCellValue
  ): (Boolean, SuDoku) =
    val oldCell = sudoku.cells(i)(j)
    val (result, newCell) =
      SuDokuCell.execute((oper, value)).run(oldCell)
    if result then
      val newCells =
        sudoku.cells.updated(i, sudoku.cells(i).updated(j, newCell))
      (true, SuDoku(newCells))
    else (false, sudoku)

  def execute(inputs: SuDokuInnerInput*): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs.toList, updateSuDoku)

end SuDokuInner
