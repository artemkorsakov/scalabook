package ru.scalabook.algorithms.trees

import munit.FunSuite
import ru.scalabook.algorithms.trees.BinarySearchTree.*

class BinarySearchTreeSuite extends FunSuite:
  test("BinarySearchTree.searchKey") {
    val myWordList =
      List(("cat", 5), ("dog", 7), ("the", 12), ("for", 4), ("then", 11))
    val myBinSearchTree = myWordList
      .foldLeft(empty[Int]) { case (dict, (key, value)) =>
        dict.insert(key, value)
      }
      .updateValue("then", 16)
    assertEquals(myBinSearchTree.searchKey("for"), Some(4))
    assertEquals(myBinSearchTree.searchKey("then"), Some(16))
    assertEquals(myBinSearchTree.searchKey("that"), None)
  }
