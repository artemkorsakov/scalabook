package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Numerical.*
import weaver.FunSuite

object NumericalSuite extends FunSuite:
  test("decToBinConv should convert decimal number to binary"):
    expect.all(
      decToBinConv(1) == "1",
      decToBinConv(2) == "10",
      decToBinConv(8) == "1000",
      decToBinConv(7) == "111"
    )

  test("gcd should return the greatest common divisor"):
    expect.all(
      gcdByEuclideanAlgorithm(4851, 3003) == 231L,
      gcd(4851, 3003) == 231L
    )

  test(
    "generalizedEuclideanAlgorithm should return the greatest common divisor and coefficients"
  ):
    expect.all(generalizedEuclideanAlgorithm(1769L, 551L) == (29L, 5L, -16L))

  test("gcdex should return Extended Euclidean algorithm's result"):
    expect.all(
      gcdex(BigInt(12121212L), BigInt(23422344L)) ==
        (BigInt(12), BigInt(347845), BigInt(-180012))
    )

  test("'power' should raise the number to a power"):
    expect.all(
      power(7, 5) == BigInt(16807),
      power(7, 6) == BigInt(117649),
      power(7, 7) == BigInt(823543)
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

  test("'sumOfDivisors' should return the sum of the number's divisors"):
    expect.all(
      sumOfDivisors(220) == BigInt(504),
      sumOfDivisors(284) == BigInt(504)
    )

  test("'countOfDivisors' should return the number of divisors of the number"):
    expect.all(countOfDivisors(100) == 9L)

  test("sumToGiven"):
    expect.all(sumToGiven(1000000) == 500000500000L)

  test("sumOfSquaresTo"):
    expect.all(sumOfSquaresTo(1000) == 333833500L)

  test("sumOfCubesTo"):
    expect.all(sumOfCubesTo(1000) == 250500250000L)
