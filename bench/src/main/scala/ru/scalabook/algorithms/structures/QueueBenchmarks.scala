package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.Queue.*

private val emptyQueue: Queue[Int]    = Queue.empty[Int]
private val nonEmptyQueue: Queue[Int] = emptyQueue.enqueue(1).enqueue(2)

@main def frontOptionQueueBench(): Unit =
  println(emptyQueue.frontOption)
  println(nonEmptyQueue.frontOption)

@main def rearOptionQueueBench(): Unit =
  println(emptyQueue.rearOption)
  println(nonEmptyQueue.rearOption)

@main def isEmptyQueueBench(): Unit =
  println(emptyQueue.isEmpty)
  println(nonEmptyQueue.isEmpty)

@main def enqueueQueueBench(): Unit =
  println(emptyQueue.enqueue(5))
  println(nonEmptyQueue.enqueue(5))

@main def dequeueQueueBench(): Unit =
  println(emptyQueue.dequeue)
  println(nonEmptyQueue.dequeue)
