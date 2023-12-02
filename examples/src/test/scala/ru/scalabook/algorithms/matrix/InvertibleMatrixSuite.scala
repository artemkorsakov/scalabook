package ru.scalabook.algorithms.matrix

import ru.scalabook.algorithms.numbers.RationalNumber
import ru.scalabook.algorithms.numbers.RationalNumber.RationalNumberIsIntegral
import weaver.FunSuite

object InvertibleMatrixSuite extends FunSuite:
  test("invertibleMatrix") {
    val matrix =
      InvertibleMatrix(
        Seq(
          Seq(RationalNumber(3), RationalNumber(2), RationalNumber(4)),
          Seq(RationalNumber(4), RationalNumber(3), RationalNumber(6)),
          Seq(RationalNumber(6), RationalNumber(4), RationalNumber(9))
        )
      )
    val invMatrix = matrix.invertibleMatrix
    val iMatrix   = matrix * invMatrix

    expect.all(
      invMatrix ==
        InvertibleMatrix(
          Seq(
            Seq(RationalNumber(3), RationalNumber(-2), RationalNumber(0)),
            Seq(RationalNumber(0), RationalNumber(3), RationalNumber(-2)),
            Seq(RationalNumber(-2), RationalNumber(0), RationalNumber(1))
          )
        ),
      iMatrix ==
        InvertibleMatrix(
          Seq(
            Seq(RationalNumber(1), RationalNumber(0), RationalNumber(0)),
            Seq(RationalNumber(0), RationalNumber(1), RationalNumber(0)),
            Seq(RationalNumber(0), RationalNumber(0), RationalNumber(1))
          )
        )
    )
  }

  test("solveEquation"):
    val matrix =
      InvertibleMatrix(
        Seq(
          Seq(RationalNumber(1), RationalNumber(5)),
          Seq(RationalNumber(2), RationalNumber(6))
        )
      )
    val solution = matrix.solveEquation(Seq(7, 10).map(RationalNumber(_)))
    expect.all(solution == Seq(RationalNumber(2), RationalNumber(1)))
end InvertibleMatrixSuite
