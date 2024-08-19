package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
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

  pureTest(
    "SuDoku.setValue should set the value of a cell"
  ):
    val initialSudoku = SuDoku.initial
    val (result, sudoku) =
      SuDoku.setValue(0, 2, SuDokuCellValue.Three).run(initialSudoku)

    expect(
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

end SuDokuSuite

/*
 test("setValue should correctly set a value in a cell") {
    forall(Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ <= 9)) { (row, col, value) =>
      val initialSudoku = SuDoku.initial
      val state = SuDoku.setValue(row, col, SuDokuCellValue(value))
      val (result, updatedSudoku) = state.run(initialSudoku).value

      expect(result == true) and
        expect(updatedSudoku.cells(row)(col).value == SuDokuCellValue(value))
    }
  }

  test("setValue should return false if row is out of bounds") {
    forall(Gen.posNum[Int].suchThat(_ > 9), Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ <= 9)) { (row, col, value) =>
      val initialSudoku = SuDoku.initial
      val state = SuDoku.setValue(row, col, SuDokuCellValue(value))
      val (result, updatedSudoku) = state.run(initialSudoku).value

      expect(result == false)
    }
  }

  test("setValue should return false if column is out of bounds") {
    forall(Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ > 9), Gen.posNum[Int].suchThat(_ <= 9)) { (row, col, value) =>
      val initialSudoku = SuDoku.initial
      val state = SuDoku.setValue(row, col, SuDokuCellValue(value))
      val (result, updatedSudoku) = state.run(initialSudoku).value

      expect(result == false)
    }
  }

  test("setValue should return false if value is out of bounds") {
    forall(Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ <= 9), Gen.posNum[Int].suchThat(_ > 9)) { (row, col, value) =>
      val initialSudoku = SuDoku.initial
      val state = SuDoku.setValue(row, col, SuDokuCellValue(value))
      val (result, updatedSudoku) = state.run(initialSudoku).value

      expect(result == false)
    }
  }
 */
