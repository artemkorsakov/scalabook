/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/HarshadNumberBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/HarshadNumber.scala
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Primes.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.HarshadNumber.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import HarshadNumberBenchmarks.*

@main def getStrongRightTruncatableHarshadPrimesBench(): Unit =
  loadStuff()
  prettyPrint(Period.MicroSecond)

object HarshadNumberBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println(getStrongRightTruncatableHarshadPrimes(9))
