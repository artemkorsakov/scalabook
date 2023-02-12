package algorithms.fundamental

import scala.annotation.tailrec
import algorithms.fundamental.Primes.primeFactorsWithPow

object Numerical:
  private val delta: Double = 0.001

  /** Преобразование десятичного числа в двоичное */
  def decToBinConv(x: Int): String =
    val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
    val binList = seqOfDivByTwo
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

  /** Return the greatest common divisor.
    */
  def gcd(a: BigInt, b: BigInt): BigInt =
    val u = if a < 0 then -a else a
    val v = if b < 0 then -b else b
    if u == v then u
    else if u == 0 then v
    else if v == 0 then u
    else
      ((~u & 1).toInt, (~v & 1).toInt) match
        case (1, 1) => gcd(u >> 1, v >> 1) << 1
        case (1, 0) => gcd(u >> 1, v)
        case (0, 1) => gcd(u, v >> 1)
        case (_, _) => if (u > v) gcd(u - v, v) else gcd(v - u, u)

  /** Extended Euclidean algorithm.
    *
    * @see
    *   <a
    *   href="https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm">detailed
    *   description</a>
    */
  def gcdex(a: BigInt, b: BigInt): (BigInt, BigInt, BigInt) =
    if a == 0 then (b, BigInt(0), BigInt(1))
    else
      val temp = gcdex(b % a, a)
      (temp._1, temp._3 - (b / a) * temp._2, temp._2)

  /** Modular multiplicative inverse.
    *
    * @see
    *   <a
    *   href="https://en.wikipedia.org/wiki/Modular_multiplicative_inverse">detailed
    *   description</a>
    */
  def gcdInverse(a: BigInt, m: BigInt): BigInt =
    val extraEuclid = gcdex(a, m)
    if extraEuclid._1 == 1 then (extraEuclid._2 % m + m) % m
    else -1

  /** Возведение в степень */
  def power(a: Long, n: Long): BigInt =
    @tailrec
    def loop(base: BigInt, power: Long, acc: BigInt): BigInt =
      if power == 0 then acc
      else if power % 2 == 0 then loop(base * base, power / 2, acc)
      else loop(base, power - 1, base * acc)
    loop(a, n, 1)

  /** Нахождения квадратного корня */
  def sqrt(x: Double): Double =
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

  /** Return the sum of the divisors of n.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Divisor_function">detailed
    *   description</a>
    */
  def sumOfDivisors(number: Long): BigInt =
    val primeDivisors = primeFactorsWithPow(number)
    primeDivisors.keySet.foldLeft(BigInt(1)) { (mul, prime) =>
      val num = BigInt(prime).pow(primeDivisors(prime).toInt + 1) - 1
      val den = BigInt(prime) - 1
      mul * (num / den)
    }

  /** Return the count of divisors of n.
    */
  def countOfDivisors(number: Long): Long =
    primeFactorsWithPow(number).values.foldLeft(1L)((mul, a) => mul * (a + 1))

  /** Returns the sum of numbers from 1 to a given.
    */
  def sumToGiven(n: Long): Long = n * (n + 1) / 2

  /** Returns the sum of squares of numbers from 1 to a given limit, inclusive.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Square_number">description</a>
    */
  def sumOfSquaresTo(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

  /** Returns the sum of cubes first natural numbers.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Cube_(algebra)">description</a>
    */
  def sumOfCubesTo(n: Long): Long =
    val s = sumToGiven(n)
    s * s
