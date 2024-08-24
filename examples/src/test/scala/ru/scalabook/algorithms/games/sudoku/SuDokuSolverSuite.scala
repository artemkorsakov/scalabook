package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDoku.getBlock
import ru.scalabook.algorithms.games.sudoku.SuDokuSolver.{
  SuDokuInput,
  blockSingleValueLoop,
  getSingleValues
}
import ru.scalabook.algorithms.games.sudoku.SuDokuSuite.{
  expect,
  forEach,
  pureTest,
  test
}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuSolverSuite extends SimpleIOSuite with Checkers:

  pureTest("SuDoku.setValue should correctly set a value in a cell - example"):
    val initialSudoku = SuDoku.initial
    val (result, sudoku) =
      SuDokuSolver.setValue(0, 2, SuDokuCellValue.Three).run(initialSudoku)

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
      val state            = SuDokuSolver.setValue(i, j, value)
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
      SuDokuSolver.setValues(inputs*).run(initialSudoku)

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
      SuDokuSolver.setValues(inputs*).run(initialSudoku)

    val (result, sudokuWithSolution) = SuDokuSolver.solutionState.run(sudoku)

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
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
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
      SuDokuSolver.solutionState.run(convertedSudoku)

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

  pureTest(
    "SuDoku.hasSolution should return true if a SuDoku puzzle has a solution"
  ):
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
      "483921657\n" +
        "967345821\n" +
        "251876493\n" +
        "548132976\n" +
        "729564138\n" +
        "136798245\n" +
        "372689514\n" +
        "814253769\n" +
        "695417382"
    )

    expect.all(
      convertedResult,
      convertedSudoku.isCorrect,
      convertedSudoku.hasSolution
    )

  pureTest(
    "SuDoku.getSingleValues should return a list of the single values in a SuDoku puzzle"
  ):
    val (result, sudoku) = SuDokuSolver.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "300705426\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "926108000\n" +
        "834059000\n" +
        "517000000"
    )

    val block00        = getBlock(0, 0)
    val singleValues00 = getSingleValues(sudoku, block00)

    val block03        = getBlock(0, 3)
    val singleValues03 = getSingleValues(sudoku, block03)

    val block06        = getBlock(0, 6)
    val singleValues06 = getSingleValues(sudoku, block06)

    val block30        = getBlock(3, 0)
    val singleValues30 = getSingleValues(sudoku, block30)

    val block33        = getBlock(3, 3)
    val singleValues33 = getSingleValues(sudoku, block33)

    val block36        = getBlock(3, 6)
    val singleValues36 = getSingleValues(sudoku, block36)

    val block60        = getBlock(6, 0)
    val singleValues60 = getSingleValues(sudoku, block60)

    val block63        = getBlock(6, 3)
    val singleValues63 = getSingleValues(sudoku, block63)

    val block66        = getBlock(6, 6)
    val singleValues66 = getSingleValues(sudoku, block66)

    expect.all(
      result,
      singleValues00.isEmpty,
      singleValues03 == List((2, 4, SuDokuCellValue.Nine)),
      singleValues06.isEmpty,
      singleValues30 == List((4, 2, SuDokuCellValue.Nine)),
      singleValues33.isEmpty,
      singleValues36.isEmpty,
      singleValues60.isEmpty,
      singleValues63 == List((6, 4, SuDokuCellValue.Seven)),
      singleValues66 == List((8, 7, SuDokuCellValue.Nine))
    )

  pureTest(
    "SuDoku.blockSingleValueLoop should set all single values in a SuDoku puzzle"
  ):
    val (initialResult, initialSudoku) = SuDokuSolver.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "300705426\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "926108000\n" +
        "834059000\n" +
        "517000000"
    )

    val (result, sudoku) = blockSingleValueLoop(initialSudoku, 0, 0)

    val expectedSudoku =
      "{[(2,4,6);(4,6,8);(1,2,5,8);(3,6,8);(1,3,6,8);(1,3,6);(9);(3,5);(7)] " +
        "[(6,7);(6,7,9);(5);(4);(2);(3,6);(1);(8);(3,5)] " +
        "[(3);(8);(1,8);(7);(9);(5);(4);(2);(6)] " +
        "[(1);(6,7,8);(2,3,8);(9);(3,6,8);(4);(2,3,5,6,7,8);(3,5,6,7);(2,3,5,8)] " +
        "[(2,6,7);(5);(9);(2,3,6,8);(1,3,6,8);(1,2,3,6);(2,3,6,7,8);(4);(1,2,3,8)] " +
        "[(2,4,6);(4,6,8);(2,3,8);(5);(1,3,6,8);(7);(2,3,6,8);(1,3,6);(9)] " +
        "[(9);(2);(6);(1);(7);(8);(3,5);(3,5);(3,4,5)] " +
        "[(8);(3);(4);(2,6);(5);(9);(2,6,7);(1,6,7);(1,2)] " +
        "[(5);(1);(7);(2,3,6);(3,4,6);(2,3,6);(2,3,6,8);(9);(2,3,4,8)]}"

    expect.all(
      initialResult,
      result,
      sudoku.show == expectedSudoku
    )

  pureTest("SuDoku.toSuDoku should convert a string to a SuDoku puzzle"):
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
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
      SuDokuSolver.solutionState.run(convertedSudoku)

    val expectedSudokuWithSolution =
      "{[(4);(6);(2);(8);(3);(1);(9);(5);(7)] " +
        "[(7);(9);(5);(4);(2);(6);(1);(8);(3)] " +
        "[(3);(8);(1);(7);(9);(5);(4);(2);(6)] " +
        "[(1);(7);(3);(9);(8);(4);(2);(6);(5)] " +
        "[(6);(5);(9);(3);(1);(2);(7);(4);(8)] " +
        "[(2);(4);(8);(5);(6);(7);(3);(1);(9)] " +
        "[(9);(2);(6);(1);(7);(8);(5);(3);(4)] " +
        "[(8);(3);(4);(2);(5);(9);(6);(7);(1)] " +
        "[(5);(1);(7);(6);(4);(3);(8);(9);(2)]}"

    expect.all(
      convertedResult,
      solutionResult,
      sudokuWithSolution.show == expectedSudokuWithSolution
    )
end SuDokuSolverSuite
