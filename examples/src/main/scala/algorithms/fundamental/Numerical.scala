package algorithms.fundamental

import scala.annotation.tailrec

object Numerical:
  private val delta: Double = 0.001

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

  /** Возведение в степень */
  def power(a: Long, n: Long): BigInt =
    @tailrec
    def loop(base: BigInt, power: Long, acc: BigInt): BigInt =
      if power == 0 then acc
      else if power % 2 == 0 then loop(base * base, power / 2, acc)
      else loop(base, power - 1, base * acc)
    loop(a, n, 1)

  /** Нахождения квадратного корня */
  def sqrt(x: Double): Double       =
    def isGoodEnough(guess: Double): Boolean =
      math.abs(guess * guess - x) < delta

    def improve(guess: Double): Double = (guess + x / guess) / 2

    def sqrtIter(guess: Double): Double =
      if isGoodEnough(guess) then guess
      else sqrtIter(improve(guess))

    sqrtIter(1.0)

    /** Нахождения кубического корня */
  def cubeRootOf(x: Double): Double =
    def isGoodEnough(guess: Double): Boolean =
      math.abs(guess * guess * guess - x) < delta

    def improve(guess: Double): Double = (x / (guess * guess) + 2 * guess) / 3

    def sqrtIter(guess: Double): Double =
      if isGoodEnough(guess) then guess
      else sqrtIter(improve(guess))

    sqrtIter(1.0)
