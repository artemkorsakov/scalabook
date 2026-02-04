package ru.scalabook.algorithms.fundamental

import scala.annotation.tailrec
import scala.util.Random

object Primes:
  lazy val primes: LazyList[Int] =
    2 #::
      LazyList
        .from(3)
        .filter: x =>
          val sqrtOfPrimes = primes.takeWhile(p => p <= math.sqrt(x))
          sqrtOfPrimes.forall(p => x % p != 0)

  def isPrime(n: Long): Boolean =
    if n < 2 then false
    else if n < 4 then true // 2 и 3 - простые
    else if n % 2 == 0 then false
    else if n < 9 then true // мы уже исключили 4,6 и 8
    else if n % 3 == 0 then false
    else
      val limit = math.ceil(math.sqrt(n.toDouble)).toLong

      @annotation.tailrec
      def loop(f: Long): Boolean =
        if f > limit then true
        else if n % f == 0 || n % (f + 2) == 0 then false
        else loop(f + 6)

      loop(5)

  @tailrec
  def isProbablyPrime(p: Long, maxTest: Int): Boolean = (maxTest <= 0) || {
    (BigInt(Random.nextLong(p)).modPow(p - 1, p) == 1) &&
    isProbablyPrime(p, maxTest - 1)
  }

  def sieveOfEratosthenes(n: Int): Array[Boolean] =
    val result = Array.fill(n + 1)(true)
    result(0) = false
    result(1) = false
    (4 to n by 2).foreach(j => result(j) = false)
    for
      i <- 3 to math.sqrt(n).toInt by 2
      if result(i)
      j <- i to n / i
    do result(j * i) = false
    result

  /** All prime numbers from 2 through n (inclusive).
    */
  def primesNoMoreThanN(n: Int): Array[Int] =
    sieveOfEratosthenes(n).zipWithIndex.collect { case (true, prime) => prime }

  /** Нахождение простых множителей */
  def primeFactorsWithPow(n: Long): Map[Long, Int] =
    @tailrec
    def reduceLoop(n: Long, i: Long, count: Int): (Long, Int) =
      if n % i == 0 then reduceLoop(n / i, i, count + 1) else (n, count)

    @tailrec
    def loop(n: Long, i: Long, acc: Map[Long, Int]): Map[Long, Int] =
      val limit = math.sqrt(n.toDouble)
      if i > limit then
        if n > 1 then acc + (n -> 1) else acc
      else if n % i == 0 then
        val (newN, newCount) = reduceLoop(n, i, 0)
        loop(newN, i + 2, acc + (i -> newCount))
      else loop(n, i + 2, acc)

    if n % 2 == 0 then
      val (newN, newCount) = reduceLoop(n, 2, 0)
      loop(newN, 3, Map(2L -> newCount))
    else loop(n, 3, Map.empty)
  end primeFactorsWithPow

  def primeFactors(n: Long): List[Long] =
    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    @tailrec
    def loop(n: Long, i: Long, acc: List[Long]): List[Long] =
      val limit = math.sqrt(n.toDouble)
      if i > limit then
        if n > 1 then n :: acc else acc
      else if n % i == 0 then loop(reduceLoop(n, i), i + 2, i :: acc)
      else loop(n, i + 2, acc)

    if n % 2 == 0 then loop(reduceLoop(n, 2), 3, 2 :: Nil)
    else loop(n, 3, Nil)
  end primeFactors

  /** Возвращает следующее простое число за заданным простым числом. */
  def nextPrime(n: Long): Option[Long] =
    if !isPrime(n) then None
    else if n == 2 then Some(3)
    else if n == 3 then Some(5)
    else
      @tailrec
      def loop(p: Long): Long =
        val nextPrime = if p % 3 == 1 then p + 4 else p + 2
        if isPrime(nextPrime) then nextPrime else loop(nextPrime)
      Some(loop(n))
end Primes
