package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Factorial.*
import spire.math.SafeLong
import weaver.FunSuite

object FactorialSuite extends FunSuite:
  test("Factorial calculation"):
    expect.all(
      factorial(0) == SafeLong(1),
      factorial(1) == SafeLong(1),
      factorial(2) == SafeLong(2),
      factorial(8) == SafeLong(40320),
      factorial(9) == SafeLong(362880),
      factorial(15) == SafeLong(1307674368000L),
      factorial(30) == SafeLong(BigInt("265252859812191058636308480000000"))
    )

  test("Calculation of the approximate factorial using the Stirling formula"):
    expect.all(
      approximateFactorial(8) == BigInt(39902),
      approximateFactorial2(8) == BigInt(40319)
    )

  test("Calculating the multiple of dividing a factorial by a prime number"):
    expect.all(primeFactor(1000, 3) == 498)
end FactorialSuite
