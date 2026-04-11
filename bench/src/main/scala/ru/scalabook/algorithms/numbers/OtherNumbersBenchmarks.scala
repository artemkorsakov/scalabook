/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.bernoulliBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.padovanBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.jacobsthalBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.pellBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.tribonacciBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.tetranacciBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.scalaz::scalaz-core:7.3.8
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/OtherNumbers.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/BinomialCoefficient.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.numbers.OtherNumbers.*

import OtherNumbersBenchmarks.*

@main def bernoulliBench(): Unit =
  (1 to 10).foreach: _ =>
    loadbernoulliBench()
  prettyPrint(Period.MilliSecond)

@main def padovanBench(): Unit =
  (1 to 10).foreach: _ =>
    loadpadovanBench()
  prettyPrint(Period.Second)

@main def jacobsthalBench(): Unit =
  loadjacobsthalBench()
  prettyPrint(Period.Second)

@main def pellBench(): Unit =
  loadpellBench()
  prettyPrint(Period.Second)

@main def tribonacciBench(): Unit =
  loadtribonacciBench()
  prettyPrint(Period.Second)

@main def tetranacciBench(): Unit =
  loadtetranacciBench()
  prettyPrint(Period.Second)

object OtherNumbersBenchmarks extends MetricInstrumented:
  def loadbernoulliBench(): Unit = loading.time:
    println(bernoulli(500))

  def loadpadovanBench(): Unit = loading.time:
    println(padovan(1_000_000))

  def loadjacobsthalBench(): Unit = loading.time:
    println(jacobsthal(1_000_000))

  def loadpellBench(): Unit = loading.time:
    println(pell(1_000_000))

  def loadtribonacciBench(): Unit = loading.time:
    println(tribonacci(1_000_000))

  def loadtetranacciBench(): Unit = loading.time:
    println(tetranacci(1_000_000))
