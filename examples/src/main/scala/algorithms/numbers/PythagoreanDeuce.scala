package algorithms.numbers

import algorithms.matrix.Matrix

/** A Pythagorean triplet is a set of three natural numbers, a &lt; b &lt; c,
  * for which, a<sup>2</sup> + b<sup>2</sup> = c<sup>2</sup>, a=m<sup>2</sup> -
  * n<sup>2</sup>, b=2*m*n, c=m<sup>2</sup> + n<sup>2</sup>. (m,n) - Pythagorean
  * deuce.
  */
case class PythagoreanDeuce(m: Long, n: Long):
  lazy val a: Long                        = m * m - n * n
  lazy val b: Long                        = 2 * m * n
  lazy val c: Long                        = m * m + n * n
  lazy val triplet: PythagoreanTriplet    = PythagoreanTriplet(a, b, c)
  lazy val columnVector: Matrix[Long]     = Matrix(Seq(Seq(m), Seq(n)))
  private lazy val a_matrix: Matrix[Long] = Matrix(Seq(Seq(2, -1), Seq(1, 0)))
  private lazy val b_matrix: Matrix[Long] = Matrix(Seq(Seq(2, 1), Seq(1, 0)))
  private lazy val c_matrix: Matrix[Long] = Matrix(Seq(Seq(1, 2), Seq(0, 1)))
  private lazy val matrices               = Seq(a_matrix, b_matrix, c_matrix)

  /** <a
    * href="https://en.wikipedia.org/wiki/Tree_of_primitive_Pythagorean_triples">Tree
    * of primitive Pythagorean triples</a>
    */
  def nextPythagoreanDeuce: Seq[PythagoreanDeuce] =
    matrices.map { mat =>
      val seq = mat.*(columnVector).elements
      PythagoreanDeuce(seq.head.head, seq.last.head)
    }

object PythagoreanDeuce:
  val primitivePythagoreanDeuce: PythagoreanDeuce = PythagoreanDeuce(2, 1)
