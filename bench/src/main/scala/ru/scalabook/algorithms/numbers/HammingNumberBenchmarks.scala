/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/HammingNumberBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/HammingNumber.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.numbers.HammingNumber.isHammingNumber

import HammingNumberBenchmarks.*

@main def isHammingNumberBench(): Unit =
  loadStuff()
  prettyPrint(Period.MicroSecond)

object HammingNumberBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(isHammingNumber(2125764000, 5))
