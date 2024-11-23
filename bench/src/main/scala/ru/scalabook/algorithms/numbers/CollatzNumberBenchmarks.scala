/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CollatzNumberBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/CollatzNumber.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import CollatzNumberBenchmarks.*

private val collatzNumber = CollatzNumber(1_000_000)

@main def collatzNumberBench(): Unit =
  (1 to 10).foreach: _ =>
    loadStuff()
  prettyPrint(Period.MicroSecond)

object CollatzNumberBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    (1 to 10_000).foreach: i =>
      collatzNumber.collatz(i)
