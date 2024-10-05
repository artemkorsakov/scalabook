/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/QueueBenchmarks.scala --main-class ru.scalabook.algorithms.structures.frontOptionQueueBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/QueueBenchmarks.scala --main-class ru.scalabook.algorithms.structures.rearOptionQueueBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/QueueBenchmarks.scala --main-class ru.scalabook.algorithms.structures.isEmptyQueueBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/QueueBenchmarks.scala --main-class ru.scalabook.algorithms.structures.enqueueQueueBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/QueueBenchmarks.scala --main-class ru.scalabook.algorithms.structures.dequeueQueueBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/structures/Queue.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.structures

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import QueueBenchmarks.*

private val emptyQueue: Queue[Int]    = Queue.empty[Int]
private val nonEmptyQueue: Queue[Int] = emptyQueue.enqueue(1).enqueue(2)

@main def frontOptionQueueBench(): Unit =
  loadfrontOptionQueueBench()
  prettyPrint(Period.MicroSecond)

@main def rearOptionQueueBench(): Unit =
  loadrearOptionQueueBench()
  prettyPrint(Period.MicroSecond)

@main def isEmptyQueueBench(): Unit =
  loadisEmptyQueueBench()
  prettyPrint(Period.MicroSecond)

@main def enqueueQueueBench(): Unit =
  loadenqueueQueueBench()
  prettyPrint(Period.MicroSecond)

@main def dequeueQueueBench(): Unit =
  loaddequeueQueueBench()
  prettyPrint(Period.MicroSecond)

object QueueBenchmarks extends MetricInstrumented:
  def loadfrontOptionQueueBench(): Unit = loading.time:
    println(emptyQueue.frontOption)
    println(nonEmptyQueue.frontOption)

  def loadrearOptionQueueBench(): Unit = loading.time:
    println(emptyQueue.rearOption)
    println(nonEmptyQueue.rearOption)

  def loadisEmptyQueueBench(): Unit = loading.time:
    println(emptyQueue.isEmpty)
    println(nonEmptyQueue.isEmpty)

  def loadenqueueQueueBench(): Unit = loading.time:
    println(emptyQueue.enqueue(5))
    println(nonEmptyQueue.enqueue(5))

  def loaddequeueQueueBench(): Unit = loading.time:
    println(emptyQueue.dequeue)
    println(nonEmptyQueue.dequeue)
end QueueBenchmarks
