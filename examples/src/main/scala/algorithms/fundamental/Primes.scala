package algorithms.fundamental

import scala.annotation.tailrec
import scala.util.Random

object Primes:
  lazy val primes: LazyList[Int] =
    2 #:: LazyList
      .from(3)
      .filter { x =>
        val sqrtOfPrimes = primes.takeWhile(p => p <= math.sqrt(x))
        sqrtOfPrimes.forall(p => x % p != 0)
      }

  def isPrime(n: Long): Boolean =
    if n < 2 then false
    else if n < 4 then true // 2 и 3 - простые
    else if n % 2 == 0 then false
    else if n < 9 then true // мы уже исключили 4,6 и 8
    else if n % 3 == 0 then false
    else
      val limit = math.ceil(math.sqrt(n)).toLong

      @annotation.tailrec
      def loop(f: Long): Boolean =
        if f > limit then true
        else if n % f == 0 || n % (f + 2) == 0 then false
        else loop(f + 6)

      loop(5)

  @tailrec
  def isProbablyPrime(p: Long, max_test: Int): Boolean =
    (max_test <= 0) || {
      (BigInt(Random.nextLong(p)).modPow(p - 1, p) == 1) && isProbablyPrime(
        p,
        max_test - 1
      )
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

  /** Нахождение простых множителей */
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def primeFactorsWithPow(n: Long): Map[Long, Long] =
    var number = n

    // Проверяем делимость на 2
    var powOfTwo = 0L
    while number % 2 == 0 do
      powOfTwo += 1
      number = number >> 1
    var map =
      if powOfTwo > 0 then Map(2L -> powOfTwo) else Map.empty[Long, Long]

    // Ищем нечетные множители
    var i = 3L
    while i <= math.sqrt(number) do
      var pow = 0L
      while number              % i == 0 do
        number /= i
        pow += 1
      if pow > 0 then map += i -> pow
      i += 2

    // Если от числа что-то осталось, то остаток тоже множитель
    if number > 1 then map + (number -> 1) else map
