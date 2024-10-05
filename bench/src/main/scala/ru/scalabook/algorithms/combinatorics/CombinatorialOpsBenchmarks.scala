/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xCombinationsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xSubsetsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xVariationsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOpsBenchmarks.scala --main-class ru.scalabook.algorithms.combinatorics.xPermutationsBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/CombinatorialOps.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.CombinatorialOps.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import CombinatorialOpsBenchmarks.*

@main def xCombinationsBench(): List[List[Int]] =
  val res = loadxCombinations
  prettyPrint(Period.MicroSecond)
  res

@main def xSubsetsBench(): List[List[Int]] =
  val res = loadxsubsets
  prettyPrint(Period.MicroSecond)
  res

@main def xVariationsBench(): List[List[Int]] =
  val res = loadxvariations
  prettyPrint(Period.MicroSecond)
  res

@main def xPermutationsBench(): List[List[Int]] =
  val res = loadxpermutations
  prettyPrint(Period.MicroSecond)
  res

object CombinatorialOpsBenchmarks extends MetricInstrumented:
  def loadxCombinations: List[List[Int]] = loading.time:
    (1 to 1000).toList.xcombinations(2)

  def loadxsubsets: List[List[Int]] = loading.time:
    (1 to 10).toList.xsubsets

  def loadxvariations: List[List[Int]] = loading.time:
    (1 to 1000).toList.xvariations(2)

  def loadxpermutations: List[List[Int]] = loading.time:
    (1 to 5).toList.xpermutations
