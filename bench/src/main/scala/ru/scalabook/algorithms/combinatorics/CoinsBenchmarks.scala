/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CoinsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.countWaysBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CoinsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.partitionBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/Coins.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.combinatorics.Coins.*

import CoinsBenchmarks.*

@main def countWaysBench(): Unit =
  (1 to 10).foreach: _ =>
    loadCountWays()

  prettyPrint(Period.Second)

@main def partitionBench(): Unit =
  (1 to 10).foreach: _ =>
    loadPartition()

  prettyPrint(Period.Second)

object CoinsBenchmarks extends MetricInstrumented:
  def loadCountWays(): Unit = loading.time:
    (1 to 10_000).foreach: i =>
      countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), i)

  def loadPartition(): Unit = loading.time:
    (1 to 1000).foreach: i =>
      partition(i)
