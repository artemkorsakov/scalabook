package ru.scalabook.algorithms.matrix

import spire.math.Rational

class InvertibleMatrix(elements: Seq[Seq[Rational]])(using
    I: Integral[Rational]
) extends SquaredMatrix[Rational](elements):
  val det: Rational = determinant
  require(det != Rational(0))

  lazy val invertibleMatrix: InvertibleMatrix =
    InvertibleMatrix(
      (0 until n).map(i =>
        (0 until n).map { j =>
          val sign = if (i + j) % 2 == 0 then 1 else -1
          Rational(sign) * minor(j, i).toSquaredMatrix.determinant / det
        }
      )
    )

  def solveEquation(xs: Seq[Rational]): Seq[Rational] =
    require(xs.length == n)
    xs.indices.map { k =>
      val matrix = xs.indices.map { i =>
        (elements(i).take(k) :+ xs(i)) ++ elements(i).drop(k + 1)
      }
      InvertibleMatrix(matrix).determinant / det
    }
end InvertibleMatrix
