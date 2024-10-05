/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/PerfectNumbersBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::cats-core:2.12.0
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/PerfectNumbers.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Numerical.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import PerfectNumbersBenchmarks.*
import ru.scalabook.algorithms.fundamental.PerfectNumbers.perfectNumbersType

@main def perfectNumbersTypeBench(): Unit =
  loadPerfectNumbersType
  prettyPrint(Period.MicroSecond)

object PerfectNumbersBenchmarks extends MetricInstrumented:
  def loadPerfectNumbersType: Unit = loading.time:
    (1 to 1_000_000).foreach: i =>
      perfectNumbersType(i)
