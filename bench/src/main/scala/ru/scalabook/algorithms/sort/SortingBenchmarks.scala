package ru.scalabook.algorithms.sort

import ru.scalabook.algorithms.sort.Sorting.*

private val unsortedArray: Array[Int] = (10_000 to 1 by -1).toArray

@main def bubbleSortBench(): Unit =
  bubbleSort(unsortedArray)

@main def selectionSortBench(): Unit =
  selectionSort(unsortedArray)

@main def insertionSortBench(): Unit =
  insertionSort(unsortedArray)

@main def mergeSortBench(): Unit =
  mergeSort(unsortedArray)

@main def quickSortBench(): Unit =
  quickSort(unsortedArray.toList)
