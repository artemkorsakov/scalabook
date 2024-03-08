package ru.scalabook.algorithms.trees

private val list   = (1 to 10000).toList
private val vector = (1 to 10000).toVector

@main def createBinaryTreeByListBench(): BinaryTree[Int] =
  BinaryTree(list)

@main def createBinaryTreeByVectorBench(): BinaryTree[Int] =
  BinaryTree(vector)
