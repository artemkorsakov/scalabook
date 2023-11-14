package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.HammingNumber.*
import weaver.FunSuite

object HammingNumberSuite extends FunSuite:
  test("isHammingNumber"):
    expect.all(
      isHammingNumber(2125764000, 5),
      isHammingNumber(2125764000, 7),
      !isHammingNumber(2125764000, 3)
    )
