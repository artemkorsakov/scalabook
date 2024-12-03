/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/others/NewtonsMethodBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/others/NewtonsMethod.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.others.NewtonsMethod.newtonsMethod
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import NewtonsMethodBenchmarks.*

private val g: Double => Double = x => math.cos(x) - x * x * x

@main def newtonsMethodBench(): Unit =
  (1 to 10).foreach: _ =>
    loadStuff()
  prettyPrint(Period.MicroSecond)

object NewtonsMethodBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(newtonsMethod(g, 0.5))
