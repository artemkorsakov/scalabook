package ru.scalabook.algorithms.trees

import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import ru.scalabook.algorithms.trees.BinaryTree.*

class BinaryTreeSuite extends ScalaCheckSuite:
  property("BinaryTree.size") {
    forAll { (list: List[Int]) =>
      assertEquals(BinaryTree(list).size, list.length)
    }
  }

  property("BinaryTree.depth") {
    forAll { (list: List[Int]) =>
      val expected =
        if list.isEmpty then 0 else list.length.toBinaryString.length
      assertEquals(BinaryTree(list).depth, expected)
    }
  }
