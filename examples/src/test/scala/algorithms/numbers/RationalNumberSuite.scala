package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.RationalNumber
import algorithms.numbers.RationalNumber.*

class RationalNumberSuite extends FunSuite:
  private val x = RationalNumber(1, 3)
  private val y = RationalNumber(5, 7)

  test("add, sub, mul, div and upend") {
    assertEquals(x.add(y), RationalNumber(22, 21))
    assertEquals(x.sub(y), RationalNumber(-8, 21))
    assertEquals(x.mul(y), RationalNumber(5, 21))
    assertEquals(x.div(y), RationalNumber(7, 15))
    assertEquals(x + y, RationalNumber(22, 21))
    assertEquals(x + 2, RationalNumber(7, 3))
    assertEquals(x - y, RationalNumber(-8, 21))
    assertEquals(x - 2, RationalNumber(-5, 3))
    assertEquals(x * y, RationalNumber(5, 21))
    assertEquals(x * 2, RationalNumber(2, 3))
    assertEquals(x / y, RationalNumber(7, 15))
    assertEquals(x / 2, RationalNumber(1, 6))
    assertEquals(x % y, RationalNumber(1, 3))
    assertEquals(x.upend, RationalNumber(3, 1))
    assertEquals(y.upend, RationalNumber(7, 5))
  }

  test("equal") {
    assertEquals(x.equal(RationalNumber(1, 3)), true)
    assertEquals(x.equal(RationalNumber(-2, -6)), true)
    assertEquals(x.equal(RationalNumber(3, 9)), true)
    assertEquals(x.equal(RationalNumber(1, 4)), false)
    assertEquals(x == RationalNumber(1, 3), true)
    assertEquals(x == RationalNumber(-2, -6), true)
    assertEquals(x == RationalNumber(3, 9), true)
    assertEquals(x == RationalNumber(1, 4), false)
    assertEquals(RationalNumber(3) == RationalNumber(3, 1), true)
    assertEquals(RationalNumber(3L) == RationalNumber(3, 1), true)
    assertEquals(RationalNumber(BigInt(3)) == RationalNumber(3, 1), true)
  }

  test("compare") {
    assertEquals(x < y, true)
    assertEquals(y < x, false)
    assertEquals(x < x, false)
    assertEquals(x <= y, true)
    assertEquals(x <= x, true)
    assertEquals(x > y, false)
    assertEquals(y > x, true)
    assertEquals(x > x, false)
    assertEquals(x >= y, false)
    assertEquals(x >= x, true)
    assertEquals(RationalNumber(2, 5) < RationalNumber(3, 7), true)
    assertEquals(RationalNumber(1, 2) > RationalNumber(3, 7), true)
  }

  test("max") {
    assertEquals(max(x, y), y)
    assertEquals(max(x, x), x)
    assertEquals(max(y, y), y)
    assertEquals(x max y, y)
    assertEquals(x max x, x)
    assertEquals(y max y, y)
  }

  test("toPercent") {
    assertEquals(math.round(RationalNumber(5, 7).toPercent * 10000), 7143L)
  }
