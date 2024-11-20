package ru.scalabook.algorithms.games.darts

object Darts:
  private val singlePoints: Seq[Int] = (1 to 20) :+ 25
  private val doublePoints: Seq[Int] = (2 to 40 by 2) :+ 50
  private val triplePoints: Seq[Int] = 3 to 60 by 3
  private val points: Seq[Int] = singlePoints ++ doublePoints ++ triplePoints

  val allDistinctWaysToCheckOut: Map[Int, Int] =
    // First step checkout
    val firstStepMap = doublePoints.map(point => point -> 1).toMap

    // Second step checkout
    val secondStepMap =
      (
        for
          value       <- points
          doublePoint <- doublePoints
        yield value + doublePoint
      ).foldLeft(firstStepMap) { case (acc, point) =>
        acc + (point -> (acc.getOrElse(point, 0) + 1))
      }

    // Third step checkout
    (
      for
        i           <- points.indices
        j           <- i until points.length
        doublePoint <- doublePoints
      yield points(i) + points(j) + doublePoint
    ).foldLeft(secondStepMap) { case (acc, point) =>
      acc + (point -> (acc.getOrElse(point, 0) + 1))
    }

  end allDistinctWaysToCheckOut
end Darts
