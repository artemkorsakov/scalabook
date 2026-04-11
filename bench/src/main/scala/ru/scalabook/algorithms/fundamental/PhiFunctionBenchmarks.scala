/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PhiFunctionBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.totientBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PhiFunctionBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.totientArrayBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::cats-core:2.12.0
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/PhiFunction.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.fundamental.PhiFunction.*

import PhiFunctionBenchmarks.*

@main def totientBench(): Unit =
  (1 to 10).foreach: _ =>
    loadTotient
  prettyPrint(Period.MilliSecond)

@main def totientArrayBench(): Unit =
  (1 to 10).foreach: _ =>
    println(loadTotientArrayBench)
  prettyPrint(Period.MilliSecond)

object PhiFunctionBenchmarks extends MetricInstrumented:
  def loadTotient: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      totient(i)

  def loadTotientArrayBench: Array[Long] = loading.time:
    totientArray(1_000_000)
