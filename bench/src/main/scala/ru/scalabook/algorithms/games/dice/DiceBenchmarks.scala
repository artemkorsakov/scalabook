package ru.scalabook.algorithms.games.dice

import spire.math.Rational

@main def diceBench(): Array[Rational] =
  Dice(6).probabilities(100)
