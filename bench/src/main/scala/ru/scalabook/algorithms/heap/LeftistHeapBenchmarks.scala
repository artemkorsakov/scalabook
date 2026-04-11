/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.isEmptyLeftistHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.sizeLeftistHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.minOptionLeftistHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.insertLeftistHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.removeLeftistHeapBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeapBenchmarks.scala --main-class ru.scalabook.algorithms.heap.mergeLeftistHeapBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/heap/LeftistHeap.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.heap

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period

import LeftistHeapBenchmarks.*

private val emptyLeftistHeap: LeftistHeap[Int]    = LeftistHeap.empty
private val nonEmptyLeftistHeap: LeftistHeap[Int] =
  LeftistHeap.empty.insert(1).insert(2)

@main def isEmptyLeftistHeapBench(): Unit =
  loadisEmptyLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

@main def sizeLeftistHeapBench(): Unit =
  loadsizeLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

@main def minOptionLeftistHeapBench(): Unit =
  loadminOptionLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

@main def insertLeftistHeapBench(): Unit =
  loadinsertLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

@main def removeLeftistHeapBench(): Unit =
  loadremoveLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

@main def mergeLeftistHeapBench(): Unit =
  loadmergeLeftistHeapBench()
  prettyPrint(Period.MicroSecond)

object LeftistHeapBenchmarks extends MetricInstrumented:
  def loadisEmptyLeftistHeapBench(): Unit = loading.time:
    println(emptyLeftistHeap.isEmpty)
    println(nonEmptyLeftistHeap.isEmpty)

  def loadsizeLeftistHeapBench(): Unit = loading.time:
    println(emptyLeftistHeap.rank)
    println(nonEmptyLeftistHeap.rank)

  def loadminOptionLeftistHeapBench(): Unit = loading.time:
    println(emptyLeftistHeap.minOption)
    println(nonEmptyLeftistHeap.minOption)

  def loadinsertLeftistHeapBench(): Unit = loading.time:
    println(emptyLeftistHeap.insert(5))
    println(nonEmptyLeftistHeap.insert(5))

  def loadremoveLeftistHeapBench(): Unit = loading.time:
    println(emptyLeftistHeap.remove)
    println(nonEmptyLeftistHeap.remove)

  def loadmergeLeftistHeapBench(): Unit = loading.time:
    println(LeftistHeap.merge(emptyLeftistHeap, emptyLeftistHeap))
    println(LeftistHeap.merge(emptyLeftistHeap, nonEmptyLeftistHeap))
    println(LeftistHeap.merge(nonEmptyLeftistHeap, nonEmptyLeftistHeap))
end LeftistHeapBenchmarks
