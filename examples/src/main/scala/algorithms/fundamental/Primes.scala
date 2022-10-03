package algorithms.fundamental

import scala.collection.mutable

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
    else if n < 4 then true
    else if n % 2 == 0 then false
    else if n < 9 then true
    else if n % 3 == 0 then false
    else
      var candidate = 5
      while candidate <= math.sqrt(n) && n % candidate != 0
      do candidate += (if (candidate % 6 == 5) 2 else 4)
      n % candidate != 0

  def isPrimeArray(n: Int): Array[Boolean] =
    val result = Array.fill(n + 1)(true)
    result(0) = false
    result(1) = false
    var i = 2
    var i2 = i * i
    while i2 <= n do
      if result(i) then (i2 to n by i).foreach(j => result(j) = false)
      i += 1
      i2 = i * i
    result
