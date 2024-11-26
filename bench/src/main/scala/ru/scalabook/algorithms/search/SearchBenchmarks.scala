/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/search/SearchBenchmarks.scala --main-class ru.scalabook.algorithms.search.linearSearchBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/search/SearchBenchmarks.scala --main-class ru.scalabook.algorithms.search.selectionSearchBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/search/SearchBenchmarks.scala --main-class ru.scalabook.algorithms.search.searchBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/search/SearchBenchmarks.scala --main-class ru.scalabook.algorithms.search.kmpSearchBench
 */

//> using dep nl.grons::metrics4-scala:4.3.3
//> using dep org.typelevel::cats-core:2.12.0
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/search/Search.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.search

import ru.scalabook.algorithms.search.Search.*

import scala.util.Random
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import SearchBenchmarks.*

private val text               = "This is a functional implementation."
private val source: String     = Random.nextString(1_000_000)
private val searchWord: String = source.takeRight(100)

@main def linearSearchBench(): Unit =
  (1 to 10).foreach: _ =>
    loadlinearSearchBench()
  prettyPrint(Period.MilliSecond)

@main def selectionSearchBench(): Unit =
  (1 to 10).foreach: _ =>
    loadselectionSearchBench()
  prettyPrint(Period.MilliSecond)

@main def searchBench(): Unit =
  (1 to 10).foreach: _ =>
    loadsearchBench()
  prettyPrint(Period.MilliSecond)

@main def kmpSearchBench(): Unit =
  (1 to 10).foreach: _ =>
    loadKmpSearchBench()
  prettyPrint(Period.MilliSecond)

object SearchBenchmarks extends MetricInstrumented:
  def loadlinearSearchBench(): Unit = loading.time:
    println(linearSearch(text.toList, 'a'))

  def loadselectionSearchBench(): Unit = loading.time:
    println(selectionSearch((1 to 100).toList, 45))

  def loadsearchBench(): Unit = loading.time:
    println(naiveSubstringSearch(searchWord, source))

  def loadKmpSearchBench(): Unit = loading.time:
    println(kmpSubstringSearch(searchWord, source))
