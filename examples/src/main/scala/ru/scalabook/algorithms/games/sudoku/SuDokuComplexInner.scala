package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner
  .SuDokuInnerComplexOperation.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.{
  SuDokuInnerInput,
  SuDokuInnerOperation
}
import ru.scalabook.algorithms.games.sudoku.SuDokuSolver.*

object SuDokuComplexInner:
  enum SuDokuInnerComplexOperation:
    case RemoveFromRow, RemoveFromColumn, RemoveFromBlock,
      RemoveFromRowExceptBlock, RemoveFromColumnExceptBlock

  type SuDokuComplexInnerInput =
    (SuDokuInnerComplexOperation, SuDokuRow, SuDokuColumn, SuDokuCellValue)

  private val updateSuDoku
      : SuDokuComplexInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        (operation, result) match
          case (RemoveFromRow, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDokuColumn
                .values
                .withFilter(k => k != j)
                .map(k => (SuDokuInnerOperation.RemoveFromCell, i, k, value))
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromColumn, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDokuRow
                .values
                .withFilter(k => k != i)
                .map(k => (SuDokuInnerOperation.RemoveFromCell, k, j, value))
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromBlock, true) =>
            val block = SuDokuBlock.getBlock(i, j)
            val inputs: List[SuDokuInnerInput] =
              sudoku
                .cells
                .keys
                .withFilter: (r, c, b) =>
                  b == block && (r != i || c != j)
                .map: (r, c, _) =>
                  (SuDokuInnerOperation.RemoveFromCell, r, c, value)
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromRowExceptBlock, true) =>
            val block = SuDokuBlock.getBlock(i, j)
            val inputs: List[SuDokuInnerInput] =
              sudoku
                .cells
                .keys
                .withFilter: (r, _, b) =>
                  r == i && b != block
                .map: (r, c, _) =>
                  (SuDokuInnerOperation.RemoveFromCell, r, c, value)
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromColumnExceptBlock, true) =>
            val block = SuDokuBlock.getBlock(i, j)
            val inputs: List[SuDokuInnerInput] =
              sudoku
                .cells
                .keys
                .withFilter: (_, c, b) =>
                  c == j && b != block
                .map: (r, c, _) =>
                  (SuDokuInnerOperation.RemoveFromCell, r, c, value)
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case _ => (false, sudoku)

  def execute(inputs: List[SuDokuComplexInnerInput]): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs, updateSuDoku)

end SuDokuComplexInner
