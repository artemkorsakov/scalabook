package algorithms.matrix

class HadamardMatrix(elements: Seq[Seq[Int]])(using I: Integral[Int])
    extends SquaredMatrix[Int](elements):
  require(elements.forall(row => row.forall(el => el == 1 || el == -1)))

  lazy val isHadamardMatrix: Boolean =
    val matrix = (this.transpose * this).toSquaredMatrix
    val n      = matrix.n
    val tl     = matrix.topLeft
    val els    = matrix.elements
    (0 until n).forall(i =>
      (0 until n).forall(j => els(i)(j) == (if (i == j) tl else 0))
    )

  require(isHadamardMatrix)
