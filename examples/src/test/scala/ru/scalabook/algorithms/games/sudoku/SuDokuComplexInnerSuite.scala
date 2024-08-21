package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.{
  SuDokuInnerInput,
  SuDokuInnerOperation
}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuComplexInnerSuite extends SimpleIOSuite with Checkers:
  test("SuDokuComplexInner.execute should correctly remove a value from a row"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromRow, i, j, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j).possibleValues == SuDokuCellValue.values.toSet
      ).and(
        forEach((0 to 8).toList): c =>
          expect(
            c == j ||
              sudoku.cells(i)(c)
                .possibleValues == (SuDokuCellValue.values.toSet - value)
          )
      )
        .and(
          forEach((0 to 8).toList): r =>
            forEach((0 to 8).toList): c =>
              expect(
                (i == r) ||
                  sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
              )
        )

  test("SuDokuComplexInner.execute should correctly remove a value from a row"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromColumn, i, j, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j).possibleValues == SuDokuCellValue.values.toSet
      ).and(
        forEach((0 to 8).toList): r =>
          expect(
            r == i ||
              sudoku.cells(r)(j)
                .possibleValues == (SuDokuCellValue.values.toSet - value)
          )
      )
        .and(
          forEach((0 to 8).toList): r =>
            forEach((0 to 8).toList): c =>
              expect(
                (c == j) ||
                  sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
              )
        )

  test(
    "SuDokuComplexInner.execute should correctly remove a value from a block"
  ):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuComplexInnerInput] =
        List((SuDokuInnerComplexOperation.RemoveFromBlock, i, j, value))

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      val blockIdx = SuDoku.getBlock(i, j)

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j).possibleValues == SuDokuCellValue.values.toSet
      ).and(
        forEach(blockIdx.toList): (r, c) =>
          expect(
            (r == i && c == j) ||
              sudoku.cells(r)(c)
                .possibleValues == (SuDokuCellValue.values.toSet - value)
          )
      )
        .and(
          forEach((0 to 8).toList): r =>
            forEach((0 to 8).toList): c =>
              expect(
                blockIdx.contains((r, c)) ||
                  sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
              )
        )

  pureTest(
    "SuDokuComplexInner.execute should not execute an operation if the previous operation is incorrect"
  ):
    val initialSudoku = SuDoku.initial
    val innerInput: SuDokuInnerInput =
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.One)
    val (_, sudokuWithGivenCell) =
      SuDokuInner.execute(innerInput).run(initialSudoku)

    val inputs: List[SuDokuComplexInnerInput] =
      List(
        (SuDokuInnerComplexOperation.RemoveFromRow, 0, 1, SuDokuCellValue.One),
        (SuDokuInnerComplexOperation.RemoveFromRow, 1, 1, SuDokuCellValue.Two)
      )

    val (result, sudoku) =
      SuDokuComplexInner.execute(inputs).run(sudokuWithGivenCell)

    expect.all(
      !result,
      sudoku == sudokuWithGivenCell
    )

  test("SuDokuComplexInner.execute should handle multiple operations"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuComplexInnerInput] =
        List(
          (SuDokuInnerComplexOperation.RemoveFromRow, i, j, value),
          (SuDokuInnerComplexOperation.RemoveFromColumn, i, j, value),
          (SuDokuInnerComplexOperation.RemoveFromBlock, i, j, value)
        )

      val (result, sudoku) =
        SuDokuComplexInner.execute(inputs).run(initialSudoku)

      val blockIdx       = SuDoku.getBlock(i, j)
      val expectedValues = SuDokuCellValue.values.toSet - value

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j).possibleValues == SuDokuCellValue.values.toSet
      ).and(
        forEach((0 to 8).toList): c =>
          expect(
            c == j ||
              sudoku.cells(i)(c).possibleValues == expectedValues
          )
      ).and(
        forEach((0 to 8).toList): r =>
          expect(
            r == i ||
              sudoku.cells(r)(j).possibleValues == expectedValues
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

end SuDokuComplexInnerSuite
