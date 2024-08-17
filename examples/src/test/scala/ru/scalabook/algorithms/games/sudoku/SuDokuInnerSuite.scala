package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuInnerSuite extends SimpleIOSuite with Checkers:
  test("SuDokuInner.execute should correctly set a value to a cell"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuInnerInput] =
        List((SuDokuInnerOperation.SetToCell, i, j, value))

      val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j).maybeValue.contains(value),
        (0 to 8).forall: r =>
          (0 to 8).forall: c =>
            (i == r && j == c) ||
              sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
      )

  test("SuDokuInner.execute should correctly remove a value from a cell"):
    forall: (row: FieldLength, col: FieldLength, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial
      val i             = row.toInt
      val j             = col.toInt
      val inputs: List[SuDokuInnerInput] =
        List((SuDokuInnerOperation.RemoveFromCell, i, j, value))

      val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

      expect.all(
        result,
        sudoku.isCorrect,
        sudoku.cells(i)(j)
          .possibleValues == (SuDokuCellValue.values.toSet - value),
        (0 to 8).forall: r =>
          (0 to 8).forall: c =>
            (i == r && j == c) ||
              sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
      )

  pureTest(
    "SuDokuInner.execute should not execute an operation if a sudoku is incorrect"
  ):
    val initialSudoku = SuDoku(Vector.empty)
    val inputs: List[SuDokuInnerInput] =
      List((SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.One))

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDokuInner.execute should not execute an operation if a row is incorrect"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] =
      List((SuDokuInnerOperation.SetToCell, 9, 0, SuDokuCellValue.One))

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDokuInner.execute should not execute an operation if a column is incorrect"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] =
      List((SuDokuInnerOperation.SetToCell, 0, 9, SuDokuCellValue.One))

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDokuInner.execute should not set a value if the cell does not contain it"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.Two),
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.One)
    )

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku.cells(0)(0).maybeValue.contains(SuDokuCellValue.Two),
      (0 to 8).forall: r =>
        (0 to 8).forall: c =>
          (0 == r && 0 == c) ||
            sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
    )

  pureTest(
    "execute should not remove a value if it is the only possible value"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.One),
      (SuDokuInnerOperation.RemoveFromCell, 0, 0, SuDokuCellValue.One)
    )

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku.cells(0)(0).maybeValue.contains(SuDokuCellValue.One),
      (0 to 8).forall: r =>
        (0 to 8).forall: c =>
          (0 == r && 0 == c) ||
            sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
    )

  pureTest(
    "SuDokuInner.execute should not execute an operation if the previous operation is incorrect"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.Two),
      (SuDokuInnerOperation.SetToCell, 0, 0, SuDokuCellValue.One),
      (SuDokuInnerOperation.SetToCell, 1, 1, SuDokuCellValue.One)
    )

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      !result,
      sudoku.cells(0)(0).maybeValue.contains(SuDokuCellValue.Two),
      (0 to 8).forall: r =>
        (0 to 8).forall: c =>
          (0 == r && 0 == c) ||
            sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
    )

  pureTest("SuDokuInner.execute should handle multiple operations"):
    val initialSudoku = SuDoku.initial
    val inputs: List[SuDokuInnerInput] = List(
      (SuDokuInnerOperation.RemoveFromCell, 0, 0, SuDokuCellValue.One),
      (SuDokuInnerOperation.RemoveFromCell, 0, 0, SuDokuCellValue.Two),
      (SuDokuInnerOperation.SetToCell, 1, 1, SuDokuCellValue.One),
      (SuDokuInnerOperation.SetToCell, 2, 2, SuDokuCellValue.Two)
    )

    val (result, sudoku) = SuDokuInner.execute(inputs).run(initialSudoku)

    expect.all(
      result,
      sudoku.cells(0)(0).possibleValues == (SuDokuCellValue.values
        .toSet - SuDokuCellValue.One - SuDokuCellValue.Two),
      sudoku.cells(1)(1).maybeValue.contains(SuDokuCellValue.One),
      sudoku.cells(2)(2).maybeValue.contains(SuDokuCellValue.Two),
      (0 to 8).forall: r =>
        (0 to 8).forall: c =>
          (0 == r && 0 == c) || (1 == r && 1 == c) || (2 == r && 2 == c) ||
            sudoku.cells(r)(c).possibleValues == SuDokuCellValue.values.toSet
    )

end SuDokuInnerSuite
