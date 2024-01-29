package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Radicals.*

@main def radBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    rad(i)

@main def allRadsBench(): Array[Int] =
  allRads(1_000_000)
