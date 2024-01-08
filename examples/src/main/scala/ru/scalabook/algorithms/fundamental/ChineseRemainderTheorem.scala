package ru.scalabook.algorithms.fundamental

import cats.syntax.traverse.*
import ru.scalabook.algorithms.fundamental.Numerical.*
import spire.compat.integral
import spire.math.SafeLong

/** Chinese remainder theorem.
  *
  * @see
  *   <a href="https://en.wikipedia.org/wiki/Chinese_remainder_theorem">Chinese
  *   remainder theorem</a>
  */
object ChineseRemainderTheorem:

  /** Return n, such than n % a<sub>i</sub> = r<sub>i</sub>.
    */
  def solution(
      aArray: Array[SafeLong],
      rArray: Array[SafeLong]
  ): Option[SafeLong] =
    val m      = aArray.product         // Step 1
    val mArray = aArray.map(a => m / a) // Step 2
    val maybeMMinus1Array =
      mArray.indices.toList.traverse: i => // Step 3
        gcdInverse(mArray(i), aArray(i))
    maybeMMinus1Array.map: mMinus1Array =>
      mArray.indices.foldLeft(SafeLong(0)): (x, i) => // Step 4
        x + (((rArray(i) * mArray(i)) % m) * mMinus1Array(i)) % m
end ChineseRemainderTheorem
