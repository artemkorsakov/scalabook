/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/PermutationBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.constructByMethod1Bench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/PermutationBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.constructByMethod2Bench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/Permutation.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.combinatorics.Permutation.*

import PermutationBenchmarks.*

@main def constructByMethod1Bench(): Unit =
  (1 to 10).foreach: _ =>
    loadConstructByMethod1
  prettyPrint(Period.Second)

@main def constructByMethod2Bench(): Unit =
  (1 to 10).foreach: _ =>
    loadConstructByMethod2
  prettyPrint(Period.Second)

object PermutationBenchmarks extends MetricInstrumented:
  def loadConstructByMethod1: Unit = loading.time:
    constructByMethod1[Int]((1 to 19_999).toVector, 20_000)

  def loadConstructByMethod2: Unit = loading.time:
    constructByMethod2((1 to 4_999).toVector)
