package ru.scalabook.algorithms.geometry

sealed trait Point

final case class CartesianPoint(x: Double, y: Double) extends Point:
  def toPolarPoint: PolarPoint =
    PolarPoint(math.sqrt(x * x + y * y), math.atan2(y, x))

final case class PolarPoint(r: Double, theta: Double) extends Point:
  def toCartesian: CartesianPoint =
    CartesianPoint(r * math.cos(theta), r * math.sin(theta))

object Point:
  def distance(p1: Point, p2: Point): Double =
    (p1, p2) match
      case (cp1 @ CartesianPoint(_, _), cp2 @ CartesianPoint(_, _)) =>
        distance(cp1, cp2)
      case (pp1 @ PolarPoint(_, _), pp2 @ PolarPoint(_, _)) =>
        distance(pp1, pp2)
      case (pp1 @ PolarPoint(_, _), cp2 @ CartesianPoint(_, _)) =>
        distance(pp1.toCartesian, cp2)
      case (cp1 @ CartesianPoint(_, _), pp2 @ PolarPoint(_, _)) =>
        distance(cp1, pp2.toCartesian)

  private def distance(p1: CartesianPoint, p2: CartesianPoint): Double =
    math.sqrt(math.pow(p1.x - p2.x, 2) + math.pow(p1.y - p2.y, 2))

  private def distance(p1: PolarPoint, p2: PolarPoint): Double =
    math.sqrt(
      p1.r * p1.r + p2.r * p2.r -
        2 * p1.r * p2.r * math.cos(p1.theta - p2.theta)
    )
end Point
