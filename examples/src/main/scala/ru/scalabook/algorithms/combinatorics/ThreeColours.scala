package ru.scalabook.algorithms.combinatorics

import spire.math.SafeLong

import scala.collection.mutable

object ThreeColours:
  private val cache: mutable.Map[(Int, Int, Int), SafeLong] =
    mutable.Map.empty[(Int, Int, Int), SafeLong]

  /** We have A white balls, B black balls and C grey balls. How many different
    * options to arrange these balls in a row?
    */
  def countABCRows(a: Int, b: Int, c: Int): SafeLong =
    val array = Array(a, b, c)
    val min   = array.min
    if min < 0 then 0
    else
      val max = array.max
      val mid = array.sum - min - max
      cache.getOrElseUpdate((min, mid, max), count(min, mid, max))

  private def count(min: Int, mid: Int, max: Int): SafeLong =
    if min == 0 && mid == 0 then 1
    else
      countABCRows(min - 1, mid, max) +
        countABCRows(min, mid - 1, max) +
        countABCRows(min, mid, max - 1)
end ThreeColours
