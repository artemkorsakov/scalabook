package ru.scalabook.algorithms.numbers

import spire.math.SafeLong

/** A Pythagorean triplet is a set of three natural numbers, a &lt; b &lt; c,
  * for which, a<sup>2</sup> + b<sup>2</sup> = c<sup>2</sup>, a=m<sup>2</sup> -
  * n<sup>2</sup>, b=2*m*n, c=m<sup>2</sup> + n<sup>2</sup>. (m,n) - Pythagorean
  * deuce.
  */
final case class PythagoreanDeuce(m: SafeLong, n: SafeLong):
  lazy val a: SafeLong                 = m * m - n * n
  lazy val b: SafeLong                 = 2 * m * n
  lazy val c: SafeLong                 = m * m + n * n
  lazy val triplet: PythagoreanTriplet = PythagoreanTriplet(a, b, c)

  /** <a
    * href="https://en.wikipedia.org/wiki/Tree_of_primitive_Pythagorean_triples">Tree
    * of primitive Pythagorean triples</a>
    */
  lazy val nextPythagoreanDeuce: Array[PythagoreanDeuce] =
    Array(
      PythagoreanDeuce(2 * m - n, m),
      PythagoreanDeuce(2 * m + n, m),
      PythagoreanDeuce(m + 2 * n, n)
    )
end PythagoreanDeuce

object PythagoreanDeuce:
  val primitivePythagoreanDeuce: PythagoreanDeuce = PythagoreanDeuce(2, 1)
