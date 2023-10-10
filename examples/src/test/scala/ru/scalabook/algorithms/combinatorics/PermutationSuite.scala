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

  test("Permutation should construct by method 2"):
    assertEquals(
      constructByMethod2(Vector(2, 3, 1)),
      Vector(
        Vector(3, 4, 2, 1),
        Vector(3, 4, 1, 2),
        Vector(2, 4, 1, 3),
        Vector(2, 3, 1, 4)
      )
    )
