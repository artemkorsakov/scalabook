package algorithms.games.poker

enum PokerSuit:
  case D, S, C, H

object PokerSuit:
  def withNameOpt(s: String): Option[PokerSuit] =
    PokerSuit.values.find(_.toString == s)
  def withNameOpt(s: Char): Option[PokerSuit] =
    PokerSuit.values.find(_.toString == s.toString)
