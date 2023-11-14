package ru.scalabook.algorithms.sort

import cats.Show
import ru.scalabook.algorithms.sort.Sorting.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SortingSuite extends SimpleIOSuite with Checkers:
  given Show[Array[Int]] = Show.show(_.mkString("[", ", ", "]"))

  test("bubbleSort"):
    forall: (array: Array[Int]) =>
      bubbleSort(array)
      expect.all((1 until array.length).forall(i => array(i - 1) <= array(i)))

  test("selectionSort"):
    forall: (array: Array[Int]) =>
      selectionSort(array)
      expect.all((1 until array.length).forall(i => array(i - 1) <= array(i)))

  test("insertionSort"):
    forall: (array: Array[Int]) =>
      insertionSort(array)
      expect.all((1 until array.length).forall(i => array(i - 1) <= array(i)))

  test("mergeSort"):
    forall: (array: Array[Int]) =>
      mergeSort(array)
      expect.all((1 until array.length).forall(i => array(i - 1) <= array(i)))

  test("quickSort"):
    forall: (list: List[Int]) =>
      expect.all(quickSort(list) == list.sorted)
