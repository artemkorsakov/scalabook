package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.CombinatorialOps.*
import weaver.FunSuite

object CombinatorialOpsSuite extends FunSuite:
  test("xcombinations"):
    expect.all(
      List("a", "b", "c").xcombinations(2) ==
        List(List("a", "b"), List("a", "c"), List("b", "c"))
    )

  test("xsubsets"):
    expect.all(
      List("a", "b", "c").xsubsets ==
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

  test("xvariations"):
    expect.all(
      List("a", "b", "c").xvariations(2) ==
        List(
          List("b", "a"),
          List("a", "b"),
          List("c", "a"),
          List("a", "c"),
          List("c", "b"),
          List("b", "c")
        )
    )

  test("xpermutations"):
    expect.all(
      List("a", "b", "c").xpermutations ==
        List(
          List("c", "b", "a"),
          List("c", "a", "b"),
          List("a", "c", "b"),
          List("b", "c", "a"),
          List("b", "a", "c"),
          List("a", "b", "c")
        )
    )
