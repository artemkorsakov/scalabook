package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDoku.{
  Column,
  Row,
  SuDokuInput,
  getBlock
}
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

end SuDokuSuite
