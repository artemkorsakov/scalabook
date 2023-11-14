package ru.scalabook.algorithms.search

import ru.scalabook.algorithms.search.Search.{
  kmpSubstringSearch,
  naiveSubstringSearch
}
import weaver.FunSuite

object SearchSuite extends FunSuite:
  private val text   = "This is a functional implementation."
  private val search = "functional"

  test("naiveSubstringSearch"):
    expect.all(
      naiveSubstringSearch(search, text) == 10,
      naiveSubstringSearch("search", text) == -1
    )

  test("kmpSubstringSearch"):
    expect.all(
      kmpSubstringSearch(search, text) == 10,
      kmpSubstringSearch("search", text) == -1
    )
