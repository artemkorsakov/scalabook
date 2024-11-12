import scala.collection.mutable.ListBuffer
def generalizedEuclideanAlgorithm(m: Long,n: Long): (Long, Long, Long) =
  @annotation.tailrec
  def loop(
      aa: Long,
      b: Long,
      a: Long,
      bb: Long,
      c: Long,
      d: Long
  ): (Long, Long, Long) =
    val r = c % d
    if r == 0 then (d, a, b)
    else
      val q = c / d
      loop(aa = a, b = bb - q * b, a = aa - q * a, bb = b, c = d, d = r)

  loop(aa = 1, b = 1, a = 0, bb = 0, c = m, d = n)
end generalizedEuclideanAlgorithm

def gcdInverse(m: Long, n: Long): Option[Long] =
  val (d, a, _) = generalizedEuclideanAlgorithm(m, n)
  if d == 1 then Some((a % n + n) % n) else None

object ChineseRemainderTheorem:
  def solution(aArray: Array[Long],rArray: Array[Long]): Option[Long] =
    val m      = aArray.product         // Шаг 1
    val mArray = aArray.map(a => m / a) // Шаг 2
    val maybeMMinus1Array =
      mArray.indices.foldLeft(Option(ListBuffer.empty[Long])): (acc, i) => // Шаг 3
        (acc, gcdInverse(mArray(i), aArray(i))) match
          case (Some(list), Some(mMinus)) => Some(list :+ mMinus)
          case _ => None
    maybeMMinus1Array.map: mMinus1Array =>
      mArray.indices.foldLeft(0L): (x, i) => // Шаг 4
        x + (((rArray(i) * mArray(i)) % m) * mMinus1Array(i)) % m

import ChineseRemainderTheorem.* 

println(solution(Array(2, 3), Array(1, 2)))
// 5, потому что 5 % 2 = 1, 5 % 3 = 2

println(solution(Array(707, 527), Array(0, 5)))