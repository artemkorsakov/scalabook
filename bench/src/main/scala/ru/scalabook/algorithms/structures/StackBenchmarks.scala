/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.headOptionStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.tailOptionStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.isEmptyStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.lengthStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.pushStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.popStackBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/StackBenchmarks.scala --main-class ru.scalabook.algorithms.structures.peekStackBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/structures/Stack.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.structures

import ru.scalabook.algorithms.structures.Stack.*
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import StackBenchmarks.*

private val emptyStack: Stack[Int] = EmptyStack
private val nonEmptyStack: Stack[Int] =
  NonEmptyStack(2, NonEmptyStack(1, EmptyStack))

@main def headOptionStackBench(): Unit =
  loadheadOptionStackBench()
  prettyPrint(Period.MicroSecond)

@main def tailOptionStackBench(): Unit =
  loadtailOptionStackBench()
  prettyPrint(Period.MicroSecond)

@main def isEmptyStackBench(): Unit =
  loadisEmptyStackBench()
  prettyPrint(Period.MicroSecond)

@main def lengthStackBench(): Unit =
  loadlengthStackBench()
  prettyPrint(Period.MicroSecond)

@main def pushStackBench(): Unit =
  loadpushStackBench()
  prettyPrint(Period.MicroSecond)

@main def popStackBench(): Unit =
  loadpopStackBench()
  prettyPrint(Period.MicroSecond)

@main def peekStackBench(): Unit =
  loadpeekStackBench()
  prettyPrint(Period.MicroSecond)

object StackBenchmarks extends MetricInstrumented:
  def loadheadOptionStackBench(): Unit = loading.time:
    println(emptyStack.headOption)
    println(nonEmptyStack.headOption)

  def loadtailOptionStackBench(): Unit = loading.time:
    println(emptyStack.tailOption)
    println(nonEmptyStack.tailOption)

  def loadisEmptyStackBench(): Unit = loading.time:
    println(emptyStack.isEmpty)
    println(nonEmptyStack.isEmpty)

  def loadlengthStackBench(): Unit = loading.time:
    println(emptyStack.length)
    println(nonEmptyStack.length)

  def loadpushStackBench(): Unit = loading.time:
    println(emptyStack.push(5))
    println(nonEmptyStack.push(5))

  def loadpopStackBench(): Unit = loading.time:
    println(emptyStack.pop())
    println(nonEmptyStack.pop())

  def loadpeekStackBench(): Unit = loading.time:
    println(emptyStack.peek())
    println(nonEmptyStack.peek())
end StackBenchmarks
