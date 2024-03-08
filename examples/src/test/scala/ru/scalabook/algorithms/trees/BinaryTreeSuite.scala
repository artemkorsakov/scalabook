package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*
import weaver.*
import weaver.scalacheck.*

object BinaryTreeSuite extends SimpleIOSuite with Checkers:
  test("BinaryTree.size"):
    forall: (list: List[Int]) =>
      expect.all(BinaryTree(list).size == list.length)

  test("BinaryTree.height"):
    forall: (list: List[Int]) =>
      val expected =
        if list.isEmpty then 0 else list.length.toBinaryString.length
      expect.all(BinaryTree(list).height == expected)
