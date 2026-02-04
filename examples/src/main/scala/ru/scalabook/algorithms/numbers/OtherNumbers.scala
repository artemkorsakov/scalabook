package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.combinatorics.BinomialCoefficient.*
import scalaz.Memo
import spire.compat.fractional
import spire.math.Rational

import scala.annotation.tailrec

object OtherNumbers:
  /** <a href="https://en.wikipedia.org/wiki/Bernoulli_number">Bernoulli
    * number</a>
    */
  val bernoulli: Int => Rational =
    Memo.mutableHashMapMemo {
      case 0 => Rational(1)
      case 1 => Rational(-1, 2)
      case n if n % 2 == 1 => Rational(0)
      case n =>
        val s = (2 to n by 2)
          .map: l =>
            bernoulli(n - l) * Rational(binomialCoefficient(n + 1, l + 1))
          .sum
        Rational(1, 2) - (s / (n + 1))
    }

  /** <a href="https://oeis.org/A000931">Padovan sequence (or Padovan
    * numbers)</a> : a<sub>n</sub> = a<sub>n-2</sub> + a<sub>n-3</sub> with
    * a<sub>0</sub> = 1, a<sub>1</sub> = a<sub>2</sub> = 0.
    */
  def padovan(n: Int): BigInt =
    @tailrec
    def loop(a: BigInt, b: BigInt, c: BigInt, i: Int): BigInt =
      if i == 0 then c else loop(b, c, a + b, i - 1)

    if n == 0 then 1
    else if n <= 2 then 0
    else loop(1, 0, 0, n - 2)

  /** <a href="https://oeis.org/A001045">Jacobsthal sequence (or Jacobsthal
    * numbers)</a>: a<sub>n</sub> = a<sub>n-1</sub> + 2*a<sub>n-2</sub> with
    * a<sub>0</sub> = 0, a<sub>1</sub> = 1.
    *
    * Also a<sub>n</sub> = nearest integer to 2<sup>n</sup>/3.
    */
  def jacobsthal(i: Int): BigInt =
    @tailrec
    def loop(a: BigInt, b: BigInt, i: Int): BigInt =
      if i == 0 then b
      else loop(b, b + 2 * a, i - 1)

    if i <= 1 then i
    else loop(0, 1, i - 1)

  /** <a href="https://oeis.org/A000129">Pell numbers</a> : a<sub>n</sub> =
    * 2*a<sub>n-1</sub> + a<sub>n-2</sub>, a<sub>0</sub> = 0, a<sub>1</sub> = 1.
    */
  def pell(i: Int): BigInt =
    @tailrec
    def loop(a: BigInt, b: BigInt, i: Int): BigInt =
      if i == 0 then b
      else loop(b, 2 * b + a, i - 1)

    if i <= 1 then i
    else loop(0, 1, i - 1)

  /** <a href="https://oeis.org/A000073">Tribonacci numbers</a> : a<sub>n</sub>
    * \= a<sub>n-1</sub> + a<sub>n-2</sub> + a<sub>n-3</sub> with a<sub>0</sub>
    * \= a<sub>1</sub> = 0, a<sub>2</sub> = 1.
    */
  def tribonacci(i: Int): BigInt =
    @tailrec
    def loop(a: BigInt, b: BigInt, c: BigInt, i: Int): BigInt =
      if i == 0 then c
      else loop(b, c, a + b + c, i - 1)

    if i <= 1 then 0
    else if i == 2 then 1
    else loop(0, 0, 1, i - 2)

  /** <a href="https://oeis.org/A000078">Tetranacci numbers</a> : a<sub>n</sub>
    * \= a<sub>n-1</sub> + a<sub>n-2</sub> + a<sub>n-3</sub> + a<sub>n-4</sub>
    * with a<sub>0</sub> = a<sub>1</sub> = a<sub>2</sub> = 0, a<sub>3</sub> = 1.
    */
  def tetranacci(i: Int): BigInt =
    @tailrec
    def loop(
        a: BigInt,
        b: BigInt,
        c: BigInt,
        d: BigInt,
        i: Int
    ): BigInt =
      if i == 0 then d
      else loop(b, c, d, a + b + c + d, i - 1)

    if i <= 2 then 0
    else if i == 3 then 1
    else loop(0, 0, 0, 1, i - 3)
end OtherNumbers
