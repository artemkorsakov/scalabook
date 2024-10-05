/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.sizeBinaryTreeByListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.heightBinaryTreeByListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.rootPathBinaryTreeByListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.levelPathBinaryTreeByListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.createBinaryTreeByListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/trees/BinaryTreeBenchmarks.scala --main-class ru.scalabook.algorithms.trees.createBinaryTreeByListBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::cats-core:2.12.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/trees/BinaryTree.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import BinaryTreeBenchmarks.*

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
  loadsizeBinaryTreeByListBench()
  prettyPrint(Period.MicroSecond)

@main def heightBinaryTreeByListBench(): Unit =
  loadheightBinaryTreeByListBench()
  prettyPrint(Period.MicroSecond)

@main def rootPathBinaryTreeByListBench(): Unit =
  loadrootPathBinaryTreeByListBench()
  prettyPrint(Period.MicroSecond)

@main def levelPathBinaryTreeByListBench(): Unit =
  loadlevelPathBinaryTreeByListBench()
  prettyPrint(Period.MicroSecond)

private val list   = (1 to 10000).toList
private val vector = (1 to 10000).toVector

@main def createBinaryTreeByListBench(): Unit =
  loadcreateBinaryTreeByListBench()
  prettyPrint(Period.MicroSecond)

@main def createBinaryTreeByVectorBench(): Unit =
  loadcreateBinaryTreeByVectorBench()
  prettyPrint(Period.MicroSecond)

object BinaryTreeBenchmarks extends MetricInstrumented:
  def loadsizeBinaryTreeByListBench(): Unit = loading.time:
    println(exampleBinaryTree.size)

  def loadheightBinaryTreeByListBench(): Unit = loading.time:
    println(exampleBinaryTree.height)

  def loadrootPathBinaryTreeByListBench(): Unit = loading.time:
    println(exampleBinaryTree.rootPath('G'))

  def loadlevelPathBinaryTreeByListBench(): Unit = loading.time:
    println(exampleBinaryTree.level(3))

  def loadcreateBinaryTreeByListBench(): Unit = loading.time:
    println(BinaryTree(list))

  def loadcreateBinaryTreeByVectorBench(): Unit = loading.time:
    println(BinaryTree(vector))
