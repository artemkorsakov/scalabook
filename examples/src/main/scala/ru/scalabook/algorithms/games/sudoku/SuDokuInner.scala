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

  type SuDokuInnerInput =
    (SuDokuInnerOperation, SuDokuRow, SuDokuColumn, SuDokuCellValue)

  private val updateSuDoku: SuDokuInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        if result && sudoku.isCorrect then
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
      i: SuDokuRow,
      j: SuDokuColumn,
      value: SuDokuCellValue
  ): (Boolean, SuDoku) =
    sudoku.getCell(i, j) match
      case None => (false, sudoku)
      case Some(oldCell) =>
        val (result, newCell) = SuDokuCell.execute((oper, value)).run(oldCell)
        if result then
          val index    = SuDokuIndex(i, j)
          val newCells = sudoku.cells.updated(index, newCell)
          (true, SuDoku(newCells))
        else (false, sudoku)

  def execute(inputs: SuDokuInnerInput*): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs.toList, updateSuDoku)

end SuDokuInner
