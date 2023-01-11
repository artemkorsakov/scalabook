package algorithms.operations

import munit.FunSuite
import algorithms.combinatorics.CombinatorialOps.*

class CombinatorialOpsSuite extends FunSuite:
  test("xcombinations") {
    assertEquals(
      List("a", "b", "c").xcombinations(2),
      List(List("a", "b"), List("a", "c"), List("b", "c"))
    )
  }

  test("xsubsets") {
    assertEquals(
      List("a", "b", "c").xsubsets,
      List(
        List("a", "b", "c"),
        List("a", "b"),
        List("a", "c"),
        List("b", "c"),
        List("a"),
        List("b"),
        List("c")
      )
    )
  }

  test("xvariations") {
    assertEquals(
      List("a", "b", "c").xvariations(2),
      List(
        List("b", "a"),
        List("a", "b"),
        List("c", "a"),
        List("a", "c"),
        List("c", "b"),
        List("b", "c")
      )
    )
  }

  test("xpermutations") {
    assertEquals(
      List("a", "b", "c").xpermutations,
      List(
        List("c", "b", "a"),
        List("c", "a", "b"),
        List("a", "c", "b"),
        List("b", "c", "a"),
        List("b", "a", "c"),
        List("a", "b", "c")
      )
    )
  }
