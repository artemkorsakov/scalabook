package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.PhiFunction.*
import weaver.FunSuite

object PhiFunctionSuite extends FunSuite:
  test("totient"):
    expect.all(
      totient(1) == 1L,
      totient(2) == 1L,
      totient(3) == 2L,
      totient(4) == 2L,
      totient(5) == 4L,
      totient(6) == 2L,
      totient(7) == 6L,
      totient(8) == 4L,
      totient(9) == 6L,
      totient(10) == 4L,
      totient(87109) == 79180L,
      totient(284029) == 282940L,
      totient(9983167) == 9973816L,
      totient(5654317604L) == 2609145504L
    )

  test("totientArray"):
    val arrayErrors =
      totientArray(10000).zipWithIndex.filter { case (actual, index) =>
        actual != totient(index)
      }
    expect(arrayErrors.isEmpty)
end PhiFunctionSuite
