package algorithms.sort

import algorithms.sort.Sorting.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class SortingSuite extends ScalaCheckSuite:
  property("bubbleSort") {
    forAll { (array: Array[Int]) =>
      bubbleSort(array)
      assert((1 until array.length).forall(i => array(i - 1) <= array(i)))
    }
  }

  property("selectionSort") {
    forAll { (array: Array[Int]) =>
      selectionSort(array)
      assert((1 until array.length).forall(i => array(i - 1) <= array(i)))
    }
  }

  property("insertionSort") {
    forAll(Gen.const(Array(5, 2, 4, 6, 1, 3))) { array =>
      insertionSort(array)
      val first = array.sameElements(Array(1, 2, 3, 4, 5, 6))
      insertionSortReverse(array)
      first && array.sameElements(Array(6, 5, 4, 3, 2, 1))
    }
  }

  property("mergeSort") {
    forAll(Gen.const(Array(5, 2, 4, 6, 1, 3))) { array =>
      mergeSort(array)
      array.sameElements(Array(1, 2, 3, 4, 5, 6))
    }
  }
