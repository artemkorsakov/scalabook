/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/ChineseRemainderTheoremBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::cats-core:2.12.0
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/ChineseRemainderTheorem.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Numerical.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import spire.math.SafeLong
import ChineseRemainderTheoremBenchmarks.*

@main def solutionChineseRemainderTheoremBench(): Unit =
  println(loadStuff)
  prettyPrint(Period.MicroSecond)

object ChineseRemainderTheoremBenchmarks extends MetricInstrumented:
  def loadStuff: Option[SafeLong] = loading.time:
    ChineseRemainderTheorem.solution(
      Array(1504170715041707L, 4503599627370517L),
      Array(0L, 8912517754604L)
    )
