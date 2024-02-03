package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.CyclicNumbers.*

@main def triangleNumberBench(): Unit =
  println(triangleNumber(1_000_000))

@main def squareNumberBench(): Unit =
  println(squareNumber(1_000_000))

@main def pentagonalNumberBench(): Unit =
  println(pentagonalNumber(1_000_000))

@main def hexagonalNumberBench(): Unit =
  println(hexagonalNumber(1_000_000))

@main def heptagonalNumberBench(): Unit =
  println(heptagonalNumber(1_000_000))

@main def octagonalNumberBench(): Unit =
  println(octagonalNumber(1_000_000))
