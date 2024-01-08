package ru.scalabook.algorithms.games.dice

import spire.math.Rational
import weaver.FunSuite

object DiceSuite extends FunSuite:
  private val fractionsPete  = Dice(4).probabilities(9)
  private val fractionsColin = Dice(6).probabilities(6)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var fractionWin = Rational(0)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var fractionLessColin = Rational(0)

  for i <- 1 until fractionsPete.length do
    fractionLessColin += fractionsColin(i - 1)
    fractionWin += fractionsPete(i) * fractionLessColin

  test("probabilities"):
    expect.all((fractionWin.doubleValue - 0.5731441).abs <= 1e-5)
