package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*
import weaver.FunSuite

object BinaryTreeOperationSuite extends FunSuite:
  private val binaryTree: BinaryTree[Int] = Branch(
    1,
    Branch(2, Branch(4, Leaf, Leaf), Leaf),
    Branch(
      3,
      Branch(5, Leaf, Branch(7, Leaf, Leaf)),
      Branch(6, Branch(8, Leaf, Leaf), Branch(10, Leaf, Leaf))
    )
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
    expect.all(
      binaryTree.preorder == Vector(1, 2, 4, 3, 5, 7, 6, 8, 10),
      otherBinaryTree.preorder ==
        Vector('A', 'B', 'D', 'C', 'E', 'G', 'F', 'H', 'J')
    )

  test("BinaryTree.inorder"):
    expect.all(
      binaryTree.inorder == Vector(4, 2, 1, 5, 7, 3, 8, 6, 10),
      otherBinaryTree.inorder ==
        Vector('D', 'B', 'A', 'E', 'G', 'C', 'H', 'F', 'J')
    )

  test("BinaryTree.postorder"):
    expect.all(
      binaryTree.postorder == Vector(4, 2, 7, 5, 8, 10, 6, 3, 1),
      otherBinaryTree.postorder ==
        Vector('D', 'B', 'G', 'E', 'H', 'J', 'F', 'C', 'A')
    )

  test("BinaryTree.areSimilar"):
    expect.all(
      areSimilar[Int, Char](binaryTree, otherBinaryTree),
      !areSimilar(Branch(9, binaryTree, Leaf), otherBinaryTree)
    )

  test("BinaryTree.areEqual"):
    expect.all(
      !areEqual(binaryTree, otherBinaryTree),
      areEqual(binaryTree, binaryTree),
      areEqual(otherBinaryTree, otherBinaryTree)
    )
