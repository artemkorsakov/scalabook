package algorithms.numbers

/** <a href="https://en.wikipedia.org/wiki/Collatz_conjecture">Collatz
  * conjecture</a>
  */
case class CollatzNumber(cacheLimit: Int):
  val cache: Array[Long] = new Array[Long](cacheLimit)
  cache(1) = 1L

  def collatz(n: Long): Long =
    if n >= cacheLimit then nextCollatz(n)
    else
      val m = n.toInt
      if cache(m) == 0 then cache(m) = nextCollatz(n)
      cache(m)

  private def nextCollatz(n: Long): Long =
    if n % 2 == 0 then collatz(n / 2) + 1
    else collatz((3 * n + 1) / 2) + 2
