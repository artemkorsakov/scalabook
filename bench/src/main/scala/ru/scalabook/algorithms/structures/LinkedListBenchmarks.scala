package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.LinkedList.*

private val emptyList: LinkedList[Int] = LinkedList.Nil
private val nonEmptyList: LinkedList[Int] =
  LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil))

@main def headOptionBench(): Unit =
  println(emptyList.headOption)
  println(nonEmptyList.headOption)

@main def tailOptionBench(): Unit =
  println(emptyList.tailOption)
  println(nonEmptyList.tailOption)

@main def isEmptyBench(): Unit =
  println(emptyList.isEmpty)
  println(nonEmptyList.isEmpty)

@main def lengthBench(): Unit =
  println(emptyList.length)
  println(nonEmptyList.length)

@main def prependBench(): Unit =
  println(emptyList.prepend(5))
  println(nonEmptyList.prepend(5))

@main def appendBench(): Unit =
  println(emptyList.append(5))
  println(nonEmptyList.append(5))

@main def getBench(): Unit =
  println(emptyList.get(0))
  println(nonEmptyList.get(0))

@main def containsBench(): Unit =
  println(emptyList.contains(0))
  println(nonEmptyList.contains(2))

@main def concatBench(): Unit =
  println(emptyList.concat(nonEmptyList))
  println(nonEmptyList.concat(nonEmptyList))
