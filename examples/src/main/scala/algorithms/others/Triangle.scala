package algorithms.others

/** <a href="https://en.wikipedia.org/wiki/Triangle">Triangle</a> */
case class Triangle(point1: (Int, Int), point2: (Int, Int), point3: (Int, Int)):
  import PointOnTriangleType.*

  def getPointOnTriangleType(point: (Int, Int)): PointOnTriangleType =
    val first =
      (point1._1 - point._1) * (point2._2 - point1._2) - (point2._1 - point1._1) * (point1._2 - point._2)
    val second =
      (point2._1 - point._1) * (point3._2 - point2._2) - (point3._1 - point2._1) * (point2._2 - point._2)
    val third =
      (point3._1 - point._1) * (point1._2 - point3._2) - (point1._1 - point3._1) * (point3._2 - point._2)
    if first == 0 || second == 0 || third == 0 then OnTheSide
    else if first < 0 && second < 0 && third < 0 || first > 0 && second > 0 && third > 0
    then Inside
    else Outside

  /** Is zero point inside? */
  def isZeroPointInside: Boolean =
    getPointOnTriangleType((0, 0)).equals(Inside)

enum PointOnTriangleType:
  case Inside, Outside, OnTheSide
