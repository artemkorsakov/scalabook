/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/sort/SortingBenchmarks.scala --main-class ru.scalabook.algorithms.sort.bubbleSortBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/sort/SortingBenchmarks.scala --main-class ru.scalabook.algorithms.sort.selectionSortBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/sort/SortingBenchmarks.scala --main-class ru.scalabook.algorithms.sort.insertionSortBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/sort/SortingBenchmarks.scala --main-class ru.scalabook.algorithms.sort.mergeSortBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/sort/SortingBenchmarks.scala --main-class ru.scalabook.algorithms.sort.quickSortBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/sort/Sorting.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.sort

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.sort.Sorting.*

import RadicalsBenchmarks.*

private val unsortedList: List[Int] = (10_000 to 1 by -1).toList

@main def bubbleSortBench(): Unit =
  (1 to 10).foreach: _ =>
    loadbubbleSortBench()
  prettyPrint(Period.MilliSecond)

@main def selectionSortBench(): Unit =
  (1 to 10).foreach: _ =>
    loadselectionSortBench()
  prettyPrint(Period.MilliSecond)

@main def insertionSortBench(): Unit =
  (1 to 10).foreach: _ =>
    loadinsertionSortBench()
  prettyPrint(Period.MilliSecond)

@main def mergeSortBench(): Unit =
  (1 to 10).foreach: _ =>
    loadmergeSortBench()
  prettyPrint(Period.MilliSecond)

@main def quickSortBench(): Unit =
  (1 to 10).foreach: _ =>
    loadquickSortBench()
  prettyPrint(Period.MilliSecond)

object RadicalsBenchmarks extends MetricInstrumented:
  def loadbubbleSortBench(): Unit = loading.time:
    bubbleSort(unsortedList)

  def loadselectionSortBench(): Unit = loading.time:
    selectionSort(unsortedList)

  def loadinsertionSortBench(): Unit = loading.time:
    insertionSort(unsortedList)

  def loadmergeSortBench(): Unit = loading.time:
    mergeSort(unsortedList)

  def loadquickSortBench(): Unit = loading.time:
    quickSort(unsortedList)
