package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.PythagoreanTriplet.*

@main def nextPythagoreanTripletBench(): Unit =
  println(pythagoreanTripletsWithGivenSum(1000).mkString("Array(", ", ", ")"))
