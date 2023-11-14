package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.PerfectNumbers.*
import ru.scalabook.algorithms.fundamental.PerfectNumbersType.*
import weaver.FunSuite

object PerfectNumbersSuite extends FunSuite:
  test("Perfect numbers"):
    expect.all(
      perfectNumbersType(6) == Perfect,
      perfectNumbersType(7) == Deficient,
      perfectNumbersType(12) == Abundant
    )
