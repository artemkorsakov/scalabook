package ru.scalabook.algorithms.geometry

import weaver.FunSuite

import scala.math.*

object PointSuite extends FunSuite:
  test("CartesianPoint to PolarPoint conversion"):
    val cartesianPoint = CartesianPoint(3, 4)
    val polarPoint     = cartesianPoint.toPolarPoint

    expect.all(
      polarPoint.r == 5.0,
      polarPoint.theta == atan2(4, 3)
    )

  test("PolarPoint to CartesianPoint conversion"):
    val polarPoint     = PolarPoint(5, atan2(4, 3))
    val cartesianPoint = polarPoint.toCartesian

    expect.all(
      math.abs(cartesianPoint.x - 3.0) < 0.0001,
      math.abs(cartesianPoint.y - 4.0) < 0.0001
    )

  test("distance between two Cartesian points"):
    val p1 = CartesianPoint(1, 1)
    val p2 = CartesianPoint(4, 5)
    expect(Point.distance(p1, p2) == 5)

  test("distance between two Polar points"):
    val p1               = PolarPoint(2, Pi / 2)
    val p2               = PolarPoint(3, Pi / 3)
    val expectedDistance = sqrt(13 - 12 * cos(Pi / 6))
    expect(Point.distance(p1, p2) == expectedDistance)

  test("distance between a Cartesian and a Polar point"):
    val p1 = CartesianPoint(1, 1)
    val p2 = PolarPoint(sqrt(2), Pi / 4)
    val expectedDistance = sqrt(
      pow(1 - sqrt(2) * cos(Pi / 4), 2) +
        pow(1 - sqrt(2) * sin(Pi / 4), 2)
    )
    expect(Point.distance(p1, p2) == expectedDistance)

  test("distance between a Polar and a Cartesian point"):
    val p1 = PolarPoint(sqrt(2), Pi / 4)
    val p2 = CartesianPoint(1, 1)
    val expectedDistance = sqrt(
      pow(1 - sqrt(2) * cos(Pi / 4), 2) +
        pow(1 - sqrt(2) * sin(Pi / 4), 2)
    )
    expect(Point.distance(p1, p2) == expectedDistance)
end PointSuite
