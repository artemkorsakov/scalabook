package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.ThreeColours.*

@main def countABCRowsBench(): Unit =
  println(countABCRows(100, 100, 100))
