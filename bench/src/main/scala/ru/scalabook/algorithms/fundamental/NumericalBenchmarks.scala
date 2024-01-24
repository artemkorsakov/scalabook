package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Numerical.*

@main def gcdByEuclideanAlgorithmBench(): Unit =
  val limit = 10_000
  val start = 1_000_000
  val end   = start + limit
  (start until end).foreach: i =>
    (i + 1 to end).foreach: j =>
      gcdByEuclideanAlgorithm(j, i)

@main def gcdBasedOnParityOfNumbersBench(): Unit =
  val limit = 10_000
  val start = 1_000_000
  val end   = start + limit
  (start until end).foreach: i =>
    (i + 1 to end).foreach: j =>
      gcdBasedOnParityOfNumbers(j, i)

@main def generalizedEuclideanAlgorithmBench(): Unit =
  val limit = 10_000
  (2 until limit).foreach: i =>
    (i + 1 to limit).foreach: j =>
      generalizedEuclideanAlgorithm(j, i)

@main def gcdInverseBench(): Unit =
  val limit = 10_000
  (2 until limit).foreach: i =>
    (i + 1 to limit).foreach: j =>
      gcdInverse(j, i)

@main def lcmBench(): Unit =
  val limit = 10_000
  val start = 1_000_000
  val end   = start + limit
  (start until end).foreach: i =>
    (i + 1 to end).foreach: j =>
      lcm(j, i)

@main def powerBench(): Unit =
  (1 to 100).foreach: i =>
    (1 to 10).foreach: j =>
      power(j, i)

@main def sqrtBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sqrt(i)

@main def cubeRootOfBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    cubeRootOf(i)

@main def isPowerOfTwoBench(): Unit =
  (1 to 1_000_000_000).foreach: i =>
    isPowerOfTwo(i)

@main def isSnoobBench(): Unit =
  val limit = 10_000
  (1 until limit).foreach: i =>
    (i + 1 to limit).foreach: j =>
      isSnoob(j, i)

@main def sumOfDivisorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfDivisors(i)

@main def countOfDivisorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    countOfDivisors(i)

@main def sumToGivenBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumToGiven(i)

@main def sumOfSquaresToBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfSquaresTo(i)

@main def sumOfCubesToBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfCubesTo(i)

@main def decToBinConvBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    decToBinConv(i)
