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
   Он содержит два поля: `pokerHandsType` и `ranks`, которые представляют собой тип руки и список рангов карт
   соответственно. Класс также содержит метод `compareTo`, который сравнивает две руки покер.
3. В сопутствующем объекте `PokerHand` объявлены служебные методы для создания покерной руки из строки,
   из типа руки и ранга карты, а также для сравнения покерных раскладов.
4. Метод `apply(hand: String)` принимает строку, представляющую собой покерный расклад,
   и пытается преобразовать ее в `PokerHand`. Если преобразование невозможно, он возвращает `None`.
5. Метод `apply(pokerHandsType: PokerHandsType, rank: PokerRank)` создает покерный расклад из заданного типа руки и
   ранга карты.
6. Метод `apply(cards: Array[PokerCard])` создает покерный расклад из массива карт.
   Он определяет тип руки и ранги карт на основе переданных карт и создает `PokerHand` с соответствующими значениями.
7. Методы `isFiveOfAKind`, `isStraightFlush`, `isFourOfAKind`, `isFullHouse`, `isFlush`, `isStraight`, `isThreeOfAKind`,
   `isTwoPair`, `isOnePair` и `highCard` определяют тип руки на основе переданных карт и возвращают соответствующие
   значения.
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

Су Доку (по-японски значит _место числа_) - название популярной головоломки.
Целью Су Доку является заменить пустые места (или нули) в сетке 9 x 9 цифрами таким образом,
чтобы в каждой строке, колонке и квадрате 3 x 3 содержались все цифры от 1 до 9.
Ниже приведен пример типичной исходной головоломки и ее решение.

![](https://projecteuler.net/project/images/p096_1.png)
![](https://projecteuler.net/project/images/p096_2.png)

**Алгоритм:**

Определим список допустимых значений ячейки игры Су Доку - это цифры от 1 до 9:

**Код:**

```dotty
enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine
```

Сама ячейка должна удовлетворять следующим условиям:

- В начальном состоянии ячейка может содержать любое из значений от 1 до 9
- Точное значение ячейки определено, если из возможных значений остался только один вариант

**Код:**

```dotty
final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None
    
object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet) 

SuDokuCell.initial.possibleValues
// HashSet(One, Two, Three, Four, Five, Six, Seven, Eight, Nine)
SuDokuCell.initial.maybeValue    
// None
SuDokuCell(Set(SuDokuCellValue.Three)).possibleValues 
// Set(Three)
SuDokuCell(Set(SuDokuCellValue.Three)).maybeValue 
// Some(Three)
```

Ячейка должна поддерживать следующие операции:

- В ячейку можно установить определенное значение в качестве единственного, если оно находится среди возможных значений
- Из возможных значений ячейки можно удалить значение, только если это не единственное возможное значение ячейки

Определим новый тип описывающий [функциональное состояние](../../fp/state.md) ячейки `SuDokuCellState`,
который по заданной ячейке возвращает кортеж из двух элементов,
где слева - признак, успешно ли завершилась предыдущая операция,
а справа - последнее стабильное состояние ячейки.

**Код:**

```dotty
object SuDokuCell:
  enum SuDokuCellOperation:
    case Set, Remove

  type SuDokuCellInput         = (SuDokuCellOperation, SuDokuCellValue)
  opaque type SuDokuCellOutput = (Boolean, SuDokuCell)

  private val updateCell
      : SuDokuCellInput => SuDokuCellOutput => SuDokuCellOutput =
    (operation, value) =>
      (result, cell) =>
        (operation, value, result) match
          // Устанавливаем значение в ячейке 
          // только если это значение среди допустимых
          case (SuDokuCellOperation.Set, value, true)
              if cell.possibleValues.contains(value) =>
            (true, SuDokuCell(Set(value)))
          // Удаляем значение в ячейке 
          // только если это не единственное значение из допустимых
          case (SuDokuCellOperation.Remove, value, true)
              if cell.possibleValues != Set(value) =>
            (true, SuDokuCell(cell.possibleValues - value))
          case _ =>
            (false, cell)

  def execute(inputs: SuDokuCellInput*): State[SuDokuCell, Boolean] =
    State[SuDokuCell, Boolean] { cell =>
      val state =
        for
          _ <- State.traverse(inputs.toList)(input =>
            State.modify[SuDokuCellOutput](updateCell(input))
          )
          output <- State.get
        yield output._1

      state.run((true, cell))._2
    }

end SuDokuCell

val inputs = List(
  (SuDokuCellOperation.Remove, SuDokuCellValue.One),
  (SuDokuCellOperation.Remove, SuDokuCellValue.Two)
)
SuDokuCell.execute(inputs*).run(SuDokuCell.initial)
// (true,SuDokuCell(HashSet(Three, Four, Eight, Seven, Nine, Six, Five)))

val input = (SuDokuCellOperation.Set, SuDokuCellValue.Two)
SuDokuCell.execute(input).run(SuDokuCell.initial)
// (true,SuDokuCell(Set(Two)))
```

Определим, что такое поле для игры Су Доку:

- Поле игры Су Доку представляет собой ячейки \\(9 \times 9\\).
- В начальном состоянии в каждой ячейке поля список возможных значений равен от 1 до 9.
- Решение игры найдено, если в каждом поле осталось ровно по одному значению

**Код:**

```dotty
final case class SuDoku(cells: Vector[Vector[SuDokuCell]])

object SuDoku:
  val initial: SuDoku =
    val defaultCells =
      (0 to 8)
        .toVector
        .map: _ =>
          (0 to 8).toVector.map: _ =>
            SuDokuCell.initial

    SuDoku(defaultCells)

  type Row          = Int
  object Row:
    def isCorrect(row: Int): Boolean = 0 <= row && row < 9
  type Column = Int
  object Column:
    def isCorrect(col: Int): Boolean = 0 <= col && col < 9
    
val sudoku = SuDoku.initial
sudoku.isCorrect     // true
sudoku.hasSolution   // false
```

Кроме того, каждая ячейка принадлежит определенному блоку \\(3 \times 3\\):

**Код:**

```dotty
def getBlock(i: Row, j: Column): IndexedSeq[(Row, Column)] =
  if Row.isCorrect(i) && Column.isCorrect(j) then
    for
      k <- 0 to 2
      l <- 0 to 2
    yield
      val r = (i / 3) * 3 + k
      val c = (j / 3) * 3 + l
      (r, c)
  else IndexedSeq.empty

SuDoku.getBlock(4, 4)
// Vector((3,3), (3,4), (3,5), (4,3), (4,4), (4,5), (5,3), (5,4), (5,5))  
```

Поле для игры в Су Доку корректное при выполнении следующих условий:

- Размер поля \\(9 \times 9\\)
- Нет ячеек с пустыми списком
- В каждой строчке нет двух ячеек с одним и тем же определенным значением
- В каждом столбце нет двух ячеек с одним и тем же определенным значением
- В каждом блоке нет двух ячеек с одним и тем же определенным значением

**Код:**

```dotty
final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  val isCorrect: Boolean =
    cells.length == 9 &&
      cells.forall(_.length == 9) &&
      cells.forall(_.forall(_.possibleValues.nonEmpty)) &&
      cells.forall(row => hasUniqueValues(row)) &&
      (0 to 8).forall(j => hasUniqueValues(cells.map(row => row(j)))) && {
        val block =
          for
            i          <- 0 to 6 by 3
            j          <- 0 to 6 by 3
            (row, col) <- getBlock(i, j)
          yield cells(row)(col)
        hasUniqueValues(block)
      }

  val hasSolution: Boolean =
    isCorrect && cells.forall(_.forall(_.maybeValue.isDefined))

object SuDoku:
  private def hasUniqueValues(cells: IndexedSeq[SuDokuCell]): Boolean =
    val values = cells.flatMap(_.maybeValue)
    values.distinct.size == values.size
```

Определим общую операцию изменения состояния игры Су Доку:
для любого типа входящих данных `A` и заданной операции `update`
(`update` по значению типа `A` возвращает функцию преобразования из `SuDokuOutput` в `SuDokuOutput`, 
где `SuDokuOutput` - кортеж из двух элементов: результат выполнения предыдущей операции и поле Су Доку)
возвращает функциональное состояние `SuDokuState`:

**Код:**

```dotty
object SuDoku:
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
          output <- State.get
        yield output._1

      state.run((true, sudoku))._2
    }  
```

Следующее, что нам понадобится - это установка начального состояния в игре,
когда заполнены часть полей из известных значений.

Поэтому остановимся на операции установки значения в заданное поле (i, j).
Эта операция состоит из четырех побочных операций:

- установка заданного значения в поле (i, j)
- удаление заданного значения из строчки i за исключением ячейки (i, j)
- удаление заданного значения из столбца j за исключением ячейки (i, j)
- удаление заданного значения из блока \\(3 \times 3\\), которому принадлежит ячейка (i, j), за исключением самой
  ячейки

Определим побочные операции установки и удаления значения в заданном поле:

**Код:**

```dotty
object SuDokuInner:
  enum SuDokuInnerOperation:
    case SetToCell, RemoveFromCell

  type SuDokuInnerInput = (SuDokuInnerOperation, Row, Column, SuDokuCellValue)

  private val updateSuDoku: SuDokuInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        // Выполняем операцию только если поле, строка и столбец корректные,
        // а также если предыдущая операция завершилась успешно
        val isCorrect =
          sudoku.isCorrect && Row.isCorrect(i) && Column.isCorrect(j) && result

        if isCorrect then
          operation match
            case SetToCell =>
              updateCell(SuDokuCellOperation.Set, sudoku, i, j, value)
            case RemoveFromCell =>
              updateCell(SuDokuCellOperation.Remove, sudoku, i, j, value)
        else (false, sudoku)

  // Определяем текущую ячейку, выполняем операцию, в случае успеха обновляем ячейку
  private def updateCell(
      oper: SuDokuCellOperation,
      sudoku: SuDoku,
      i: Int,
      j: Int,
      value: SuDokuCellValue
  ): (Boolean, SuDoku) =
    val oldCell = sudoku.cells(i)(j)
    val (result, newCell) =
      SuDokuCell.execute((oper, value)).run(oldCell)
    if result then
      val newCells =
        sudoku.cells.updated(i, sudoku.cells(i).updated(j, newCell))
      (true, SuDoku(newCells))
    else (false, sudoku)

  def execute(inputs: SuDokuInnerInput*): SuDokuState =
    SuDoku.executeInnerOperation(inputs.toList, updateSuDoku)

end SuDokuInner 

val initialSudoku = SuDoku.initial
val set: SuDokuInnerInput =
  (SuDokuInnerOperation.SetToCell, 1, 2, SuDokuCellValue.Six)
val (_, sudoku0) = SuDokuInner.execute(set).run(initialSudoku)
sudoku0.cells(1)(2)
// SuDokuCell(Set(Six))

val remove: SuDokuInnerInput =
  (SuDokuInnerOperation.RemoveFromCell, 2, 1, SuDokuCellValue.Five)
val (_, sudoku1) = SuDokuInner.execute(remove).run(initialSudoku)
sudoku1.cells(2)(1)
// SuDokuCell(HashSet(Four, Three, One, Nine, Eight, Six, Seven, Two))
```

Три более сложные побочные операции на удаление:

- удаление заданного значения из строчки i за исключением ячейки (i, j)
- удаление заданного значения из столбца j за исключением ячейки (i, j)
- удаление заданного значения из квадрата \\(3 \times 3\\), которому принадлежит ячейка (i, j), 
  за исключением самой ячейки

можно определить через "простое" удаление, определенное выше:

**Код:**

```dotty
object SuDokuComplexInner:
  enum SuDokuInnerComplexOperation:
    case RemoveFromRow, RemoveFromColumn, RemoveFromBlock

  type SuDokuComplexInnerInput =
    (SuDokuInnerComplexOperation, Row, Column, SuDokuCellValue)

  private val updateSuDoku
      : SuDokuComplexInnerInput => SuDokuOutput => SuDokuOutput =
    (operation, i, j, value) =>
      (result, sudoku) =>
        (operation, result) match
          case (RemoveFromRow, true) =>
            val inputs: List[SuDokuInnerInput] =
              (0 to 8).flatMap: k =>
                if k == j then None
                else Some((SuDokuInnerOperation.RemoveFromCell, i, k, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromColumn, true) =>
            val inputs: List[SuDokuInnerInput] =
              (0 to 8).flatMap: k =>
                if k == i then None
                else Some((SuDokuInnerOperation.RemoveFromCell, k, j, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case (RemoveFromBlock, true) =>
            val inputs: List[SuDokuInnerInput] =
              SuDoku.getBlock(i, j).flatMap: (r, c) =>
                if r == i && c == j then None
                else Some((SuDokuInnerOperation.RemoveFromCell, r, c, value))
              .toList
            SuDokuInner.execute(inputs*).run(sudoku)
          case _ => (false, sudoku)

  def execute(inputs: List[SuDokuComplexInnerInput]): SuDokuState =
    SuDoku.executeInnerOperation(inputs, updateSuDoku)

end SuDokuComplexInner

val initialSudoku = SuDoku.initial
val inputs: List[SuDokuComplexInnerInput] =
  List(
    (SuDokuInnerComplexOperation.RemoveFromRow, 1, 1, SuDokuCellValue.Six),
    (SuDokuInnerComplexOperation.RemoveFromColumn, 1, 1, SuDokuCellValue.Six),
    (SuDokuInnerComplexOperation.RemoveFromBlock, 1, 1, SuDokuCellValue.Six)
  )
val (_, sudoku) = SuDokuComplexInner.execute(inputs).run(initialSudoku)

sudoku.cells(1)(1)
// SuDokuCell(HashSet(Five, Nine, One, Seven, Three, Two, Eight, Four, Six))
sudoku.cells(0)(0)
// SuDokuCell(HashSet(Five, Nine, One, Seven, Three, Two, Eight, Four))
sudoku.cells(1)(0)
// SuDokuCell(HashSet(Five, Nine, One, Seven, Three, Two, Eight, Four))
sudoku.cells(0)(1) 
// SuDokuCell(HashSet(Five, Nine, One, Seven, Three, Two, Eight, Four))
```

После того как подготовлены все побочные операции, перейдем к операции установки единственного значения в ячейку.

Напомню, что при установке конкретного значения в заданную ячейку необходимо не только задать значение в эту ячейку, 
а также и удалить значение из списка возможных значений во всех ячейках той же строки, того же столбца и того же блока.
При этом, если в выполняемой пачке операций возникла ситуация, когда одна из операций не выполнилась,
то необходимо вернуться в исходное состояние, бывшее до выполнения всей цепочки операций.

**Код:**

```dotty
type SuDokuInput =
  (Row, Column, SuDokuCellValue)

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

      // Если все операции завершились успешно, то...
      if result && resultSet && resultRemove then (true, sudokuRemove)
      else (false, sudoku)

/** Устанавливает заданное значение в ячейку. */
def setValue(i: Int, j: Int, value: SuDokuCellValue): SuDokuState =
  setValues((i, j, value))

/** Устанавливает заданные значения в пачку ячеек. */
def setValues(inputs: SuDokuInput*): SuDokuState =
  SuDoku.executeInnerOperation(inputs.toList, updateSuDoku)
  
val initialSudoku = SuDoku.initial
val (result, sudoku) =
  SuDoku.setValue(0, 2, SuDokuCellValue.Three).run(initialSudoku)

sudoku.cells(0)(2)
// SuDokuCell(Set(Three))
sudoku.cells(0)(0)
// SuDokuCell(HashSet(Nine, Two, One, Four, Eight, Six, Seven, Five))
sudoku.cells(1)(2)
// SuDokuCell(HashSet(Nine, Two, One, Four, Eight, Six, Seven, Five))
sudoku.cells(1)(1)  
// SuDokuCell(HashSet(Nine, Two, One, Four, Eight, Six, Seven, Five)) 
```

##### Поиск решения головоломки

Поиск значений, которые можно вычеркнуть:

- проходимся по каждой строчке и по каждому столбцу
- в каждой ячейке ищем среди списка возможных такие значения, 
  которые при вставке в заданное поле приводят к ошибке - 
  это означает, что в данную ячейку такое значение вставлять нельзя и его можно вычеркнуть
- вычеркиваем значение
- пытаемся найти следующий кандидат на вычеркивание в той же ячейке
- если в ячейке не осталось значений, которые можно вычеркнуть, то переходим к следующей ячейке

**Код:**

```dotty
@tailrec
def crossOutLoop(sudoku: SuDoku, i: Int, j: Int): SuDokuOutput =
  // Дошли до конца поля
  if i >= 9 then (true, sudoku)
  // Дошли до конца столбца, переходим к следующей строке
  else if j >= 9 then crossOutLoop(sudoku, i + 1, 0)
  else
    val currentValues = sudoku.cells(i)(j).possibleValues
    // Точное значение определено, переходим к следующей ячейке
    if currentValues.size == 1 then
      crossOutLoop(sudoku, i, j + 1)
    else // Ищем значение в ячейке, которое можно вычеркнуть
      val maybeInvalidValue = currentValues.find: value =>
        !setValue(i, j, value).run(sudoku)._1
      maybeInvalidValue match
        // Такого значения нет, переходим к следующей ячейке
        case None => crossOutLoop(sudoku, i, j + 1)
        case Some(invalidValue) =>
          val input: SuDokuInnerInput =
            (SuDokuInnerOperation.RemoveFromCell, i, j, invalidValue)
          val (result, newSudoku) =
            SuDokuInner.execute(input).run(sudoku)
          // Если результат вычеркивания успешен,
          // то ищем в заданной ячейке следующий кандидат на вычеркивание
          if result then crossOutLoop(newSudoku, i, j)
          // Если не успешен, значит решения головоломки не существует
          else (false, sudoku)
    end if
```

Головоломка решается до тех пор, пока:

- не найдено решение
- если есть значения, которые можно вычеркнуть

**Код:**

```dotty
def getSolution: SuDokuState =
  State[SuDoku, Boolean] {
    (sudoku: SuDoku) =>

      @tailrec
      def loop(sudoku: SuDoku): SuDokuOutput =
        val (result, newSudoku)   = crossOutLoop(sudoku, 0, 0)
        val (result2, newSudoku2) = blockSingleValueLoop(newSudoku, 0, 0)
        if result && result2 && sudoku == newSudoku2 then
          // Ничего не изменилось, не можем найти решение
          (result, sudoku)
        else if result && result2 then
          loop(newSudoku)
        else (false, sudoku)

      loop(sudoku)
  }
```

В результате получим:

![](https://projecteuler.net/project/images/p096_1.png)
![](https://projecteuler.net/project/images/p096_2.png)

```dotty
val initialSudoku = SuDoku.initial
val inputs: Seq[SuDokuInput] = Seq(
  (0, 2, SuDokuCellValue.Three), (0, 4, SuDokuCellValue.Two), (0, 6, SuDokuCellValue.Six),
  (1, 0, SuDokuCellValue.Nine), (1, 3, SuDokuCellValue.Three), (1, 5, SuDokuCellValue.Five), (1, 8, SuDokuCellValue.One),
  (2, 2, SuDokuCellValue.One), (2, 3, SuDokuCellValue.Eight), (2, 5, SuDokuCellValue.Six), (2, 6, SuDokuCellValue.Four),
  (3, 2, SuDokuCellValue.Eight), (3, 3, SuDokuCellValue.One), (3, 5, SuDokuCellValue.Two), (3, 6, SuDokuCellValue.Nine),
  (4, 0, SuDokuCellValue.Seven), (4, 8, SuDokuCellValue.Eight),
  (5, 2, SuDokuCellValue.Six), (5, 3, SuDokuCellValue.Seven), (5, 5, SuDokuCellValue.Eight), (5, 6, SuDokuCellValue.Two),
  (6, 2, SuDokuCellValue.Two), (6, 3, SuDokuCellValue.Six), (6, 5, SuDokuCellValue.Nine), (6, 6, SuDokuCellValue.Five),
  (7, 0, SuDokuCellValue.Eight), (7, 3, SuDokuCellValue.Two), (7, 5, SuDokuCellValue.Three), (7, 8, SuDokuCellValue.Nine),
  (8, 2, SuDokuCellValue.Five), (8, 4, SuDokuCellValue.One), (8, 5, SuDokuCellValue.Three)
)

val (_, sudoku) = SuDoku.setValues(inputs*).run(initialSudoku)

val (result, sudokuWithSolution) = SuDoku.getSolution.run(sudoku)
// true

// 4;8;3;9;2;1;6;5;7
// 9;6;7;3;4;5;8;2;1
// 2;5;1;8;7;6;4;9;3
// 5;4;8;1;3;2;9;7;6
// 7;2;9;5;6;4;1;3;8
// 1;3;6;7;9;8;2;4;5
// 3;7;2;6;8;9;5;1;4
// 8;1;4;2;5;3;7;6;9
// 6;9;5;4;1;7;3;8;2
```

---

**Ссылки:**

- [Darts - Project Euler](https://projecteuler.net/problem=109)
