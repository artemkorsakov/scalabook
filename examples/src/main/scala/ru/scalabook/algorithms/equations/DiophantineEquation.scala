package ru.scalabook.algorithms.equations

import scala.annotation.tailrec

/** x<sup>2</sup> – D*y<sup>2</sup> = 1 */
final case class DiophantineEquation(d: Int):

  def minimalEquation: Option[(BigInt, BigInt)] =
    val sd = math.sqrt(d.toDouble)
    if sd.isWhole then None
    else
      val a = BigInt(math.round(sd))
      val k = a.pow(2) - d

      @tailrec
      def loop(iteration: Iteration): Option[(BigInt, BigInt)] =
        if iteration.isSuccess then Some((iteration.a, iteration.b))
        else loop(iteration.getNext)

      loop(Iteration(a, BigInt(1), k, d))

  /** Is x<sup>2</sup> – D*y<sup>2</sup> = 1? */
  def isSuccess(x: BigInt, y: BigInt): Boolean =
    x.pow(2) == y.pow(2) * d + 1

end DiophantineEquation

final case class Iteration(a: BigInt, b: BigInt, k: BigInt, d: Int):
  lazy val isSuccess: Boolean = k == BigInt(1)

  /** At each iteration we find m > 0, such that k divides a + b*m and
    * \|m<sup>2</sup> − d| minimal.
    */
  private lazy val getM: BigInt =
    val last   = BigInt(math.sqrt(2.0 * d).toInt)
    val maybeM = (BigInt(1) to last)
      .filter(isMCorrect)
      .minByOption(i => (i * i - d).abs)

    @tailrec
    def loop(m: BigInt): BigInt =
      if isMCorrect(m) then m else loop(m + 1)

    maybeM.getOrElse(loop(last + 1))
  end getM

  /** Update a, b and k to (a*m + N*b)/|k|, (a + b*m)/|k|, (m<sup>2</sup> - N)/k
    */
  def getNext: Iteration =
    if isSuccess then this
    else
      val m    = getM
      val newA = (a * m + b * d) / k.abs
      val newB = (a + b * m) / k.abs
      val newK = (m * m - d) / k
      Iteration(newA, newB, newK, d)

  private def isMCorrect(m: BigInt): Boolean = (a + b * m) % k.abs == BigInt(0)

end Iteration
