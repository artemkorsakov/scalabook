package algorithms.numbers

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import algorithms.numbers.CyclicNumbers.*

class CyclicNumbersSuite extends ScalaCheckSuite:
  private val FIRST_TRIANGLES   =
    Array(1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91)
  private val FIRST_SQUARE      = Array(1, 4, 9, 16, 25, 36, 49, 64, 81)
  private val FIRST_PENTAGONALS = Array(1, 5, 12, 22, 35, 51, 70, 92)
  private val FIRST_HEXAGONALS  = Array(1, 6, 15, 28, 45, 66, 91)
  private val FIRST_HEPTAGONAL  = Array(1, 7, 18, 34, 55, 81)
  private val FIRST_OCTAGONAL   = Array(1, 8, 21, 40, 65, 96)

  property("check firsts numbers") {
    forAll(
      Gen.oneOf(
        Seq(
          (1 to 13).forall(i => triangleNumber(i) == FIRST_TRIANGLES(i - 1)),
          (1 to 9).forall(i => squareNumber(i) == FIRST_SQUARE(i - 1)),
          (1 to 8).forall(i => pentagonalNumber(i) == FIRST_PENTAGONALS(i - 1)),
          (1 to 7).forall(i => hexagonalNumber(i) == FIRST_HEXAGONALS(i - 1)),
          (1 to 6).forall(i => heptagonalNumber(i) == FIRST_HEPTAGONAL(i - 1)),
          (1 to 6).forall(i => octagonalNumber(i) == FIRST_OCTAGONAL(i - 1))
        )
      )
    ) { actual => actual }
  }

  property("check IS methods") {
    forAll(Gen.oneOf(1 until 100)) { i =>
      isTriangle(i) == FIRST_TRIANGLES.contains(i) &&
      isSquare(i) == FIRST_SQUARE.contains(i) &&
      isPentagonal(i) == FIRST_PENTAGONALS.contains(i) &&
      isHexagonal(i) == FIRST_HEXAGONALS.contains(i) &&
      isHeptagonal(i) == FIRST_HEPTAGONAL.contains(i) &&
      isOctagonal(i) == FIRST_OCTAGONAL.contains(i)
    }
  }

  property("check special is numbers") {
    forAll(
      Gen.oneOf(
        Seq(
          isPentagonal(5482660),
          isPentagonal(7042750),
          isPentagonal(1560090),
          isPentagonal(8602840),
          isPentagonal(40755),
          isTriangle(40755),
          isHexagonal(40755),
          isTriangle(8256),
          isSquare(5625),
          isPentagonal(2882),
          isHexagonal(8128),
          isHeptagonal(2512),
          isOctagonal(1281)
        )
      )
    ) { actual => actual }
  }
