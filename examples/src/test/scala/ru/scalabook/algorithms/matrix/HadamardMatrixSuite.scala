package ru.scalabook.algorithms.matrix

import weaver.FunSuite

object HadamardMatrixSuite extends FunSuite:
  test("isHadamardMatrix"):
    expect.all(
      HadamardMatrix(Seq(Seq(1, 1), Seq(1, -1))).isHadamardMatrix,
      HadamardMatrix(
        Seq(
          Seq(1, 1, 1, 1),
          Seq(1, -1, 1, -1),
          Seq(1, 1, -1, -1),
          Seq(1, -1, -1, 1)
        )
      ).isHadamardMatrix
    )
