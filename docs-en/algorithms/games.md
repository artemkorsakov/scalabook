# Games

## Darts

> [Darts Game](https://projecteuler.net/problem=109).
>
> Count the number of ways to score a given number of points.

**Algorithm:**

- **Defining point values**:
    - Three sequences of points are created:
    - `singlePoints`: points from 1 to 20 and 25 (single points).
    - `doublePoints`: even points from 2 to 40 and 50 (double points).
    - `triplePoints`: multiples of 3 from 3 to 60 (triple points).
    - All these points are combined into a single sequence `points`.
- **First step** — finish with one throw:
    - A map `firstStepMap` is created in which for each double point (from `doublePoints`)
      the number of ways to complete in 1 throw is set to 1 (i.e. one double point).
- **Second step** — finish with two throws:
    - A map `secondStepMap` is created that includes all possible
      sums of the first throw (from `points`) and the double throw (from `doublePoints`).
    - For each sum, it is checked whether it already exists in `firstStepMap`.
      If it does, the number of ways to complete is incremented by `1`, otherwise a new entry is added.
- **Third step** — finish with three throws:
    - A final map is created that includes all possible
      sums of two throws (from `points`) and the double throw (from `doublePoints`).
    - For each resulting sum, it is checked whether it already exists in `secondStepMap`.
      If it does, the number of ways to complete is incremented by `1`, otherwise a new entry is added.
- **Returning the result**:
    - The method returns a map where keys are the scored point values,
      and values are the number of distinct ways to finish the game using those points.

**Code:**

```dotty
object Darts:
  private val singlePoints: Seq[Int] = (1 to 20) :+ 25
  private val doublePoints: Seq[Int] = (2 to 40 by 2) :+ 50
  private val triplePoints: Seq[Int] = 3 to 60 by 3
  private val points: Seq[Int] = singlePoints ++ doublePoints ++ triplePoints

  val allDistinctWaysToCheckOut: Map[Int, Int] =
    // First step checkout
    val firstStepMap = doublePoints.map(point => point -> 1).toMap

    // Second step checkout
    val secondStepMap =
      (
        for
          value       <- points
          doublePoint <- doublePoints
        yield value + doublePoint
      ).foldLeft(firstStepMap) { case (acc, point) =>
        acc + (point -> (acc.getOrElse(point, 0) + 1))
      }

    // Third step checkout
    (
      for
        i           <- points.indices
        j           <- i until points.length
        doublePoint <- doublePoints
      yield points(i) + points(j) + doublePoint
    ).foldLeft(secondStepMap) { case (acc, point) =>
      acc + (point -> (acc.getOrElse(point, 0) + 1))
    }

  end allDistinctWaysToCheckOut
end Darts

Darts.allDistinctWaysToCheckOut.values.sum
// 42336
```

## Dice

**Algorithm:**

Count the array of probabilities of scoring a given number of throws
of a die with a given number of sides.

**Code:**

```dotty
import scala.collection.mutable

final case class Rational(num: Int, den: Int)

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

Dice(6).probabilities(1).mkString(", ")
// Array(1/6, 1/6, 1/6, 1/6, 1/6, 1/6)
```

## Nim

**Algorithm:**

[Nim (game)](https://en.wikipedia.org/wiki/Nim).

**Code:**

```dotty
object Nim:
  def getX(head: Long, params: Long*): Long =
    params.indices.foldLeft(head)((res, i) => res ^ params(i))

Nim.getX(1, 2, 3) 
// 0L
```

## Poker

**Algorithm:**

Poker.

##### Playing Card

Definition of a playing card in poker in Scala.

The following code implements several classes and data types
that can be used to represent cards in a poker game.

1. `PokerSuit` is an enumeration that defines the four suits in poker:
   "D" — diamonds, "S" — spades, "C" — clubs, "H" — hearts.
2. `PokerRank` is an enumeration that defines the 13 card ranks in poker.
   Each rank has an integer value that represents its rank in the game.
   For example, "A" (ace) has value 14, "K" (king) has value 13, etc.
3. `PokerCard` is an immutable case class that represents a card in poker.
   It contains two fields: `pokerRank` and `pokerSuit`, which represent the rank and suit of the card respectively.
4. The companion objects `PokerSuit` and `PokerRank` declare utility methods.
   In particular, `withNameOpt` is a method that attempts to find an enumeration member by its string representation.
5. The companion object `PokerCard` declares an additional constructor `apply`.
   This method takes a string that represents a card, e.g. "AS" for ace of spades,
   and attempts to convert it to a `PokerCard`. If the conversion fails, it returns `None`.

**Code:**

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
  implicit val pokerRankOrdering: Ordering[PokerRank] =
    Ordering.fromLessThan((a, b) => b.rank < a.rank)

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
end PokerRank

final case class PokerCard(pokerRank: PokerRank, pokerSuit: PokerSuit)

object PokerCard:
  def apply(card: String): Option[PokerCard] =
    if card.length < 2 || card.length > 3 then None
    else
      for
        pokerRank <- PokerRank.withNameOpt(card.dropRight(1))
        pokerSuit <- PokerSuit.withNameOpt(card.last)
      yield PokerCard(pokerRank, pokerSuit)
```

##### Poker Hand

The following code implements the logic of a poker game. It includes the definition of different types of cards, their ranks and suits,
as well as the definition of a hand in poker.

1. `PokerHandsType` is an enumeration that defines all possible hand types in poker.
2. `PokerHand` is an immutable case class that represents a hand in poker.
   It contains two fields: `pokerHandsType` and `ranks`, which represent the hand type and the list of card ranks
   respectively. The class also contains a `compareTo` method that compares two poker hands.
3. The companion object `PokerHand` declares utility methods for creating a poker hand from a string,
   from a hand type and card rank, as well as for comparing poker hands.
4. The method `apply(hand: String)` takes a string representing a poker hand
   and attempts to convert it to a `PokerHand`. If the conversion fails, it returns `None`.
5. The method `apply(pokerHandsType: PokerHandsType, rank: PokerRank)` creates a poker hand from a given hand type and
   card rank.
6. The method `apply(cards: Array[PokerCard])` creates a poker hand from an array of cards.
   It determines the hand type and card ranks based on the provided cards and creates a `PokerHand` with the corresponding values.
7. The methods `isFiveOfAKind`, `isStraightFlush`, `isFourOfAKind`, `isFullHouse`, `isFlush`, `isStraight`, `isThreeOfAKind`,
   `isTwoPair`, `isOnePair` and `highCard` determine the hand type based on the provided cards and return the corresponding
   values.
8. The methods `fourOfAKind`, `fullHouse`, `threeOfAKind`, `twoPair` and `onePair` create a poker hand of a certain
   type with the corresponding card ranks.
9. The method `highCard` creates a `HIGH_CARD` type poker hand with the corresponding card ranks.
10. The method `compareTo` compares two poker hands. It compares the hand types, and then the card ranks in case of equal types.

**Code:**

```dotty
enum PokerHandsType:
  case FIVE_OF_A_KIND,
    STRAIGHT_FLUSH,
    FOUR_OF_A_KIND,
    FULL_HOUSE,
    FLUSH, 
    STRAIGHT, 
    THREE_OF_A_KIND,
    TWO_PAIR, 
    ONE_PAIR, 
    HIGH_CARD 

final case class PokerHand(
    pokerHandsType: PokerHandsType,
    ranks: List[PokerRank]
):
  def compareTo(pokerHand: PokerHand): Int =
    val typesDiff = pokerHandsType.ordinal - pokerHand.pokerHandsType.ordinal
    if typesDiff != 0 then typesDiff
    else
      ranks
        .zip(pokerHand.ranks)
        .map((a, b) => b.rank - a.rank)
        .find(r => r != 0)
        .getOrElse(0)

object PokerHand:
  def apply(hand: String): Option[PokerHand] =
    val cards = hand.split(" ").flatMap(PokerCard.apply)
    if cards.length == 5 then Some(PokerHand(cards)) else None

  def apply(pokerHandsType: PokerHandsType, rank: PokerRank): PokerHand =
    PokerHand(pokerHandsType, List(rank))

  private def apply(cards: Array[PokerCard]): PokerHand =
    val sortedRanks         = cards.map(_.pokerRank).sorted
    val sortedDistinctRanks = sortedRanks.distinct
    lazy val straightPokerRank =
      if sortedRanks.head == PokerRank.A && sortedRanks(1) == PokerRank.FIVE
      then PokerRank.FIVE
      else sortedRanks.head

    if isFiveOfAKind(sortedDistinctRanks) then
      PokerHand(PokerHandsType.FIVE_OF_A_KIND, cards.head.pokerRank)
    else if isStraightFlush(sortedRanks, cards) then
      PokerHand(PokerHandsType.STRAIGHT_FLUSH, straightPokerRank)
    else if isFourOfAKind(sortedDistinctRanks, cards) then
      fourOfAKind(sortedRanks)
    else if isFullHouse(sortedDistinctRanks, cards) then
      fullHouse(sortedRanks)
    else if isFlush(cards) then
      PokerHand(PokerHandsType.FLUSH, sortedRanks.toList)
    else if isStraight(sortedRanks) then
      PokerHand(PokerHandsType.STRAIGHT, straightPokerRank)
    else if isThreeOfAKind(sortedDistinctRanks, cards, sortedRanks) then
      threeOfAKind(sortedRanks)
    else if isTwoPair(sortedDistinctRanks, cards, sortedRanks) then
      twoPair(sortedRanks)
    else if isOnePair(sortedDistinctRanks) then
      onePair(sortedRanks)
    else highCard(sortedRanks)
    end if
  end apply

  private def isFiveOfAKind(sortedDistinctRanks: Array[PokerRank]): Boolean =
    sortedDistinctRanks.length == 1

  private def isStraightFlush(
      sortedRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    isStraight(sortedRanks) && isFlush(cards)

  private def isFourOfAKind(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(_.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 1 || countOfFirstCard == 4)

  private def fourOfAKind(sortedRanks: Array[PokerRank]): PokerHand =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val pokerRank1 =
      if isFirstEqualSecond then sortedRanks.head else sortedRanks(1)
    val pokerRank2 =
      if isFirstEqualSecond then sortedRanks(4) else sortedRanks.head
    PokerHand(PokerHandsType.FOUR_OF_A_KIND, List(pokerRank1, pokerRank2))

  private def isFullHouse(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(_.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 2 || countOfFirstCard == 3)

  private def fullHouse(sortedRanks: Array[PokerRank]): PokerHand =
    val pokerRank1 = sortedRanks(2)
    val pokerRank2 =
      if sortedRanks(1).rank == sortedRanks(2).rank then sortedRanks(3)
      else sortedRanks(1)
    PokerHand(PokerHandsType.FULL_HOUSE, List(pokerRank1, pokerRank2))

  private def isFlush(cards: Array[PokerCard]): Boolean =
    cards.map(_.pokerSuit.ordinal).distinct.length == 1

  private def isStraight(sortedRanks: Array[PokerRank]): Boolean =
    (sortedRanks sameElements
      Array(
        PokerRank.FIVE,
        PokerRank.FOUR,
        PokerRank.THREE,
        PokerRank.TWO,
        PokerRank.A
      )) ||
      (0 until sortedRanks.length - 1).forall: i =>
        sortedRanks(i).rank - sortedRanks(i + 1).rank == 1

  private def isThreeOfAKind(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard],
      sortedRanks: Array[PokerRank]
  ): Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 1 && countOfLastCard == 1) ||
      (countOfFirstCard == 3 && countOfLastCard == 1) ||
      (countOfFirstCard == 1 && countOfLastCard == 3)
    }

  private def threeOfAKind(sortedRanks: Array[PokerRank]): PokerHand =
    val indexThreeOfAKind = (0 until sortedRanks.length - 2)
      .find: i =>
        sortedRanks(i).rank == sortedRanks(i + 1).rank &&
          sortedRanks(i + 1).rank == sortedRanks(i + 2).rank
      .getOrElse(0)
    PokerHand(
      PokerHandsType.THREE_OF_A_KIND,
      List(
        sortedRanks(2),
        sortedRanks(if indexThreeOfAKind == 0 then 3 else 0),
        sortedRanks(if indexThreeOfAKind == 2 then 1 else 4)
      )
    )

  private def isTwoPair(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard],
      sortedRanks: Array[PokerRank]
  ): Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 2 && countOfLastCard == 2) ||
      (countOfFirstCard == 2 && countOfLastCard == 1) ||
      (countOfFirstCard == 1 && countOfLastCard == 2)
    }

  private def twoPair(sortedRanks: Array[PokerRank]): PokerHand =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val isThirdEqualFourth = sortedRanks(2).rank == sortedRanks(3).rank
    val thirdIndex =
      if isFirstEqualSecond && isThirdEqualFourth then 4
      else if isFirstEqualSecond then 2
      else 0
    PokerHand(
      PokerHandsType.TWO_PAIR,
      List(
        sortedRanks(1),
        sortedRanks(3),
        sortedRanks(thirdIndex)
      )
    )

  private def isOnePair(sortedDistinctRanks: Array[PokerRank]): Boolean =
    sortedDistinctRanks.length == 4

  private def onePair(sortedRanks: Array[PokerRank]): PokerHand =
    val index = (0 until sortedRanks.length - 1)
      .find(i => sortedRanks(i).rank == sortedRanks(i + 1).rank)
      .getOrElse(0)
    PokerHand(
      PokerHandsType.ONE_PAIR,
      List(
        sortedRanks(index),
        sortedRanks(if index == 0 then 2 else 0),
        sortedRanks(if index < 2 then 3 else 1),
        sortedRanks(if index == 3 then 2 else 4)
      )
    )

  private def highCard(sortedRanks: Array[PokerRank]): PokerHand =
    PokerHand(PokerHandsType.HIGH_CARD, sortedRanks.toList)
end PokerHand
```

## Sudoku

Su Doku (meaning _place for a number_ in Japanese) is the name of a popular puzzle.
The goal of Su Doku is to fill in empty cells (or zeros) in a 9x9 grid with digits such that
every row, column, and 3x3 square contains all digits from 1 to 9.
Below is an example of a typical initial puzzle and its solution.

![](https://projecteuler.net/project/images/p096_1.png)
![](https://projecteuler.net/project/images/p096_2.png)

### Sudoku Cell

Define the list of allowable values for a Sudoku game cell — these are the digits from 1 to 9:

```dotty
enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine
```

The cell itself must satisfy the following conditions:

- The cell has a set of allowable values
- The exact value of the cell is defined if only one option remains among the possible values
- In the initial state, the cell can contain any value from 1 to 9

```dotty
final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None
    
object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet) 

SuDokuCell(Set(SuDokuCellValue.Three)).possibleValues 
// Set(Three)
SuDokuCell(Set(SuDokuCellValue.Three)).maybeValue 
// Some(Three)
SuDokuCell.initial.possibleValues
// HashSet(One, Two, Three, Four, Five, Six, Seven, Eight, Nine)
SuDokuCell.initial.maybeValue    
// None
```

The cell must support the following operations:

- A specific value can be set in the cell as the sole value, if it is among the possible values
- A value can be removed from the cell's possible values, only if it is not the cell's only possible value

Define a new type describing the [functional state](../../fp/state.md) of the cell `State[SuDokuCell, Boolean]`,
which takes a cell and returns the output state `(Boolean, SuDokuCell)` — a tuple of two elements,
where the left side indicates whether the previous operation completed successfully,
and the right side holds the last stable state of the cell.

```dotty
object SuDokuCell:
  enum SuDokuCellOperation:
    case Set, Remove

  private type Input  = (SuDokuCellOperation, SuDokuCellValue)
  private type Output = (Boolean, SuDokuCell)

  private val updateCell: Input => Output => Output =
    (operation, value) =>
      (result, cell) =>
        (operation, value, result) match
          // Set the value in the cell only if
          // this value is among the allowable ones
          case (SuDokuCellOperation.Set, value, true)
              if cell.possibleValues.contains(value) =>
            (true, SuDokuCell(Set(value)))
          // Remove the value from the cell only if
          // it is not the only value from the allowable ones
          case (SuDokuCellOperation.Remove, value, true)
              if cell.possibleValues != Set(value) =>
            (true, SuDokuCell(cell.possibleValues - value))
          case _ => // The operation cannot be performed
            (false, cell)

  def execute(inputs: (SuDokuCellOperation, SuDokuCellValue)*)
      : State[SuDokuCell, Boolean] =
    State[SuDokuCell, Boolean] { cell =>
      val state =
        for
          _ <- State.traverse(inputs.toList)(input =>
            State.modify[Output](updateCell(input))
          )
          (result, _) <- State.get[Output]
        yield result

      val (_, output) = state.run((true, cell))
      output
    }

end SuDokuCell

val inputs = List(
  (SuDokuCellOperation.Remove, SuDokuCellValue.One),
  (SuDokuCellOperation.Remove, SuDokuCellValue.Two)
)
SuDokuCell.execute(inputs*).run(SuDokuCell.initial)
// (true,SuDokuCell(HashSet(Six, Four, Seven, Eight, Five, Nine, Three)))

val input = (SuDokuCellOperation.Set, SuDokuCellValue.Two)
SuDokuCell.execute(input).run(SuDokuCell.initial)
// (true,SuDokuCell(Set(Two)))
```

### Sudoku Board

Define what a Sudoku board is:

- A Sudoku game board consists of \\(9 \times 9\\) cells
- A Sudoku board contains nine \\(3 \times 3\\) blocks
- Each cell belongs to a specific row, column, and block

Let us number the blocks as shown in the table:

|       | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
|-------|---|---|---|---|---|---|---|---|---|
| **1** | 1 | 1 | 1 | 2 | 2 | 2 | 3 | 3 | 3 |
| **2** | 1 | 1 | 1 | 2 | 2 | 2 | 3 | 3 | 3 |
| **3** | 1 | 1 | 1 | 2 | 2 | 2 | 3 | 3 | 3 |
| **4** | 4 | 4 | 4 | 5 | 5 | 5 | 6 | 6 | 6 |
| **5** | 4 | 4 | 4 | 5 | 5 | 5 | 6 | 6 | 6 |
| **6** | 4 | 4 | 4 | 5 | 5 | 5 | 6 | 6 | 6 |
| **7** | 7 | 7 | 7 | 8 | 8 | 8 | 9 | 9 | 9 |
| **8** | 7 | 7 | 7 | 8 | 8 | 8 | 9 | 9 | 9 |
| **9** | 7 | 7 | 7 | 8 | 8 | 8 | 9 | 9 | 9 |

```dotty
enum SuDokuRow:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth
  
enum SuDokuColumn:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth
  
enum SuDokuBlock:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth
  
object SuDokuBlock:
  def getBlock(row: SuDokuRow, col: SuDokuColumn): SuDokuBlock =
    (row, col) match
      case (r, c) if r.ordinal < 3 && c.ordinal < 3 => SuDokuBlock.First
      case (r, c) if r.ordinal < 3 && c.ordinal < 6 => SuDokuBlock.Second
      case (r, c) if r.ordinal < 3                  => SuDokuBlock.Third
      case (r, c) if r.ordinal < 6 && c.ordinal < 3 => SuDokuBlock.Fourth
      case (r, c) if r.ordinal < 6 && c.ordinal < 6 => SuDokuBlock.Fifth
      case (r, c) if r.ordinal < 6                  => SuDokuBlock.Sixth
      case (_, c) if c.ordinal < 3                  => SuDokuBlock.Seventh
      case (_, c) if c.ordinal < 6                  => SuDokuBlock.Eighth
      case _                                        => SuDokuBlock.Ninth
end SuDokuBlock
  
SuDokuBlock.getBlock(SuDokuRow.Fourth, SuDokuColumn.First)
// Fourth
  
type SuDokuIndex = (SuDokuRow, SuDokuColumn, SuDokuBlock)

object SuDokuIndex:
  def apply(row: SuDokuRow, column: SuDokuColumn): SuDokuIndex =
    (row, column, SuDokuBlock.getBlock(row, column))
    
final case class SuDoku(cells: Map[SuDokuIndex, SuDokuCell]):
  def getCell(row: SuDokuRow, column: SuDokuColumn): Option[SuDokuCell] =
    getCell(SuDokuIndex(row, column))
  
  def getCell(index: SuDokuIndex): Option[SuDokuCell] = cells.get(index)     
```

Initially, the list of possible values in each Sudoku cell ranges from 1 to 9.

```dotty
object SuDoku:
  val initial: SuDoku =
    val defaultCells =
      for
        row <- SuDokuRow.values
        col <- SuDokuColumn.values
      yield SuDokuIndex(row, col) -> SuDokuCell.initial

    SuDoku(defaultCells.toMap)
```

A Sudoku board is valid if the following conditions are met:

- In each row, there are no two cells with the same known value
- In each column, there are no two cells with the same known value
- In each block, there are no two cells with the same known value

|                  | Same Column |     | Same Block   |
|------------------|-------------|-----|--------------|
| **Same Row**     | 1?          | 1?  | ...          |
| ...              | 1?          | ... | ...          |
| **Same Block**   | ...         | ... | 1?           |

Moreover, the puzzle is solved if the value of every cell is known.

|       | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
|-------|---|---|---|---|---|---|---|---|---|
| **1** | 4 | 8 | 3 | 9 | 2 | 1 | 6 | 5 | 7 |
| **2** | 9 | 6 | 7 | 3 | 4 | 5 | 8 | 2 | 1 |
| **3** | 2 | 5 | 1 | 8 | 7 | 6 | 4 | 9 | 3 |
| **4** | 5 | 4 | 8 | 1 | 3 | 2 | 9 | 7 | 6 |
| **5** | 7 | 2 | 9 | 5 | 6 | 4 | 1 | 3 | 8 |
| **6** | 1 | 3 | 6 | 7 | 9 | 8 | 2 | 4 | 5 |
| **7** | 3 | 7 | 2 | 6 | 8 | 9 | 5 | 1 | 4 |
| **8** | 8 | 1 | 4 | 2 | 5 | 3 | 7 | 6 | 9 |
| **9** | 6 | 9 | 5 | 4 | 1 | 7 | 3 | 8 | 2 |

```dotty
final case class SuDoku(cells: Map[SuDokuIndex, SuDokuCell]): 
  val isCorrect: Boolean =
    cells.groupBy(_._1._1).forall((_, c) => hasUniqueValues(c.values)) &&
      cells.groupBy(_._1._2).forall((_, c) => hasUniqueValues(c.values)) &&
      cells.groupBy(_._1._3).forall((_, c) => hasUniqueValues(c.values))

  val hasSolution: Boolean =
    isCorrect && cells.forall(_._2.maybeValue.isDefined)

object SuDoku:
  private def hasUniqueValues(cells: Iterable[SuDokuCell]): Boolean =
    val values = cells.flatMap(_.maybeValue)
    values.toSet.size == values.size
    
val sudoku = SuDoku.initial
sudoku.isCorrect     // true
sudoku.hasSolution   // false   
```

### Commands

Define a common operation for changing the state of a Sudoku game:
for any type of input data `A` and a given operation `update`
(`update` given a value of type `A` returns a transformation function from `SuDokuOutput` to `SuDokuOutput`,
where `SuDokuOutput` is a tuple of two elements: the result of the previous operation and the Sudoku board)
returns a functional state `SuDokuState`:

```dotty
object SuDokuSolver:
  type SuDokuState  = State[SuDoku, Boolean]
  type SuDokuOutput = (Boolean, SuDoku)

  def executeInnerOperation[A](
      inputs: List[A],
      update: A => SuDokuOutput => SuDokuOutput
  ): SuDokuState =
    State[SuDoku, Boolean] { sudoku =>
      val state =
        for
          _ <- State.traverse(inputs)(input =>
            State.modify[SuDokuOutput](update(input))
          )
          (result, _) <- State.get[SuDokuOutput]
        yield result

      val (_, output) = state.run((true, sudoku))
      output
    }
```

Next, what we need is setting the initial state in the game,
when part of the board is filled with known values.

So let us settle on the operation of setting a value at a given position {i, j}.
This operation consists of four side operations:

- setting the given value at position {i, j}
- removing the given value from row i except for cell {i, j}
- removing the given value from column j except for cell {i, j}
- removing the given value from the \\(3 \times 3\\) block that cell {i, j} belongs to, except for the cell itself

#### Elementary side operations

Define the side operations of setting and removing a value at a given position:

```dotty
object SuDokuInner:
  enum SuDokuInnerOperation:
    case SetToCell, RemoveFromCell

  type SuDokuInnerInput =
    (SuDokuInnerOperation, SuDokuRow, SuDokuColumn, SuDokuCellValue)

  private val updateSuDoku: SuDokuInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        if result && sudoku.isCorrect then
          operation match
            case SetToCell =>
              updateCell(SuDokuCellOperation.Set, sudoku, i, j, value)
            case RemoveFromCell =>
              updateCell(SuDokuCellOperation.Remove, sudoku, i, j, value)
        else (false, sudoku)

  // Define the current cell, execute the operation, in case of success update the cell
  private def updateCell(
      oper: SuDokuCellOperation,
      sudoku: SuDoku,
      i: SuDokuRow,
      j: SuDokuColumn,
      value: SuDokuCellValue
  ): (Boolean, SuDoku) =
    sudoku.getCell(i, j) match
      case None => (false, sudoku)
      case Some(oldCell) =>
        val (result, newCell) = SuDokuCell.execute((oper, value)).run(oldCell)
        if result then
          val index    = SuDokuIndex(i, j)
          val newCells = sudoku.cells.updated(index, newCell)
          (true, SuDoku(newCells))
        else (false, sudoku)

  def execute(inputs: SuDokuInnerInput*): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs.toList, updateSuDoku)

end SuDokuInner
```

Results of the set and remove value operations:

```dotty
val initialSudoku = SuDoku.initial
val set: SuDokuInnerInput =
  (
    SuDokuInnerOperation.SetToCell,
    SuDokuRow.First,
    SuDokuColumn.Second,
    SuDokuCellValue.Six
  )
val (_, sudoku0) = SuDokuInner.execute(set).run(initialSudoku)
sudoku0.getCell(SuDokuRow.First, SuDokuColumn.Second)
// Some(SuDokuCell(Set(Six)))

val remove: SuDokuInnerInput =
  (
    SuDokuInnerOperation.RemoveFromCell,
    SuDokuRow.Second,
    SuDokuColumn.First,
    SuDokuCellValue.Five
  )
val (_, sudoku1) = SuDokuInner.execute(remove).run(initialSudoku)
sudoku1.getCell(SuDokuRow.Second, SuDokuColumn.First)
// Some(SuDokuCell(HashSet(Eight, Six, Seven, Nine, Four, Two, Three, One)))
```

#### Multi-level side operations

Three more complex side operations for removal:

- removing the given value from row i except for cell {i, j}
- removing the given value from column j except for cell {i, j}
- removing the given value from the \\(3 \times 3\\) block that cell {i, j} belongs to,
  except for the cell itself

|       | 1     | 2     | 3     | 4     | 5     | 6     | 7     | 8     | 9     |
|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|
| **1** | 7     | ~~7~~ | ~~7~~ | ~~7~~ | ~~7~~ | ~~7~~ | ~~7~~ | ~~7~~ | ~~7~~ |
| **2** | ~~7~~ | ~~7~~ | ~~7~~ |       |       |       |       |       |       |
| **3** | ~~7~~ | ~~7~~ | ~~7~~ |       |       |       |       |       |       |
| **4** | ~~7~~ |       |       |       |       |       |       |       |       |
| **5** | ~~7~~ |       |       |       |       |       |       |       |       |
| **6** | ~~7~~ |       |       |       |       |       |       |       |       |
| **7** | ~~7~~ |       |       |       |       |       |       |       |       |
| **8** | ~~7~~ |       |       |       |       |       |       |       |       |
| **9** | ~~7~~ |       |       |       |       |       |       |       |       |

These operations can be defined through the "simple" removal defined above:

```dotty
object SuDokuComplexInner:
  enum SuDokuInnerComplexOperation:
    case RemoveFromRow, RemoveFromColumn, RemoveFromBlock

  type SuDokuComplexInnerInput =
    (SuDokuInnerComplexOperation, SuDokuRow, SuDokuColumn, SuDokuCellValue)

  private val updateSuDoku
      : SuDokuComplexInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        (operation, result) match
          case (RemoveFromRow, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDokuColumn
                .values
                .withFilter(k => k != j)
                .map(k => (SuDokuInnerOperation.RemoveFromCell, i, k, value))
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromColumn, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDokuRow
                .values
                .withFilter(k => k != i)
                .map(k => (SuDokuInnerOperation.RemoveFromCell, k, j, value))
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromBlock, true) =>
            val block = SuDokuBlock.getBlock(i, j)
            val inputs: List[SuDokuInnerInput] =
              sudoku
                .cells
                .keys
                .withFilter: (r, c, b) =>
                  b == block && (r != i || c != j)
                .map: (r, c, _) =>
                  (SuDokuInnerOperation.RemoveFromCell, r, c, value)
                .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case _ => (false, sudoku)

  def execute(inputs: List[SuDokuComplexInnerInput]): SuDokuState =
    SuDokuSolver.executeInnerOperation(inputs, updateSuDoku)

end SuDokuComplexInner
```

Results of the operations:

```dotty
val initialSudoku = SuDoku.initial
val inputs: List[SuDokuComplexInnerInput] =
  List(
    (RemoveFromRow, SuDokuRow.Second, SuDokuColumn.Second, Six),
    (RemoveFromColumn, SuDokuRow.Second, SuDokuColumn.Second, Six),
    (RemoveFromBlock, SuDokuRow.Second, SuDokuColumn.Second, Six)
  )
val (_, sudoku) = SuDokuComplexInner.execute(inputs).run(initialSudoku)

sudoku.getCell(SuDokuRow.Second, SuDokuColumn.Second)
// Some(SuDokuCell(HashSet(Two, Five, Four, One, Six, Seven, Three, Nine, Eight)))
sudoku.getCell(SuDokuRow.First, SuDokuColumn.First)
// Some(SuDokuCell(HashSet(Two, Five, Four, One, Seven, Three, Nine, Eight)))
sudoku.getCell(SuDokuRow.Second, SuDokuColumn.First)
// Some(SuDokuCell(HashSet(Two, Five, Four, One, Seven, Three, Nine, Eight)))
sudoku.getCell(SuDokuRow.First, SuDokuColumn.Second)
// Some(SuDokuCell(HashSet(Two, Five, Four, One, Seven, Three, Nine, Eight)))
```

#### Main operations

After all side operations have been prepared, let us move on to the main ones.

##### Setting the only possible value in a cell

When setting a specific value at a given position, it is necessary not only to set the value in that cell,
but also to remove the value from the list of possible values in all cells of the same row, same column, and same block.
If during the batch of operations being executed, one of the operations fails,
it is necessary to return to the initial state that existed before executing the entire chain of operations.

```dotty
type SuDokuInput =
  (SuDokuRow, SuDokuColumn, SuDokuCellValue)

private val updateSuDoku: SuDokuInput => SuDokuOutput => SuDokuOutput =
  (i, j, value) =>
    (result, sudoku) =>
      val inputSet: SuDokuInnerInput =
        (SuDokuInnerOperation.SetToCell, i, j, value)
      val (resultSet, sudokuSet) =
        SuDokuInner.execute(inputSet).run(sudoku)

      val inputsRemove: List[SuDokuComplexInnerInput] =
        List(
          (SuDokuInnerComplexOperation.RemoveFromRow, i, j, value),
          (SuDokuInnerComplexOperation.RemoveFromColumn, i, j, value),
          (SuDokuInnerComplexOperation.RemoveFromBlock, i, j, value)
        )
      val (resultRemove, sudokuRemove) =
        SuDokuComplexInner.execute(inputsRemove).run(sudokuSet)

      if result && resultSet && resultRemove then (true, sudokuRemove)
      else (false, sudoku)
```

When executing a batch of operations to set values, a situation may arise
in which, in one of the cells from which a value was removed, only one option remains among the possible values:

|       | 1 | 2 | 3          |
|-------|---|---|------------|
| **1** | 7 |   |            |
| **2** |   |   |            |
| **3** |   |   | (1, ~~7~~) |

For example, after setting the value `7` at cell {1,1} in cell {3,3} among the possible options only `1` remains.
This means that for cell {3,3} we can also run the value-setting operation
(the value `1` is already set, but we still need to cross out `1` from the row, column, and block).

- determine the list of indices where there is already a single value
- compose the list of indices where a single value will be set
- execute the value-setting operation
- determine the list of indices where there is already a single value, for the new state of the Sudoku board
- for all indices not found in the first two points, run the value-setting operation again

```dotty
final case class SuDoku(cells: Map[SuDokuIndex, SuDokuCell]):
  ...

  val singletonIndexes: Vector[SuDokuIndex] =
    cells.collect {
      case (index, cell) if cell.maybeValue.isDefined =>
        index
    }.toVector
  
object SuDokuSolver:    
  def setValue(
      i: SuDokuRow,
      j: SuDokuColumn,
      value: SuDokuCellValue
  ): SuDokuState =
    setValues((i, j, value))

  def setValues(inputs: SuDokuInput*): SuDokuState =
    State[SuDoku, Boolean] { sudoku =>
      // List of indices where the value is already known
      val initialIndexes: Vector[SuDokuIndex] =
        sudoku.singletonIndexes
      // List of indices where the value will be set via inputs
      val indexesAfterRun: Vector[SuDokuIndex] =
        inputs.map((r, c, _) => (r, c, SuDokuBlock.getBlock(r, c)))
          .toVector

      // Execution of the value-setting operation
      val (newResult, newSudoku) =
        executeInnerOperation(inputs.toList, updateSuDoku).run(sudoku)

      // New indices that appeared after executing the operations
      val newIndexes =
        newSudoku.singletonIndexes.diff(initialIndexes ++ indexesAfterRun)

      if newIndexes.isEmpty then (newResult, newSudoku)
      else
        // If new values appeared, unique in their cells,
        // then we also need to cross them out from rows, columns, and blocks
        val newInputs: Vector[SuDokuInput] =
          for
            (r, c, _) <- newIndexes
            cell      <- newSudoku.getCell(r, c)
            value     <- cell.maybeValue
          yield (r, c, value)
        val (extraResult, extraSudoku) = setValues(newInputs*).run(newSudoku)
        (newResult && extraResult, extraSudoku)
    }
  
val initialSudoku = SuDoku.initial
val (result, sudoku) =
  SuDokuSolver.setValue(SuDokuRow.First, SuDokuColumn.Third, SuDokuCellValue.Three).run(initialSudoku)

sudoku.getCell(SuDokuRow.First, SuDokuColumn.Third)
// Some(SuDokuCell(Set(Three)))
sudoku.getCell(SuDokuRow.First, SuDokuColumn.First)
// Some(SuDokuCell(HashSet(Eight, Two, Four, One, Six, Nine, Five, Seven)))
sudoku.getCell(SuDokuRow.Second, SuDokuColumn.Third)
// Some(SuDokuCell(HashSet(Eight, Two, Four, One, Six, Nine, Five, Seven)))
sudoku.getCell(SuDokuRow.Second, SuDokuColumn.Second)
// Some(SuDokuCell(HashSet(Eight, Two, Four, One, Six, Nine, Five, Seven)))
```

##### Finding values that can be crossed out

Find such values that can be crossed out from a cell, since setting values in the board leads to an error.

In the following example, the value `9` cannot be set at cell {1,2}, because otherwise it would lead to an error
when trying to cross out the value `9` from cell {3,3}.
This means that the value `9` can be crossed out from the list of possible values for cell {1,2}.

|       | 1     | 2       | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
|-------|-------|---------|---|---|---|---|---|---|---|
| **1** | (1,2) | (1,3,9) | 4 |   |   |   |   |   |   |
| **2** | (2,3) | 5       | 6 |   |   |   |   |   |   |
| **3** | 7     | 8       | 9 |   |   |   |   |   |   |
| **4** |       |         |   |   |   |   |   |   |   |
| **5** |       |         |   |   |   |   |   |   |   |
| **6** |       |         |   |   |   |   |   |   |   |
| **7** |       |         |   |   |   |   |   |   |   |
| **8** |       |         |   |   |   |   |   |   |   |
| **9** |       |         |   |   |   |   |   |   |   |

**Algorithm:**

- go through each row and each column
- in each cell, search among the list of possible values for such values
  that upon insertion into a given position lead to an error —
  this means that this value cannot be inserted into this cell and can be crossed out
- cross out the value
- try to find the next candidate for crossing out in the same cell
- if no remaining values can be crossed out in a cell, move on to the next cell

```dotty
object SuDokuRow:
  def next(row: SuDokuRow): Option[SuDokuRow] =
    val next   = row.ordinal + 1
    val values = SuDokuRow.values
    if next >= values.length then None else Some(values(next))

object SuDokuColumn:
  def next(row: SuDokuColumn): Option[SuDokuColumn] =
    val next   = row.ordinal + 1
    val values = SuDokuColumn.values
    if next >= values.length then None else Some(values(next))

object SuDokuSolver:
  @tailrec
  def crossOutLoop(
      sudoku: SuDoku,
      maybeRow: Option[SuDokuRow],
      maybeColumn: Option[SuDokuColumn]
  ): SuDokuOutput =
    val maybeCell =
      for
        row  <- maybeRow
        col  <- maybeColumn
        cell <- sudoku.getCell(row, col)
      yield cell
    (maybeRow, maybeColumn, maybeCell) match
      case (None, _, _) => // Reached the end of the board
        (true, sudoku)
      case (Some(row), None, _) => // Reached the end of the row
        crossOutLoop(sudoku, SuDokuRow.next(row), Some(SuDokuColumn.First))
      case (Some(row), Some(col), None) => // Invalid cell
        (false, sudoku)
      case (Some(row), Some(col), Some(sudokuCell)) =>
        val currentValues = sudokuCell.possibleValues
        if currentValues.size == 1 then
          // Exact value is defined, move to the next cell
          crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
        else
          // Find values in the cell that can be crossed out
          val invalidValues = currentValues.filter: value =>
            !setValue(row, col, value).run(sudoku)._1
          if invalidValues.isEmpty then
            // No such values, move to the next cell
            crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
          else
            val input: Seq[SuDokuInnerInput] =
              invalidValues.map: invalidValue =>
                (SuDokuInnerOperation.RemoveFromCell, row, col, invalidValue)
              .toSeq
            val (result, newSudoku) = SuDokuInner.execute(input*).run(sudoku)
            // If successful, move to the next cell
            if result then
              crossOutLoop(newSudoku, Some(row), SuDokuColumn.next(col))
            // If not, then no solution exists for the puzzle
            else (false, sudoku)
        end if
    end match
  end crossOutLoop  
```

##### Only possible position for a value in a block

Find such a value in a cell that does not appear in other cells of the block,
but at the same time this value is not the only possible one in its own cell.
Since this value does not appear in other cells, it can only be at the given position.
Such values can be set as the only ones in their cells,
thereby reducing the possible options for other values.
There can be at most one such value per cell, otherwise the Sudoku is incorrectly composed.

For example, at cell {1,1} the value `2` can be set as the only possible one, since this number does not appear in other cells.

|       | 1       | 2       | 3       | 4 | 5 | 6 | 7 | 8 | 9 |
|-------|---------|---------|---------|---|---|---|---|---|---|
| **1** | (1,2)   | (1,3,9) | 4       |   |   |   |   |   |   |
| **2** | (1,3,9) | 5       | 6       |   |   |   |   |   |   |
| **3** | 7       | 8       | (1,3,9) |   |   |   |   |   |   |

|       | 1     | 2     | 3     | 4     | 5     | 6     | 7     | 8     | 9     |
|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|
| **4** |       |       |       |       |       |       |       |       |       |
| **5** |       |       |       |       |       |       |       |       |       |
| **6** |       |       |       |       |       |       |       |       |       |
| **7** |       |       |       |       |       |       |       |       |       |
| **8** |       |       |       |       |       |       |       |       |       |
| **9** |       |       |       |       |       |       |       |       |       |

```dotty
def getSingleValues(
    sudoku: SuDoku,
    block: SuDokuBlock
): List[(SuDokuRow, SuDokuColumn, SuDokuCellValue)] =
  val blockCells = sudoku.cells.filter { case ((_, _, blk), cell) =>
    blk == block
  }
  def notInOtherCells(index: SuDokuIndex, value: SuDokuCellValue): Boolean =
    blockCells.forall { (otherIndex, otherCell) =>
      otherIndex == index || !otherCell.possibleValues.contains(value)
    }
  blockCells.foldLeft(List.empty[SuDokuInput]) { case (acc, (index, cell)) =>
    // If the value in the cell is already determined, move to the next one
    if cell.maybeValue.isDefined then acc
    else
      // Otherwise, find the first value that does not appear in others
      cell
        .possibleValues
        .collectFirst:
          case value if notInOtherCells(index, value) =>
            val input: SuDokuInput = (index._1, index._2, value)
            input :: acc
        .getOrElse(acc)
  }
end getSingleValues

@tailrec
def blockSingleValueLoop(
    sudoku: SuDoku,
    blocks: List[SuDokuBlock]
): SuDokuOutput =
  blocks match
    case Nil => (true, sudoku) // Gone through all blocks
    case head :: tail =>
      val singleValues = getSingleValues(sudoku, head)
      if singleValues.isEmpty then
        // If there are no single values, move to the next blocks
        blockSingleValueLoop(sudoku, tail)
      else
        // If there are, set such values
        val (result, sudoku2) = setValues(singleValues*).run(sudoku)
        // If the setting completed successfully, move to the next block
        if result then blockSingleValueLoop(sudoku2, tail)
        else (false, sudoku)
```

#### Finding the solution to the puzzle

A puzzle is solved as long as:

- a solution is found
- if there are values that can be crossed out
- if there are values that in a given block can only be in certain cells

Define the state changes of the Sudoku based on all the listed operations:

```dotty
private val blockSingleValueState = State[SuDoku, Boolean](
  blockSingleValueLoop(_, SuDokuBlock.values.toList)
)
private val crossOutState = State[SuDoku, Boolean](
  crossOutLoop(_, Some(SuDokuRow.First), Some(SuDokuColumn.First))
)
private val loopState: State[SuDoku, Boolean] =
  blockSingleValueState.map2(crossOutState)(_ && _)
```

Recursively run the puzzle solution until a solution is found and the Sudoku board changes.
As soon as none of the operations was able to make any new changes to the Sudoku board, return the result.

```dotty
@tailrec
private def solutionLoop(sudoku: SuDoku): SuDokuOutput =
  if sudoku.hasSolution then (true, sudoku)
  else
    val (result, newSudoku) = loopState.run(sudoku)
    if !result then (false, sudoku)
    else if sudoku != newSudoku then solutionLoop(newSudoku)
    else (result, sudoku)

val solutionState: SuDokuState = State[SuDoku, Boolean](solutionLoop)
```

Summary:

![](https://projecteuler.net/project/images/p096_1.png)
![](https://projecteuler.net/project/images/p096_2.png)

```dotty
val (_, convertedSudoku) = SuDokuSolver.toSuDoku(
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
val (_, sudokuWithSolution) =   SuDokuSolver.solutionState.run(convertedSudoku)

// |(4)|(8)|(3)|(9)|(2)|(1)|(6)|(5)|(7)|
// |(9)|(6)|(7)|(3)|(4)|(5)|(8)|(2)|(1)|
// |(2)|(5)|(1)|(8)|(7)|(6)|(4)|(9)|(3)|
// |(5)|(4)|(8)|(1)|(3)|(2)|(9)|(7)|(6)|
// |(7)|(2)|(9)|(5)|(6)|(4)|(1)|(3)|(8)|
// |(1)|(3)|(6)|(7)|(9)|(8)|(2)|(4)|(5)|
// |(3)|(7)|(2)|(6)|(8)|(9)|(5)|(1)|(4)|
// |(8)|(1)|(4)|(2)|(5)|(3)|(7)|(6)|(9)|
// |(6)|(9)|(5)|(4)|(1)|(7)|(3)|(8)|(2)|
```

## Eight Queens Problem

**Algorithm:**

The "eight queens" puzzle asks how to place eight queens on a chessboard so that
none of them attacks another (i.e., no two queens should be on the same vertical, horizontal, or diagonal line).

One way to solve this problem is to go across the board, placing one queen in each column.
After placing k − 1 queens, we need to place the k-th one in such a place
that it does not attack any of those already on the board.
This approach can be formulated recursively: suppose
we have already generated a sequence of all possible ways to place k − 1 queens on the first k − 1 columns of the board.
For each of these ways, we generate an extended set of positions by adding a queen to each row of the k-th column.
Then these positions must be filtered, keeping only those where the queen in the k-th column 
does not attack any of the others.
By proceeding with this process, we will generate all solutions to this problem.

**Code:**

The goal is to place N queens on an N x N chessboard so that no queen attacks another.

The `queens` function takes an integer `boardSize` as the input parameter for the board size
and returns a list of lists of queen positions, where each position is represented as a tuple of two integers `(row, column)`.

The `isSafe` function takes a list of queen positions as an input parameter and returns a boolean value
indicating whether the positions are safe, i.e., no queen attacks another.
If the list is not empty, it checks that the current queen's position `(row, col)` does not attack any of the previous queens in the list `tail`.
The predicate checks two conditions:

- `r != row`: the rows of the current queen and previous queens differ.
- `math.abs(r - row) != math.abs(c - col)`: the queens are not on the same diagonal.
  
If the list is empty, the function returns `true`, indicating that the position is safe (since there are no previous queens that could attack).

The `queenCols` function uses recursion to generate all possible queen positions:

If `k` is `0`, the function returns a list containing an empty list, indicating that there are no queens to place.
Otherwise, the function recursively calls itself with `k - 1` and generates all possible positions for the remaining queens.
For each recursive result, the function generates new queen positions `(newRow, k)` and adds them to the recursive result.
It then filters the resulting list using `isSafe` to ensure that only safe positions are kept.

```dotty
def queens(boardSize: Int): List[List[(Int, Int)]] =
  def isSafe(positions: List[(Int, Int)]): Boolean =
    positions match
      case (row, col) :: tail =>
        tail.forall: (r, c) =>
          r != row &&                              // same row
            math.abs(r - row) != math.abs(c - col) // same diagonal
      case Nil => true

  def queenCols(k: Int): List[List[(Int, Int)]] =
    if k == 0 then List(List.empty)
    else
      queenCols(k - 1).flatMap: restOfQueens =>
        (1 to boardSize).map: newRow =>
          (newRow, k) :: restOfQueens
      .filter(isSafe)

  queenCols(boardSize)
end queens
```

---

**References:**

- [Source code][sample]
- [Darts - Project Euler](https://projecteuler.net/problem=109)

[sample]: https://gitverse.ru/scalabook/algorithms
