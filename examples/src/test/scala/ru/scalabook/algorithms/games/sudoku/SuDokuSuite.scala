package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.{Column, Row}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuSuite extends SimpleIOSuite with Checkers:
  pureTest(
    "SuDoku.initial should create a 9x9 grid of SuDokuCell with initial values"
  ):
    val sudoku = SuDoku.initial

    expect.all(
      sudoku.cells.length == 9,
      sudoku.cells.forall(_.length == 9),
      sudoku.isCorrect
    )

  pureTest(
    "SuDoku.initial should have all cells with the same initial possible values"
  ):
    val sudoku = SuDoku.initial

    val initialPossibleValues = SuDokuCell.initial.possibleValues
    val allCellsHaveInitialValues =
      sudoku.cells.flatten.forall(_.possibleValues == initialPossibleValues)

    expect(allCellsHaveInitialValues)

  test("SuDoku.Row.isCorrect should return true if the row is correct"):
    forall: (row: FieldLength) =>
      expect(Row.isCorrect(row.toInt))

  test("SuDoku.Row.isCorrect should return false if the row is not correct"):
    forall: (row: InvalidFieldLength) =>
      expect(!Row.isCorrect(row.toInvalidInt))

  test("SuDoku.Column.isCorrect should return true if the column is correct"):
    forall: (col: FieldLength) =>
      expect(Column.isCorrect(col.toInt))

  test(
    "SuDoku.Column.isCorrect should return false if the column is not correct"
  ):
    forall: (col: InvalidFieldLength) =>
      expect(!Column.isCorrect(col.toInvalidInt))
end SuDokuSuite
