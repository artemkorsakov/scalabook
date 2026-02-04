/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.headOptionLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.tailOptionLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.isEmptyLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.lengthLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.prependLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.appendLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.getLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.containsLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.concatLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.filterLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.mapLinkedListBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/structures/LinkedListBenchmarks.scala --main-class ru.scalabook.algorithms.structures.foldLinkedListBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/structures/LinkedList.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.structures

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import LinkedListBenchmarks.*

private val emptyList: LinkedList[Int]    = LinkedList.Nil
private val nonEmptyList: LinkedList[Int] =
  LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil))

@main def headOptionLinkedListBench(): Unit =
  loadheadOptionLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def tailOptionLinkedListBench(): Unit =
  loadtailOptionLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def isEmptyLinkedListBench(): Unit =
  loadisEmptyLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def lengthLinkedListBench(): Unit =
  loadlengthLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def prependLinkedListBench(): Unit =
  loadprependLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def appendLinkedListBench(): Unit =
  loadappendLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def getLinkedListBench(): Unit =
  loadgetLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def containsLinkedListBench(): Unit =
  loadcontainsLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def concatLinkedListBench(): Unit =
  loadconcatLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def filterLinkedListBench(): Unit =
  loadfilterLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def mapLinkedListBench(): Unit =
  loadmapLinkedListBench()
  prettyPrint(Period.MicroSecond)

@main def foldLinkedListBench(): Unit =
  loadfoldLinkedListBench()
  prettyPrint(Period.MicroSecond)

object LinkedListBenchmarks extends MetricInstrumented:
  def loadheadOptionLinkedListBench(): Unit = loading.time:
    println(emptyList.headOption)
    println(nonEmptyList.headOption)

  def loadtailOptionLinkedListBench(): Unit = loading.time:
    println(emptyList.tailOption)
    println(nonEmptyList.tailOption)

  def loadisEmptyLinkedListBench(): Unit = loading.time:
    println(emptyList.isEmpty)
    println(nonEmptyList.isEmpty)

  def loadlengthLinkedListBench(): Unit = loading.time:
    println(emptyList.length)
    println(nonEmptyList.length)

  def loadprependLinkedListBench(): Unit = loading.time:
    println(emptyList.prepend(5))
    println(nonEmptyList.prepend(5))

  def loadappendLinkedListBench(): Unit = loading.time:
    println(emptyList.append(5))
    println(nonEmptyList.append(5))

  def loadgetLinkedListBench(): Unit = loading.time:
    println(emptyList.get(0))
    println(nonEmptyList.get(0))

  def loadcontainsLinkedListBench(): Unit = loading.time:
    println(emptyList.contains(0))
    println(nonEmptyList.contains(2))

  def loadconcatLinkedListBench(): Unit = loading.time:
    println(emptyList.concat(nonEmptyList))
    println(nonEmptyList.concat(nonEmptyList))

  def loadfilterLinkedListBench(): Unit = loading.time:
    println(emptyList.filter(_ % 2 == 0))
    println(nonEmptyList.filter(_ % 2 == 0))

  def loadmapLinkedListBench(): Unit = loading.time:
    println(emptyList.map(_ + 1))
    println(nonEmptyList.map(_ + 1))

  def loadfoldLinkedListBench(): Unit = loading.time:
    println(emptyList.fold(100)(_ + _))
    println(nonEmptyList.fold(100)(_ + _))
end LinkedListBenchmarks
