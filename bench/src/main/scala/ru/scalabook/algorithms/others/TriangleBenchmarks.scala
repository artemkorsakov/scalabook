/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/others/TriangleBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/geometry/Triangle.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.geometry.Triangle
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import TriangleBenchmarks.*

@main def getPointOnTriangleTypeBench: Unit =
  loadStuff()
  prettyPrint(Period.MicroSecond)

object TriangleBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(
      Triangle((-340, 495), (-153, -910), (835, -947)).getPointOnTriangleType((
        0,
        0
      ))
    )
