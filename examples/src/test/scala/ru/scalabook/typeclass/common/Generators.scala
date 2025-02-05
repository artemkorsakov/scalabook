package ru.scalabook.typeclass.common

import org.scalacheck.{Arbitrary, Gen}
import spire.math.Rational

object Generators:
  val smallNumbersGen: Gen[(Int, Int, Int)] =
    for
      x <- Gen.choose(1, 1000)
      y <- Gen.choose(1, 1000)
      z <- Gen.choose(1, 1000)
    yield (x, y, z)

  given rationalArb: Arbitrary[Rational] = Arbitrary(
    for
      x <- Gen.long
      y <- Gen.posNum[Long]
    yield Rational(x, y)
  )
end Generators
