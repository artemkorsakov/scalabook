/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.isEmptyBinaryHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.sizeBinaryHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.heightBinaryHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.minOptionBinaryHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.insertBinaryHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.removeBinaryHeapBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/heap/BinaryHeap.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.heap

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import BinaryHeapBenchmarks.*

private val emptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty
private val nonEmptyBinaryHeap: BinaryHeap[Int] =
  BinaryHeap.empty.insert(1).insert(2)

@main def isEmptyBinaryHeapBench(): Unit =
  loadisEmptyBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

@main def sizeBinaryHeapBench(): Unit =
  loadsizeBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

@main def heightBinaryHeapBench(): Unit =
  loadheightBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

@main def minOptionBinaryHeapBench(): Unit =
  loadminOptionBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

@main def insertBinaryHeapBench(): Unit =
  loadinsertBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

@main def removeBinaryHeapBench(): Unit =
  loadremoveBinaryHeapBench()
  prettyPrint(Period.MicroSecond)

object BinaryHeapBenchmarks extends MetricInstrumented:
  def loadisEmptyBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.isEmpty)
    println(nonEmptyBinaryHeap.isEmpty)

  def loadsizeBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.size)
    println(nonEmptyBinaryHeap.size)

  def loadheightBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.height)
    println(nonEmptyBinaryHeap.height)

  def loadminOptionBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.minOption)
    println(nonEmptyBinaryHeap.minOption)

  def loadinsertBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.insert(5))
    println(nonEmptyBinaryHeap.insert(5))

  def loadremoveBinaryHeapBench(): Unit = loading.time:
    println(emptyBinaryHeap.remove)
    println(nonEmptyBinaryHeap.remove)
end BinaryHeapBenchmarks
