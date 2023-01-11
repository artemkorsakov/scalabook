package algorithms.matrix

/** Squared matrix m * n. */
class SquaredMatrix[T](elements: Seq[Seq[T]])(using I: Integral[T])
    extends Matrix[T](elements):
  require(isSquared)

  lazy val isSymmetrical: Boolean = this == transpose

  lazy val isSkewSymmetrical: Boolean = this == transpose * I.fromInt(-1)

  lazy val isUpperTriangular: Boolean =
    (0 until n).forall(i =>
      (0 until n).forall(j => j >= i || elements(i)(j) == I.zero)
    )

  lazy val isLowerTriangular: Boolean =
    (0 until n).forall(i =>
      (0 until n).forall(j => i >= j || elements(i)(j) == I.zero)
    )

  lazy val isHessenbergMatrix: Boolean =
    (0 until n).forall(i =>
      (0 until n).forall(j => j + 1 >= i || elements(i)(j) == I.zero)
    )

  lazy val isIdentityMatrix: Boolean =
    (0 until n).forall(i =>
      (0 until n).forall(j =>
        elements(i)(j) == (if i == j then I.one else I.zero)
      )
    )

  lazy val isOrthogonalMatrix: Boolean =
    (this * this.transpose).toSquaredMatrix.isIdentityMatrix

  /** <a href="https://en.wikipedia.org/wiki/Determinant">Determinant</a> of a
    * matrix.
    */
  def determinant: T =
    if elements.length == 1 then topLeft
    else
      (0 until n).foldLeft(I.zero) { (sum, i) =>
        val el  = elements.head(i)
        val mul =
          if el == I.zero then I.zero
          else
            I.times(
              el,
              SquaredMatrix(elements).minor(0, i).toSquaredMatrix.determinant
            )
        if i % 2 == 0 then I.plus(sum, mul) else I.minus(sum, mul)
      }

  lazy val trace: T = mainDiagonal.foldLeft(I.zero)(I.plus)

object SquaredMatrix:
  def identityMatrix(n: Int): SquaredMatrix[Int] =
    new SquaredMatrix[Int](
      (0 until n).map(i => (0 until n).map(j => if (i == j) 1 else 0))
    )
