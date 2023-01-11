package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.PythagoreanTriplet
import algorithms.numbers.PythagoreanTriplet.*

class PythagoreanTripletSuite extends FunSuite:
  private val nextTriplet = primitivePythagoreanTriplet.nextPythagoreanTriplet

  test("nextPythagoreanTriplet") {
    assertEquals(primitivePythagoreanTriplet, PythagoreanTriplet(3, 4, 5))
    assertEquals(
      nextTriplet,
      IndexedSeq(
        PythagoreanTriplet(5, 12, 13),
        PythagoreanTriplet(21, 20, 29),
        PythagoreanTriplet(15, 8, 17)
      )
    )
    assertEquals(
      nextTriplet.flatMap(_.nextPythagoreanTriplet),
      IndexedSeq(
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

  test("pythagoreanTripletsWithGivenSum") {
    assertEquals(
      pythagoreanTripletsWithGivenSum(1000),
      Seq(PythagoreanTriplet(375, 200, 425))
    )
  }
