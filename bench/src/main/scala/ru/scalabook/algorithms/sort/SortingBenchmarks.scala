package ru.scalabook.algorithms.sort

import ru.scalabook.algorithms.sort.Sorting.*

private val unsortedList: List[Int] = (10_000 to 1 by -1).toList

@main def bubbleSortBench(): Unit =
  bubbleSort(unsortedList)

@main def selectionSortBench(): Unit =
  selectionSort(unsortedList)

@main def insertionSortBench(): Unit =
  insertionSort(unsortedList)

@main def mergeSortBench(): Unit =
  mergeSort(unsortedList)

@main def quickSortBench(): Unit =
  quickSort((2500 to 1 by -1).toList)
