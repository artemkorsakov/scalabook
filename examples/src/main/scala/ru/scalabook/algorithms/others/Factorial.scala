package ru.scalabook.algorithms.others

object Factorial {
  def factorial(n: Int): BigInt =
    (2 to n).foldLeft(BigInt(1))((f, i) => f * BigInt(i))

  def approximateFactorial(n: Int): BigInt =
    (BigDecimal(math.sqrt(2 * math.Pi * n)) *
      BigDecimal(n / math.E).pow(n)).toBigInt

}
