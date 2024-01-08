package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Numerical.*
import weaver.FunSuite

object NumericalSuite extends FunSuite:
  test("gcd should return the greatest common divisor"):
    expect.all(
      gcdByEuclideanAlgorithm(4851, 3003) == 231L,
      gcd(4851, 3003) == 231L
    )

  test(
    "generalizedEuclideanAlgorithm should return the greatest common divisor and coefficients"
  ):
    expect.all(
      generalizedEuclideanAlgorithm(1769L, 551L) == (29L, 5L, -16L),
      generalizedEuclideanAlgorithm(
        12121212L,
        23422344L
      ) == (12L, 347845L, -180012L)
    )

  test("'power' should raise the number to a power"):
    expect.all(
      power(7, 5) == 16807,
      power(7, 6) == 117649,
      power(7, 7) == 823543
    )

  test("'sqrt' should return an approximation of the square root of a number"):
    expect.all(
      math.abs(sqrt(9.0) - 3.0) <= 0.001,
      math.abs(sqrt(137) - 11.7047) <= 0.001
    )

  test(
    "'cubeRootOf' should return an approximation of the cube root of a number"
  ):
    expect.all(
      math.abs(cubeRootOf(27.0) - 3.0) <= 0.001,
      math.abs(cubeRootOf(137) - 5.1551) <= 0.001
    )

  test("'isPowerOfTwo' should check if the number is a power of 2"):
    expect.all(
      isPowerOfTwo(16),
      !isPowerOfTwo(15)
    )

  test(
    "'isSnoob' should return true if the given numbers have the same number of bit units"
  ):
    expect.all(isSnoob(11, 13))

  test("'sumOfDivisors' should return the sum of the number's divisors"):
    expect.all(
      sumOfDivisors(220) == 504,
      sumOfDivisors(284) == 504
    )

  test("'countOfDivisors' should return the number of divisors of the number"):
    expect.all(countOfDivisors(100) == 9)

  test("sumToGiven"):
    expect.all(sumToGiven(1000000) == 500000500000L)

  test("sumOfSquaresTo"):
    expect.all(sumOfSquaresTo(1000) == 333833500L)

  test("sumOfCubesTo"):
    expect.all(sumOfCubesTo(1000) == 250500250000L)

  test("decToBinConv should convert decimal number to binary"):
    expect.all(
      decToBinConv(1) == "1",
      decToBinConv(2) == "10",
      decToBinConv(8) == "1000",
      decToBinConv(7) == "111"
    )
end NumericalSuite
