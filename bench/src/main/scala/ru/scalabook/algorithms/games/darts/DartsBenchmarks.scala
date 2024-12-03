/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/games/darts/DartsBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/games/darts/Darts.scala
//> using file ../../../MetricInstrumented.scala
//> using file ../../../Period.scala

package ru.scalabook.algorithms.games.darts

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import DartsBenchmarks.*

@main def dartsBench(): Unit =
  (1 to 10).foreach: _ =>
    println(loadStuff)
  prettyPrint(Period.MicroSecond)

object DartsBenchmarks extends MetricInstrumented:
  def loadStuff: Map[Int, Int] = loading.time:
    Darts.allDistinctWaysToCheckOut
