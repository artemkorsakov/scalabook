package algorithms.primes

import munit.FunSuite
import algorithms.primes.PhiFunction.*

class PhiFunctionSuite extends FunSuite:
  test("totient") {
    assertEquals(totient(1), 1L)
    assertEquals(totient(2), 1L)
    assertEquals(totient(3), 2L)
    assertEquals(totient(4), 2L)
    assertEquals(totient(5), 4L)
    assertEquals(totient(6), 2L)
    assertEquals(totient(7), 6L)
    assertEquals(totient(8), 4L)
    assertEquals(totient(9), 6L)
    assertEquals(totient(10), 4L)
    assertEquals(totient(87109), 79180L)
    assertEquals(totient(284029), 282940L)
    assertEquals(totient(9983167), 9973816L)
    assertEquals(totient(5654317604L), 2609145504L)
  }

  test("totientArray") {
    totientArray(10000).zipWithIndex.foreach { case (actual, index) =>
      assertEquals(actual, totient(index))
    }
  }
