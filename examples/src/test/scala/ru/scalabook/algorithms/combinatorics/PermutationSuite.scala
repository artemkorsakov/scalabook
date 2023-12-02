package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Permutation.*
import weaver.FunSuite

object PermutationSuite extends FunSuite:
  test("Permutation should construct by method 1"):
    expect.all(
      constructByMethod1(Vector(2, 3, 1), 4) ==
        Vector(
          Vector(4, 2, 3, 1),
          Vector(2, 4, 3, 1),
          Vector(2, 3, 4, 1),
          Vector(2, 3, 1, 4)
        )
    )

  test("Permutation should construct by method 2"):
    expect.all(
      constructByMethod2(Vector(2, 3, 1)) ==
        Vector(
          Vector(3, 4, 2, 1),
          Vector(3, 4, 1, 2),
          Vector(2, 4, 1, 3),
          Vector(2, 3, 1, 4)
        )
    )
end PermutationSuite
