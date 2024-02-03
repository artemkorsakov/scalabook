package ru.scalabook.algorithms.numbers

import spire.math.SafeLong

/** <a href="https://en.wikipedia.org/wiki/Collatz_conjecture">Collatz
  * conjecture</a>
  */
final case class CollatzNumber(cacheLimit: Int):
  private val cache: Array[SafeLong] = new Array[SafeLong](cacheLimit)
  cache(1) = SafeLong(1)

  def collatz(n: Long): SafeLong =
    if n >= cacheLimit then nextCollatz(n)
    else
      val m = n.toInt
      if Option(cache(m)).isEmpty then cache(m) = nextCollatz(n)
      cache(m)

  private def nextCollatz(n: Long): SafeLong =
    if n % 2 == 0 then collatz(n / 2) + 1
    else collatz((3 * n + 1) / 2) + 2
