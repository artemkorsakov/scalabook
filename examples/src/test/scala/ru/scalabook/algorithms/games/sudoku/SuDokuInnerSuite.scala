package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuInnerSuite extends SimpleIOSuite with Checkers:
  private val initialValues = SuDokuCellValue.values.toSet

  test("SuDokuInner.execute should correctly set a value to a cell"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku           = SuDoku.initial
      val input: SuDokuInnerInput =
        (SuDokuInnerOperation.SetToCell, row, col, value)

      val (result, sudoku) = SuDokuInner.execute(input).run(initialSudoku)

      expect.all(result, sudoku.isCorrect) and
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._2 == col then
              expect(cell.maybeValue.contains(value))
            else expect(cell.possibleValues == initialValues)

  test("SuDokuInner.execute should correctly remove a value from a cell"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku           = SuDoku.initial
      val input: SuDokuInnerInput =
        (SuDokuInnerOperation.RemoveFromCell, row, col, value)

      val (result, sudoku) = SuDokuInner.execute(input).run(initialSudoku)

      expect.all(result, sudoku.isCorrect) and
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._2 == col then
              expect(cell.possibleValues == (initialValues - value))
            else expect(cell.possibleValues == initialValues)

  pureTest(
    "SuDokuInner.execute should not execute an operation if a sudoku is incorrect"
  ):
    val initialSudoku           = SuDoku(Map.empty)
    val input: SuDokuInnerInput =
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      )

    val (result, sudoku) = SuDokuInner.execute(input).run(initialSudoku)

    expect.all(
      !result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDokuInner.execute should not set a value if the cell does not contain it"
  ):
    val initialSudoku                  = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.Two
      ),
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      )
    )

    val (result, sudoku) = SuDokuInner.execute(inputs*).run(initialSudoku)

    expect.all(!result, sudoku.isCorrect) and
      forEach(sudoku.cells.toVector): (index: SuDokuIndex, cell: SuDokuCell) =>
        if index._1 == SuDokuRow.First && index._2 == SuDokuColumn.First then
          expect(cell.maybeValue.contains(SuDokuCellValue.Two))
        else expect(cell.possibleValues == initialValues)

  pureTest(
    "execute should not remove a value if it is the only possible value"
  ):
    val initialSudoku                  = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      ),
      (
        SuDokuInnerOperation.RemoveFromCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      )
    )

    val (result, sudoku) = SuDokuInner.execute(inputs*).run(initialSudoku)

    expect.all(!result, sudoku.isCorrect) and
      forEach(sudoku.cells.toVector): (index: SuDokuIndex, cell: SuDokuCell) =>
        if index._1 == SuDokuRow.First && index._2 == SuDokuColumn.First then
          expect(cell.maybeValue.contains(SuDokuCellValue.One))
        else expect(cell.possibleValues == initialValues)

  pureTest(
    "SuDokuInner.execute should not execute an operation if the previous operation is incorrect"
  ):
    val initialSudoku                  = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.Two
      ),
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      ),
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.Second,
        SuDokuColumn.Second,
        SuDokuCellValue.One
      )
    )

    val (result, sudoku) = SuDokuInner.execute(inputs*).run(initialSudoku)

    expect.all(!result, sudoku.isCorrect) and
      forEach(sudoku.cells.toVector): (index: SuDokuIndex, cell: SuDokuCell) =>
        if index._1 == SuDokuRow.First && index._2 == SuDokuColumn.First then
          expect(cell.maybeValue.contains(SuDokuCellValue.Two))
        else expect(cell.possibleValues == initialValues)

  pureTest("SuDokuInner.execute should handle multiple operations"):
    val initialSudoku                  = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (
        SuDokuInnerOperation.RemoveFromCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.One
      ),
      (
        SuDokuInnerOperation.RemoveFromCell,
        SuDokuRow.First,
        SuDokuColumn.First,
        SuDokuCellValue.Two
      ),
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.Second,
        SuDokuColumn.Second,
        SuDokuCellValue.One
      ),
      (
        SuDokuInnerOperation.SetToCell,
        SuDokuRow.Third,
        SuDokuColumn.Third,
        SuDokuCellValue.Two
      )
    )

    val (result, sudoku) = SuDokuInner.execute(inputs*).run(initialSudoku)

    expect.all(result, sudoku.isCorrect) and
      forEach(sudoku.cells.toVector): (index: SuDokuIndex, cell: SuDokuCell) =>
        if index._1 == SuDokuRow.First && index._2 == SuDokuColumn.First then
          expect(cell.possibleValues ==
            (SuDokuCellValue.values
              .toSet - SuDokuCellValue.One - SuDokuCellValue.Two))
        else if index._1 == SuDokuRow.Second && index._2 == SuDokuColumn.Second
        then
          expect(cell.maybeValue.contains(SuDokuCellValue.One))
        else if index._1 == SuDokuRow.Third && index._2 == SuDokuColumn.Third
        then
          expect(cell.maybeValue.contains(SuDokuCellValue.Two))
        else expect(cell.possibleValues == initialValues)

end SuDokuInnerSuite
