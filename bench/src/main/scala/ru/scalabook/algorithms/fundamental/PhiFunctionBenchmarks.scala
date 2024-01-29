package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.PhiFunction.*

@main def totientBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    totient(i)

@main def totientArrayBench(): Array[Long] =
  totientArray(1_000_000)
