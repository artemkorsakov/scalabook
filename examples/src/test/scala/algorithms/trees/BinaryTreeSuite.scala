package algorithms.trees

import algorithms.trees.BinaryTree.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class BinaryTreeSuite extends ScalaCheckSuite:
  property("BinaryTree.size") {
    forAll { (list: List[Int]) =>
      BinaryTree(list).size == list.length
    }
  }

  property("BinaryTree.depth") {
    forAll { (list: List[Int]) =>
      val expected = if list.isEmpty then 0 else list.length.toBinaryString.length
      BinaryTree(list).depth == expected
    }
  }
