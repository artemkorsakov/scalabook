/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/PythagoreanTripletBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/PythagoreanTriplet.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Numerical.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.PythagoreanTriplet.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import PythagoreanTripletBenchmarks.*

@main def nextPythagoreanTripletBench(): Unit =
  (1 to 10).foreach: _ =>
    loadStuff()
  prettyPrint(Period.MilliSecond)

object PythagoreanTripletBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(pythagoreanTripletsWithGivenSum(1000).mkString("Array(", ", ", ")"))
