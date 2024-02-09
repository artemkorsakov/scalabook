package ru.scalabook.algorithms.sort

import ru.scalabook.algorithms.sort.Sorting.*

private val unsortedList: List[Int] = (10_000 to 1 by -1).toList

@main def bubbleSortBench(): Unit =
  bubbleSort(unsortedList)

@main def selectionSortBench(): Unit =
  selectionSort(unsortedList)

@main def insertionSortBench(): Unit =
  insertionSort(unsortedList)

private val unsortedArray: Array[Int] = (10_000 to 1 by -1).toArray

@main def mergeSortBench(): Unit =
  mergeSort(unsortedArray)

@main def quickSortBench(): Unit =
  quickSort(unsortedArray.toList)

@main def quickSort123Bench(): Unit =
  println(insertionSort(List(3, 1, 4, 2)))
