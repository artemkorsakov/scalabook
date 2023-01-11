package algorithms.others

import munit.FunSuite
import algorithms.others.PointOnTriangleType.*
import algorithms.others.Triangle
import algorithms.others.Triangle.*

class TriangleSuite extends FunSuite:
  test("getPointOnTriangleType") {
    assertEquals(
      Triangle((-340, 495), (-153, -910), (835, -947))
        .getPointOnTriangleType((0, 0)),
      Inside
    )
    assertEquals(
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((0, 0)),
      Outside
    )
    assertEquals(
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((-175, 41)),
      OnTheSide
    )
  }

  test("isZeroPointInside") {
    assertEquals(
      Triangle((-340, 495), (-153, -910), (835, -947)).isZeroPointInside,
      true
    )
    assertEquals(
      Triangle((-175, 41), (-421, -714), (574, -645)).isZeroPointInside,
      false
    )
  }
