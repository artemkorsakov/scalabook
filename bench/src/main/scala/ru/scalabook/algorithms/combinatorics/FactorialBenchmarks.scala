/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/FactorialBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.factorialBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/FactorialBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.approximateFactorialBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/FactorialBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.approximateFactorial2Bench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/FactorialBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.primeFactorBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/Factorial.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.combinatorics.Factorial.*
import ru.scalabook.algorithms.fundamental.Primes.primes
import spire.math.SafeLong

import FactorialBenchmarks.*

@main def factorialBench(): Unit =
  (1 to 5).foreach: _ =>
    val _ = loadFactorial

  prettyPrint(Period.Second)

@main def approximateFactorialBench(): Unit =
  (1 to 10).foreach: _ =>
    val _ = loadApproximateFactorial

  prettyPrint(Period.Second)

@main def approximateFactorial2Bench(): Unit =
  (1 to 10).foreach: _ =>
    val _ = loadApproximateFactorial2

  prettyPrint(Period.Second)

@main def primeFactorBench(): Unit =
  (1 to 10).foreach: _ =>
    loadPrimeFactorBench

  prettyPrint(Period.Second)

object FactorialBenchmarks extends MetricInstrumented:
  def loadFactorial: SafeLong = loading.time:
    factorial(100000)

  def loadApproximateFactorial: BigInt = loading.time:
    approximateFactorial(100000)

  def loadApproximateFactorial2: BigInt = loading.time:
    approximateFactorial2(100000)

  def loadPrimeFactorBench: Unit = loading.time:
    val limit = 100_000
    primes.take(limit).toList.foreach: p =>
      primeFactor(limit, p)
