package algorithms.games.poker

import algorithms.games.poker.PokerCard.*

class PokerHand(hand: String):
  private val split: Array[String]                = hand.split(" ")
  require(split.length == 5)
  private val cards: Seq[PokerCard]               = split.flatMap(string2PokerCard).toSeq
  private val sortedRanks: Seq[PokerRank]         =
    cards
      .map(card => card.pokerRank)
      .sortWith((pr1, pr2) => pr2.rank - pr1.rank < 0)
  private val sortedDistinctRanks: Seq[PokerRank] = sortedRanks.distinct

  private def isFiveOfAKind: Boolean                        =
    sortedDistinctRanks.length == 1
  private def fiveOfAKind: (PokerHandsType, Seq[PokerRank]) =
    (
      PokerHandsType.FIVE_OF_A_KIND,
      Seq(
        cards.head.pokerRank,
        PokerRank.TWO,
        PokerRank.TWO,
        PokerRank.TWO,
        PokerRank.TWO
      )
    )

  private def isStraightFlush: Boolean                        =
    isStraight && isFlush
  private def straightFlush: (PokerHandsType, Seq[PokerRank]) =
    (PokerHandsType.STRAIGHT_FLUSH, straight._2)

  private def isStraight: Boolean                        =
    sortedRanks == Seq(
      PokerRank.FIVE,
      PokerRank.FOUR,
      PokerRank.THREE,
      PokerRank.TWO,
      PokerRank.A
    ) || (0 until sortedRanks.length - 1).forall(i =>
      sortedRanks(i).rank - sortedRanks(i + 1).rank == 1
    )
  private def straight: (PokerHandsType, Seq[PokerRank]) =
    val pokerRank =
      if sortedRanks.head == PokerRank.A && sortedRanks(1) == PokerRank.FIVE
      then PokerRank.FIVE
      else sortedRanks.head
    (
      PokerHandsType.STRAIGHT,
      Seq(pokerRank, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private def isFlush: Boolean                        =
    cards.map(_.pokerSuit.ordinal).distinct.length == 1
  private def flush: (PokerHandsType, Seq[PokerRank]) =
    (PokerHandsType.FLUSH, sortedRanks)

  private def isFourOfAKind: Boolean                        =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(c => c.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 1 || countOfFirstCard == 4)
  private def fourOfAKind: (PokerHandsType, Seq[PokerRank]) =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val pokerRank1         =
      if isFirstEqualSecond then sortedRanks.head else sortedRanks(1)
    val pokerRank2         =
      if isFirstEqualSecond then sortedRanks(4) else sortedRanks.head
    (
      PokerHandsType.FOUR_OF_A_KIND,
      Seq(pokerRank1, pokerRank2, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private def isFullHouse: Boolean                        =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(c => c.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 2 || countOfFirstCard == 3)
  private def fullHouse: (PokerHandsType, Seq[PokerRank]) =
    val pokerRank1 = sortedRanks(2)
    val pokerRank2 =
      if sortedRanks(1).rank == sortedRanks(2).rank then sortedRanks(3)
      else sortedRanks(1)
    (
      PokerHandsType.FULL_HOUSE,
      Seq(pokerRank1, pokerRank2, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private def isThreeOfAKind: Boolean                        =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 1 && countOfLastCard == 1) || (countOfFirstCard == 3 && countOfLastCard == 1) || (countOfFirstCard == 1 && countOfLastCard == 3)
    }
  private def threeOfAKind: (PokerHandsType, Seq[PokerRank]) =
    val indexThreeOfAKind = (0 until sortedRanks.length - 2)
      .find(i =>
        sortedRanks(i).rank == sortedRanks(i + 1).rank && sortedRanks(
          i + 1
        ).rank == sortedRanks(i + 2).rank
      )
      .getOrElse(0)
    (
      PokerHandsType.THREE_OF_A_KIND,
      Seq(
        sortedRanks(2),
        sortedRanks(if (indexThreeOfAKind == 0) 3 else 0),
        sortedRanks(if (indexThreeOfAKind == 2) 1 else 4),
        PokerRank.TWO,
        PokerRank.TWO
      )
    )

  private def isTwoPair: Boolean                        =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 2 && countOfLastCard == 2) || (countOfFirstCard == 2 && countOfLastCard == 1) || (countOfFirstCard == 1 && countOfLastCard == 2)
    }
  private def twoPair: (PokerHandsType, Seq[PokerRank]) =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val isThirdEqualFourth = sortedRanks(2).rank == sortedRanks(3).rank
    val thirdIndex         =
      if (isFirstEqualSecond && isThirdEqualFourth) 4
      else if isFirstEqualSecond then 2
      else 0
    (
      PokerHandsType.TWO_PAIR,
      Seq(
        sortedRanks(1),
        sortedRanks(3),
        sortedRanks(thirdIndex),
        PokerRank.TWO,
        PokerRank.TWO
      )
    )

  private def isOnePair: Boolean                        =
    sortedDistinctRanks.length == 4
  private def onePair: (PokerHandsType, Seq[PokerRank]) =
    val index = (0 until sortedRanks.length - 1)
      .find(i => sortedRanks(i).rank == sortedRanks(i + 1).rank)
      .getOrElse(0)
    (
      PokerHandsType.ONE_PAIR,
      Seq(
        sortedRanks(index),
        sortedRanks(if (index == 0) 2 else 0),
        sortedRanks(if (index < 2) 3 else 1),
        sortedRanks(if (index == 3) 2 else 4),
        PokerRank.TWO
      )
    )

  private def highCard: (PokerHandsType, Seq[PokerRank]) =
    (PokerHandsType.HIGH_CARD, sortedRanks)

  val (pokerHandsType: PokerHandsType, ranks: Seq[PokerRank]) =
    if isFiveOfAKind then fiveOfAKind
    else if isStraightFlush then straightFlush
    else if isFourOfAKind then fourOfAKind
    else if isFullHouse then fullHouse
    else if isFlush then flush
    else if isStraight then straight
    else if isThreeOfAKind then threeOfAKind
    else if isTwoPair then twoPair
    else if isOnePair then onePair
    else highCard

  def compareTo(pokerHand: PokerHand): Int =
    val diff =
      pokerHandsType.ordinal.compareTo(pokerHand.pokerHandsType.ordinal)
    if diff != 0 then diff
    else
      ranks.indices
        .map(i => pokerHand.ranks(i).rank.compareTo(ranks(i).rank))
        .find(r => r != 0)
        .getOrElse(0)

  def ==(pokerHand: PokerHand): Boolean    = this.equal(pokerHand)
  def equal(pokerHand: PokerHand): Boolean = compareTo(pokerHand) == 0

end PokerHand
