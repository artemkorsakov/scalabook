package ru.scalabook.algorithms.numbers

import weaver.FunSuite

object CollatzNumberSuite extends FunSuite:
  private val collatzNumber = CollatzNumber(100)

  test("collatz"):
    expect.all(
      collatzNumber.collatz(1) == 1L,
      collatzNumber.collatz(3) == 8L,
      collatzNumber.collatz(19) == 21L,
      collatzNumber.collatz(27) == 112L
    )
