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

##### Игровая карта

Определение игровой карты в покере на Scala.

Следующий код реализует несколько классов и типов данных,
которые могут быть использованы для представления карт в игре покер.

1. `PokerSuit` - это перечисление, которое определяет четыре масти в покере:
   "D" - бубны, "S" - пики, "C" - трефы, "H" - червы.
2. `PokerRank` - это перечисление, которое определяет 13 рангов карт в покере.
   Каждый ранг имеет целочисленное значение, которое представляет собой его ранг в игре.
   Например, "A" (туз) имеет значение 14, "K" (король) - 13 и т.д.
3. `PokerCard` - это нерасширяемый кейс-класс, который представляет карту в покере.
   Он содержит два поля: `pokerRank` и `pokerSuit`, которые представляют собой ранг и масть карты соответственно.
4. В сопутствующих объектах `PokerSuit` и `PokerRank` объявлены служебные методы.
   В частности, `withNameOpt` - это метод, который пытается найти элемент перечисления по его строковому представлению.
5. В сопутствующем объекте `PokerCard` объявлен дополнительный конструктор `apply`.
   Этот метод принимает строку, которая представляет собой карту, например "AS" для туза пик,
   и пытается преобразовать ее в `PokerCard`. Если преобразование невозможно, он возвращает `None`.

**Код:**

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


##### Покерный расклад

Следующий код реализует логику игры в покер. Он включает в себя определение разных типов карт, их рангов и мастей,
а также определение руки в игре покер.

1. `PokerHandsType` - это перечисление, определяющее все возможные типы рук в покере.
2. `PokerHand` - это нерасширяемый кейс-класс, который представляет расклад в игре покер.
   Он содержит два поля: `pokerHandsType` и `ranks`, которые представляют собой тип руки и список рангов карт соответственно. Класс также содержит метод `compareTo`, который сравнивает две руки покер.
3. В сопутствующем объекте `PokerHand` объявлены служебные методы для создания покерной руки из строки,
   из типа руки и ранга карты, а также для сравнения покерных раскладов.
4. Метод `apply(hand: String)` принимает строку, представляющую собой покерный расклад,
   и пытается преобразовать ее в `PokerHand`. Если преобразование невозможно, он возвращает `None`.
5. Метод `apply(pokerHandsType: PokerHandsType, rank: PokerRank)` создает покерный расклад из заданного типа руки и ранга карты.
6. Метод `apply(cards: Array[PokerCard])` создает покерный расклад из массива карт.
   Он определяет тип руки и ранги карт на основе переданных карт и создает `PokerHand` с соответствующими значениями.
7. Методы `isFiveOfAKind`, `isStraightFlush`, `isFourOfAKind`, `isFullHouse`, `isFlush`, `isStraight`, `isThreeOfAKind`,
   `isTwoPair`, `isOnePair` и `highCard` определяют тип руки на основе переданных карт и возвращают соответствующие значения.
8. Методы `fourOfAKind`, `fullHouse`, `threeOfAKind`, `twoPair` и `onePair` создают покерный расклад
   определенного типа с соответствующими рангами карт.
9. Метод `highCard` создает покерный расклад типа `HIGH_CARD` с соответствующими рангами карт.
10. Метод `compareTo` сравнивает две руки покер. Он сравнивает типы рук, а затем ранги карт в случае равенства типов.

**Код:**

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

**Метрики:**

???

## Судоку

**Алгоритм:**

Судоку.

**Оценка:**

- **Время** - ???
- **Память** - ???

В первую очередь определим ячейку для игры Су Доку:

- Возможные значения ячейки - от 1 до 9
- В начальном состоянии ячейка может содержать любое из значений от 1 до 9
- Значение ячейки определено, если из возможных значений остался только один вариант
- В ячейку можно установить определенное значение

**Код:**

```dotty
enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  def apply(cellValue: SuDokuCellValue): SuDokuCell = SuDokuCell(Set(cellValue))
```

Изменять состояние ячейки можно не только путем установки конкретного значения, 
но и путем удаления из списка возможных части значений.

- Доступно удаление значения из списка возможных

**Код:**

```dotty
enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

  def removeValue(cellValue: SuDokuCellValue): Option[SuDokuCell] =
    maybeValue match
      case Some(value) if value == cellValue => None
      case _ => Some(SuDokuCell(possibleValues - cellValue))

object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  def apply(cellValue: SuDokuCellValue): SuDokuCell = SuDokuCell(Set(cellValue))
```


Определим поле \\(9 \times 9\\) для игры Су Доку и добавим следующие операции:

- `removeValue` - удаление значения из заданной ячейки
- `setValue` - установка заданного значения в заданную ячейку

Эти операции позволят нам определить первоначальное состояние игры, когда заполнены некоторые ячейки.

**Код:**

```dotty
final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  def setValue(i: Int, j: Int, value: SuDokuCellValue): Option[SuDoku] = ???

  def removeValue(i: Int, j: Int, value: SuDokuCellValue): Option[SuDoku] =
    if doesCellNotExist(i, j) then None
    else
      cells(i)(j).removeValue(value).map: updatedCell =>
        SuDoku(
          cells.updated(i, cells(i).updated(j, updatedCell))
        )

  private def doesCellNotExist(i: Int, j: Int): Boolean =
    cells.length <= i || cells(i).length <= j
```

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
