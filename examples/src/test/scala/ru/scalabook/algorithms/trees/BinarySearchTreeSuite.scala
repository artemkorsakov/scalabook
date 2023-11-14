package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinarySearchTree.*
import weaver.FunSuite

object BinarySearchTreeSuite extends FunSuite:
  test("BinarySearchTree.searchKey"):
    val myWordList =
      List(("cat", 5), ("dog", 7), ("the", 12), ("for", 4), ("then", 11))
    val myBinSearchTree = myWordList
      .foldLeft(empty[Int]) { case (dict, (key, value)) =>
        dict.insert(key, value)
      }
      .updateValue("then", 16)
    expect.all(
      myBinSearchTree.searchKey("for").contains(4),
      myBinSearchTree.searchKey("then").contains(16),
      myBinSearchTree.searchKey("that").isEmpty
    )
