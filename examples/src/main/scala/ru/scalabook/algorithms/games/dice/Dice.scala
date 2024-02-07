package ru.scalabook.algorithms.games.dice

import spire.compat.numeric
import spire.math.{Rational, SafeLong}

import scala.collection.mutable

final case class Dice(maxCube: Int):
  private val cache: mutable.Map[(Int, Int), SafeLong] =
    mutable.Map.empty[(Int, Int), SafeLong]

  /** Массив вероятностей получения заданных очков из куба с максимальной гранью
    * maxCube и заданным количеством бросков.
    */
  def probabilities(step: Int): Array[Rational] =
    val sums  = (1 to maxCube * step).map(i => cubeCount(step, i)).toArray
    val total = sums.sum
    sums.map(Rational(_, total))

  private def cubeCount(steps: Int, sum: Int): SafeLong =
    if steps < 0 || sum < steps then SafeLong(0)
    else if steps == sum then SafeLong(1)
    else if steps == 0 then SafeLong(0)
    else
      cache.getOrElseUpdate(
        (steps, sum),
        (1 to maxCube).map(i => cubeCount(steps - 1, sum - i)).sum
      )
end Dice
