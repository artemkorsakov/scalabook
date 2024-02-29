package ru.scalabook.algorithms.search

import org.scalacheck.Gen
import ru.scalabook.algorithms.search.Search.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SearchSuite extends SimpleIOSuite with Checkers:
  private val text   = "This is a functional implementation."
  private val search = "functional"

  pureTest("linearSearch"):
    expect.all(
      linearSearch(text.toList, 'a').contains('a'),
      linearSearch(text.toList, 'b').isEmpty
    )

  test("binarySearch"):
    val list = (1 to 100).toList
    forall(Gen.choose(0, 100)): (k: Int) =>
      expect(binarySearch((1 to 100).toList, k).contains(k))

  test("binarySearch - invalid cases"):
    val list = (1 to 100).toList
    forall(Gen.choose(101, 200)): (k: Int) =>
      expect(binarySearch((1 to 100).toList, k).isEmpty)

  pureTest("naiveSubstringSearch"):
    expect.all(
      naiveSubstringSearch(search, text) == 10,
      naiveSubstringSearch("search", text) == -1
    )

  pureTest("kmpSubstringSearch"):
    expect.all(
      kmpSubstringSearch(search, text) == 10,
      kmpSubstringSearch("search", text) == -1
    )
end SearchSuite
