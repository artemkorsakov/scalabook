// scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/BinomialCoefficientBenchmarks.scala

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/BinomialCoefficient.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.combinatorics

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period

@main def binomialCoefficientBench(): Unit =
  import BinomialCoefficientBenchmarks.*

  (0 until 10).foreach: _ =>
    loadStuff()

  prettyPrint(Period.Second)

object BinomialCoefficientBenchmarks extends MetricInstrumented:
  def loadStuff(): BigInt = loading.time:
    BinomialCoefficient.binomialCoefficient(100000, 50000)
