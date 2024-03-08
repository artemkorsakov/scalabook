package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*
import weaver.*
import weaver.scalacheck.*

object BinaryTreeSuite extends SimpleIOSuite with Checkers:
  private val exampleBinaryTree: BinaryTree[Char] = Branch(
    'A',
    Branch('B', Branch('D', Leaf, Leaf), Leaf),
    Branch(
      'C',
      Branch('E', Leaf, Branch('G', Leaf, Leaf)),
      Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
    )
  )

  test("BinaryTree.size"):
    forall: (list: List[Int]) =>
      expect.all(BinaryTree(list).size == list.length)

  pureTest("BinaryTree.height"):
    expect(exampleBinaryTree.height == 4)
