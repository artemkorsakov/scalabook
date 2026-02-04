package ru.scalabook.algorithms.games.dice

import spire.math.Rational
import weaver.FunSuite

object DiceSuite extends FunSuite:
  private val fractionsPete  = Dice(4).probabilities(9)
  private val fractionsColin = Dice(6).probabilities(6)

  private val (
    fractionWin,
    _
  ) = (1 until fractionsPete.length).foldLeft((Rational(0), Rational(0))) {
    case ((win, less), i) =>
      val newLess = less + fractionsColin(i - 1)
      val newWin  = win + fractionsPete(i) * newLess
      (newWin, newLess)
  }

  test("dice 1 step"):
    expect.all(Dice(6).probabilities(1).forall(r => r == Rational(1, 6)))

  test("probabilities"):
    expect.all((fractionWin.doubleValue - 0.5731441).abs <= 1e-5)
