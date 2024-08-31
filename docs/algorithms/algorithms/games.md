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

### Ячейка Су Доку

Определим список допустимых значений ячейки игры Су Доку - это цифры от 1 до 9:

```dotty
enum SuDokuCellValue:
  case One, Two, Three, Four, Five, Six, Seven, Eight, Nine
```

Сама ячейка должна удовлетворять следующим условиям:

- У ячейки есть множество допустимых значений
- Точное значение ячейки определено, если из возможных значений остался только один вариант
- В начальном состоянии ячейка может содержать любое из значений от 1 до 9

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

Ячейка должна поддерживать следующие операции:

- В ячейку можно установить определенное значение в качестве единственного, если оно находится среди возможных значений
- Из возможных значений ячейки можно удалить значение, только если это не единственное возможное значение ячейки

Определим новый тип описывающий [функциональное состояние](../../fp/state.md) ячейки `State[SuDokuCell, Boolean]`,
который по заданной ячейке возвращает выходное состояние `(Boolean, SuDokuCell)` - кортеж из двух элементов,
где слева - признак, успешно ли завершилась предыдущая операция,
а справа - последнее стабильное состояние ячейки.

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
          // Устанавливаем значение в ячейке только если 
          // это значение среди допустимых
          case (SuDokuCellOperation.Set, value, true)
              if cell.possibleValues.contains(value) =>
            (true, SuDokuCell(Set(value)))
          // Удаляем значение в ячейке только если 
          // это не единственное значение из допустимых
          case (SuDokuCellOperation.Remove, value, true)
              if cell.possibleValues != Set(value) =>
            (true, SuDokuCell(cell.possibleValues - value))
          case _ => // Операция не может быть выполнена
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

### Поле для игры Су Доку

Определим, что такое поле для игры Су Доку:

- Поле игры Су Доку представляет собой ячейки \\(9 \times 9\\)
- Поле игры Су Доку содержит девять блоков \\(3 \times 3\\)
- Каждая ячейка принадлежит определенной строке, столбцу, блоку

Пронумеруем блоки как показано в таблице:

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

Изначально в каждой ячейке Су Доку список возможных значений равен от 1 до 9.

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

Поле для игры в Су Доку корректное при выполнении следующих условий:

- В каждой строчке нет двух ячеек с одним и тем же известным значением
- В каждом столбце нет двух ячеек с одним и тем же известным значением
- В каждом блоке нет двух ячеек с одним и тем же известным значением

|                  | Тот же столбец |     | Тот же блок |
|------------------|----------------|-----|-------------|
| **Та же строка** | 1?             | 1?  | ...         |
| ...              | 1?             | ... | ...         |
| **Тот же блок**  | ...            | ... | 1?          |

Кроме того, решение игры найдено, если значение каждой ячейки известно.

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

### Команды

Определим общую операцию изменения состояния игры Су Доку:
для любого типа входящих данных `A` и заданной операции `update`
(`update` по значению типа `A` возвращает функцию преобразования из `SuDokuOutput` в `SuDokuOutput`, 
где `SuDokuOutput` - кортеж из двух элементов: результат выполнения предыдущей операции и поле Су Доку)
возвращает функциональное состояние `SuDokuState`:

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

Следующее, что нам понадобится - это установка начального состояния в игре,
когда заполнены часть полей из известных значений.

Поэтому остановимся на операции установки значения в заданное поле {i, j}.
Эта операция состоит из четырех побочных операций:

- установка заданного значения в поле {i, j}
- удаление заданного значения из строчки i за исключением ячейки {i, j}
- удаление заданного значения из столбца j за исключением ячейки {i, j}
- удаление заданного значения из блока \\(3 \times 3\\), которому принадлежит ячейка {i, j}, за исключением самой
  ячейки

#### Элементарные побочные операции

Определим побочные операции установки и удаления значения в заданном поле:

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

  // Определяем текущую ячейку, выполняем операцию, в случае успеха обновляем ячейку
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

Результат выполнения операций установки и удаления значения:

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

#### Многоступенчатые побочные операции

Три более сложные побочные операции на удаление:

- удаление заданного значения из строчки i за исключением ячейки {i, j}
- удаление заданного значения из столбца j за исключением ячейки {i, j}
- удаление заданного значения из квадрата \\(3 \times 3\\), которому принадлежит ячейка {i, j}, 
  за исключением самой ячейки

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

Эти операции можно определить через "простое" удаление, определенное выше:

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

Результат выполнения операций:

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

Также возможны ситуации, когда какое-то число может быть только в определенной строке или
определенном столбце блока, даже если неизвестно, в какой именно ячейке может находиться такое число.

Например, у нас имеется блок:

|       | 1     | 2     | 3 |
|-------|-------|-------|---|
| **1** | (1,2) | (1,3) | 4 |
| **2** | (2,3) | 5     | 6 |
| **3** | 7     | 8     | 9 |

Вне зависимости от того, где будет в итоге расположено число 1 - в ячейке {1,1} или ячейке {1,2} 
точно известно, что оно будет расположено в 1 строке.

Аналогично известно, что число 2 будет точно расположено в 1 столбце.

Это означает, что 1 можно вычеркнуть из первой строки других блоков, а 2 - из первого столбца других блоков.

Например:

|       | 1     | 2     | 3 | 4     | 5     | 6     | 7     | 8     | 9     |
|-------|-------|-------|---|-------|-------|-------|-------|-------|-------|
| **1** | (1,2) | (1,3) | 4 | ~~1~~ | ~~1~~ | ~~1~~ | ~~1~~ | ~~1~~ | ~~1~~ |
| **2** | (2,3) | 5     | 6 |       |       |       |       |       |       |
| **3** | 7     | 8     | 9 |       |       |       |       |       |       |
| **4** | ~~2~~ |       |   |       |       |       |       |       |       |
| **5** | ~~2~~ |       |   |       |       |       |       |       |       |
| **6** | ~~2~~ |       |   |       |       |       |       |       |       |
| **7** | ~~2~~ |       |   |       |       |       |       |       |       |
| **8** | ~~2~~ |       |   |       |       |       |       |       |       |
| **9** | ~~2~~ |       |   |       |       |       |       |       |       |

Добавим эти две операции:

```dotty
enum SuDokuInnerComplexOperation:
  case ..., RemoveFromRowExceptBlock, RemoveFromColumnExceptBlock
  
private val updateSuDoku
    : SuDokuComplexInnerInput => SuDokuOutput => SuDokuOutput =
  (operation, i, j, value) =>
    (result, sudoku) =>
      (operation, result) match
        ...
        case (RemoveFromRowExceptBlock, true) =>
          val block = SuDokuBlock.getBlock(i, j)
          val inputs: List[SuDokuInnerInput] =
            sudoku
              .cells
              .keys
              .withFilter: (r, _, b) =>
                r == i && b != block
              .map: (r, c, _) =>
                (SuDokuInnerOperation.RemoveFromCell, r, c, value)
              .toList
          SuDokuInner.execute(inputs*).run(sudoku)
        case (RemoveFromColumnExceptBlock, true) =>
          val block = SuDokuBlock.getBlock(i, j)
          val inputs: List[SuDokuInnerInput] =
            sudoku
              .cells
              .keys
              .withFilter: (_, c, b) =>
                c == j && b != block
              .map: (r, c, _) =>
                (SuDokuInnerOperation.RemoveFromCell, r, c, value)
              .toList
          SuDokuInner.execute(inputs*).run(sudoku)
        case _ => (false, sudoku)  
```

#### Основные операции

После того как подготовлены все побочные операции, перейдем к основным.

##### Установка единственного возможного значения в ячейку

Напомню, что при установке конкретного значения в заданную ячейку необходимо не только задать значение в эту ячейку, 
а также и удалить значение из списка возможных значений во всех ячейках той же строки, того же столбца и того же блока.
При этом, если в выполняемой пачке операций возникла ситуация, когда одна из операций не выполнилась,
то необходимо вернуться в исходное состояние, бывшее до выполнения всей цепочки операций.

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

def setValue(
    i: SuDokuRow,
    j: SuDokuColumn,
    value: SuDokuCellValue
): SuDokuState =
  setValues((i, j, value))

def setValues(inputs: SuDokuInput*): SuDokuState =
  executeInnerOperation(inputs.toList, updateSuDoku)
  
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

##### Поиск значений, которые можно вычеркнуть

Найдем такие значения, которые можно вычеркнуть из ячейки, т.к. установка значений в поле приводит к ошибке.

В следующем примере в ячейке {1,2} нельзя установить значение 9, т.к. в противном случае это приведет к ошибке,
при попытке вычеркнуть значение 9 из ячейки {3,3}. 
А это значит, что значение 9 можно вычеркнуть из списка возможных для ячейки {1,2}.

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

**Алгоритм:**

- проходимся по каждой строчке и по каждому столбцу
- в каждой ячейке ищем среди списка возможных такие значения,
  которые при вставке в заданное поле приводят к ошибке -
  это означает, что в данную ячейку такое значение вставлять нельзя и его можно вычеркнуть
- вычеркиваем значение
- пытаемся найти следующий кандидат на вычеркивание в той же ячейке
- если в ячейке не осталось значений, которые можно вычеркнуть, то переходим к следующей ячейке

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
      case (None, _, _) => // Дошли до конца поля
        (true, sudoku)
      case (Some(row), None, _) => // Дошли до конца строки
        crossOutLoop(sudoku, SuDokuRow.next(row), Some(SuDokuColumn.First))
      case (Some(row), Some(col), None) => // Некорректная ячейка
        (false, sudoku)
      case (Some(row), Some(col), Some(sudokuCell)) =>
        val currentValues = sudokuCell.possibleValues
        if currentValues.size == 1 then
          // Точное значение определено, переходим к следующей ячейке
          crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
        else
          // Ищем значения в ячейке, которые можно вычеркнуть
          val invalidValues = currentValues.filter: value =>
            !setValue(row, col, value).run(sudoku)._1
          if invalidValues.isEmpty then
            // Таких значений нет, переходим к следующей ячейке
            crossOutLoop(sudoku, Some(row), SuDokuColumn.next(col))
          else
            val input: Seq[SuDokuInnerInput] =
              invalidValues.map: invalidValue =>
                (SuDokuInnerOperation.RemoveFromCell, row, col, invalidValue)
              .toSeq
            val (result, newSudoku) = SuDokuInner.execute(input*).run(sudoku)
            // Если успех, то переходим к следующей ячейке
            if result then
              crossOutLoop(newSudoku, Some(row), SuDokuColumn.next(col))
            // Если нет, значит решения головоломки не существует
            else (false, sudoku)
        end if
    end match
  end crossOutLoop  
```

##### Единственно возможное место для значения в блоке

Находим такое значение в ячейке, которое не встречается в других ячейках блока,
но при этом это значение не единственное возможное в своей ячейке. 
Т.к. такое значение не встречается в других ячейках, то оно может находиться только в заданной ячейке.
Такие значения можно установить единственными в своих ячейках, 
тем самым уменьшив возможные варианты для других значений. 
В каждой ячейке возможно максимум одно такое значение, иначе Су Доку составлено некорректно.

Например, в ячейке {1,1} можно установить единственным значением 2, т.к. это число не встречается в других ячейках.

|       | 1       | 2       | 3       | 4 | 5 | 6 | 7 | 8 | 9 |
|-------|---------|---------|---------|---|---|---|---|---|---|
| **1** | (1,2)   | (1,3,9) | 4       |   |   |   |   |   |   |
| **2** | (1,3,9) | 5       | 6       |   |   |   |   |   |   |
| **3** | 7       | 8       | (1,3,9) |   |   |   |   |   |   |
| **4** |         |         |         |   |   |   |   |   |   |
| **5** |         |         |         |   |   |   |   |   |   |
| **6** |         |         |         |   |   |   |   |   |   |
| **7** |         |         |         |   |   |   |   |   |   |
| **8** |         |         |         |   |   |   |   |   |   |
| **9** |         |         |         |   |   |   |   |   |   |

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
    // Если значение в ячейке уже определено, то переходим к следующей
    if cell.maybeValue.isDefined then acc
    else
      // Если нет - ищем первое значение, которое не встречается в других
      cell
        .possibleValues
        .collectFirst:
          case value if notInOtherCells(index, value) =>
            (index._1, index._2, value) :: acc
        .getOrElse(acc)
  }
end getSingleValues

@tailrec
def blockSingleValueLoop(
    sudoku: SuDoku,
    blocks: List[SuDokuBlock]
): SuDokuOutput =
  blocks match
    case Nil => (true, sudoku) // Обошли все блоки
    case head :: tail =>
      val singleValues = getSingleValues(sudoku, head)
      if singleValues.isEmpty then
        // Если значений-одиночек нет, переходим к следующим блокам
        blockSingleValueLoop(sudoku, tail)
      else
        // Если есть, устанавливаем такие значения
        val (result, sudoku2) = setValues(singleValues*).run(sudoku)
        // Если установка закончилась успешно, переходим к следующему блоку
        if result then blockSingleValueLoop(sudoku2, tail)
        else (false, sudoku)
```

##### Удаления значения из строки или столбца других блоков

Вернемся к ситуации, когда какое-то число может быть только в определенной строке или определенном столбце блока.

Например, у нас имеется блок:

|       | 1     | 2     | 3 |
|-------|-------|-------|---|
| **1** | (1,2) | (1,3) | 4 |
| **2** | (2,3) | 5     | 6 |
| **3** | 7     | 8     | 9 |

Вне зависимости от того, где будет в итоге расположено число 1 - в ячейке {1,1} или ячейке {1,2}
точно известно, что оно будет расположено в 1 строке.
Аналогично известно, что число 2 будет точно расположено в 1 столбце.

Это означает, что 1 можно вычеркнуть из первой строки других блоков, а 2 - из первого столбца других блоков.

```dotty
def getRowColValues(
    sudoku: SuDoku,
    block: SuDokuBlock
): List[SuDokuComplexInnerInput] =
  val blockCells = sudoku.cells.filter { case ((_, _, blk), cell) =>
    blk == block
  }

  SuDokuCellValue.values.foldLeft(List.empty[SuDokuComplexInnerInput]) {
    case (inputs, value) =>
      val cells =
        blockCells.filter(_._2.possibleValues.contains(value)).keySet
      val rows = cells.map(_._1)
      val cols = cells.map(_._2)

      if rows.size == 1 && cols.size > 1 then
        // Данное значение встречается только в одной строке
        (
          SuDokuInnerComplexOperation.RemoveFromRowExceptBlock,
          rows.head,
          cols.head,
          value
        ) :: inputs
      else if cols.size == 1 && rows.size > 1 then
        // Данное значение встречается только в одном столбце
        (
          SuDokuInnerComplexOperation.RemoveFromColumnExceptBlock,
          rows.head,
          cols.head,
          value
        ) :: inputs
      else inputs
  }
end getRowColValues

@tailrec
def blockRowColValuesLoop(
    sudoku: SuDoku,
    blocks: List[SuDokuBlock]
): SuDokuOutput =
  blocks match
    case Nil => (true, sudoku) // Обошли все блоки
    case head :: tail =>
      val inputs = getRowColValues(sudoku, head)
      if inputs.isEmpty then
        // Если удалять нечего, переходим к следующим блокам
        blockRowColValuesLoop(sudoku, tail)
      else
        // Если есть, выполняем операции
        val (result, sudoku2) = SuDokuComplexInner.execute(inputs).run(sudoku)
        // Если успех, переходим к следующему блоку
        if result then blockSingleValueLoop(sudoku2, tail)
        else (false, sudoku)
```

#### Поиск решения головоломки

Головоломка решается до тех пор, пока:

- не найдено решение
- если есть значения, которые можно вычеркнуть
- если есть значения, которые в заданном блоке могут находиться только в определенных ячейках
- Если есть значения, которые могут находиться только в определенной строке или столбце блока

Определим изменения состояния Су Доку на основе всех перечисленных операций:

```dotty
private val blockSingleValueState = State[SuDoku, Boolean](
  blockSingleValueLoop(_, SuDokuBlock.values.toList)
)
private val crossOutState = State[SuDoku, Boolean](
  crossOutLoop(_, Some(SuDokuRow.First), Some(SuDokuColumn.First))
)
private val blockRowColValuesState = State[SuDoku, Boolean](
  blockRowColValuesLoop(_, SuDokuBlock.values.toList)
)
private val loopState: State[SuDoku, Boolean] =
  blockSingleValueState.map2(crossOutState)(_ && _)
    .map2(blockRowColValuesState)(_ && _)
```

Рекурсивно запускаем решение головоломки до тех пор, пока происходит изменение поля Су Доку.
Как только ни одна из операций не смогла внести новые изменения в поле, возвращаем результат.

```dotty
@tailrec
private def solutionLoop(sudoku: SuDoku): SuDokuOutput =
  val (result, newSudoku) = loopState.run(sudoku)
  if !result then (false, sudoku)
  else if sudoku != newSudoku then solutionLoop(newSudoku)
  else (result, sudoku)

val solutionState: SuDokuState = State[SuDoku, Boolean](solutionLoop)
```

В результате получим:

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

---

**Ссылки:**

- [Darts - Project Euler](https://projecteuler.net/problem=109)
