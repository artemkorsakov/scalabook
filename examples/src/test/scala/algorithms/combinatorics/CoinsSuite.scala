package algorithms.combinatorics

import munit.FunSuite
import algorithms.combinatorics.Coins.*

class CoinsSuite extends FunSuite:
  test("Кол-во путей получить заданную сумму из заданного состава монет") {
    assertEquals(countWays(Array(200), 200), 1L)
    assertEquals(countWays(Array(200), 199), 0L)
    assertEquals(countWays(Array(1, 2, 5), 10), 10L)
    assertEquals(countWays(Array(1, 2, 5, 10), 10), 11L)
    assertEquals(countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), 200), 73682L)
  }

  test("Кол-во партиций") {
    assertEquals(partition(10), BigInt(42))
    assertEquals(partition(100), BigInt(190569292))
    assertEquals(partition(1000), BigInt("24061467864032622473692149727991"))
  }
