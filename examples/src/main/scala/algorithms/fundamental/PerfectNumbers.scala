package algorithms.fundamental

import algorithms.fundamental.Numerical.*
import algorithms.fundamental.PerfectNumbersType.*

object PerfectNumbers:

  /** Return numberType:
    *
    * Perfect: a perfect number is a positive integer that is equal to the sum
    * of its positive divisors, excluding the number itself.
    *
    * Deficient: a deficient number is a positive integer that is less to the
    * sum of its positive divisors, excluding the number itself.
    *
    * Abundant: an abundant number is a positive integer that is more to the sum
    * of its positive divisors, excluding the number itself.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Perfect_number">Perfect
    *   number</a>
    */
  def perfectNumbersType(n: Long): PerfectNumbersType =
    val sum = sumOfDivisors(n) - n
    if sum == n then Perfect
    else if sum < n then Deficient
    else Abundant

enum PerfectNumbersType:
  case Perfect, Deficient, Abundant
