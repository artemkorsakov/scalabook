package ru.scalabook.algorithms.fundamental

import munit.FunSuite
import ru.scalabook.algorithms.fundamental.PerfectNumbers.*
import ru.scalabook.algorithms.fundamental.PerfectNumbersType.*

class PerfectNumbersSuite extends FunSuite:
  test("Совершенные числа") {
    assertEquals(perfectNumbersType(6), Perfect)
    assertEquals(perfectNumbersType(7), Deficient)
    assertEquals(perfectNumbersType(12), Abundant)
  }
