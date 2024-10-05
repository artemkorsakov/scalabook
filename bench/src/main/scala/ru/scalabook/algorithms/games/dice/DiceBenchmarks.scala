/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/games/dice/DiceBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/games/dice/Dice.scala
//> using file ../../../MetricInstrumented.scala
//> using file ../../../Period.scala

package ru.scalabook.algorithms.games.dice

import spire.math.Rational
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import DiceBenchmarks.*

@main def diceBench(): Unit =
  println(loadStuff.mkString(", "))
  prettyPrint(Period.MicroSecond)

object DiceBenchmarks extends MetricInstrumented:
  def loadStuff: Array[Rational] = loading.time:
    Dice(6).probabilities(100)
