package algorithms.combinatorics

object Coins:

  /** How many options to get a given sum from given coins?
    *
    * @see
    *   <a href="https://projecteuler.net/overview=031">detailed description</a>
    */
  def countWays(coins: Array[Int], sum: Int): Long =
    val ways = new Array[Long](sum + 1)
    ways(0) = 1
    coins.foreach(coin => (coin to sum).foreach(i => ways(i) += ways(i - coin)))
    ways(sum)

  /** A way of writing n as a sum of positive integers.
    *
    * @see
    *   <a
    *   href="https://en.wikipedia.org/wiki/Partition_(number_theory)">detailed
    *   description</a>
    */
  def partition(number: Int): BigInt =
    val array = new Array[BigInt](number + 1)
    array(0) = BigInt(1)
    array(1) = BigInt(1)
    array(2) = BigInt(2)

    @SuppressWarnings(
      Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
    )
    def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
      var k  = s
      var op = k * (3 * k - 1) / 2
      var p  = pS
      while (op <= n) {
        if ((k + 1) % 2 == 0) {
          p += array(n - op)
        } else {
          p -= array(n - op)
        }
        k += s
        op = k * (3 * k - 1) / 2
      }
      p

    for (n <- 3 to number) {
      val p = partitionPart(1, n, BigInt(0))
      array(n) = partitionPart(-1, n, p)
    }
    array(number)
