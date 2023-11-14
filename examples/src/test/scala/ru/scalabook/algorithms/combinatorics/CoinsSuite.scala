package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Coins.*
import weaver.FunSuite

object CoinsSuite extends FunSuite:
  test("Number of ways to get a given amount from a given coin composition"):
    expect.all(
      countWays(Array(200), 200) == 1L,
      countWays(Array(200), 199) == 0L,
      countWays(Array(1, 2, 5), 10) == 10L,
      countWays(Array(1, 2, 5, 10), 10) == 11L,
      countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), 200) == 73682L
    )

  test("Number of partitions"):
    expect.all(
      partition(10) == BigInt(42),
      partition(100) == BigInt(190569292),
      partition(1000) == BigInt("24061467864032622473692149727991")
    )
