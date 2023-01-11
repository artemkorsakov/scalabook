package algorithms.matrix

import munit.FunSuite
import algorithms.combinatorics.BinomialCoefficient
import algorithms.matrix.{Matrix, Vector}

class MatrixSuite extends FunSuite:
  test("rows, columns, diagonals") {
    val matrix = Matrix(Seq(Seq(1, 2), Seq(3, 4), Seq(5, 6)))
    assertEquals(matrix.rows, Seq(Seq(1, 2), Seq(3, 4), Seq(5, 6)))
    assertEquals(matrix.columns, Seq(Seq(1, 3, 5), Seq(2, 4, 6)))
    assertEquals(matrix.diagonals, Seq(Seq(1, 4), Seq(2), Seq(3, 6), Seq(5)))
    assertEquals(matrix.oppDiagonals, Seq(Seq(1), Seq(2, 3), Seq(4, 5), Seq(6)))
  }

  test("mainDiagonal") {
    assertEquals(Matrix(Seq(Seq(1, 2))).mainDiagonal, Seq(1))
    assertEquals(Matrix(Seq(Seq(1, 2), Seq(3, 4))).mainDiagonal, Seq(1, 4))
    assertEquals(
      Matrix(Seq(Seq(1, 2), Seq(3, 4), Seq(3, 4))).mainDiagonal,
      Seq(1, 4)
    )
  }

  test("isSquared") {
    assertEquals(Matrix(Seq(Seq(2, 0), Seq(-1, 3), Seq(7, 5))).isSquared, false)
    assertEquals(Matrix(Seq(Seq(2, 0), Seq(-1, 3))).isSquared, true)
  }

  test("matrixTranspose") {
    Seq(
      (Seq(Seq(1, 2)), Seq(Seq(1), Seq(2))),
      (Seq(Seq(1, 2), Seq(3, 4)), Seq(Seq(1, 3), Seq(2, 4))),
      (
        Seq(Seq(1, 2), Seq(3, 4), Seq(5, 6)),
        Seq(Seq(1, 3, 5), Seq(2, 4, 6))
      ),
      (
        Seq(Seq(1.1, 2.2), Seq(3.3, 4.4)),
        Seq(Seq(1.1, 3.3), Seq(2.2, 4.4))
      ),
      (
        Seq(
          Seq(BigInt(1), BigInt(2)),
          Seq(BigInt(3), BigInt(4)),
          Seq(BigInt(5), BigInt(6))
        ),
        Seq(
          Seq(BigInt(1), BigInt(3), BigInt(5)),
          Seq(BigInt(2), BigInt(4), BigInt(6))
        )
      ),
      (
        Seq(Seq(1L, 2L), Seq(3L, 4L), Seq(5L, 6L)),
        Seq(Seq(1L, 3L, 5L), Seq(2L, 4L, 6L))
      )
    ).foreach { case (matrix1, matrix2) =>
      assertEquals(matrix1.transpose, matrix2)
      assertEquals(matrix2.transpose, matrix1)
      assertEquals(matrix1.transpose.transpose, matrix1)
      assertEquals(matrix2.transpose.transpose, matrix2)
    }
  }

  private val matrix = Matrix(
    Seq(
      Seq(-2, -1, -1, -4),
      Seq(-1, -2, -1, -6),
      Seq(-1, -1, 2, 4),
      Seq(2, 1, -3, -8)
    )
  )

  test("minorMatrix") {
    assertEquals(
      matrix.minor(0, 0),
      Matrix(Seq(Seq(-2, -1, -6), Seq(-1, 2, 4), Seq(1, -3, -8)))
    )
    assertEquals(
      matrix.minor(0, 1),
      Matrix(Seq(Seq(-1, -1, -6), Seq(-1, 2, 4), Seq(2, -3, -8)))
    )
    assertEquals(
      matrix.minor(0, 2),
      Matrix(Seq(Seq(-1, -2, -6), Seq(-1, -1, 4), Seq(2, 1, -8)))
    )
    assertEquals(
      matrix.minor(0, 3),
      Matrix(Seq(Seq(-1, -2, -1), Seq(-1, -1, 2), Seq(2, 1, -3)))
    )
    assertEquals(
      matrix.minor(1, 0),
      Matrix(Seq(Seq(-1, -1, -4), Seq(-1, 2, 4), Seq(1, -3, -8)))
    )
    assertEquals(
      matrix.minor(1, 1),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, 2, 4), Seq(2, -3, -8)))
    )
    assertEquals(
      matrix.minor(1, 2),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, -1, 4), Seq(2, 1, -8)))
    )
    assertEquals(
      matrix.minor(1, 3),
      Matrix(Seq(Seq(-2, -1, -1), Seq(-1, -1, 2), Seq(2, 1, -3)))
    )
    assertEquals(
      matrix.minor(2, 0),
      Matrix(Seq(Seq(-1, -1, -4), Seq(-2, -1, -6), Seq(1, -3, -8)))
    )
    assertEquals(
      matrix.minor(2, 1),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, -1, -6), Seq(2, -3, -8)))
    )
    assertEquals(
      matrix.minor(2, 2),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, -2, -6), Seq(2, 1, -8)))
    )
    assertEquals(
      matrix.minor(2, 3),
      Matrix(Seq(Seq(-2, -1, -1), Seq(-1, -2, -1), Seq(2, 1, -3)))
    )
    assertEquals(
      matrix.minor(3, 0),
      Matrix(Seq(Seq(-1, -1, -4), Seq(-2, -1, -6), Seq(-1, 2, 4)))
    )
    assertEquals(
      matrix.minor(3, 1),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, -1, -6), Seq(-1, 2, 4)))
    )
    assertEquals(
      matrix.minor(3, 2),
      Matrix(Seq(Seq(-2, -1, -4), Seq(-1, -2, -6), Seq(-1, -1, 4)))
    )
    assertEquals(
      matrix.minor(3, 3),
      Matrix(Seq(Seq(-2, -1, -1), Seq(-1, -2, -1), Seq(-1, -1, 2)))
    )
  }

  test("add") {
    assertEquals(
      Matrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ) +
        Matrix(
          Seq(
            Seq(8, -5, -6, -4),
            Seq(-13, -22, -11, -65),
            Seq(45, 45, 34, 35),
            Seq(23, 12, -33, -82)
          )
        ),
      Matrix(
        Seq(
          Seq(6, -6, -7, -8),
          Seq(-14, -24, -12, -71),
          Seq(44, 44, 36, 39),
          Seq(25, 13, -36, -90)
        )
      )
    )
  }

  test("matrix multiplication by number") {
    assertEquals(
      Matrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ) * 10,
      Matrix(
        Seq(
          Seq(-20, -10, -10, -40),
          Seq(-10, -20, -10, -60),
          Seq(-10, -10, 20, 40),
          Seq(20, 10, -30, -80)
        )
      )
    )
  }

  test("matrix multiplication") {
    assertEquals(
      Matrix(Seq(Seq(3, 4, 2, 5), Seq(0, -1, 3, 2), Seq(1, 2, 3, 0))) *
        Matrix(
          Seq(Seq(1, 2, 3), Seq(-3, 5, 4), Seq(6, 2, 1), Seq(1, -1, 0))
        ),
      Matrix(Seq(Seq(8, 25, 27), Seq(23, -1, -1), Seq(13, 18, 14)))
    )
    assertEquals(
      Matrix(Seq(Seq(3, -1, -1), Seq(2, 0, 1), Seq(1, 1, 1))) *
        Matrix(Seq(Seq(2, 1), Seq(-1, 0), Seq(0, 1))),
      Matrix(Seq(Seq(7, 2), Seq(4, 3), Seq(1, 2)))
    )
  }

  test("matrix multiplication by row") {
    assertEquals(
      Matrix(Seq(Seq(3, 4, 2, 5), Seq(0, -1, 3, 2), Seq(1, 2, 3, 0))) *
        Vector(Seq(1, -3, 6, 1)),
      Vector(Seq(8, 23, 13))
    )
  }

  test("power") {
    assertEquals(
      Matrix(Seq(Seq(2, 0), Seq(-1, 3))).power(2),
      Matrix(Seq(Seq(4, 0), Seq(-5, 9)))
    )
    assertEquals(
      Matrix(Seq(Seq(1, 1), Seq(1, 0))).power(20),
      Matrix(Seq(Seq(10946, 6765), Seq(6765, 4181)))
    )
    assertEquals(
      Matrix(
        Seq(
          Seq(1, 2, 1, 0),
          Seq(1, 1, 0, -1),
          Seq(-2, 0, 1, 2),
          Seq(0, 2, 1, 1)
        )
      ).power(100),
      Matrix(
        Seq(
          Seq(1, 200, 100, 0),
          Seq(100, 1, 0, -100),
          Seq(-200, 0, 1, 200),
          Seq(0, 200, 100, 1)
        )
      )
    )
    assertEquals(
      Matrix(Seq(Seq(1, 1), Seq(1, 0))).power(50, 1000000),
      Matrix(Seq(Seq(11074, 269025), Seq(269025, 742049)))
    )
  }

  test("concatenation") {
    assertEquals(
      Matrix(Seq(Seq(2, 0), Seq(-1, 3), Seq(7, 5))).concatenation(
        Matrix(Seq(Seq(1, 2, 3, 4), Seq(5, 6, 7, 8), Seq(3, 4, 2, 6)))
      ),
      Matrix(
        Seq(
          Seq(2, 0, 1, 2, 3, 4),
          Seq(-1, 3, 5, 6, 7, 8),
          Seq(7, 5, 3, 4, 2, 6)
        )
      )
    )
  }

  test("vectorization") {
    assertEquals(
      Matrix(Seq(Seq(2, 0), Seq(-1, 3), Seq(7, 5))).vectorization,
      Seq(2, -1, 7, 0, 3, 5)
    )
  }
