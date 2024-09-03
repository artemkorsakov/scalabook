package ru.scalabook.algorithms.structures

private val emptyList: LinkedList[Int] = LinkedList.Nil
private val nonEmptyList: LinkedList[Int] =
  LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil))

@main def headOptionLinkedListBench(): Unit =
  println(emptyList.headOption)
  println(nonEmptyList.headOption)

@main def tailOptionLinkedListBench(): Unit =
  println(emptyList.tailOption)
  println(nonEmptyList.tailOption)

@main def isEmptyLinkedListBench(): Unit =
  println(emptyList.isEmpty)
  println(nonEmptyList.isEmpty)

@main def lengthLinkedListBench(): Unit =
  println(emptyList.length)
  println(nonEmptyList.length)

@main def prependLinkedListBench(): Unit =
  println(emptyList.prepend(5))
  println(nonEmptyList.prepend(5))

@main def appendLinkedListBench(): Unit =
  println(emptyList.append(5))
  println(nonEmptyList.append(5))

@main def getLinkedListBench(): Unit =
  println(emptyList.get(0))
  println(nonEmptyList.get(0))

@main def containsLinkedListBench(): Unit =
  println(emptyList.contains(0))
  println(nonEmptyList.contains(2))

@main def concatLinkedListBench(): Unit =
  println(emptyList.concat(nonEmptyList))
  println(nonEmptyList.concat(nonEmptyList))

@main def filterLinkedListBench(): Unit =
  println(emptyList.filter(_ % 2 == 0))
  println(nonEmptyList.filter(_ % 2 == 0))

@main def mapLinkedListBench(): Unit =
  println(emptyList.map(_ + 1))
  println(nonEmptyList.map(_ + 1))

@main def foldLinkedListBench(): Unit =
  println(emptyList.fold(100)(_ + _))
  println(nonEmptyList.fold(100)(_ + _))
