package ru.scalabook.algorithms.search

import ru.scalabook.algorithms.search.Search.*

import scala.util.Random

private val text               = "This is a functional implementation."
private val source: String     = Random.nextString(1_000_000)
private val searchWord: String = source.takeRight(100)

@main def linearSearchBench(): Unit =
  println(linearSearch(text.toList, 'a'))

@main def searchBench(): Unit =
  println(naiveSubstringSearch(searchWord, source))
  println(kmpSubstringSearch(searchWord, source))
