package ru.scalabook.algorithms.search

import org.scalacheck.Gen
import ru.scalabook.algorithms.search.Search.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SearchSuite extends SimpleIOSuite with Checkers:
  private val text    = "This is a functional implementation."
  private val search  = "functional"
  private val numbers = (1 to 100).toList

  pureTest("linearSearch"):
    expect.all(
      linearSearch(text.toList, 'a').contains('a'),
      linearSearch(text.toList, 'b').isEmpty
    )

  test("binarySearch"):
    forall(Gen.choose(1, 100)): (k: Int) =>
      expect(binarySearch(numbers, k).contains(k))

  test("binarySearch - invalid cases"):
    forall(Gen.choose(101, 200)): (k: Int) =>
      expect(binarySearch(numbers, k).isEmpty)

  test("selectionSearch"):
    forall(Gen.choose(1, 100)): (k: Int) =>
      expect(selectionSearch(numbers, k).contains(k))

  test("selectionSearch - invalid cases"):
    forall(Gen.choose(101, 200)): (k: Int) =>
      expect(binarySearch(numbers, k).isEmpty)

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
