package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDoku.{
  Column,
  Row,
  SuDokuInput,
  getBlock
}
import ru.scalabook.algorithms.games.sudoku.SuDokuCellValue.Six
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

  pureTest("SuDoku.setValue should correctly set a value in a cell - example"):
    val initialSudoku = SuDoku.initial
    val (result, sudoku) =
      SuDoku.setValue(0, 2, SuDokuCellValue.Three).run(initialSudoku)

    expect.all(
      result,
      sudoku.show ==
        "{[(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(3);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9)] " +
        "[(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9);(1,2,3,4,5,6,7,8,9)]}"
    )

  test("SuDoku.setValue should correctly set a value in a cell"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku    = SuDoku.initial
      val i                = row.toInt
      val j                = col.toInt
      val state            = SuDoku.setValue(i, j, value)
      val (result, sudoku) = state.run(initialSudoku)

      val expectedValues = SuDokuCellValue.values.toSet - value
      val blockIdx       = getBlock(i, j)

      expect.all(
        result,
        sudoku.cells(i)(j).maybeValue.contains(value)
      ).and(
        forEach((0 to 8).toList): c =>
          expect(
            c == j || sudoku.cells(i)(c).possibleValues == expectedValues
          )
      ).and(
        forEach((0 to 8).toList): r =>
          expect(
            r == i || sudoku.cells(r)(j).possibleValues == expectedValues
          )
      ).and(
        forEach(blockIdx.toList): (r, c) =>
          expect(
            (r == i && c == j) ||
              sudoku.cells(r)(c).possibleValues == expectedValues
          )
      )
        .and(
          forEach((0 to 8).toList): r =>
            forEach((0 to 8).toList): c =>
              expect(
                r == i || c == j || blockIdx.contains((r, c)) ||
                  sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
              )
        )

  pureTest("SuDoku.setValue should false if one of the operators fails"):
    val initialSudoku = SuDoku.initial
    val inputs: Seq[SuDokuInput] = Seq(
      (0, 0, SuDokuCellValue.One),
      (0, 1, SuDokuCellValue.Two),
      (0, 2, SuDokuCellValue.Three),
      (0, 3, SuDokuCellValue.Four),
      (0, 4, SuDokuCellValue.Five),
      (0, 5, SuDokuCellValue.Six),
      (0, 6, SuDokuCellValue.Seven),
      (0, 7, SuDokuCellValue.Eight),
      (8, 8, SuDokuCellValue.Nine)
    )
    val (result, sudoku) =
      SuDoku.setValues(inputs*).run(initialSudoku)

    val expectedSudoku =
      "{[(1);(2);(3);(4);(5);(6);(7);(8);(9)] " +
        "[(4,5,6,7,8,9);(4,5,6,7,8,9);(4,5,6,7,8,9);(1,2,3,7,8,9);(1,2,3,7,8,9);(1,2,3,7,8,9);(1,2,3,4,5,6,9);(1,2,3,4,5,6,9);(1,2,3,4,5,6,9)] " +
        "[(4,5,6,7,8,9);(4,5,6,7,8,9);(4,5,6,7,8,9);(1,2,3,7,8,9);(1,2,3,7,8,9);(1,2,3,7,8,9);(1,2,3,4,5,6,9);(1,2,3,4,5,6,9);(1,2,3,4,5,6,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)] " +
        "[(2,3,4,5,6,7,8,9);(1,3,4,5,6,7,8,9);(1,2,4,5,6,7,8,9);(1,2,3,5,6,7,8,9);(1,2,3,4,6,7,8,9);(1,2,3,4,5,7,8,9);(1,2,3,4,5,6,8,9);(1,2,3,4,5,6,7,9);(1,2,3,4,5,6,7,8,9)]}"

    expect.all(
      !result,
      sudoku.show == expectedSudoku
    )

  pureTest(
    "SuDoku.getSolution should return the solution of the sudoku puzzle"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: Seq[SuDokuInput] = Seq(
      (0, 2, SuDokuCellValue.Three),
      (0, 4, SuDokuCellValue.Two),
      (0, 6, SuDokuCellValue.Six),
      (1, 0, SuDokuCellValue.Nine),
      (1, 3, SuDokuCellValue.Three),
      (1, 5, SuDokuCellValue.Five),
      (1, 8, SuDokuCellValue.One),
      (2, 2, SuDokuCellValue.One),
      (2, 3, SuDokuCellValue.Eight),
      (2, 5, SuDokuCellValue.Six),
      (2, 6, SuDokuCellValue.Four),
      (3, 2, SuDokuCellValue.Eight),
      (3, 3, SuDokuCellValue.One),
      (3, 5, SuDokuCellValue.Two),
      (3, 6, SuDokuCellValue.Nine),
      (4, 0, SuDokuCellValue.Seven),
      (4, 8, SuDokuCellValue.Eight),
      (5, 2, SuDokuCellValue.Six),
      (5, 3, SuDokuCellValue.Seven),
      (5, 5, SuDokuCellValue.Eight),
      (5, 6, SuDokuCellValue.Two),
      (6, 2, SuDokuCellValue.Two),
      (6, 3, SuDokuCellValue.Six),
      (6, 5, SuDokuCellValue.Nine),
      (6, 6, SuDokuCellValue.Five),
      (7, 0, SuDokuCellValue.Eight),
      (7, 3, SuDokuCellValue.Two),
      (7, 5, SuDokuCellValue.Three),
      (7, 8, SuDokuCellValue.Nine),
      (8, 2, SuDokuCellValue.Five),
      (8, 4, SuDokuCellValue.One),
      (8, 5, SuDokuCellValue.Three)
    )

    val (_, sudoku) =
      SuDoku.setValues(inputs*).run(initialSudoku)

    val (result, sudokuWithSolution) = SuDoku.getSolution.run(sudoku)

    val expectedSudoku =
      "{[(4);(8);(3);(9);(2);(1);(6);(5);(7)] " +
        "[(9);(6);(7);(3);(4);(5);(8);(2);(1)] " +
        "[(2);(5);(1);(8);(7);(6);(4);(9);(3)] " +
        "[(5);(4);(8);(1);(3);(2);(9);(7);(6)] " +
        "[(7);(2);(9);(5);(6);(4);(1);(3);(8)] " +
        "[(1);(3);(6);(7);(9);(8);(2);(4);(5)] " +
        "[(3);(7);(2);(6);(8);(9);(5);(1);(4)] " +
        "[(8);(1);(4);(2);(5);(3);(7);(6);(9)] " +
        "[(6);(9);(5);(4);(1);(7);(3);(8);(2)]}"

    expect.all(
      result,
      sudokuWithSolution.show == expectedSudoku
    )

  pureTest("SuDoku.toSuDoku should convert a string to a SuDoku puzzle"):
    val (convertedResult, convertedSudoku) = SuDoku.toSuDoku(
      "003020600\n" +
        "900305001\n" +
        "001806400\n" +
        "008102900\n" +
        "700000008\n" +
        "006708200\n" +
        "002609500\n" +
        "800203009\n" +
        "005010300"
    )
    val (solutionResult, sudokuWithSolution) =
      SuDoku.getSolution.run(convertedSudoku)

    val expectedConvertedSudoku =
      "{[(4,5);(4,5,7,8);(3);(4,9);(2);(1,4,7);(6);(5,7,8,9);(5,7)] " +
        "[(9);(2,4,6,7,8);(4,7);(3);(4,7);(5);(7,8);(2,7,8);(1)] " +
        "[(2,5);(2,5,7);(1);(8);(7,9);(6);(4);(2,3,5,7,9);(2,3,5,7)] " +
        "[(3,4,5);(3,4,5);(8);(1);(3,4,5,6);(2);(9);(3,4,5,6,7);(3,4,5,6,7)] " +
        "[(7);(1,2,3,4,5,9);(4,9);(4,5,9);(3,4,5,6,9);(4);(1);(1,3,4,5,6);(8)] " +
        "[(1,3,4,5);(1,3,4,5,9);(6);(7);(3,4,5,9);(8);(2);(1,3,4,5);(3,4,5)] " +
        "[(1,3,4);(1,3,4,7);(2);(6);(4,7,8);(9);(5);(1,4,7,8);(4,7)] " +
        "[(8);(1,4,6,7);(4,7);(2);(4,5,7);(3);(1,7);(1,4,6,7);(9)] " +
        "[(4,6);(4,6,7,9);(5);(4);(1);(4,7);(3);(2,4,6,7,8);(2,4,6,7)]}"

    val expectedSudokuWithSolution =
      "{[(4);(8);(3);(9);(2);(1);(6);(5);(7)] " +
        "[(9);(6);(7);(3);(4);(5);(8);(2);(1)] " +
        "[(2);(5);(1);(8);(7);(6);(4);(9);(3)] " +
        "[(5);(4);(8);(1);(3);(2);(9);(7);(6)] " +
        "[(7);(2);(9);(5);(6);(4);(1);(3);(8)] " +
        "[(1);(3);(6);(7);(9);(8);(2);(4);(5)] " +
        "[(3);(7);(2);(6);(8);(9);(5);(1);(4)] " +
        "[(8);(1);(4);(2);(5);(3);(7);(6);(9)] " +
        "[(6);(9);(5);(4);(1);(7);(3);(8);(2)]}"

    expect.all(
      convertedResult,
      convertedSudoku.show == expectedConvertedSudoku,
      solutionResult,
      sudokuWithSolution.show == expectedSudokuWithSolution
    )

  pureTest("SuDoku.toSuDoku should convert a string to a SuDoku puzzle"):
    val (convertedResult, convertedSudoku) = SuDoku.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "000705026\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "920108000\n" +
        "034059000\n" +
        "507000000"
    )
    val (solutionResult, sudokuWithSolution) =
      SuDoku.getSolution.run(convertedSudoku)

    val expectedSudokuWithSolution =
      "{[(2,4,6);(4,6,8);(1,2,5,8);(3,6,8);(1,3,6,8);(1,3,6);(9);(3,5);(7)] " +
        "[(6,7);(6,7,9);(5,9);(4);(2);(3,6);(1);(8);(3,5)] " +
        "[(3);(8,9);(1,8,9);(7);(1,8,9);(5);(4);(2);(6)] " +
        "[(1);(6,7,8);(2,3,8);(9);(3,6,8);(4);(2,3,5,6,7,8);(3,5,6,7);(2,3,5,8)] " +
        "[(2,6,7);(5);(2,3,8,9);(2,3,6,8);(1,3,6,8);(1,2,3,6);(2,3,6,7,8);(4);(1,2,3,8)] " +
        "[(2,4,6);(4,6,8);(2,3,8);(5);(1,3,6,8);(7);(2,3,6,8);(1,3,6);(9)] " +
        "[(9);(2);(6);(1);(3,4,7);(8);(3,5,7);(3,5,7);(3,4,5)] " +
        "[(8);(3);(4);(2,6);(5);(9);(2,6,7);(1,6,7);(1,2)] " +
        "[(5);(1);(7);(2,3,6);(3,4,6);(2,3,6);(2,3,6,8);(3,6,9);(2,3,4,8)]}"

    expect.all(
      convertedResult,
      solutionResult,
      sudokuWithSolution.show == expectedSudokuWithSolution
    )
end SuDokuSuite
