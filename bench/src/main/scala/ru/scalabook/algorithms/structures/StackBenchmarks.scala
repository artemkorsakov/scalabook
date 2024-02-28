package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.Stack.*

private val emptyStack: Stack[Int] = EmptyStack
private val nonEmptyStack: Stack[Int] =
  NonEmptyStack(2, NonEmptyStack(1, EmptyStack))

@main def headOptionStackBench(): Unit =
  println(emptyStack.headOption)
  println(nonEmptyStack.headOption)

@main def tailOptionStackBench(): Unit =
  println(emptyStack.tailOption)
  println(nonEmptyStack.tailOption)

@main def isEmptyStackBench(): Unit =
  println(emptyStack.isEmpty)
  println(nonEmptyStack.isEmpty)

@main def lengthStackBench(): Unit =
  println(emptyStack.length)
  println(nonEmptyStack.length)

@main def pushStackBench(): Unit =
  println(emptyStack.push(5))
  println(nonEmptyStack.push(5))

@main def popStackBench(): Unit =
  println(emptyStack.pop())
  println(nonEmptyStack.pop())

@main def peekStackBench(): Unit =
  println(emptyStack.peek())
  println(nonEmptyStack.peek())
