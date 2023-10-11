package ru.scalabook.algorithms.others

import scala.annotation.tailrec

object Factorial:
  def factorial(n: Int): BigInt =
    (2 to n).foldLeft(BigInt(1))((f, i) => f * BigInt(i))

  def approximateFactorial(n: Int): BigInt =
    (BigDecimal(math.sqrt(2 * math.Pi * n)) *
      BigDecimal(n / math.E).pow(n)).toBigInt

  def primeFactor(n: Int, p: Int): Int =
    @tailrec
    def loop(pj: Int, acc: Int): Int =
      if pj == 0 then acc
      else
        val next = pj / p
        loop(next, acc + next)
    loop(n, 0)
