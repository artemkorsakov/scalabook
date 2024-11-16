/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.gcdByEuclideanAlgorithmBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.gcdBasedOnParityOfNumbersBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.generalizedEuclideanAlgorithmBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.gcdInverseBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.lcmBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.powerBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sqrtBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.cubeRootOfBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.isPowerOfTwoBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.isSnoobBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sumOfDivisorsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.countOfDivisorsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sumToGivenBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sumOfSquaresToBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sumOfCubesToBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/NumericalBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.decToBinConvBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Numerical.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Numerical.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import NumericalBenchmarks.*

@main def gcdByEuclideanAlgorithmBench(): Unit =
  (1 to 10).foreach: _ =>
    loadgcdByEuclideanAlgorithm
  prettyPrint(Period.Second)

@main def gcdBasedOnParityOfNumbersBench(): Unit =
  (1 to 5).foreach: _ =>
    loadgcdBasedOnParityOfNumbersBench
  prettyPrint(Period.Second)

@main def generalizedEuclideanAlgorithmBench(): Unit =
  (1 to 5).foreach: _ =>
    loadgeneralizedEuclideanAlgorithmBench
  prettyPrint(Period.Second)

@main def gcdInverseBench(): Unit =
  (1 to 5).foreach: _ =>
    loadgcdInverseBench
  prettyPrint(Period.Second)

@main def lcmBench(): Unit =
  loadlcmBench
  prettyPrint(Period.MicroSecond)

@main def powerBench(): Unit =
  (1 to 10).foreach: _ =>
    loadpowerBench
  prettyPrint(Period.MilliSecond)

@main def sqrtBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsqrtBench
  prettyPrint(Period.MilliSecond)

@main def cubeRootOfBench(): Unit =
  (1 to 10).foreach: _ =>
    loadcubeRootOfBench
  prettyPrint(Period.MilliSecond)

@main def isPowerOfTwoBench(): Unit =
  (1 to 10).foreach: _ =>
    loadisPowerOfTwoBench
  prettyPrint(Period.MilliSecond)

@main def isSnoobBench(): Unit =
  (1 to 5).foreach: _ =>
    loadisSnoobBench
  prettyPrint(Period.Second)

@main def sumOfDivisorsBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsumOfDivisorsBench
  prettyPrint(Period.Second)

@main def countOfDivisorsBench(): Unit =
  (1 to 10).foreach: _ =>
    loadcountOfDivisorsBench
  prettyPrint(Period.Second)

@main def sumToGivenBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsumToGivenBench
  prettyPrint(Period.MilliSecond)

@main def sumOfSquaresToBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsumOfSquaresToBench
  prettyPrint(Period.MilliSecond)

@main def sumOfCubesToBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsumOfCubesToBench
  prettyPrint(Period.MilliSecond)

@main def decToBinConvBench(): Unit =
  (1 to 10).foreach: _ =>
    loaddecToBinConvBench
  prettyPrint(Period.Second)

object NumericalBenchmarks extends MetricInstrumented:
  def loadgcdByEuclideanAlgorithm: Unit = loading.time:
    val limit = 10_000
    val start = 1_000_000
    val end   = start + limit
    (start until end).foreach: i =>
      (i + 1 to end).foreach: j =>
        gcdByEuclideanAlgorithm(j, i)

  def loadgcdBasedOnParityOfNumbersBench: Unit = loading.time:
    val limit = 10_000
    val start = 1_000_000
    val end   = start + limit
    (start until end).foreach: i =>
      (i + 1 to end).foreach: j =>
        gcdBasedOnParityOfNumbers(j, i)

  def loadgeneralizedEuclideanAlgorithmBench: Unit = loading.time:
    val limit = 10_000
    (2 until limit).foreach: i =>
      (i + 1 to limit).foreach: j =>
        generalizedEuclideanAlgorithm(j, i)

  def loadgcdInverseBench: Unit = loading.time:
    val limit = 10_000
    (2 until limit).foreach: i =>
      (i + 1 to limit).foreach: j =>
        gcdInverse(j, i)

  def loadlcmBench: Unit = loading.time:
    val limit = 10_000
    val start = 1_000_000
    val end   = start + limit
    (start until end).foreach: i =>
      (i + 1 to end).foreach: j =>
        lcm(j, i)

  def loadpowerBench: Unit = loading.time:
    (1 to 100).foreach: i =>
      (1 to 10).foreach: j =>
        power(j, i)

  def loadsqrtBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      sqrt(i)

  def loadcubeRootOfBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      cubeRootOf(i)

  def loadisPowerOfTwoBench: Unit = loading.time:
    (1 to 1_000_000_000).foreach: i =>
      isPowerOfTwo(i)

  def loadisSnoobBench: Unit = loading.time:
    val limit = 10_000
    (1 until limit).foreach: i =>
      (i + 1 to limit).foreach: j =>
        isSnoob(j, i)

  def loadsumOfDivisorsBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      sumOfDivisors(i)

  def loadcountOfDivisorsBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      countOfDivisors(i)

  def loadsumToGivenBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      sumToGiven(i)

  def loadsumOfSquaresToBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      sumOfSquaresTo(i)

  def loadsumOfCubesToBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      sumOfCubesTo(i)

  def loaddecToBinConvBench: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      decToBinConv(i)
end NumericalBenchmarks
