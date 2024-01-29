package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.PerfectNumbers.perfectNumbersType

@main def perfectNumbersTypeBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    perfectNumbersType(i)
