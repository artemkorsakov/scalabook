package algorithms.fundamental

import scala.annotation.tailrec

object Numerical:
  /** Преобразование десятичного числа в двоичное */
  def decToBinConv(x: Int): String =
    val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
    val binList       = seqOfDivByTwo
      .takeWhile(a => a > 0)
      .map(a => a % 2)
    binList.mkString.reverse

  /** Вычисление наибольшего общего делителя посредством алгоритма Евклида. */
  @tailrec
  def gcdByEuclideanAlgorithm(a: Long, b: Long): Long =
    if b == 0 then a
    else gcdByEuclideanAlgorithm(b, a % b)

  /** Вычисление наибольшего общего делителя */
  def gcd(a: Long, b: Long): Long =
    val u = math.abs(a)
    val v = math.abs(b)
    if u == v then u
    else if u == 0 then v
    else if v == 0 then u
    else
      (~u & 1, ~v & 1) match
        case (1, 1) => gcd(u >> 1, v >> 1) << 1
        case (1, 0) => gcd(u >> 1, v)
        case (0, 1) => gcd(u, v >> 1)
        case (_, _) => if (u > v) gcd(u - v, v) else gcd(v - u, u)

  def power(a: Long, n: Long): BigInt =
    val bin        = n.toBinaryString.reverse
    val powerArray = new Array[Long](bin.length)
    powerArray(0) = a
    (1 until powerArray.length).foreach(i =>
      powerArray(i) = powerArray(i - 1) * powerArray(i - 1)
    )
    powerArray.indices.foldLeft(BigInt(1)) { (acc, i) =>
      if bin(i) == '1' then acc * powerArray(i) else acc
    }
