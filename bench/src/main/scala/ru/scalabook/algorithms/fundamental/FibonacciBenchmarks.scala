/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.memoizedFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.iterativeFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.fastFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.approximateFibonacciBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.scalaz::scalaz-core:7.3.8
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/fundamental/Fibonacci.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Fibonacci.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import FibonacciBenchmarks.*

@main def memoizedFibBench(): Unit =
  println(loadMemoizedFib)
  prettyPrint(Period.MicroSecond)

@main def iterativeFibBench(): Unit =
  println(loadIterativeFib)
  prettyPrint(Period.MicroSecond)

@main def fastFibBench(): Unit =
  println(loadFastFib)
  prettyPrint(Period.MicroSecond)

@main def approximateFibonacciBench(): Unit =
  println(loadApproximateFibonacci)
  prettyPrint(Period.MicroSecond)

object FibonacciBenchmarks extends MetricInstrumented:
  def loadMemoizedFib: BigInt = loading.time:
    memoizedFib(2000)

  def loadIterativeFib: BigInt = loading.time:
    iterativeFib(1_000_000)

  def loadFastFib: BigInt = loading.time:
    fastFib(1_000_000)

  def loadApproximateFibonacci: BigInt = loading.time:
    approximateFibonacci(1_000_000)
