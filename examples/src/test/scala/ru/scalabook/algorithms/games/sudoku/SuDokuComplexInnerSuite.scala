package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.{
  SuDokuInnerInput,
  SuDokuInnerOperation
}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuComplexInnerSuite extends SimpleIOSuite with Checkers:
  private val initialValues = SuDokuCellValue.values.toSet

  test("SuDokuComplexInner.execute should correctly remove a value from a row"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromRow, row, col, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._2 != col then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  test("SuDokuComplexInner.execute should correctly remove a value from a row"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromColumn, row, col, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 != row && index._2 == col then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  test(
    "SuDokuComplexInner.execute should correctly remove a value from a block"
  ):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromBlock, row, col, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      val block = SuDokuBlock.getBlock(row, col)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._3 == block && (index._1 != row || index._2 != col) then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  pureTest(
    "SuDokuComplexInner.execute should not execute an operation if the previous operation is incorrect"
  ):
    val initialSudoku = SuDoku.initial
    val innerInput: SuDokuInnerInput =
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      )
    val (_, sudokuWithGivenCell) =
      SuDokuInner.execute(innerInput).run(initialSudoku)

    val inputs: List[SuDokuComplexInnerInput] =
      List(
        (
          SuDokuInnerComplexOperation.RemoveFromRow,
          SuDokuRow.First,
          SuDokuColumn.Second,
          SuDokuCellValue.One
        ),
        (
          SuDokuInnerComplexOperation.RemoveFromRow,
          SuDokuRow.Second,
          SuDokuColumn.Second,
          SuDokuCellValue.Two
        )
      )

    val (result, sudoku) =
      SuDokuComplexInner.execute(inputs).run(sudokuWithGivenCell)

    expect.all(
      !result,
      sudoku == sudokuWithGivenCell
    )

  test("SuDokuComplexInner.execute should handle multiple operations"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List(
          (SuDokuInnerComplexOperation.RemoveFromRow, row, col, value),
          (SuDokuInnerComplexOperation.RemoveFromColumn, row, col, value),
          (SuDokuInnerComplexOperation.RemoveFromBlock, row, col, value)
        )

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      val block = SuDokuBlock.getBlock(row, col)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._2 != col ||
              index._1 != row && index._2 == col ||
              index._3 == block && (index._1 != row || index._2 != col)
            then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  test(
    "SuDokuComplexInner.execute should correctly remove a value from a row except the block"
  ):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List((
          SuDokuInnerComplexOperation.RemoveFromRowExceptBlock,
          row,
          col,
          value
        ))

      val block = SuDokuBlock.getBlock(row, col)
      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._3 != block then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  test(
    "SuDokuComplexInner.execute should correctly remove a value from a column except the block"
  ):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val inputs: List[SuDokuComplexInnerInput] =
        List((
          SuDokuInnerComplexOperation.RemoveFromRowExceptBlock,
          row,
          col,
          value
        ))

      val block = SuDokuBlock.getBlock(row, col)
      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect
      ).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._2 == col && index._3 != block then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

end SuDokuComplexInnerSuite
