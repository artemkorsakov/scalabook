package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.others.Factorial.*
import weaver.FunSuite

object FactorialSuite extends FunSuite:
  test("Factorial calculation"):
    expect.all(
      factorial(0) == BigInt(1),
      factorial(1) == BigInt(1),
      factorial(2) == BigInt(2),
      factorial(8) == BigInt(40320),
      factorial(9) == BigInt(362880),
      factorial(15) == BigInt("1307674368000"),
      factorial(30) == BigInt("265252859812191058636308480000000")
    )

  test("Calculation of the approximate factorial using the Stirling formula"):
    expect.all(
      approximateFactorial(8) == BigInt(39902),
      approximateFactorial2(8) == BigInt(40319)
    )

  test("Calculating the multiple of dividing a factorial by a prime number"):
    expect.all(primeFactor(1000, 3) == 498)
end FactorialSuite
