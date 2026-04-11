/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/others/OtherAlgorithmsBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/others/OtherAlgorithms.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.others

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.others.OtherAlgorithms.functionAckermann

import OtherAlgorithmsBenchmarks.*

@main def functionAckermannBench(): Unit =
  loadStuff()
  prettyPrint(Period.MicroSecond)

object OtherAlgorithmsBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(functionAckermann(3, 3))
