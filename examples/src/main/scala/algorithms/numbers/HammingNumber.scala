package algorithms.numbers

import algorithms.fundamental.Primes.primesNoMoreThanN

/** A positive number is a generalized <a
  * href="https://en.wikipedia.org/wiki/Regular_number">Hamming number</a> of
  * type n, if it has no prime factor larger than n.
  */
object HammingNumber:
  def isHammingNumber(number: Int, n: Int): Boolean =
    isHammingNumber(number.toLong, n)

  def isHammingNumber(number: Long, n: Int): Boolean =
    if number < 2 || number <= n then true
    else if n < 2 then false
    else
      var temp       = number
      val primesList = primesNoMoreThanN(n)
      var isHamming  = false
      var i          = 0
      while i < primesList.length && !isHamming
      do
        val prime = primesList(i)
        while temp % prime == 0
        do temp /= prime
        if temp == 1 then isHamming = true
        i += 1
      isHamming
