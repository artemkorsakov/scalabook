package algorithms.matrix

import munit.FunSuite

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
