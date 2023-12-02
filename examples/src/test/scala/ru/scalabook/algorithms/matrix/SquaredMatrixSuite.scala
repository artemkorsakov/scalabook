package ru.scalabook.algorithms.matrix

import ru.scalabook.algorithms.numbers.RationalNumber
import ru.scalabook.algorithms.numbers.RationalNumber.RationalNumberIsIntegral
import weaver.FunSuite

object SquaredMatrixSuite extends FunSuite:
  test("isSymmetrical"):
    expect.all(
      !SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isSymmetrical,
      SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isSymmetrical
    )

  test("isSkewSymmetrical"):
    expect.all(
      !SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isSkewSymmetrical,
      !SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isSkewSymmetrical,
      SquaredMatrix(Seq(Seq(0, 1), Seq(-1, 0))).isSkewSymmetrical
    )

  test("isUpperTriangular"):
    expect.all(
      !SquaredMatrix(Seq(Seq(2, 0), Seq(-1, 3))).isUpperTriangular,
      SquaredMatrix(Seq(Seq(2, 10), Seq(0, 3))).isUpperTriangular
    )

  test("isLowerTriangular"):
    expect.all(
      !SquaredMatrix(Seq(Seq(2, 10), Seq(-1, 3))).isLowerTriangular,
      SquaredMatrix(Seq(Seq(2, 0), Seq(0, 3))).isLowerTriangular
    )

  test("isHessenbergMatrix"):
    expect.all(
      !SquaredMatrix(
        Seq(Seq(2, 10, 0), Seq(0, 3, 1), Seq(1, 0, 3))
      ).isHessenbergMatrix,
      SquaredMatrix(
        Seq(Seq(2, 10, 0), Seq(1, 3, 1), Seq(0, 1, 3))
      ).isHessenbergMatrix
    )

  test("isIdentityMatrix"):
    expect.all(
      !SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(1, 0, 1))
      ).isIdentityMatrix,
      SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isIdentityMatrix
    )

  test("isOrthogonalMatrix"):
    expect.all(
      !SquaredMatrix(
        Seq(Seq(2, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isOrthogonalMatrix,
      SquaredMatrix(
        Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1))
      ).isOrthogonalMatrix,
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
      ).isOrthogonalMatrix,
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

  test("matrixDeterminant"):
    expect.all(
      SquaredMatrix(Seq(Seq(-2, -1), Seq(-1, -2))).determinant == 3,
      SquaredMatrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ).determinant == -8,
      SquaredMatrix(
        Seq(
          Seq(-2, -1, -1, -4),
          Seq(-1, -2, -1, -6),
          Seq(-1, -1, 2, 4),
          Seq(2, 1, -3, -8)
        )
      ).trace == -10
    )

  test("identityMatrix"):
    expect.all(
      SquaredMatrix.identityMatrix(2) ==
        SquaredMatrix(Seq(Seq(1, 0), Seq(0, 1))),
      SquaredMatrix.identityMatrix(3) ==
        SquaredMatrix(Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(0, 0, 1)))
    )

  test("isIdentityMatrix"):
    expect.all(SquaredMatrix.identityMatrix(2).isIdentityMatrix)
end SquaredMatrixSuite
