/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.primesBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.isPrimeBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.sieveOfEratosthenesBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.primeFactorsWithPowBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.primeFactorsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PrimesBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.nextPrimeBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.fundamental.Primes.*

import PrimesBenchmarks.*

@main def primesBench(): Unit =
  (1 to 10).foreach: _ =>
    println(loadprimesBench)
  prettyPrint(Period.Second)

@main def isPrimeBench(): Unit =
  (1 to 10).foreach: _ =>
    loadisPrimeBench()
  prettyPrint(Period.MilliSecond)

@main def sieveOfEratosthenesBench(): Unit =
  (1 to 10).foreach: _ =>
    println(loadsieveOfEratosthenesBench)
  prettyPrint(Period.MilliSecond)

@main def primeFactorsWithPowBench(): Unit =
  (1 to 10).foreach: _ =>
    loadprimeFactorsWithPowBench()
  prettyPrint(Period.MilliSecond)

@main def primeFactorsBench(): Unit =
  (1 to 10).foreach: _ =>
    loadprimeFactorsBench()
  prettyPrint(Period.MilliSecond)

@main def nextPrimeBench(): Unit =
  (1 to 10).foreach: _ =>
    loadnextPrimeBench()
  prettyPrint(Period.MilliSecond)

object PrimesBenchmarks extends MetricInstrumented:
  def loadprimesBench: Boolean = loading.time:
    primes.take(100_000).length == 100_000

  def loadisPrimeBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      isPrime(i)

  def loadsieveOfEratosthenesBench: Array[Boolean] = loading.time:
    sieveOfEratosthenes(1_000_000)

  def loadprimeFactorsWithPowBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      primeFactorsWithPow(i)

  def loadprimeFactorsBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      primeFactors(i)

  def loadnextPrimeBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      nextPrime(i)
end PrimesBenchmarks
