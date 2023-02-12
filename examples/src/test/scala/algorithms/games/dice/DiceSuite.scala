package algorithms.games.dice

import munit.FunSuite
import algorithms.games.dice.Dice
import algorithms.numbers.RationalNumber

class DiceSuite extends FunSuite:
  private val fractionsPete  = Dice(4).probabilities(9)
  private val fractionsColin = Dice(6).probabilities(6)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var fractionWin = new RationalNumber(0, 1)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var fractionLessColin = new RationalNumber(0, 1)

  for (i <- 1 until fractionsPete.length) do
    fractionLessColin += fractionsColin(i - 1)
    fractionWin += fractionsPete(i) * fractionLessColin

  test("probabilities") {
    val res = math.round(fractionWin.toPercent * 10000000) / 10000000.0
    assertEqualsDouble(res, 0.5731441, 1e-10)
  }
