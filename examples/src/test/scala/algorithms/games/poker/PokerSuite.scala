package algorithms.games.poker

import munit.FunSuite
import algorithms.games.poker.Poker

class PokerSuite extends FunSuite:
  test("doesPlayer1Win") {
    // Pair of Fives, Pair of Eights
    assertEquals(
      Poker("5H 5C 6S 7S KD", "2C 3S 8S 8D TD").doesPlayer1Win,
      false
    )
    // Highest card Ace, Highest card Queen
    assertEquals(Poker("5D 8C 9S JS AC", "2C 5C 7D 8S QH").doesPlayer1Win, true)
    // Three Aces, Flush with Diamonds
    assertEquals(
      Poker("2D 9C AS AH AC", "3D 6D 7D TD QD").doesPlayer1Win,
      false
    )
    // Pair of Queens Highest card Nine, Pair of Queens Highest card Seven
    assertEquals(Poker("4D 6S 9H QH QC", "3D 6D 7H QD QS").doesPlayer1Win, true)
    // Full House With Three Fours, Full House with Three Threes
    assertEquals(Poker("2H 2D 4C 4D 4S", "3C 3D 3S 9S 9D").doesPlayer1Win, true)
  }
