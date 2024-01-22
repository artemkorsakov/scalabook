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
