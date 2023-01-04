package algorithms.search

import algorithms.search.Search.{kmpSubstringSearch, naiveSubstringSearch}
import munit.FunSuite

class SearchSuite extends FunSuite:
  private val text   = "This is a functional implementation."
  private val search = "functional"

  test("naiveSubstringSearch") {
    assertEquals(naiveSubstringSearch(search, text), 10)
    assertEquals(naiveSubstringSearch("search", text), -1)
  }

  test("kmpSubstringSearch") {
    assertEquals(kmpSubstringSearch(search, text), 10)
    assertEquals(kmpSubstringSearch("search", text), -1)
  }
