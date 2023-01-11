package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.PythagoreanDeuce.*
import algorithms.numbers.PythagoreanTriplet

class PythagoreanDeuceSuite extends FunSuite:
  private val nextDeuces = primitivePythagoreanDeuce.nextPythagoreanDeuce

  test("nextPythagoreanDeuce") {
    assertEquals(primitivePythagoreanDeuce.triplet, PythagoreanTriplet(3, 4, 5))
    assertEquals(
      nextDeuces.map(_.triplet),
      Seq(
        PythagoreanTriplet(5, 12, 13),
        PythagoreanTriplet(21, 20, 29),
        PythagoreanTriplet(15, 8, 17)
      )
    )
    assertEquals(
      nextDeuces.flatMap(_.nextPythagoreanDeuce.map(_.triplet)),
      Seq(
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
  }
