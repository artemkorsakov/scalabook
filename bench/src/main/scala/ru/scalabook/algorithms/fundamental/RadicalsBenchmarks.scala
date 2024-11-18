/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/RadicalsBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.radBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/RadicalsBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.allRadsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/RadicalsBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.isSquareFreeBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::cats-core:2.12.0
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Radicals.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Radicals.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import RadicalsBenchmarks.*

@main def radBench(): Unit =
  (1 to 10).foreach: _ =>
    loadradBench()
  prettyPrint(Period.Second)

@main def allRadsBench(): Unit =
  (1 to 10).foreach: _ =>
    println(loadallRadsBench)
  prettyPrint(Period.Second)

@main def isSquareFreeBench(): Unit =
  (1 to 10).foreach: _ =>
    loadisSquareFreeBench()
  prettyPrint(Period.Second)

object RadicalsBenchmarks extends MetricInstrumented:
  def loadradBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      rad(i)

  def loadallRadsBench: Array[Int] = loading.time:
    allRads(1_000_000)

  def loadisSquareFreeBench(): Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      isSquareFree(i)
