package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Primes.{nextPrime, primesNoMoreThanN}

import scala.annotation.tailrec

/** The radical of n, rad(n), is the product of distinct prime factors of n. For
  * example, 504 = 2<sup>3</sup> × 3<sup>2</sup> × 7, so rad(504) = 2 × 3 × 7 = 42.
  */
object Radicals:
  /** Return the product of distinct prime factors of n.
    */
  def rad(n: Long): Long =
    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    @tailrec
    def loop(n: Long, i: Long, acc: Long): Long =
      val limit = math.sqrt(n.toDouble)
      if i > limit then
        if n > 1 then n * acc else acc
      else if n % i == 0 then loop(reduceLoop(n, i), i + 2, i * acc)
      else loop(n, i + 2, acc)

    if n % 2 == 0 then loop(reduceLoop(n, 2), 3, 2)
    else loop(n, 3, 1)

  /** Is square-free integer?
    * @see
    *   <a href="https://en.wikipedia.org/wiki/Square-free_integer">detailed
    *   description</a>
    */
  def isSquareFree(n: Long): Boolean =
    val s = math.sqrt(n.toDouble).toLong

    @tailrec
    def loop(p: Long): Boolean =
      if p > s then true
      else if n % (p * p) == 0 then false
      else
        nextPrime(p) match
          case Some(nextP) => loop(nextP)
          case _           => true

    loop(2L)

  /** Return array of products of distinct prime factors of any n not more
    * limit.
    */
  def allRads(limit: Int): Array[Int] =
    val rads = new Array[Int](limit + 1)
    rads(1) = 1
    primesNoMoreThanN(limit).foreach: p =>
      (1 to limit / p).foreach: i =>
        rads(i * p) = if rads(i * p) == 0 then p else rads(i * p) * p
    rads
end Radicals
