package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.BinomialCoefficient.*

@main def binomialCoefficientBench(): BigInt =
  binomialCoefficient(100000, 50000)
