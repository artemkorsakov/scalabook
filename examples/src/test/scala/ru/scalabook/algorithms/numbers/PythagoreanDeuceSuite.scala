package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.PythagoreanDeuce.*
import weaver.FunSuite

object PythagoreanDeuceSuite extends FunSuite:
  private val nextDeuces = primitivePythagoreanDeuce.nextPythagoreanDeuce

  test("nextPythagoreanDeuce"):
    expect.all(
      primitivePythagoreanDeuce.triplet == PythagoreanTriplet(3, 4, 5),
      nextDeuces.map(_.triplet) sameElements Array(
        PythagoreanTriplet(5, 12, 13),
        PythagoreanTriplet(21, 20, 29),
        PythagoreanTriplet(15, 8, 17)
      ),
      nextDeuces.flatMap(
        _.nextPythagoreanDeuce.map(_.triplet)
      ) sameElements Array(
        PythagoreanTriplet(7, 24, 25),
        PythagoreanTriplet(55, 48, 73),
        PythagoreanTriplet(45, 28, 53),
        PythagoreanTriplet(39, 80, 89),
        PythagoreanTriplet(119, 120, 169),
        PythagoreanTriplet(77, 36, 85),
        PythagoreanTriplet(33, 56, 65),
        PythagoreanTriplet(65, 72, 97),
        PythagoreanTriplet(35, 12, 37)
      )
    )
end PythagoreanDeuceSuite
