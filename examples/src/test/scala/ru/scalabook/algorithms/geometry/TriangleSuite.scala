package ru.scalabook.algorithms.geometry

import weaver.FunSuite

object TriangleSuite extends FunSuite:
  test("getPointOnTriangleType"):
    expect.all(
      Triangle((-340, 495), (-153, -910), (835, -947))
        .getPointOnTriangleType((0, 0)) == PointOnTriangleType.Inside,
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((0, 0)) == PointOnTriangleType.Outside,
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((-175, 41)) == PointOnTriangleType.OnTheSide
    )

  test("isZeroPointInside"):
    expect.all(
      Triangle((-340, 495), (-153, -910), (835, -947)).isZeroPointInside,
      !Triangle((-175, 41), (-421, -714), (574, -645)).isZeroPointInside
    )
