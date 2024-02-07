# Игры

## Дартс

**Алгоритм:**

[Игра Дартс](https://projecteuler.net/problem=109).

Подсчитать количество способов выбить заданное количество очков.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
object Darts:
  private val singlePoints: Seq[Int] = (1 to 20) :+ 25
  private val doublePoints: Seq[Int] = (2 to 40 by 2) :+ 50
  private val triplePoints: Seq[Int] = 3 to 60 by 3
  private val points: Seq[Int] = singlePoints ++ doublePoints ++ triplePoints

  val allDistinctWaysToCheckOut: Map[Int, Int] =
    // First step checkout
    var integerMap = doublePoints.map(point => point -> 1).toMap

    // Second step checkout
    points.foreach: value =>
      doublePoints.foreach: doublePoint =>
        val point = value + doublePoint
        integerMap += (point -> (integerMap.getOrElse(point, 0) + 1))
    
    // Third step checkout
    points.indices.foreach: i =>
      (i until points.length).foreach: j =>
        doublePoints.foreach: doublePoint =>
          val point = points(i) + points(j) + doublePoint
          integerMap += (point -> (integerMap.getOrElse(point, 0) + 1))

    integerMap
  end allDistinctWaysToCheckOut
end Darts

result.values.sum 
// 42336
```

**Метрики:**

Составление карты количества способов выбить заданное количество очков занимает меньше минуты.

```dotty
@main def dartsBench(): Map[Int, Int] =
  Darts.allDistinctWaysToCheckOut

// CPU Time: 377 ms
// Allocation memory size: 27,83 MB      
```

## Кубики

**Алгоритм:**

Подсчитать массив вероятностей выпадения очков при заданном количестве бросков
кубика с заданным числом граней.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
final case class Dice(maxCube: Int):
  private val cache: mutable.Map[(Int, Int), Int] =
    mutable.Map.empty[(Int, Int), Int]

  def probabilities(step: Int): Array[Rational] =
    val sums  = (1 to maxCube * step).map(i => cubeCount(step, i)).toArray
    val total = sums.sum
    sums.map(Rational(_, total))

  private def cubeCount(steps: Int, sum: Int): Int =
    if steps < 0 || sum < steps then 0
    else if steps == sum then 1
    else if steps == 0 then 0
    else
      cache.getOrElseUpdate(
        (steps, sum),
        (1 to maxCube).map(i => cubeCount(steps - 1, sum - i)).sum
      )
end Dice

Dice(6).probabilities(1)
// Array(1/6, 1/6, 1/6, 1/6, 1/6, 1/6)
```

**Метрики:**

Вычисление карты вероятностей для стандартного кубика в 6 граней при 100 бросках занимает 1 секунду.

```dotty
@main def diceBench(): Array[Rational] =
  Dice(6).probabilities(100)

// CPU Time: 1026 ms
// Allocation memory size: 88,05 MB      
```

## Nim

**Алгоритм:**

[Ним (игра)](https://ru.wikipedia.org/wiki/%D0%9D%D0%B8%D0%BC_(%D0%B8%D0%B3%D1%80%D0%B0)).

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
object Nim:
  def getX(head: Long, params: Long*): Long =
    params.indices.foldLeft(head)((res, i) => res ^ params(i))

Nim.getX(1, 2, 3) 
// 0L
```

**Метрики:**

???

## Покер

**Алгоритм:**

Покер.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

Игровая карта

```dotty
enum PokerSuit:
  case D, S, C, H

object PokerSuit:
  def withNameOpt(s: Char): Option[PokerSuit] =
    PokerSuit.values.find(_.toString == s.toString)

enum PokerRank(val rank: Int):
  case A     extends PokerRank(14)
  case K     extends PokerRank(13)
  case Q     extends PokerRank(12)
  case J     extends PokerRank(11)
  case TEN   extends PokerRank(10)
  case NINE  extends PokerRank(9)
  case EIGHT extends PokerRank(8)
  case SEVEN extends PokerRank(7)
  case SIX   extends PokerRank(6)
  case FIVE  extends PokerRank(5)
  case FOUR  extends PokerRank(4)
  case THREE extends PokerRank(3)
  case TWO   extends PokerRank(2)

object PokerRank:
  def withNameOpt(s: String): Option[PokerRank] =
    s match
      case "A"        => Some(PokerRank.A)
      case "K"        => Some(PokerRank.K)
      case "Q"        => Some(PokerRank.Q)
      case "J"        => Some(PokerRank.J)
      case "10" | "T" => Some(PokerRank.TEN)
      case "9"        => Some(PokerRank.NINE)
      case "8"        => Some(PokerRank.EIGHT)
      case "7"        => Some(PokerRank.SEVEN)
      case "6"        => Some(PokerRank.SIX)
      case "5"        => Some(PokerRank.FIVE)
      case "4"        => Some(PokerRank.FOUR)
      case "3"        => Some(PokerRank.THREE)
      case "2"        => Some(PokerRank.TWO)
      case _          => None
      
final case class PokerCard(pokerRank: PokerRank, pokerSuit: PokerSuit)

object PokerCard:
  def string2PokerCard(card: String): Option[PokerCard] =
    if card.length < 2 || card.length > 3 then None
    else
      (
        PokerRank.withNameOpt(card.dropRight(1)),
        PokerSuit.withNameOpt(card.last)
      ).mapN(PokerCard.apply)      
```

Покерная рука

```dotty
enum PokerHandsType:
  case FIVE_OF_A_KIND, // All cards are equal
    STRAIGHT_FLUSH,    // A straight flush is a hand that contains five cards of sequential rank, all of the same suit
    FOUR_OF_A_KIND,    // Four of a kind is a hand that contains four cards of one rank and one card of another rank
    FULL_HOUSE,        // A full house is a hand that contains three cards of one rank and two cards of another rank
    FLUSH,             // A flush is a hand that contains five cards all of the same suit, not all of sequential rank
    STRAIGHT,          // A straight is a hand that contains five cards of sequential rank, not all of the same suit
    THREE_OF_A_KIND,   // Three of a kind is a hand that contains three cards of one rank and two cards of two other ranks (the kickers)
    TWO_PAIR,          // Two pair is a hand that contains two cards of one rank, two cards of another rank and one card of a third rank (the kicker)
    ONE_PAIR,          // One pair is a hand that contains two cards of one rank and three cards of three other ranks (the kickers)
    HIGH_CARD          // High card is a hand that does not fall into any other category

final class PokerHand(hand: String):
  private val split: Array[String] = hand.split(" ")
  require(split.length == 5)
  private val cards: Seq[PokerCard] = split.flatMap(string2PokerCard).toSeq
  private val sortedRanks: Seq[PokerRank] =
    cards
      .map(card => card.pokerRank)
      .sortWith((pr1, pr2) => pr2.rank - pr1.rank < 0)
  private val sortedDistinctRanks: Seq[PokerRank] = sortedRanks.distinct

  private lazy val isFiveOfAKind: Boolean =
    sortedDistinctRanks.length == 1
  private lazy val fiveOfAKind: (PokerHandsType, Seq[PokerRank]) =
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

  private lazy val isStraightFlush: Boolean =
    isStraight && isFlush
  private lazy val straightFlush: (PokerHandsType, Seq[PokerRank]) =
    (PokerHandsType.STRAIGHT_FLUSH, straight._2)

  private lazy val isStraight: Boolean =
    sortedRanks == Seq(
      PokerRank.FIVE,
      PokerRank.FOUR,
      PokerRank.THREE,
      PokerRank.TWO,
      PokerRank.A
    ) || (0 until sortedRanks.length - 1).forall(i =>
      sortedRanks(i).rank - sortedRanks(i + 1).rank == 1
    )
  private lazy val straight: (PokerHandsType, Seq[PokerRank]) =
    val pokerRank =
      if sortedRanks.head == PokerRank.A && sortedRanks(1) == PokerRank.FIVE
      then PokerRank.FIVE
      else sortedRanks.head
    (
      PokerHandsType.STRAIGHT,
      Seq(pokerRank, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private lazy val isFlush: Boolean =
    cards.map(_.pokerSuit.ordinal).distinct.length == 1
  private lazy val flush: (PokerHandsType, Seq[PokerRank]) =
    (PokerHandsType.FLUSH, sortedRanks)

  private lazy val isFourOfAKind: Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(c => c.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 1 || countOfFirstCard == 4)
  private lazy val fourOfAKind: (PokerHandsType, Seq[PokerRank]) =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val pokerRank1 =
      if isFirstEqualSecond then sortedRanks.head else sortedRanks(1)
    val pokerRank2 =
      if isFirstEqualSecond then sortedRanks(4) else sortedRanks.head
    (
      PokerHandsType.FOUR_OF_A_KIND,
      Seq(pokerRank1, pokerRank2, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private lazy val isFullHouse: Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(c => c.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 2 || countOfFirstCard == 3)
  private lazy val fullHouse: (PokerHandsType, Seq[PokerRank]) =
    val pokerRank1 = sortedRanks(2)
    val pokerRank2 =
      if sortedRanks(1).rank == sortedRanks(2).rank then sortedRanks(3)
      else sortedRanks(1)
    (
      PokerHandsType.FULL_HOUSE,
      Seq(pokerRank1, pokerRank2, PokerRank.TWO, PokerRank.TWO, PokerRank.TWO)
    )

  private lazy val isThreeOfAKind: Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 1 && countOfLastCard == 1) || (countOfFirstCard == 3 && countOfLastCard == 1) || (countOfFirstCard == 1 && countOfLastCard == 3)
    }
  private lazy val threeOfAKind: (PokerHandsType, Seq[PokerRank]) =
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
        sortedRanks(if indexThreeOfAKind == 0 then 3 else 0),
        sortedRanks(if indexThreeOfAKind == 2 then 1 else 4),
        PokerRank.TWO,
        PokerRank.TWO
      )
    )

  private lazy val isTwoPair: Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 2 && countOfLastCard == 2) || (countOfFirstCard == 2 && countOfLastCard == 1) || (countOfFirstCard == 1 && countOfLastCard == 2)
    }
  private lazy val twoPair: (PokerHandsType, Seq[PokerRank]) =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val isThirdEqualFourth = sortedRanks(2).rank == sortedRanks(3).rank
    val thirdIndex =
      if isFirstEqualSecond && isThirdEqualFourth then 4
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

  private lazy val isOnePair: Boolean =
    sortedDistinctRanks.length == 4
  private lazy val onePair: (PokerHandsType, Seq[PokerRank]) =
    val index = (0 until sortedRanks.length - 1)
      .find(i => sortedRanks(i).rank == sortedRanks(i + 1).rank)
      .getOrElse(0)
    (
      PokerHandsType.ONE_PAIR,
      Seq(
        sortedRanks(index),
        sortedRanks(if index == 0 then 2 else 0),
        sortedRanks(if index < 2 then 3 else 1),
        sortedRanks(if index == 3 then 2 else 4),
        PokerRank.TWO
      )
    )

  private lazy val highCard: (PokerHandsType, Seq[PokerRank]) =
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
```

**Метрики:**

???

## Судоку

**Алгоритм:**

Судоку.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
final case class SuDoku(cells: Array[Array[Array[Int]]]):

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def result: Option[Array[Array[Int]]] =
    var hasToRemove = true
    var isValid     = true
    while isValid && isNotAnswer && hasToRemove
    do
      hasToRemove = false
      cells.indices.foreach(i =>
        cells(i).indices
          .withFilter(j => cells(i)(j).length != 1)
          .foreach(j =>
            cells(i)(j).withFilter(!setValueWithoutChanges(i, j, _)).foreach {
              value =>
                hasToRemove = true
                if !removeValue(i, j, value) then isValid = false
            }
          )
      )

    if isValid && !isNotAnswer then Some(cells.map(_.map(_.head)))
    else None
  end result

  private def isNotAnswer: Boolean =
    cells.exists(_.exists(_.length != 1))

  /** Set the specified value in the specified field.
    */
  private def setValue(i: Int, j: Int, value: Int): Boolean =
    val tempSuDoku = copySuDoku
    tempSuDoku.cells(i)(j) = Array(value)
    val result = tempSuDoku.removeValueFromRowColSqr(
      i,
      j,
      value
    ) && tempSuDoku.isSuDokuMatrixValid
    if result then
      tempSuDoku.cells.indices.foreach(i => cells(i) = tempSuDoku.cells(i))
    result

  /** Set the specified value in the specified field but then return to the
    * previous state of the cells regardless of the result.
    */
  private def setValueWithoutChanges(i: Int, j: Int, value: Int): Boolean =
    val tempSuDoku = copySuDoku
    tempSuDoku.cells(i)(j) = Array(value)
    tempSuDoku.removeValueFromRowColSqr(
      i,
      j,
      value
    ) && tempSuDoku.isSuDokuMatrixValid

  /** Check that each row contains digits from 1 to 9 at least once. Check that
    * each column contains digits from 1 to 9 at least once.
    */
  private def isSuDokuMatrixValid: Boolean =
    !(1 to 9).exists(n => cells.exists(_.forall(c => !c.contains(n)))) &&
      !(1 to 9).exists(n =>
        cells.head.indices.exists(j =>
          cells.map(_(j)).forall(c => !c.contains(n))
        )
      )

  /** Removes a given value from the row, column, and square to which the point
    * belongs.
    */
  private def removeValueFromRowColSqr(i: Int, j: Int, value: Int): Boolean =
    (0 to 8).forall(k =>
      k == j || removeValue(i, k, value)
    ) && // Removes a given value from the row
      (0 to 8).forall(k =>
        k == i || removeValue(k, j, value)
      ) &&                 // Removes a given value from the column
      (0 to 2).forall(k => // Removes a given value from the square
        (0 to 2).forall { l =>
          val r = (i / 3) * 3 + k
          val c = (j / 3) * 3 + l
          (r == i && c == j) || removeValue(r, c, value)
        }
      )

  /** Removes a given value from the cell (i, j). */
  private def removeValue(i: Int, j: Int, value: Int): Boolean =
    if cells(i)(j).length == 1 then cells(i)(j).head != value
    else
      cells(i)(j) = cells(i)(j).filter(_ != value)
      cells(i)(j).length != 1 || setValue(i, j, cells(i)(j).head)

  private def copySuDoku: SuDoku =
    SuDoku(cells.map(_.map(_.map(identity))))

end SuDoku

object SuDoku:
  def apply(): SuDoku =
    val defaultCells = (0 to 8)
      .map(_ => (0 to 8).map(_ => Array(1, 2, 3, 4, 5, 6, 7, 8, 9)).toArray)
      .toArray
    SuDoku(defaultCells)

  def toSuDoku(source: String): Option[SuDoku] =
    val sudoku = SuDoku()
    val rows   = source.trim.split("\n")
    val isCorrect = (0 to 8).forall(i =>
      (0 to 8).forall { j =>
        val d = rows(i).charAt(j).getNumericValue
        d == 0 || sudoku.setValue(i, j, d)
      }
    )
    if isCorrect then Some(sudoku) else None

end SuDoku

val maybeSuDoku = toSuDoku(
  "003020600\n" +
    "900305001\n" +
    "001806400\n" +
    "008102900\n" +
    "700000008\n" +
    "006708200\n" +
    "002609500\n" +
    "800203009\n" +
    "005010300"
)

maybeSuDoku.foreach: sudoku =>
  sudoku.result.foreach: result =>
    result.foreach(row => println(row.mkString("Array(", ", ", ")")))

// Array(4, 8, 3, 9, 2, 1, 6, 5, 7)
// Array(9, 6, 7, 3, 4, 5, 8, 2, 1)
// Array(2, 5, 1, 8, 7, 6, 4, 9, 3)
// Array(5, 4, 8, 1, 3, 2, 9, 7, 6)
// Array(7, 2, 9, 5, 6, 4, 1, 3, 8)
// Array(1, 3, 6, 7, 9, 8, 2, 4, 5)
// Array(3, 7, 2, 6, 8, 9, 5, 1, 4)
// Array(8, 1, 4, 2, 5, 3, 7, 6, 9)
// Array(6, 9, 5, 4, 1, 7, 3, 8, 2)
```

**Метрики:**

???

---

**Ссылки:**

- [Darts - Project Euler](https://projecteuler.net/problem=109)
