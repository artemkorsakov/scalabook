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
    case RemoveFromRow, RemoveFromColumn, RemoveFromBlock

  type SuDokuComplexInnerInput =
    (SuDokuInnerComplexOperation, Row, Column, SuDokuCellValue)

  private val updateSuDoku
      : SuDokuComplexInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        (operation, result) match
          case (RemoveFromRow, true) =>
            val inputs: List[SuDokuInnerInput] =
              (0 to 8).flatMap: k =>
                if k == j then None
                else Some((SuDokuInnerOperation.RemoveFromCell, i, k, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromColumn, true) =>
            val inputs: List[SuDokuInnerInput] =
              (0 to 8).flatMap: k =>
                if k == i then None
                else Some((SuDokuInnerOperation.RemoveFromCell, k, j, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromBlock, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDoku.getBlock(i, j).flatMap: (r, c) =>
                if r == i && c == j then None
                else Some((SuDokuInnerOperation.RemoveFromCell, r, c, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case _ => (false, sudoku)

  def execute(inputs: List[SuDokuComplexInnerInput]): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs, updateSuDoku)

end SuDokuComplexInner
