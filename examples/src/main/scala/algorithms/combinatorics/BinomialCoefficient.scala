package algorithms.combinatorics

import scala.annotation.tailrec

object BinomialCoefficient:

  /** Биномиальный коэффициент из n по k.
    *
    * @see
    *   <a
    *   href="https://ru.wikipedia.org/wiki/%D0%91%D0%B8%D0%BD%D0%BE%D0%BC%D0%B8%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9_%D0%BA%D0%BE%D1%8D%D1%84%D1%84%D0%B8%D1%86%D0%B8%D0%B5%D0%BD%D1%82">wikipedia</a>
    */
  @tailrec
  def binomialCoefficient(n: Int, k: Int): BigInt =
    if k < 0 || k > n then BigInt(0)
    else if 2 * k > n then binomialCoefficient(n, n - k)
    else
      val (num, den) = (0 until k).foldLeft((BigInt(1), BigInt(1))) {
        case ((num, den), i) =>
          (num * BigInt(n - i), den * BigInt(k - i))
      }
      num / den
