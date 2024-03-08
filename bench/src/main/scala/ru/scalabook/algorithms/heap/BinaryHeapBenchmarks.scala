package ru.scalabook.algorithms.heap

import ru.scalabook.algorithms.heap.BinaryHeap.*

private val emptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty
private val nonEmptyBinaryHeap: BinaryHeap[Int] =
  BinaryHeap.empty.insert(1).insert(2)

@main def isEmptyBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.isEmpty)
  println(nonEmptyBinaryHeap.isEmpty)

@main def sizeBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.size)
  println(nonEmptyBinaryHeap.size)

@main def heightBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.height)
  println(nonEmptyBinaryHeap.height)

@main def minOptionBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.minOption)
  println(nonEmptyBinaryHeap.minOption)

@main def insertBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.insert(5))
  println(nonEmptyBinaryHeap.insert(5))

@main def removeBinaryHeapBench(): Unit =
  println(emptyBinaryHeap.remove)
  println(nonEmptyBinaryHeap.remove)
