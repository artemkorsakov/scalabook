package ru.scalabook.algorithms.games.poker

import cats.syntax.apply.*

final case class PokerCard(pokerRank: PokerRank, pokerSuit: PokerSuit)

object PokerCard:
  def string2PokerCard(card: String): Option[PokerCard] =
    if card.length < 2 || card.length > 3 then None
    else
      (
        PokerRank.withNameOpt(card.dropRight(1)),
        PokerSuit.withNameOpt(card.last)
      ).mapN(PokerCard.apply)
