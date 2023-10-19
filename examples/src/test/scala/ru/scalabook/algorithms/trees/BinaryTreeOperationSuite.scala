package ru.scalabook.algorithms.trees

import munit.FunSuite
import ru.scalabook.algorithms.trees.BinaryTree.{Branch, Leaf}

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

  private val otherBinaryTree: BinaryTree[Char] = Branch(
    'A',
    Branch('B', Branch('D', Leaf, Leaf), Leaf),
    Branch(
      'C',
      Branch('E', Leaf, Branch('G', Leaf, Leaf)),
      Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
    )
  )

  test("BinaryTree.preorder"):
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
    assertEquals(
      otherBinaryTree.preorder,
      Vector('A', 'B', 'D', 'C', 'E', 'G', 'F', 'H', 'J')
    )

  test("BinaryTree.inorder"):
    assertEquals(binaryTree.inorder, Vector(0, 1, 2, 4, 3, 5, 6))
    assertEquals(
      otherBinaryTree.inorder,
      Vector('D', 'B', 'A', 'E', 'G', 'C', 'H', 'F', 'J')
    )

  test("BinaryTree.postorder"):
    assertEquals(binaryTree.postorder, Vector(0, 2, 1, 3, 4, 6, 5))
    assertEquals(
      otherBinaryTree.postorder,
      Vector('D', 'B', 'G', 'E', 'H', 'J', 'F', 'C', 'A')
    )
