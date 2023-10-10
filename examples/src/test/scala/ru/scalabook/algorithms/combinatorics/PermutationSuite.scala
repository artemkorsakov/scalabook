package ru.scalabook.algorithms.combinatorics

import munit.FunSuite
import ru.scalabook.algorithms.combinatorics.Permutation.*

class PermutationSuite extends FunSuite:
  test("Permutation should construct by method 1"):
    assertEquals(
      constructByMethod1(Vector(2, 3, 1), 4),
      Vector(
        Vector(4, 2, 3, 1),
        Vector(2, 4, 3, 1),
        Vector(2, 3, 4, 1),
        Vector(2, 3, 1, 4)
      )
    )
