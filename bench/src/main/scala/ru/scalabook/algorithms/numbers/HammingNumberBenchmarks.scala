package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.HammingNumber.isHammingNumber

@main def isHammingNumberBench(): Boolean =
  isHammingNumber(2125764000, 5)
