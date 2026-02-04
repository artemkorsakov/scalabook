package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.HarshadNumber.*
import weaver.FunSuite

object HarshadNumberSuite extends FunSuite:
  private val first50HarshadNumbers = Array(
    10, 12, 18, 20, 21, 24, 27, 30, 36,
    40, 42, 45, 48, 50, 54, 60, 63, 70, 72, 80, 81, 84, 90, 100, 102, 108, 110,
    111, 112, 114, 117, 120, 126, 132, 133, 135, 140, 144, 150, 152, 153, 156,
    162, 171, 180, 190, 192, 195, 198, 200
  )

  test("isHarshadNumber"):
    expect.all(
      isHarshadNumber(1728),
      isHarshadNumber(1729),
      !isHarshadNumber(1730),
      first50HarshadNumbers.forall(isHarshadNumber)
    )

  test("isStrongHarshadNumber"):
    expect.all(
      isStrongHarshadNumber(198),
      !isStrongHarshadNumber(1728),
      !isStrongHarshadNumber(1729),
      !isStrongHarshadNumber(1730)
    )

  test("getStrongRightTruncatableHarshadPrimes"):
    expect.all(
      getStrongRightTruncatableHarshadPrimes(3) ==
        Seq(181L, 211, 271, 277, 421, 457, 631),
      getStrongRightTruncatableHarshadPrimes(4).sum == 90619L,
      getStrongRightTruncatableHarshadPrimes(5).sum == 388207L
    )
end HarshadNumberSuite
