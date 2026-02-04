package ru.scalabook.algorithms.combinatorics

import spire.math.SafeLong

import scala.annotation.tailrec

object Factorial:
  def factorial(n: Int): SafeLong = (2 to n).foldLeft(SafeLong(1))(_ * _)

  def approximateFactorial(n: Int): BigInt =
    (BigDecimal(math.sqrt(2 * math.Pi * n)) *
      BigDecimal(n / math.E).pow(n)).toBigInt

  def approximateFactorial2(n: Int): BigInt =
    (BigDecimal(math.sqrt(2 * math.Pi * n)) *
      BigDecimal(n / math.E).pow(n) *
      (1 + 1 / BigDecimal(12.0 * n) + 1 / BigDecimal(288.0 * n * n) -
        139 / BigDecimal(51840.0 * n * n * n) -
        571 / BigDecimal(2488320.0 * n * n * n * n))).toBigInt

  def primeFactor(n: Int, p: Int): Int =
    @tailrec
    def loop(pj: Int, acc: Int): Int =
      if pj == 0 then acc
      else
        val next = pj / p
        loop(next, acc + next)
    loop(n, 0)
end Factorial
