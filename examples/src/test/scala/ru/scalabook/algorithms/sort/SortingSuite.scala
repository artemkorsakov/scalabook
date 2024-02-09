package ru.scalabook.algorithms.sort

import cats.Show
import ru.scalabook.algorithms.sort.Sorting.*
import weaver.scalacheck.Checkers
import weaver.{Expectations, SimpleIOSuite}

object SortingSuite extends SimpleIOSuite with Checkers:
  given Show[Array[Int]] = Show.show(_.mkString("[", ", ", "]"))

  test("bubbleSort"):
    forall: (list: List[Int]) =>
      val sortedList = bubbleSort(list)
      checkSortedList(sortedList)

  test("selectionSort"):
    forall: (list: List[Int]) =>
      val sortedList = selectionSort(list)
      checkSortedList(sortedList)

  test("insertionSort"):
    forall: (list: List[Int]) =>
      val sortedList = insertionSort(list)
      checkSortedList(sortedList)

  test("mergeSort"):
    forall: (list: List[Int]) =>
      val sortedList = mergeSort(list)
      checkSortedList(sortedList)

  test("quickSort"):
    forall: (list: List[Int]) =>
      val sortedList = quickSort(list)
      checkSortedList(sortedList)

  private def checkSortedList(list: List[Int]): Expectations =
    list match
      case h1 :: h2 :: tail => expect(h1 <= h2) and checkSortedList(h2 :: tail)
      case _                => success

end SortingSuite
