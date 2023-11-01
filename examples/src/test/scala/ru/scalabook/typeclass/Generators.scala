package ru.scalabook.typeclass

import org.scalacheck.Gen

object Generators:
  val smallNumbersGen: Gen[(Int, Int, Int)] =
    for
      x <- Gen.choose(1, 1000)
      y <- Gen.choose(1, 1000)
      z <- Gen.choose(1, 1000)
    yield (x, y, z)
