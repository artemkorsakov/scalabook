package algorithms.matrix

import algorithms.numbers.RationalNumber

class InvertibleMatrix(elements: Seq[Seq[RationalNumber]])(using
    I: Integral[RationalNumber]
) extends SquaredMatrix[RationalNumber](elements):
  val det: RationalNumber = determinant
  require(det != RationalNumber(0))

  lazy val invertibleMatrix: InvertibleMatrix =
    InvertibleMatrix(
      (0 until n).map(i =>
        (0 until n).map { j =>
          val sign = if (i + j) % 2 == 0 then 1 else -1
          RationalNumber(sign) * minor(j, i).toSquaredMatrix.determinant / det
        }
      )
    )

  def solveEquation(xs: Seq[RationalNumber]): Seq[RationalNumber] =
    require(xs.length == n)
    xs.indices.map { k =>
      val matrix = xs.indices.map { i =>
        (elements(i).take(k) :+ xs(i)) ++ elements(i).drop(k + 1)
      }
      InvertibleMatrix(matrix).determinant / det
    }
