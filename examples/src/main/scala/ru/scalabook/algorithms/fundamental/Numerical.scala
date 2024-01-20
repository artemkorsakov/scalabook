package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Primes.primeFactorsWithPow
import spire.math.SafeLong

import scala.annotation.tailrec

object Numerical:
  private val delta: Double = 1e-5

  /** Вычисление наибольшего общего делителя посредством алгоритма Евклида. */
  @tailrec
  def gcdByEuclideanAlgorithm(a: Long, b: Long): Long =
    if b == 0 then a
    else gcdByEuclideanAlgorithm(b, a % b)

  /** Вычисление наибольшего общего делителя на основе четности чисел */
  def gcdBasedOnParityOfNumbers(a: Long, b: Long): Long =
    val u = math.abs(a)
    val v = math.abs(b)
    if u == v then u
    else if u == 0 then v
    else if v == 0 then u
    else
      (~u & 1, ~v & 1) match
        case (1, 1) => gcdBasedOnParityOfNumbers(u >> 1, v >> 1) << 1
        case (1, 0) => gcdBasedOnParityOfNumbers(u >> 1, v)
        case (0, 1) => gcdBasedOnParityOfNumbers(u, v >> 1)
        case (_, _) =>
          if u > v then gcdBasedOnParityOfNumbers((u - v) >> 1, v)
          else gcdBasedOnParityOfNumbers((v - u) >> 1, u)

  /** Вычисление наибольшего общего делителя */
  def gcd(a: Long, b: Long): Long = gcdByEuclideanAlgorithm(a: Long, b: Long)

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

  /** Обратное по модулю число.
    *
    * @see
    *   <a
    *   href="https://ru.wikipedia.org/wiki/%D0%9E%D0%B1%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D0%BF%D0%BE_%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8E_%D1%87%D0%B8%D1%81%D0%BB%D0%BE">wiki</a>
    */
  def gcdInverse(m: SafeLong, n: SafeLong): Option[SafeLong] =
    val (d, a, _) = generalizedEuclideanAlgorithm(m, n)
    if d == 1 then Some((a % n + n) % n) else None

  private def generalizedEuclideanAlgorithm(
      m: SafeLong,
      n: SafeLong
  ): (SafeLong, SafeLong, SafeLong) =
    @tailrec
    def loop(
        aa: SafeLong,
        b: SafeLong,
        a: SafeLong,
        bb: SafeLong,
        c: SafeLong,
        d: SafeLong
    ): (SafeLong, SafeLong, SafeLong) =
      val r = c % d
      if r == 0 then (d, a, b)
      else
        val q = c / d
        loop(aa = a, b = bb - q * b, a = aa - q * a, bb = b, c = d, d = r)
    loop(aa = 1, b = 1, a = 0, bb = 0, c = m, d = n)
  end generalizedEuclideanAlgorithm

  /** Вычисление наименьшего общего кратного двух чисел */
  def lcm(x: Long, y: Long): Long =
    math.abs(x * y) / gcd(x, y)

  /** Возведение в степень */
  def power(a: SafeLong, n: SafeLong): SafeLong =
    @tailrec
    def loop(base: SafeLong, power: SafeLong, acc: SafeLong): SafeLong =
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

  /** Возвращает сумму делителей числа n.
    *
    * @see
    *   <a
    *   href="https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%B4%D0%B5%D0%BB%D0%B8%D1%82%D0%B5%D0%BB%D0%B5%D0%B9">detailed
    *   description</a>
    */
  def sumOfDivisors(number: Long): SafeLong =
    val primeDivisors = primeFactorsWithPow(number)
    primeDivisors.foldLeft(SafeLong(1)) { case (mul, (prime, power)) =>
      val num = SafeLong(prime).pow(power + 1) - 1
      val den = prime - 1
      mul * (num / den)
    }

  /** Return the count of divisors of n.
    */
  def countOfDivisors(number: Long): SafeLong =
    primeFactorsWithPow(number).values.foldLeft(SafeLong(1)): (mul, a) =>
      mul * (a + 1)

  /** Сумма чисел от 1 до заданного. */
  def sumToGiven(n: Long): Long = n * (n + 1) / 2

  /** Сумма квадратов чисел от 1 до заданного.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Square_number">description</a>
    */
  def sumOfSquaresTo(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

  /** Сумма кубов чисел от 1 до заданного.
    *
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Cube_(algebra)">description</a>
    */
  def sumOfCubesTo(n: Long): Long =
    val s = sumToGiven(n)
    s * s

  /** Преобразование десятичного числа в двоичное */
  def decToBinConv(x: Int): String =
    val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
    val binList = seqOfDivByTwo
      .takeWhile(a => a > 0)
      .map(a => a % 2)
    binList.mkString.reverse
end Numerical
