package ru.scalabook.algorithms.combinatorics

import spire.math.SafeLong

object Coins:

  /** Количество способов получить заданную сумму с помощью заданного набора
    * монет.
    *
    * @see
    *   <a href="https://projecteuler.net/overview=031">detailed description</a>
    */
  def countWays(coins: Array[Int], sum: Int): SafeLong =
    val ways = Array.fill(sum + 1)(SafeLong(0))
    ways(0) = 1
    coins.foreach: coin =>
      (coin to sum).foreach: i =>
        ways(i) += ways(i - coin)
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
    if number >= 1 then array(1) = BigInt(1)
    if number >= 2 then array(2) = BigInt(2)

    @SuppressWarnings(
      Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
    )
    def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
      var k  = s
      var op = k * (3 * k - 1) / 2
      var p  = pS
      while op <= n do
        if (k + 1) % 2 == 0 then
          p += array(n - op)
        else
          p -= array(n - op)
        k += s
        op = k * (3 * k - 1) / 2
      p

    for n <- 3 to number do
      val p = partitionPart(1, n, BigInt(0))
      array(n) = partitionPart(-1, n, p)
    array(number)
  end partition
end Coins
