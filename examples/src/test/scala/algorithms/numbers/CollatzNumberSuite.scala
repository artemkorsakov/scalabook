package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.CollatzNumber.*

class CollatzNumberSuite extends FunSuite:
  private val collatzNumber = CollatzNumber(100)

  test("collatz") {
    assertEquals(collatzNumber.collatz(1), 1L)
    assertEquals(collatzNumber.collatz(3), 8L)
    assertEquals(collatzNumber.collatz(19), 21L)
    assertEquals(collatzNumber.collatz(27), 112L)
  }
