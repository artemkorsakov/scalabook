/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.triangleNumberBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.squareNumberBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.pentagonalNumberBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.hexagonalNumberBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.heptagonalNumberBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbersBenchmarks.scala --main-class ru.scalabook.algorithms.numbers.octagonalNumberBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/CyclicNumbers.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.CyclicNumbers.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import CyclicNumbersBenchmarks.*

@main def triangleNumberBench(): Unit =
  loadtriangleNumberBench()
  prettyPrint(Period.MicroSecond)

@main def squareNumberBench(): Unit =
  loadsquareNumberBench()
  prettyPrint(Period.MicroSecond)

@main def pentagonalNumberBench(): Unit =
  loadpentagonalNumberBench()
  prettyPrint(Period.MicroSecond)

@main def hexagonalNumberBench(): Unit =
  loadhexagonalNumberBench()
  prettyPrint(Period.MicroSecond)

@main def heptagonalNumberBench(): Unit =
  loadheptagonalNumberBench()
  prettyPrint(Period.MicroSecond)

@main def octagonalNumberBench(): Unit =
  loadoctagonalNumberBench()
  prettyPrint(Period.MicroSecond)

object CyclicNumbersBenchmarks extends MetricInstrumented:
  def loadtriangleNumberBench(): Unit = loading.time:
    println(triangleNumber(1_000_000))

  def loadsquareNumberBench(): Unit = loading.time:
    println(squareNumber(1_000_000))

  def loadpentagonalNumberBench(): Unit = loading.time:
    println(pentagonalNumber(1_000_000))

  def loadhexagonalNumberBench(): Unit = loading.time:
    println(hexagonalNumber(1_000_000))

  def loadheptagonalNumberBench(): Unit = loading.time:
    println(heptagonalNumber(1_000_000))

  def loadoctagonalNumberBench(): Unit = loading.time:
    println(octagonalNumber(1_000_000))
