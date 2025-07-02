package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.fundamental.Numerical.*
import spire.math.SafeLong

import scala.collection.mutable
import scala.annotation.tailrec

/** A Pythagorean triplet is a set of three natural numbers, a &lt; b &lt; c,
  * for which, a<sup>2</sup> + b<sup>2</sup> = c<sup>2</sup>.
  */
final case class PythagoreanTriplet(a: SafeLong, b: SafeLong, c: SafeLong):
  /** <a
    * href="https://en.wikipedia.org/wiki/Tree_of_primitive_Pythagorean_triples">Tree
    * of primitive Pythagorean triples</a>
    */
  lazy val nextPythagoreanTriplet: Array[PythagoreanTriplet] =
    Array(
      PythagoreanTriplet(
        a - 2 * b + 2 * c,
        2 * a - b + 2 * c,
        2 * a - 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        a + 2 * b + 2 * c,
        2 * a + b + 2 * c,
        2 * a + 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        -a + 2 * b + 2 * c,
        -2 * a + b + 2 * c,
        -2 * a + 2 * b + 3 * c
      )
    )
end PythagoreanTriplet

object PythagoreanTriplet:
  val primitivePythagoreanTriplet: PythagoreanTriplet =
    PythagoreanTriplet(3, 4, 5)

  /** Return all Pythagorean triplets such as a+b+c = sum.
    * @see
    *   <a href="https://projecteuler.net/overview=009">detailed description</a>
    */
  def pythagoreanTripletsWithGivenSum(sum: Long): Array[PythagoreanTriplet] =
    if sum % 2 == 1 then new Array(0)
    else
      val arrayBuffer = mutable.ArrayBuffer.empty[PythagoreanTriplet]
      val s2          = sum / 2
      val sqrt        = math.sqrt(s2.toDouble).toLong
      val mLimit      = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

      @tailrec
      def reduceLoop(n: Long, i: Long): Long =
        if n % i == 0 then reduceLoop(n / i, i) else n

      for m <- 2L to mLimit do
        if s2 % m == 0 then
          // сократим пространство поиска, удалив все делители 2
          val sm = reduceLoop(s2 / m, 2)

          @tailrec
          def loop(k: Long): Unit =
            if k >= 2 * m || k > sm then ()
            else if sm % k == 0 && gcd(k, m) == 1 then
              val d = s2 / (k * m)
              val n = k - m
              val a = d * (m * m - n * n)
              val b = 2 * d * m * n
              val c = d * (m * m + n * n)
              arrayBuffer += PythagoreanTriplet(a, b, c)
              loop(k + 2)
            else loop(k + 2)

          loop(if m % 2 == 1 then m + 2 else m + 1)

      arrayBuffer.toArray
end PythagoreanTriplet
