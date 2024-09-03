package ru.scalabook.algorithms.heap

private val emptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty
private val nonEmptyLeftistHeap: LeftistHeap[Int] =
  LeftistHeap.empty.insert(1).insert(2)

@main def isEmptyLeftistHeapBench(): Unit =
  println(emptyLeftistHeap.isEmpty)
  println(nonEmptyLeftistHeap.isEmpty)

@main def sizeLeftistHeapBench(): Unit =
  println(emptyLeftistHeap.rank)
  println(nonEmptyLeftistHeap.rank)

@main def minOptionLeftistHeapBench(): Unit =
  println(emptyLeftistHeap.minOption)
  println(nonEmptyLeftistHeap.minOption)

@main def insertLeftistHeapBench(): Unit =
  println(emptyLeftistHeap.insert(5))
  println(nonEmptyLeftistHeap.insert(5))

@main def removeLeftistHeapBench(): Unit =
  println(emptyLeftistHeap.remove)
  println(nonEmptyLeftistHeap.remove)

@main def mergeLeftistHeapBench(): Unit =
  println(LeftistHeap.merge(emptyLeftistHeap, emptyLeftistHeap))
  println(LeftistHeap.merge(emptyLeftistHeap, nonEmptyLeftistHeap))
  println(LeftistHeap.merge(nonEmptyLeftistHeap, nonEmptyLeftistHeap))
