package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDoku.*
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
      sudoku.isCorrect,
      !sudoku.hasSolution
    )

  pureTest("SuDoku should not be correct if cells are not 9x9"):
    val incorrectRow  = Vector.fill(8)(Vector.fill(9)(SuDokuCell.initial))
    val incorrectCols = Vector.fill(9)(Vector.fill(8)(SuDokuCell.initial))
    expect.all(
      !SuDoku(incorrectRow).isCorrect,
      !SuDoku(incorrectCols).isCorrect
    )

  pureTest("SuDoku should not be correct if cells have empty possible values"):
    val incorrectCells = Vector.fill(9)(Vector.fill(9)(SuDokuCell(Set())))
    val sudoku         = SuDoku(incorrectCells)
    expect(!sudoku.isCorrect)

  test("SuDoku should not be correct if rows have duplicate values"):
    forall:
      (
          row: FieldLength,
          col1: FieldLength,
          col2: FieldLength,
          value: SuDokuCellValue
      ) =>
        val sudoku = SuDoku.initial
        val cell   = SuDokuCell(Set(value))
        val col3   = if col1 == col2 then (col2.toInt + 1) % 9 else col2.toInt
        val incorrectRow =
          sudoku.cells(row.toInt)
            .updated(col1.toInt, cell)
            .updated(col3, cell)
        val updateCells = sudoku.cells.updated(row.toInt, incorrectRow)
        expect(!SuDoku(updateCells).isCorrect)

  test("SuDoku should not be correct if cols have duplicate values"):
    forall:
      (
          row1: FieldLength,
          row2: FieldLength,
          col: FieldLength,
          value: SuDokuCellValue
      ) =>
        val sudoku = SuDoku.initial
        val cell   = SuDokuCell(Set(value))
        val row3   = if row1 == row2 then (row2.toInt + 1) % 9 else row2.toInt

        val incorrectRow1 = sudoku.cells(row1.toInt).updated(col.toInt, cell)
        val incorrectRow2 = sudoku.cells(row3.toInt).updated(col.toInt, cell)

        val updateCells =
          sudoku.cells
            .updated(row1.toInt, incorrectRow1)
            .updated(row3.toInt, incorrectRow2)
        expect(!SuDoku(updateCells).isCorrect)

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

  pureTest(
    "SuDoku.getBlock should return correct block indices for valid row and column"
  ):
    val blockIndices = SuDoku.getBlock(1, 1)

    val expectedIndices = IndexedSeq(
      (0, 0),
      (0, 1),
      (0, 2),
      (1, 0),
      (1, 1),
      (1, 2),
      (2, 0),
      (2, 1),
      (2, 2)
    )

    expect(blockIndices == expectedIndices)

  pureTest(
    "SuDoku.getBlock should return correct block indices for another valid row and column"
  ):
    val blockIndices = SuDoku.getBlock(4, 5)

    val expectedIndices = IndexedSeq(
      (3, 3),
      (3, 4),
      (3, 5),
      (4, 3),
      (4, 4),
      (4, 5),
      (5, 3),
      (5, 4),
      (5, 5)
    )

    expect(blockIndices == expectedIndices)

  pureTest("SuDoku.getBlock should return empty sequence for invalid row"):
    val blockIndices = SuDoku.getBlock(9, 1)
    expect(blockIndices.isEmpty)

  pureTest("SuDoku.getBlock should return empty sequence for invalid column"):
    val blockIndices = SuDoku.getBlock(1, 9)
    expect(blockIndices.isEmpty)

  pureTest(
    "SuDoku.getBlock should return empty sequence for both invalid row and column"
  ):
    val blockIndices = SuDoku.getBlock(9, 9)
    expect(blockIndices.isEmpty)
end SuDokuSuite
