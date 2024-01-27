package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Permutation.*

@main def constructByMethod1Bench(): Vector[Vector[Int]] =
  constructByMethod1[Int]((1 to 19_999).toVector, 20_000)

@main def constructByMethod2Bench(): Vector[Vector[Int]] =
  constructByMethod2((1 to 4_999).toVector)
