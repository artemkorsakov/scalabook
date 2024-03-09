package ru.scalabook.algorithms.trees

import cats.data.NonEmptyList
import ru.scalabook.algorithms.trees.BinaryTree.*
import weaver.*
import weaver.scalacheck.*

object BinaryTreeSuite extends SimpleIOSuite with Checkers:
  pureTest("isEmpty returns true for Leaf and false for non-Leaf"):
    expect(Leaf.isEmpty) and
      expect(!Branch(1, Leaf, Leaf).isEmpty)

  pureTest("size returns the correct size of the tree"):
    expect(Leaf.size == 0) and
      expect(Branch(1, Leaf, Leaf).size == 1) and
      expect(Branch(1, Branch(2, Leaf, Leaf), Branch(3, Leaf, Leaf)).size == 3)

  pureTest("height returns the correct height of the tree"):
    expect(Leaf.height == 0) and
      expect(Branch(1, Leaf, Leaf).height == 1) and
      expect(Branch(
        1,
        Branch(2, Leaf, Leaf),
        Branch(3, Leaf, Leaf)
      ).height == 2)

  pureTest("rootPath returns the correct path to the root element"):
    val tree = Branch(1, Branch(2, Leaf, Leaf), Branch(3, Leaf, Leaf))
    expect(tree.rootPath(1).contains(NonEmptyList.one(1))) and
      expect(tree.rootPath(2).contains(NonEmptyList.of(1, 2))) and
      expect(tree.rootPath(3).contains(NonEmptyList.of(1, 3))) and
      expect(tree.rootPath(4).isEmpty)

  pureTest("level returns the correct elements at a given level"):
    val tree = Branch(1, Branch(2, Leaf, Leaf), Branch(3, Leaf, Leaf))
    expect(tree.level(0) == List(1)) and
      expect(tree.level(1) == List(3, 2)) and
      expect(tree.level(2) == List.empty) and
      expect(tree.level(3) == List.empty)

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

  pureTest("BinaryTree.preorder"):
    expect.all(
      binaryTree.preorder == Vector(1, 2, 4, 3, 5, 7, 6, 8, 10),
      otherBinaryTree.preorder ==
        Vector('A', 'B', 'D', 'C', 'E', 'G', 'F', 'H', 'J')
    )

  pureTest("BinaryTree.inorder"):
    expect.all(
      binaryTree.inorder == Vector(4, 2, 1, 5, 7, 3, 8, 6, 10),
      otherBinaryTree.inorder ==
        Vector('D', 'B', 'A', 'E', 'G', 'C', 'H', 'F', 'J')
    )

  pureTest("BinaryTree.postorder"):
    expect.all(
      binaryTree.postorder == Vector(4, 2, 7, 5, 8, 10, 6, 3, 1),
      otherBinaryTree.postorder ==
        Vector('D', 'B', 'G', 'E', 'H', 'J', 'F', 'C', 'A')
    )

  pureTest("BinaryTree.areSimilar"):
    expect.all(
      areSimilar[Int, Char](binaryTree, otherBinaryTree),
      !areSimilar(Branch(9, binaryTree, Leaf), otherBinaryTree)
    )

  pureTest("BinaryTree.areEqual"):
    expect.all(
      !areEqual(binaryTree, otherBinaryTree),
      areEqual(binaryTree, binaryTree),
      areEqual(otherBinaryTree, otherBinaryTree)
    )

end BinaryTreeSuite
