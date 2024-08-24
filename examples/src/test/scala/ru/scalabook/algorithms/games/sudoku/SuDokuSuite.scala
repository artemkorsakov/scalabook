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
      sudoku.isCorrect,
      !sudoku.hasSolution
    )

  test("SuDoku should not be correct if there is not a row"):
    forall:
      (
          row: SuDokuRow,
          col1: SuDokuColumn,
          col2: SuDokuColumn,
          value: SuDokuCellValue
      ) =>
        if col1 == col2 then success
        else
          val initialSudoku = SuDoku.initial
          val block1        = SuDokuBlock.getBlock(row, col1)
          val block2        = SuDokuBlock.getBlock(row, col2)
          val cell          = SuDokuCell(Set(value))
          val cells = initialSudoku
            .cells
            .updated((row, col1, block1), cell)
            .updated((row, col2, block2), cell)
          val sudoku = SuDoku(cells)
          expect(!sudoku.isCorrect)

  test("SuDoku should not be correct if there is not a column"):
    forall:
      (
          row1: SuDokuRow,
          row2: SuDokuRow,
          col: SuDokuColumn,
          value: SuDokuCellValue
      ) =>
        if row1 == row2 then success
        else
          val initialSudoku = SuDoku.initial
          val block1        = SuDokuBlock.getBlock(row1, col)
          val block2        = SuDokuBlock.getBlock(row2, col)
          val cell          = SuDokuCell(Set(value))
          val cells = initialSudoku
            .cells
            .updated((row1, col, block1), cell)
            .updated((row2, col, block2), cell)
          val sudoku = SuDoku(cells)
          expect(!sudoku.isCorrect)

  test("SuDoku should not be correct if there is not a block"):
    forall: (block: SuDokuBlock, value: SuDokuCellValue) =>
      val initialSudoku = SuDoku.initial

      val indexes =
        SuDoku.initial.cells.keys.filter(_._3 == block).take(2).toList
      matches(indexes) { case first :: second :: Nil =>
        val cell = SuDokuCell(Set(value))
        val cells = initialSudoku
          .cells
          .updated(first, cell)
          .updated(second, cell)
        val sudoku = SuDoku(cells)
        expect(!sudoku.isCorrect)
      }

  pureTest(
    "SuDoku.initial should have all cells with the same initial possible values"
  ):
    val sudoku      = SuDoku.initial
    val initialCell = SuDokuCell.initial

    forEach(sudoku.cells.values.toVector): cell =>
      expect.same(initialCell, cell)

end SuDokuSuite
