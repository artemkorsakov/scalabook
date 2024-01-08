package ru.scalabook.algorithms.matrix

import weaver.FunSuite
import spire.math.Rational
import spire.compat.integral

object InvertibleMatrixSuite extends FunSuite:
  test("invertibleMatrix") {
    val matrix =
      InvertibleMatrix(
        Seq(
          Seq(Rational(3), Rational(2), Rational(4)),
          Seq(Rational(4), Rational(3), Rational(6)),
          Seq(Rational(6), Rational(4), Rational(9))
        )
      )
    val invMatrix = matrix.invertibleMatrix
    val iMatrix   = matrix * invMatrix

    expect.all(
      invMatrix ==
        InvertibleMatrix(
          Seq(
            Seq(Rational(3), Rational(-2), Rational(0)),
            Seq(Rational(0), Rational(3), Rational(-2)),
            Seq(Rational(-2), Rational(0), Rational(1))
          )
        ),
      iMatrix ==
        InvertibleMatrix(
          Seq(
            Seq(Rational(1), Rational(0), Rational(0)),
            Seq(Rational(0), Rational(1), Rational(0)),
            Seq(Rational(0), Rational(0), Rational(1))
          )
        )
    )
  }

  test("solveEquation"):
    val matrix =
      InvertibleMatrix(
        Seq(
          Seq(Rational(1), Rational(5)),
          Seq(Rational(2), Rational(6))
        )
      )
    val solution = matrix.solveEquation(Seq(7, 10).map(Rational(_)))
    expect.all(solution == Seq(Rational(2), Rational(1)))
end InvertibleMatrixSuite
