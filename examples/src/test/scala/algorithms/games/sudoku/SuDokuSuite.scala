package algorithms.games.sudoku

import munit.FunSuite
import algorithms.games.sudoku.SuDoku.*

class SuDokuSuite extends FunSuite:
  private val suDoku = toSuDoku(
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

  private val result: Array[Array[Int]] =
    suDoku.flatMap(_.result).getOrElse(Array.empty[Array[Int]])

  test("SuDoku.result") {
    assertEquals(result(0).toSeq, Array(4, 8, 3, 9, 2, 1, 6, 5, 7).toSeq)
    assertEquals(result(1).toSeq, Array(9, 6, 7, 3, 4, 5, 8, 2, 1).toSeq)
    assertEquals(result(2).toSeq, Array(2, 5, 1, 8, 7, 6, 4, 9, 3).toSeq)
    assertEquals(result(3).toSeq, Array(5, 4, 8, 1, 3, 2, 9, 7, 6).toSeq)
    assertEquals(result(4).toSeq, Array(7, 2, 9, 5, 6, 4, 1, 3, 8).toSeq)
    assertEquals(result(5).toSeq, Array(1, 3, 6, 7, 9, 8, 2, 4, 5).toSeq)
    assertEquals(result(6).toSeq, Array(3, 7, 2, 6, 8, 9, 5, 1, 4).toSeq)
    assertEquals(result(7).toSeq, Array(8, 1, 4, 2, 5, 3, 7, 6, 9).toSeq)
    assertEquals(result(8).toSeq, Array(6, 9, 5, 4, 1, 7, 3, 8, 2).toSeq)
  }
