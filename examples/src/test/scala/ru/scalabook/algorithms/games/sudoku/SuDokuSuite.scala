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
      sudoku.cells.forall(_.length == 9)
    )

  pureTest(
    "SuDoku.initial should have all cells with the same initial possible values"
  ):
    val sudoku = SuDoku.initial

    val initialPossibleValues = SuDokuCell.initial.possibleValues
    val allCellsHaveInitialValues =
      sudoku.cells.flatten.forall(_.possibleValues == initialPossibleValues)

    expect(allCellsHaveInitialValues)

  pureTest(
    "SuDoku.Row.isCorrect should return true if the row is correct"
  ):
    forEach((0 to 8).toVector): (row: Int) =>
      expect(Row.isCorrect(row))
    .and
    expect.all(
      !Row.isCorrect(-1),
      !Row.isCorrect(9)
    )

  pureTest(
    "SuDoku.Column.isCorrect should return true if the column is correct"
  ):
    forEach((0 to 8).toVector): (col: Int) =>
      expect(Column.isCorrect(col))
    .and
    expect.all(
      !Column.isCorrect(-1),
      !Column.isCorrect(9)
    )
end SuDokuSuite
