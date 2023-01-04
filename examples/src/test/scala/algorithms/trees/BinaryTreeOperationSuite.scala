package algorithms.trees

import algorithms.trees.BinaryTree.{Branch, Leaf}
import munit.FunSuite

class BinaryTreeOperationSuite extends FunSuite:
  private val binaryTree: BinaryTree[Int] = Branch(
    5,
    Branch(
      4,
      Branch(1, Branch(0, Leaf, Leaf), Branch(2, Leaf, Leaf)),
      Branch(3, Leaf, Leaf)
    ),
    Branch(6, Leaf, Leaf)
  )

  test("BinaryTree.preorder") {
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
  }

  test("BinaryTree.inorder") {
    assertEquals(binaryTree.inorder, Vector(0, 1, 2, 4, 3, 5, 6))
  }

  test("BinaryTree.postorder") {
    assertEquals(binaryTree.postorder, Vector(0, 2, 1, 3, 4, 6, 5))
  }
