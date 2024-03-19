package ru.scalabook.algorithms.geometry

import ru.scalabook.algorithms.geometry.PointOnTriangleType.*
import ru.scalabook.algorithms.geometry.Triangle
import ru.scalabook.algorithms.geometry.Triangle.*
import weaver.FunSuite

object TriangleSuite extends FunSuite:
  test("getPointOnTriangleType"):
    expect.all(
      Triangle((-340, 495), (-153, -910), (835, -947))
        .getPointOnTriangleType((0, 0)) == Inside,
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((0, 0)) == Outside,
      Triangle((-175, 41), (-421, -714), (574, -645))
        .getPointOnTriangleType((-175, 41)) == OnTheSide
    )

  test("isZeroPointInside"):
    expect.all(
      Triangle((-340, 495), (-153, -910), (835, -947)).isZeroPointInside,
      !Triangle((-175, 41), (-421, -714), (574, -645)).isZeroPointInside
    )
