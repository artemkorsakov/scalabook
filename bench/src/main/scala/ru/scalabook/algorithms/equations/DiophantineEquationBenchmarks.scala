/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/equations/DiophantineEquationBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/equations/DiophantineEquation.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.equations

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import DiophantineEquationBenchmarks.*

@main def minimalDiophantineEquationBench(): Unit =
  println(loadStuff)
  prettyPrint(Period.Second)

object DiophantineEquationBenchmarks extends MetricInstrumented:
  def loadStuff: Option[(BigInt, BigInt)] = loading.time:
    DiophantineEquation(67).minimalEquation
