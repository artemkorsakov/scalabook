package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Coins.*

@main def countWaysBench(): Unit =
  (1 to 10_000).foreach: i =>
    countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), i)

@main def partitionBench(): Unit =
  (1 to 1000).foreach: i =>
    partition(i)
