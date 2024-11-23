/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.memoizedFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.iterativeFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.fastFibBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/fundamental/FibonacciBenchmarks.scala --main-class ru.scalabook.algorithms.fundamental.approximateFibonacciBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
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
  prettyPrint(Period.Second)

@main def iterativeFibBench(): Unit =
  (1 to 10).foreach: _ =>
    val _ = loadIterativeFib
  prettyPrint(Period.Second)

@main def fastFibBench(): Unit =
  (1 to 10).foreach: _ =>
    val _ = loadFastFib
  prettyPrint(Period.Second)

@main def approximateFibonacciBench(): Unit =
  (1 to 10).foreach: _ =>
    val _ = loadApproximateFibonacci
  prettyPrint(Period.Second)

object FibonacciBenchmarks extends MetricInstrumented:
  def loadMemoizedFib: BigInt = loading.time:
    memoizedFib(2000)

  def loadIterativeFib: BigInt = loading.time:
    iterativeFib(1_000_000)

  def loadFastFib: BigInt = loading.time:
    fastFib(1_000_000)

  def loadApproximateFibonacci: BigInt = loading.time:
    approximateFibonacci(1_000_000)
