package ru.scalabook.algorithms.games.queens

import ru.scalabook.algorithms.games.queens.Queens.queens
import weaver.FunSuite

object QueensSuite extends FunSuite:
  private val boardSize = 4

  test("Queens on a 4x4 board"):
    val result = queens(4)
    expect(result.nonEmpty)

  test("Queens on a 4x4 board - solution is valid"):
    val result = queens(4)
    forEach(result): solution =>
      forEach(solution): (row, col) =>
        forEach(solution): (r, c) =>
          if row == r && col == c then success
          else
            expect.all(
              row != r,
              col != c,
              math.abs(row - r) != math.abs(col - c)
            )

  test("Queens on a 4x4 board - all queens are on the board"):
    val result = queens(boardSize)
    forEach(result): solution =>
      forEach(solution): (row, col) =>
        expect.all(
          row >= 1 && row <= boardSize,
          col >= 1 && col <= boardSize
        )

  test("Queens on a 4x4 board - no duplicate rows or columns"):
    val result = queens(boardSize)
    forEach(result): solution =>
      val rows = solution.map(_._1).toSet
      val cols = solution.map(_._2).toSet
      expect.all(
        rows.size == boardSize,
        cols.size == boardSize
      )

end QueensSuite
