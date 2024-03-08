package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*

private val exampleBinaryTree: BinaryTree[Char] = Branch(
  'A',
  Branch('B', Branch('D', Leaf, Leaf), Leaf),
  Branch(
    'C',
    Branch('E', Leaf, Branch('G', Leaf, Leaf)),
    Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
  )
)

@main def sizeBinaryTreeByListBench(): Unit =
  println(exampleBinaryTree.size)

@main def heightBinaryTreeByListBench(): Unit =
  println(exampleBinaryTree.height)

@main def rootPathBinaryTreeByListBench(): Unit =
  println(exampleBinaryTree.rootPath('G'))

@main def levelPathBinaryTreeByListBench(): Unit =
  println(exampleBinaryTree.level(3))

private val list   = (1 to 10000).toList
private val vector = (1 to 10000).toVector

@main def createBinaryTreeByListBench(): BinaryTree[Int] =
  BinaryTree(list)

@main def createBinaryTreeByVectorBench(): BinaryTree[Int] =
  BinaryTree(vector)
