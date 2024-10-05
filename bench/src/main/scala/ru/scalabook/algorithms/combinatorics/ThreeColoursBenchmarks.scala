/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/ThreeColoursBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/ThreeColours.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.combinatorics.ThreeColours.*
import spire.math.SafeLong
import ThreeColoursBenchmarks.*

@main def countABCRowsBench(): Unit =
  println(loadCountABCRows)

  prettyPrint(Period.MicroSecond)

object ThreeColoursBenchmarks extends MetricInstrumented:
  def loadCountABCRows: SafeLong = loading.time:
    countABCRows(100, 100, 100)
