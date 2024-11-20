package ru.scalabook.algorithms.combinatorics

import spire.math.SafeLong
import scala.annotation.tailrec

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

    def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
      @tailrec
      def loop(k: Int, p: BigInt): BigInt =
        val op = k * (3 * k - 1) / 2
        if op > n then p
        else
          val newP =
            if (k + 1) % 2 == 0 then p + array(n - op)
            else p - array(n - op)
          loop(k + s, newP)

      loop(s, pS)

    (3 to number).foreach: n =>
      val p = partitionPart(1, n, BigInt(0))
      array(n) = partitionPart(-1, n, p)
    array(number)
  end partition
end Coins
