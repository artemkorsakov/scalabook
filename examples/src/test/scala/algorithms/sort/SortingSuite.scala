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
    forAll { (array: Array[Int]) =>
      insertionSort(array)
      assert((1 until array.length).forall(i => array(i - 1) <= array(i)))
    }
  }

  property("mergeSort") {
    forAll { (array: Array[Int]) =>
      mergeSort(array)
      assert((1 until array.length).forall(i => array(i - 1) <= array(i)))
    }
  }
