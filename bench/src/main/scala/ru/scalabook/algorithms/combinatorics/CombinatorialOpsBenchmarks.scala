/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xCombinationsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xSubsetsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xVariationsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xPermutationsBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOps.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.combinatorics.CombinatorialOps.*

import CombinatorialOpsBenchmarks.*

@main def xCombinationsBench(): Unit =
  (0 until 10).foreach: _ =>
    loadxCombinations
  prettyPrint(Period.MilliSecond)

@main def xSubsetsBench(): Unit =
  (0 until 10).foreach: _ =>
    loadxsubsets
  prettyPrint(Period.MilliSecond)

@main def xVariationsBench(): Unit =
  (0 until 10).foreach: _ =>
    loadxvariations
  prettyPrint(Period.MilliSecond)

@main def xPermutationsBench(): Unit =
  (0 until 10).foreach: _ =>
    loadxpermutations
  prettyPrint(Period.MilliSecond)

object CombinatorialOpsBenchmarks extends MetricInstrumented:
  def loadxCombinations: List[List[Int]] = loading.time:
    (1 to 1000).toList.xcombinations(2)

  def loadxsubsets: List[List[Int]] = loading.time:
    (1 to 10).toList.xsubsets

  def loadxvariations: List[List[Int]] = loading.time:
    (1 to 1000).toList.xvariations(2)

  def loadxpermutations: List[List[Int]] = loading.time:
    (1 to 5).toList.xpermutations
