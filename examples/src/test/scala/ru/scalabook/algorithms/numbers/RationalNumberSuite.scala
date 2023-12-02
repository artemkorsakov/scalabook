package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.RationalNumber.*
import weaver.FunSuite

object RationalNumberSuite extends FunSuite:
  private val x = RationalNumber(1, 3)
  private val y = RationalNumber(5, 7)

  test("add, sub, mul, div and upend"):
    expect.all(
      x.add(y) == RationalNumber(22, 21),
      x.sub(y) == RationalNumber(-8, 21),
      x.mul(y) == RationalNumber(5, 21),
      x.div(y) == RationalNumber(7, 15),
      x + y == RationalNumber(22, 21),
      x + 2 == RationalNumber(7, 3),
      x - y == RationalNumber(-8, 21),
      x - 2 == RationalNumber(-5, 3),
      x * y == RationalNumber(5, 21),
      x * 2 == RationalNumber(2, 3),
      x / y == RationalNumber(7, 15),
      x / 2 == RationalNumber(1, 6),
      x % y == RationalNumber(1, 3),
      x.upend == RationalNumber(3, 1),
      y.upend == RationalNumber(7, 5)
    )

  test("equal"):
    expect.all(
      x.equal(RationalNumber(1, 3)),
      x.equal(RationalNumber(-2, -6)),
      x.equal(RationalNumber(3, 9)),
      !x.equal(RationalNumber(1, 4)),
      x == RationalNumber(1, 3),
      x == RationalNumber(-2, -6),
      x == RationalNumber(3, 9),
      !(x == RationalNumber(1, 4)),
      RationalNumber(3) == RationalNumber(3, 1),
      RationalNumber(3L) == RationalNumber(3, 1),
      RationalNumber(BigInt(3)) == RationalNumber(3, 1)
    )

  test("compare") {
    expect.all(
      x < y,
      !(y < x),
      !(x < x),
      x <= y,
      x <= x,
      !(x > y),
      y > x,
      !(x > x),
      !(x >= y),
      x >= x,
      RationalNumber(2, 5) < RationalNumber(3, 7),
      RationalNumber(1, 2) > RationalNumber(3, 7)
    )
  }

  test("max"):
    expect.all(
      max(x, y) == y,
      max(x, x) == x,
      max(y, y) == y,
      (x max y) == y,
      (x max x) == x,
      (y max y) == y
    )

  test("toPercent"):
    expect.all(math.round(RationalNumber(5, 7).toPercent * 10000) == 7143L)
end RationalNumberSuite
