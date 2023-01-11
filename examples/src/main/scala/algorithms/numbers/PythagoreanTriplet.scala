package algorithms.numbers

import algorithms.fundamental.Numerical
import algorithms.fundamental.Numerical.*
import algorithms.matrix.Matrix

import scala.collection.mutable

/** A Pythagorean triplet is a set of three natural numbers, a &lt; b &lt; c,
  * for which, a<sup>2</sup> + b<sup>2</sup> = c<sup>2</sup>.
  */
case class PythagoreanTriplet(a: Long, b: Long, c: Long):
  lazy val columnVector: Matrix[Long]     = Matrix(Seq(Seq(a), Seq(b), Seq(c)))
  private lazy val a_matrix: Matrix[Long] = Matrix(
    Seq(Seq(1, -2, 2), Seq(2, -1, 2), Seq(2, -2, 3))
  )
  private lazy val b_matrix: Matrix[Long] = Matrix(
    Seq(Seq(1, 2, 2), Seq(2, 1, 2), Seq(2, 2, 3))
  )
  private lazy val c_matrix: Matrix[Long] = Matrix(
    Seq(Seq(-1, 2, 2), Seq(-2, 1, 2), Seq(-2, 2, 3))
  )
  private lazy val matrices               = IndexedSeq(a_matrix, b_matrix, c_matrix)

  /** <a
    * href="https://en.wikipedia.org/wiki/Tree_of_primitive_Pythagorean_triples">Tree
    * of primitive Pythagorean triples</a>
    */
  def nextPythagoreanTriplet: IndexedSeq[PythagoreanTriplet] =
    matrices.map { mat =>
      val seq = mat.*(columnVector).elements
      PythagoreanTriplet(seq.head.head, seq(1).head, seq.last.head)
    }

object PythagoreanTriplet:
  val primitivePythagoreanTriplet: PythagoreanTriplet =
    PythagoreanTriplet(3, 4, 5)

  /** Return all Pythagorean triplets such as a+b+c = sum.
    * @see
    *   <a href="https://projecteuler.net/overview=009">detailed description</a>
    */
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def pythagoreanTripletsWithGivenSum(sum: Long): Seq[PythagoreanTriplet] =
    if sum % 2 == 1 then Seq.empty[PythagoreanTriplet]
    else
      val arrayBuffer = mutable.ArrayBuffer.empty[PythagoreanTriplet]
      val s2          = sum / 2
      val sqrt        = math.sqrt(s2.toDouble).toLong
      val mLimit      = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

      for (m <- 2L to mLimit)
        if (s2 % m == 0) {
          var sm = s2 / m
          while sm % 2 == 0 do // reduce the search space by
            sm /= 2 // removing all factors 2
          var k = if m % 2 == 1 then m + 2 else m + 1
          while k < 2 * m && k <= sm
          do
            if sm % k == 0 && Numerical.gcd(k, m) == 1 then
              val d = s2 / (k * m)
              val n = k - m
              val a = d * (m * m - n * n)
              val b = 2 * d * m * n
              val c = d * (m * m + n * n)
              arrayBuffer += PythagoreanTriplet(a, b, c)
            k += 2
        }

      arrayBuffer.toSeq
