package algorithms.matrix

import munit.FunSuite
import algorithms.matrix.SquaredMatrix
import algorithms.numbers.RationalNumber
import algorithms.numbers.RationalNumber.RationalNumberIsIntegral

class SquaredMatrixSuite extends FunSuite:
  test("isSymmetrical") {
    assert(!SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isSymmetrical)
    assert(SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isSymmetrical)
  }

  test("isSkewSymmetrical") {
    assert(!SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isSkewSymmetrical)
    assert(!SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isSkewSymmetrical)
    assert(SquaredMatrix(Seq(Seq(0, 1), Seq(-1, 0))).isSkewSymmetrical)
  }

  test("isUpperTriangular") {
    assert(!SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isUpperTriangular)
    assert(SquaredMatrix(Seq(Seq(2, 10), Seq(0, 3))).isUpperTriangular)
  }

  test("isLowerTriangular") {
    assert(!SquaredMatrix(Seq(Seq(2, 10), Seq(-1, 3))).isLowerTriangular)
    assert(SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isLowerTriangular)
  }

  test("isHessenbergMatrix") {
    assert(
      !SquaredMatrix(
        Seq(Seq(2, 10, 0), Seq(0, 3, 1), Seq(1, 0, 3))
      ).isHessenbergMatrix
    )
    assert(
      SquaredMatrix(
        Seq(Seq(2, 10, 0), Seq(1, 3, 1), Seq(0, 1, 3))
      ).isHessenbergMatrix
    )
  }

  test("isIdentityMatrix") {
    assert(
      !SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(1, 0, 1))
      ).isIdentityMatrix
    )
    assert(
      SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isIdentityMatrix
    )
  }

  test("isOrthogonalMatrix") {
    assert(
      !SquaredMatrix(
        Seq(Seq(2, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isOrthogonalMatrix
    )
    assert(
      SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isOrthogonalMatrix
    )
    assert(
      !SquaredMatrix(
        Seq(
          Seq(
            RationalNumber(1, 3),
            RationalNumber(1, 2),
            RationalNumber(1, 2),
            RationalNumber(1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(1, 2),
            RationalNumber(-1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(-1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(-1, 2),
            RationalNumber(1, 2)
          )
        )
      ).isOrthogonalMatrix
    )
    assert(
      SquaredMatrix(
        Seq(
          Seq(
            RationalNumber(1, 2),
            RationalNumber(1, 2),
            RationalNumber(1, 2),
            RationalNumber(1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(1, 2),
            RationalNumber(-1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(-1, 2)
          ),
          Seq(
            RationalNumber(1, 2),
            RationalNumber(-1, 2),
            RationalNumber(-1, 2),
            RationalNumber(1, 2)
          )
        )
      ).isOrthogonalMatrix
    )
  }

  test("matrixDeterminant") {
    assertEquals(SquaredMatrix(Seq(Seq(-2, -1), Seq(-1, -2))).determinant, 3)
    assertEquals(
      SquaredMatrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ).determinant,
      -8
    )
    assertEquals(
      SquaredMatrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ).trace,
      -10
    )
  }

  test("identityMatrix") {
    assertEquals(
      SquaredMatrix.identityMatrix(2),
      SquaredMatrix(Seq(Seq(1, 0), Seq(0, 1)))
    )
    assertEquals(
      SquaredMatrix.identityMatrix(3),
      SquaredMatrix(Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1)))
    )
  }

  test("isIdentityMatrix") {
    assert(SquaredMatrix.identityMatrix(2).isIdentityMatrix)
  }
