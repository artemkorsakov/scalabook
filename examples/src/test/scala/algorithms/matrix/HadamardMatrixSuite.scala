package algorithms.matrix

import munit.FunSuite
import algorithms.matrix.HadamardMatrix

class HadamardMatrixSuite extends FunSuite:
  test("isHadamardMatrix") {
    assert(HadamardMatrix(Seq(Seq(1, 1), Seq(1, -1))).isHadamardMatrix)
    assert(
      HadamardMatrix(
        Seq(
          Seq(1, 1, 1, 1),
          Seq(1, -1, 1, -1),
          Seq(1, 1, -1, -1),
          Seq(1, -1, -1, 1)
        )
      ).isHadamardMatrix
    )
  }
