package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.fundamental.Primes.primesNoMoreThanN
import scala.annotation.tailrec

/** A positive number is a generalized <a
  * href="https://en.wikipedia.org/wiki/Regular_number">Hamming number</a> of
  * type n, if it has no prime factor larger than n.
  */
object HammingNumber:
  def isHammingNumber(number: Long, n: Int): Boolean =
    if number < 2 || number <= n then true
    else if n < 2 then false
    else
      val primesList = primesNoMoreThanN(n)

      @tailrec
      def reduceLoop(n: Long, i: Long): Long =
        if n % i == 0 then reduceLoop(n / i, i) else n

      @tailrec
      def loop(n: Long, i: Int): Boolean =
        if i >= primesList.length then false
        else
          val newN = reduceLoop(n, primesList(i))
          if newN == 1 then true
          else loop(newN, i + 1)

      loop(number, 0)

end HammingNumber
