package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Primes.primeFactorsWithPow

import scala.annotation.tailrec

object Numerical:
  private val delta: Double = 1e-5

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

  /** Обобщенный алгоритм Евклида. */
  def generalizedEuclideanAlgorithm(m: Long, n: Long): (Long, Long, Long) =
    @tailrec
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
        case (_, _) => if u > v then gcd(u - v, v) else gcd(v - u, u)

  /** Вычисление наибольшего общего делителя. */
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
        case (_, _) => if u > v then gcd(u - v, v) else gcd(v - u, u)

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

  /** Вычисление наименьшего общего кратного двух чисел */
  def lcm(x: Long, y: Long): Long = math.abs(x * y) / gcd(x, y)

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

    @tailrec
    def sqrtIter(guess: Double): Double =
      if isGoodEnough(guess) then guess
      else sqrtIter(improve(guess))

    sqrtIter(1.0)

    /** Нахождения кубического корня */
  def cubeRootOf(x: Double): Double =
    def isGoodEnough(guess: Double): Boolean =
      math.abs(guess * guess * guess - x) < delta

    def improve(guess: Double): Double = (x / (guess * guess) + 2 * guess) / 3

    @tailrec
    def sqrtIter(guess: Double): Double =
      if isGoodEnough(guess) then guess
      else sqrtIter(improve(guess))

    sqrtIter(1.0)

  /** Проверка, является ли заданное число степенью двойки. */
  def isPowerOfTwo(x: Int): Boolean = (x & (x - 1)) == 0

  /** Проверяет, содержат ли данные числа `x` и `y` одинаковое количество
    * битовых единиц или нет.
    */
  @tailrec
  def isSnoob(x: Int, y: Int): Boolean =
    if x == 0 && y == 0 then true
    else if x == 0 || y == 0 then false
    else if x % 2 == 1 && y % 2 == 0 then isSnoob(x, y >> 1)
    else if x % 2 == 0 && y % 2 == 1 then isSnoob(x >> 1, y)
    else isSnoob(x >> 1, y >> 1)

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
end Numerical
