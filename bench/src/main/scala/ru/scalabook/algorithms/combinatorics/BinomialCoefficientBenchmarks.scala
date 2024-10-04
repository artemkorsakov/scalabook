// scala-cli run bench/src/main/scala/ru/scalabook/algorithms/combinatorics/BinomialCoefficientBenchmarks.scala

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/combinatorics/BinomialCoefficient.scala

package ru.scalabook.algorithms.combinatorics

import nl.grons.metrics4.scala.DefaultInstrumented
import nl.grons.metrics4.scala.Timer

@main def binomialCoefficientBench(): Unit =
  (0 to 4).foreach: _ =>
    BinomialCoefficientBenchmarks.loadStuff()

  println(
    s"Число тестовых запусков = ${BinomialCoefficientBenchmarks.loading.count}"
  )
  println(
    f"Минимальное время = ${BinomialCoefficientBenchmarks.loading.min / 1000000000.0}%.3f секунд"
  )
  println(
    f"Максимальное время = ${BinomialCoefficientBenchmarks.loading.max / 1000000000.0}%.3f секунд"
  )
  println(
    f"Среднее время = ${BinomialCoefficientBenchmarks.loading.mean / 1000000000.0}%.3f секунд"
  )
  println(
    f"Стандартное отклонение = ${BinomialCoefficientBenchmarks.loading.stdDev / 1000000000.0}%.3f секунд"
  )
  println(
    f"75 персентиль = ${BinomialCoefficientBenchmarks.loading.snapshot.get75thPercentile / 1000000000.0}%.3f секунд"
  )
  println(
    f"95 персентиль = ${BinomialCoefficientBenchmarks.loading.snapshot.get95thPercentile / 1000000000.0}%.3f секунд"
  )
end binomialCoefficientBench

object BinomialCoefficientBenchmarks extends DefaultInstrumented:
  val loading: Timer = metrics.timer("loading")

  def loadStuff(): BigInt = loading.time {
    BinomialCoefficient.binomialCoefficient(100000, 50000)
  }
