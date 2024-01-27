package ru.scalabook.algorithms.search

import ru.scalabook.algorithms.search.Search.*

import scala.util.Random

private val source: String     = Random.nextString(1_000_000)
private val searchWord: String = source.takeRight(100)

@main def searchBench(): Unit =
  println(naiveSubstringSearch(searchWord, source))
  println(kmpSubstringSearch(searchWord, source))
