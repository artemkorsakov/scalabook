# Задачи №81-№100

## Задача №81

[Задача №81 - Path Sum: Two Ways](https://projecteuler.net/problem=81)

> В представленной ниже матрице 5 на 5 путь с минимальной суммой при движении из верхнего левого угла в нижний правый 
> (шагами только **либо направо, либо вниз**) выделен красным жирным шрифтом. Его сумма равна 2427.
>
> ![](https://latex.codecogs.com/svg.image?%5Cbegin%7Bpmatrix%7D%5Ccolor%7Bred%7D%7B131%7D&673&234&103&18%5C%5C%5Ccolor%7Bred%7D%7B201%7D&%5Ccolor%7Bred%7D%7B96%7D&%5Ccolor%7Bred%7D%7B342%7D&965&150%5C%5C630&803&%5Ccolor%7Bred%7D%7B746%7D&%5Ccolor%7Bred%7D%7B422%7D&111%5C%5C537&699&497&%5Ccolor%7Bred%7D%7B121%7D&956%5C%5C805&732&524&%5Ccolor%7Bred%7D%7B37%7D&%5Ccolor%7Bred%7D%7B331%7D%5Cend%7Bpmatrix%7D)
> 
> Найдите сумму наименьшего пути, взяв матрицу 80 на 80 из текстового файла matrix.txt размером 31KБ, 
> двигаясь шагами (либо направо, либо вниз) из верхнего левого угла в нижний правый.

**Алгоритм:**

Если разрешено двигаться только вниз и вправо, то минимальный путь из заданной ячейки `(i, j)` равен 
сумме значения этой ячейки и минимального из минимальных путей ячейки строчкой ниже `(i + 1, j)` или 
ячейки столбца справа `(i, j + 1)`.

Начнем двигаться с последней строки последнего столбца и будем переходить к предыдущему столбцу той же строки до тех пор,
пока не достигнем первого столбца. Тогда перейдем на предыдущую строчку в последний столбец.
И так до тех пор, пока не достигнем ячейки в первом столбце первой строки.

При этом к значению каждой ячейки будем прибавлять минимальное значение из ячеек снизу и справа (если таковы имеются).
Тогда в каждый момент времени в значение ячейки будет вставляться значение её минимального пути,
т.к. в ячейках снизу и справа уже будет стоять их минимальный путь (мы прошли через них ранее).
В конечном итоге в ячейке первого столбца первой строки будет вычислен её минимальный путь. 

**Код:**

```dotty
def getMinSumRightBottom(matrix: Array[Array[Long]]): Long =
  val n = matrix.length
  val m = matrix(0).length
  (m - 2 to 0 by -1).foreach: j =>
    matrix(n - 1)(j) = matrix(n - 1)(j) + matrix(n - 1)(j + 1)
  (n - 2 to 0 by -1).foreach: i =>
    matrix(i)(m - 1) = matrix(i)(m - 1) + matrix(i + 1)(m - 1)

  (n - 2 to 0 by -1).foreach: i =>
    (m - 2 to 0 by -1).foreach: j =>
      matrix(i)(j) =
        matrix(i)(j) + math.min(matrix(i + 1)(j), matrix(i)(j + 1))

  matrix(0)(0)

val matrix: Array[Array[Long]] =
  Array(
    Array(131L, 673L, 234L, 103L, 18L),
    Array(201L, 96L, 342L, 965L, 150L),
    Array(630L, 803L, 746L, 422L, 111L),
    Array(537L, 699L, 497L, 121L, 956L),
    Array(805L, 732L, 524L, 37L, 331L)
  )
getMinSumRightBottom(matrix) // 2427
```

## Задача №82

[Задача №82 - Path Sum: Three Ways](https://projecteuler.net/problem=82)

> > Примечание: Данная задача является более сложной версией 81-й задачи.
> 
> В представленной ниже матрице 5 на 5 путь от любого элемента левого столбца до любого элемента правого столбца, 
> при передвижении шагами вверх, вниз и вправо, с минимальной суммой выделен красным жирным шрифтом. Его сумма равна 994.
> 
> ![](https://latex.codecogs.com/svg.image?%5Cbegin%7Bpmatrix%7D131&673&%5Ccolor%7Bred%7D%7B234%7D&%5Ccolor%7Bred%7D%7B103%7D&%5Ccolor%7Bred%7D%7B18%7D%5C%5C%5Ccolor%7Bred%7D%7B201%7D&%5Ccolor%7Bred%7D%7B96%7D&%5Ccolor%7Bred%7D%7B342%7D&965&150%5C%5C630&803&746&422&111%5C%5C537&699&497&121&956%5C%5C805&732&524&37&331%5Cend%7Bpmatrix%7D)
> 
> Найдите сумму наименьшего пути, взяв матрицу 80 на 80 из текстового файла matrix.txt размером 31KБ, 
> двигаясь шагами (вверх, вниз, вправо) от левого столбца к правому столбцу.

**Алгоритм:**

- в последнем столбце уже расположены минимальные суммы пути, 
  т.к. из ячеек последнего столбца движения возможны только вверх-вниз,
  что приводит исключительно к увеличению суммы
- пойдем по каждому столбцу `j`, начиная с предпоследнего и до первого
- пойдем по каждой ячейке столбца `j` по каждой строке `i`, начиная с первой строчки и до последней
- минимальное значение пути в ячейке `(i, j)` равно минимальному из трех значений:
    - минимальное значение пути в ячейке выше `(i - 1, j)` (мы его посчитали ранее исходя из порядка обхода)
    - минимальное значение пути в ячейке справа `(i, j + 1)` (мы его посчитали ранее исходя из порядка обхода)
    - `getSumFromGivenRow(i: Int, j: Int)` - минимальному значению из всех путей, начинающихся с `(i, j)`,
      спускающихся до определенной строчки `ii` и сворачивающих вправо
    - вычисление `getSumFromGivenRow(i: Int, j: Int)` происходит путем обхода всех строчек `ii` ниже `i`. 
      Для каждой `ii` вычисляется сумма значений ячеек текущего столбца от `i` до `ii` так, как будто 
      мы начали с `(i, j)`, а затем спустились до `(ii, j)`. К полученной сумме прибавляется значение ячейки `(ii, j + 1)`, 
      как будто в этом месте свернули вправо. В ячейке `(ii, j + 1)` уже хранится минимальное значение пути,
      т.к. предыдущий столбец был пройден ранее исходя из порядка обхода

**Код:**

```dotty
def getMinThreeWaysSum(matrix: Array[Array[Long]]): Long =
  val n = matrix.length
  val m = matrix(0).length

  def getSumFromGivenRow(i: Int, j: Int): Long =
    (i + 1 until n).map: ii =>
      matrix(ii)(j + 1) + (i + 1 to ii).map(iii => matrix(iii)(j)).sum
    .minOption.getOrElse(Long.MaxValue)

  (m - 2 to 0 by -1).foreach: j =>
    matrix(0)(j) =
      matrix(0)(j) + math.min(matrix(0)(j + 1), getSumFromGivenRow(0, j))

    (1 until n).foreach: i =>
      matrix(i)(j) = matrix(i)(j) +
        Seq(matrix(i - 1)(j), matrix(i)(j + 1), getSumFromGivenRow(i, j)).min

  matrix.map(_(0)).min
end getMinThreeWaysSum

val matrix: Array[Array[Long]] =
  Array(
    Array(131L, 673L, 234L, 103L, 18L),
    Array(201L, 96L, 342L, 965L, 150L),
    Array(630L, 803L, 746L, 422L, 111L),
    Array(537L, 699L, 497L, 121L, 956L),
    Array(805L, 732L, 524L, 37L, 331L)
  )
getMinThreeWaysSum(matrix) // 994
```

## Задача №83

[Задача №83 - Path Sum: Four Ways](https://projecteuler.net/problem=83)

> > Примечание: Данная задача является намного более сложной версией 81-й задачи.
> 
> В представленной ниже матрице 5 на 5 путь с минимальной суммой из верхнего левого угла в нижний правый, 
> при передвижении шагами вверх, вниз, вправо или влево, выделен красным жирным шрифтом. Его сумма равна 2297.
> 
> ![](https://latex.codecogs.com/svg.image?%5Cbegin%7Bpmatrix%7D%5Ccolor%7Bred%7D%7B131%7D&673&%5Ccolor%7Bred%7D%7B234%7D&%5Ccolor%7Bred%7D%7B103%7D&%5Ccolor%7Bred%7D%7B18%7D%5C%5C%5Ccolor%7Bred%7D%7B201%7D&%5Ccolor%7Bred%7D%7B96%7D&%5Ccolor%7Bred%7D%7B342%7D&965&%5Ccolor%7Bred%7D%7B150%7D%5C%5C630&803&746&%5Ccolor%7Bred%7D%7B422%7D&%5Ccolor%7Bred%7D%7B111%7D%5C%5C537&699&497&%5Ccolor%7Bred%7D%7B121%7D&956%5C%5C805&732&524&%5Ccolor%7Bred%7D%7B37%7D&%5Ccolor%7Bred%7D%7B331%7D%5Cend%7Bpmatrix%7D)
> 
> Найдите сумму наименьшего пути, взяв матрицу 80 на 80 из текстового файла matrix.txt размером 31KБ, 
> передвигаясь шагами в любых направлениях (вверх, вниз, вправо, влево) из верхнего левого угла в нижний правый.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №84

[Задача №84 - Monopoly Odds](https://projecteuler.net/problem=84)

> Стандартная доска игры **Монополия** выглядит следующим образом:
> 
> ![Monopoly](https://projecteuler.net/resources/images/0084_monopoly_board.png?1678992052)
>
> Игрок начинает на клетке GO и складывает выпавшие на двух шестигранных кубиках числа, 
> чтобы определить количество клеток, которое он должен пройти по часовой стрелке. 
> Пользуясь только этим правилом, вероятность посещения каждой клетки равна 2,5 %. 
> Однако попадание на G2J (Отправляйтесь в тюрьму), CC (Общественный фонд) и CH (Шанс) меняет распределение.
> 
> В дополнение к G2J и одной карте в CC и в CH, которые приказывают игроку отправиться в тюрьму, 
> также если игрок выкидывает три дубля подряд, он не двигается в свой третий ход, а отправляется сразу в тюрьму.
> 
> В начале игры карты CC и CH перемешиваются. Когда игрок попадает на клетку CC или CH, 
> он берет верхнюю карту с соответствующей колоды и, после выполнения указанных на ней инструкций, 
> возвращает ее в низ колоды. В каждой колоде 16 карт, однако для решения этой задачи важны только карты, 
> связанные с перемещением игрока. Любые другие инструкции игнорируются и игрок остается на той же клетке.
> 
> - Общественный фонд (2/16 карт):
>     - Пройдите к GO
>     - Идите в JAIL
> - Chance (10/16 cards):
>     - Пройдите к GO
>     - Идите в JAIL
>     - Идите на C1
>     - Идите на E3
>     - Идите на H2
>     - Идите на R1
>     - Пройдите к следующей R (железнодорожная станция)
>     - Пройдите к следующей R
>     - Пройдите к следующей U (коммунальное предприятие)
>     - Вернитесь назад на 3 клетки
>
> Суть этой задачи заключается в вероятности посещения одной конкретной клетки. 
> То есть, вероятность оказаться на этой клетке по завершении хода. 
> Поэтому ясно, что за исключением G2J, чья вероятность посещения равна нулю, 
> клетка CH будет иметь наименьшую вероятность посещения, потому что в 5/8 случаев игроку придется переместиться 
> на другую клетку, а нас интересует именно клетка, на которой завершится ход игрока. 
> Мы также не будем разделять попадание в тюрьму как посетитель или как заключенный, 
> также не берем во внимание правило, что выкинув дубль, игрок выходит из тюрьмы - 
> предположим, что игроки платят за выход из тюрьмы на следующий же ход после попадания в нее.
> 
> Начиная с GO и последовательно нумеруя клетки от 00 до 39, мы можем соединить эти двузначные числа, 
> чтобы получить соответствующую определенному множеству клеток строку.
> 
> Статистически можно показать, что три наиболее популярных клетки будут 
> JAIL (6,24%) = Клетка 10, E3 (3,18%) = Клетка 24, и GO (3,09%) = Клетка 00. 
> Итак, их можно перечислить как строку из шести цифр: 102400.
> 
> Найдите такую шестизначную строку, если игроки будут использовать вместо двух шестигранных кубиков два четырехгранных.

**Алгоритм:**

Для начала определим поля Монополии, карточки "Общественная казна" и "Шанс".

**Код:**

```dotty
trait CC
trait CH

enum MonopolyField(val i: Byte):
  case GO   extends MonopolyField(0)
  case A1   extends MonopolyField(1)
  case CC1  extends MonopolyField(2), CC
  case A2   extends MonopolyField(3)
  case T1   extends MonopolyField(4)
  case R1   extends MonopolyField(5)
  case B1   extends MonopolyField(6)
  case CH1  extends MonopolyField(7), CH
  case B2   extends MonopolyField(8)
  case B3   extends MonopolyField(9)
  case JAIL extends MonopolyField(10)
  case C1   extends MonopolyField(11)
  case U1   extends MonopolyField(12)
  case C2   extends MonopolyField(13)
  case C3   extends MonopolyField(14)
  case R2   extends MonopolyField(15)
  case D1   extends MonopolyField(16)
  case CC2  extends MonopolyField(17), CC
  case D2   extends MonopolyField(18)
  case D3   extends MonopolyField(19)
  case FP   extends MonopolyField(20)
  case E1   extends MonopolyField(21)
  case CH2  extends MonopolyField(22), CH
  case E2   extends MonopolyField(23)
  case E3   extends MonopolyField(24)
  case R3   extends MonopolyField(25)
  case F1   extends MonopolyField(26)
  case F2   extends MonopolyField(27)
  case U2   extends MonopolyField(28)
  case F3   extends MonopolyField(29)
  case G2J  extends MonopolyField(30)
  case G1   extends MonopolyField(31)
  case G2   extends MonopolyField(32)
  case CC3  extends MonopolyField(33), CC
  case G3   extends MonopolyField(34)
  case R4   extends MonopolyField(35)
  case CH3  extends MonopolyField(36), CH
  case H1   extends MonopolyField(37)
  case T2   extends MonopolyField(38)
  case H2   extends MonopolyField(39)
end MonopolyField

enum CommunityChest:
  case AdvanceToGO, GoToJAIL, Stay0, Stay1, Stay2, Stay3, Stay4, Stay5, Stay6,
    Stay7, Stay8, Stay9, Stay10, Stay11, Stay12, Stay13

enum Chance:
  case AdvanceToGO, GoToJAIL, GoToC1, GoToE3, GoToH2, GoToR1, GoToNextR1,
    GoToNextR2, GoToNextU, GoBack3Squares, Stay0, Stay1, Stay2, Stay3, Stay4,
    Stay5
```

Определим класс для текущего набора карточек "Общественная казна" и "Шанс", 
а также методы переноса верхней карты вниз колоды.
Метод `initialCards` перемешивает карточки в начале игры.

**Код:**

```dotty
final case class Cards(
    cardsCC: Vector[CommunityChest],
    cardsCH: Vector[Chance]
):
  def shiftCC: Cards =
    Cards(cardsCC.drop(1) ++ cardsCC.headOption, cardsCH)

  def shiftCH: Cards =
    Cards(cardsCC, cardsCH.drop(1) ++ cardsCH.headOption)

def initialCards: Cards = Cards(
  Random.shuffle(CommunityChest.values.toVector),
  Random.shuffle(Chance.values.toVector)
)
```

Определим в качестве текущего статуса определенный набор карт в данный момент, а также количество выброшенных дублей.

**Код:**

```dotty
type MonopolyStatus = (Cards, Byte)
type MonopolyState  = MonopolyStatus => (MonopolyField, MonopolyStatus)
```

Если игрок встал на поле "Общественная казна", то переход к следующему полю происходит согласно правилам:

> Когда игрок попадает на клетку CC или CH, он берет верхнюю карту с соответствующей колоды и, после
> выполнения указанных на ней инструкций, возвращает ее в низ колоды. 
> В каждой колоде 16 карт, однако для решения этой задачи важны только карты, связанные с перемещением игрока. 
> Любые другие инструкции игнорируются и игрок остается на той же клетке.
> 
> Общественный фонд (2/16 карт):
> 
> - Пройдите к GO
> - Идите в JAIL

**Код:**

```dotty
def goFromCC(field: CC & MonopolyField): MonopolyState = (cards, doubles) =>
  cards.cardsCC.headOption match
    case Some(CommunityChest.AdvanceToGO) =>
      (MonopolyField.GO, (cards.shiftCC, doubles))
    case Some(CommunityChest.GoToJAIL) =>
      (MonopolyField.JAIL, (cards.shiftCC, doubles))
    case _ => (field, (cards.shiftCC, doubles))
```

Если игрок встал на поле "Шанс", то переход к следующему полю происходит согласно правилам:

> Когда игрок попадает на клетку CC или CH, он берет верхнюю карту с соответствующей колоды и, после
> выполнения указанных на ней инструкций, возвращает ее в низ колоды. 
> В каждой колоде 16 карт, однако для решения этой задачи важны только карты, связанные с перемещением игрока. 
> Любые другие инструкции игнорируются и игрок остается на той же клетке.
> 
> Chance (10/16 cards):
> 
> - Пройдите к GO
> - Идите в JAIL
> - Идите на C1
> - Идите на E3
> - Идите на H2
> - Идите на R1
> - Пройдите к следующей R (железнодорожная станция)
> - Пройдите к следующей R
> - Пройдите к следующей U (коммунальное предприятие)
> - Вернитесь назад на 3 клетки

```dotty
def goFromCH(field: CH & MonopolyField): MonopolyState = (cards, doubles) =>
  cards.cardsCH.headOption match
    case Some(Chance.AdvanceToGO) =>
      (MonopolyField.GO, (cards.shiftCH, doubles))
    case Some(Chance.GoToJAIL) =>
      (MonopolyField.JAIL, (cards.shiftCH, doubles))
    case Some(Chance.GoToC1) => (MonopolyField.C1, (cards.shiftCH, doubles))
    case Some(Chance.GoToE3) => (MonopolyField.E3, (cards.shiftCH, doubles))
    case Some(Chance.GoToH2) => (MonopolyField.H2, (cards.shiftCH, doubles))
    case Some(Chance.GoToR1) => (MonopolyField.R1, (cards.shiftCH, doubles))
    case Some(Chance.GoToNextR1) | Some(Chance.GoToNextR2) =>
      field match
        case MonopolyField.CH1 =>
          (MonopolyField.R2, (cards.shiftCH, doubles))
        case MonopolyField.CH2 =>
          (MonopolyField.R3, (cards.shiftCH, doubles))
        case MonopolyField.CH3 =>
          (MonopolyField.R1, (cards.shiftCH, doubles))
    case Some(Chance.GoToNextU) =>
      field match
        case MonopolyField.CH1 =>
          (MonopolyField.U1, (cards.shiftCH, doubles))
        case MonopolyField.CH2 =>
          (MonopolyField.U2, (cards.shiftCH, doubles))
        case MonopolyField.CH3 =>
          (MonopolyField.U1, (cards.shiftCH, doubles))
    case Some(Chance.GoBack3Squares) =>
      field match
        case MonopolyField.CH1 =>
          (MonopolyField.T1, (cards.shiftCH, doubles))
        case MonopolyField.CH2 =>
          (MonopolyField.D3, (cards.shiftCH, doubles))
        case MonopolyField.CH3 =>
          goFromCC(MonopolyField.CC3)(cards.shiftCH, doubles)
    case _ => (field, (cards.shiftCH, doubles))
```

Переход к следующей клетке из текущей:

> В дополнение к G2J и одной карте в CC и в CH, которые приказывают игроку отправиться в тюрьму, 
> также если игрок выкидывает три дубля подряд, он не двигается в свой третий ход, а отправляется сразу в тюрьму.

```dotty
def goToNextField(
    field: MonopolyField,
    dice1: Byte,
    dice2: Byte
): MonopolyState = (cards, doubles) =>
  val newDoubles = if dice1 == dice2 then (doubles + 1).toByte else 0
  if newDoubles == 3 then (MonopolyField.JAIL, (cards, 0))
  else
    val newFieldOrdinal = (field.i + dice1 + dice2) % 40
    MonopolyField.fromOrdinal(newFieldOrdinal) match
      case MonopolyField.G2J => (MonopolyField.JAIL, (cards, newDoubles))
      case cc: (CC & MonopolyField) => goFromCC(cc)(cards, newDoubles)
      case ch: (CH & MonopolyField) => goFromCH(ch)(cards, newDoubles)
      case newField                 => (newField, (cards, newDoubles))
```

Получение итоговой статистики:

- задаем число граней на кубике и количество "испытаний"
- начинаем с поля `GO`, с перемешанными картами "Общественная казна" и "Шанс", а также нулевым числом дублей
- используя итеративную функцию, принимающую текущее поле, статус, собранную статистику и номер итерации,
  проходимся по текущей итерации:
    - если мы достигли заданного числа испытаний, то возвращаем собранную статистику
    - "бросаем кубики"
    - используя `goToNextField` вычисляем следующее поле и следующий статус
    - рекурсивно вызываем функцию для новых поля и статуса, обновленной статистики и числа итераций

```dotty
def getFieldStatistics(maxDiceValue: Byte,count: Int): Vector[(MonopolyField, Int)] =
  val initialField                          = MonopolyField.GO
  val initialMonopolyStatus: MonopolyStatus = (initialCards, 0)

  @tailrec
  def loop(
      field: MonopolyField,
      status: MonopolyStatus,
      statistics: Map[MonopolyField, Int],
      counter: Int
  ): Map[MonopolyField, Int] =
    if counter >= count then statistics
    else
      val dice1                 = (Random.nextInt(maxDiceValue) + 1).toByte
      val dice2                 = (Random.nextInt(maxDiceValue) + 1).toByte
      val (newField, newStatus) = goToNextField(field, dice1, dice2)(status)
      loop(
        newField,
        newStatus,
        statistics + (newField -> (statistics.getOrElse(newField, 0) + 1)),
        counter + 1
      )

  loop(initialField, initialMonopolyStatus, Map.empty, 0)
    .toVector.sortBy(-_._2)
```

Итого, для шестигранного кубика и миллиона испытаний получим следующий топ-3:

```dotty
val stats1 = getFieldStatistics(6, 1000000).take(3)
// Vector((JAIL,62355), (E3,31815), (GO,31010))
```

## Задача №85

[Задача №85 - Counting Rectangles](https://projecteuler.net/problem=85)

> Внимательно сосчитав, можно понять, что в прямоугольной сетке 3 на 2 содержится восемнадцать прямоугольников:
>
> ![](https://projecteuler.net/project/images/p085.png)
> 
> Несмотря на то, что не существует такой прямоугольной сетки, которая содержит ровно два миллиона прямоугольников, 
> найдите площадь сетки с ближайшим количеством прямоугольников.

**Алгоритм:**

##### Количество прямоугольников в сетке n*m

Обозначим \\(S\_{n, m}\\) как количество прямоугольников в сетке \\(n \times m\\).
Заметим, что количество прямоугольников в сетке \\(n \times m\\) равно:

- количеству прямоугольников, "не залезающих" на последнюю строку, 
  т.е. количеству прямоугольников в сетке \\(n \times (m - 1)\\)
- **плюс** количеству прямоугольников, "входящих" только в последнюю строку, 
  т.е. количеству прямоугольников в сетке \\(n \times 1\\)
- **плюс** количеству прямоугольников, "пересекающих" строки n - 1 и n.
    - последнее количество равно количеству прямоугольников в сетке \\(n \times (m - 1)\\),
      у которых есть хотя бы одна клетка из последней строки (в этом случае мы просто "расширим" такие прямоугольники
      на одну клетку ниже и получим все прямоугольники, "пересекающие" строки n - 1 и n)
    - это количество равно количеству всех прямоугольников в сетке \\(n \times (m - 1)\\) 
      **минус** количество прямоугольников в сетке \\(n \times (m - 2)\\) - тех, которые "не заходят" в ряд m - 1

Итого, получается формула \\(S\_{n, m} = S\_{n, m - 1} + S\_{n, 1} + S\_{n, m - 1} - S\_{n, m - 2}\\).

Т.к. \\(S\_{n, m} - S\_{n, m - 1} = S\_{n, m - 1} - S\_{n, m - 2} + S\_{n, 1}\\), то мы можем продолжить "уменьшать" m
и получим, что \\(S\_{n, m} = S\_{n, m - 1} + S\_{n, 2} - S\_{n, 1} + (m - 2) \times S\_{n, 1}\\),
а т.к. по полученной формуле \\(S\_{n, 2} = S\_{n, 1} + S\_{n, 1} + S\_{n, 1} - 0\\),
то выходит, что \\(S\_{n, m} = S\_{n, m - 1} + m \times S\_{n, 1}\\).

Дальше уменьшаем m: \\(S\_{n, m} = S\_{n, m - 1} + m \times S\_{n, 1} = S\_{n, m - 2} + (m + (m - 1)) \times S\_{n, 1} = ... = (m + (m - 1) + ... + 1) \times S\_{n, 1} = \frac{m(m + 1)}{2} \times S\_{n, 1}\\).

Очевидно, что \\(S\_{n, m} = S\_{m, n}\\), т.к. количество прямоугольников в сетке не изменится, если поменять местами строки и столбцы.
А это означает, что \\(S\_{n, 1} = S\_{1, n} = \frac{n(n + 1)}{2} \times S\_{1, 1}\\), где \\(S\_{1, 1} = 1\\) - 
мы можем расположить только 1 прямоугольник в сетке \\(1 \times 1\\).

Итого \\(S\_{n, m} = \frac{m(m + 1)}{2} \times \frac{n(n + 1)}{2}\\).

**Код:**

```dotty
def getRectanglesCount(n: Long, m: Long): Long =
  n * (n + 1) * m * (m + 1) / 4
```

##### Ближайшее к 2 миллионам количество прямоугольников в сетке n*m

Допустим, мы знаем `n` (число строк) в итоговом ответе. Какое должно быть `m` (число столбцов) так,
чтобы количество прямоугольников было ближайшим к двум миллионам?

\\(\frac{m(m + 1)}{2} \times \frac{n(n + 1)}{2} \approx 2000000 \Rightarrow m^2 + m - \frac{8000000}{n(n + 1)} \approx 0 \Rightarrow m \approx \frac{-1 \pm \sqrt{1 + \frac{32000000}{n(n + 1)}}}{2}\\).

Очевидно, что `m` положительное, поэтому `m` - либо целая часть от положительного корня, 
либо целая часть от положительного корня + 1.
У любых других значений `m` разница между количеством прямоугольников в сетке и двумя миллионами
будет больше, чем у указанных двух.

**Код:**

```dotty
def getM(n: Long): IndexedSeq[(Long, Long)] =
  val root = ((-1 + math.sqrt(1 + 32_000_000.0 / (n * (n + 1)))) / 2).toLong
  IndexedSeq((n, root), (n, root + 1))
```

Учитывая, что \\(S\_{n, m} = S\_{m, n}\\), можно предположить, что \\(n \leq m\\).
Поэтому максимально возможное значение n, которое необходимо разобрать составляет:

\\((\frac{n(n + 1)}{2})^2 \approx 2000000 \Rightarrow n^2 + n - 2000 \times \sqrt{2} \approx 0 \Rightarrow n \approx \frac{-1 \pm \sqrt{ 1 + 8000 \times \sqrt{2}}}{2}\\).

Итого:

- для всех n от 1 до вышеописанного лимита
- вычисляем два "подходящих" значения m
- среди полученных пар вычисляем ту, у которой количество прямоугольников в сетке наиболее близко к двум миллионам

**Код:**

```dotty
def difference(nm: (Long, Long)): Long =
  math.abs(getRectanglesCount(nm._1, nm._2) - 2_000_000)

val limit  = ((math.sqrt(1 + 8000 * math.sqrt(2)) - 1) / 2).toInt + 1
val pairs  = (1 to limit).flatMap(n => getM(n))
val (n, m) = pairs.minBy(difference)
```

## Задача №86

[Задача №86 - Cuboid Route](https://projecteuler.net/problem=86)

> Паук S сидит в одном углу комнаты в форме прямоугольного параллелепипеда размерами 6 на 5 на 3, 
> а муха F сидит в противоположном углу. Путешествуя по поверхностям комнаты, 
> кратчайший путь "по прямой" от S до F имеет длину 10 и показан на рисунке ниже.
>
> ![](https://projecteuler.net/project/images/p086.png)
> 
> Однако, в любом прямоугольном параллелепипеде существует до трех кандидатов на "кратчайший" путь, 
> и кратчайший путь не всегда имеет целую длину.
>
> Рассматривая все комнаты в форме прямоугольного параллелепипеда с максимальными размерами M на M на M, 
> существует ровно 2060 прямоугольных параллелепипедов, для которых кратчайшее расстояние - целое число, 
> при M = 100, и это - наименьшее значение M, при котором количество решений превышает две тысячи: 
> при M = 99 количество решений равно 1975.
>
> Найдите наименьшее значение M, при котором количество решений превышает один миллион.

**Алгоритм:**

Обозначим длины сторон параллелепипеда от меньшей к большей за x, y, z. Тогда \\(1 \le x \le y \le z \le M\\).
Если отобразить параллелепипед на плоскость так, чтобы меньшая сторона "продолжила" среднюю, то получиться
прямоугольник со сторонами (x + y, z). 
Тогда длина кратчайшего пути исходного параллелепипеда равна \\(\sqrt{(x + y)^2 + z^2}\\).
Это [Пифагорова тройка](../../algorithms/algorithms/fibonacci.md).
Обозначим эту тройку как (a, b, c).

Тогда количество решений для заданной Пифагоровой тройки будет равно количеству допустимых x таких, 
чтобы выполнялись следующий условия:

- \\(1 \le x \le y \le z \le limit\\)
- \\(x + y = a\\)
- \\(z = b\\)
- Следовательно:
    - \\(a \le 2 \times limit\\)
    - \\(b \le limit\\)
    - \\(1 \le x \le a - x \le b \Rightarrow max(1, a - b) \le x \le \frac{a}{2} \\)

Получаем, что количество решений для заданных a, b и M равно \\(max(\frac{a}{2} - max(a - b, 1) + 1, 0)\\)

**Код:**

```dotty
def getCountForLegs(a: Long, b: Long, limit: Long): Long =
  if a > 2 * limit || b > limit then 0
  else math.max((a / 2) - math.max(1, a - b) + 1, 0)
```

Для заданной примитивной Пифагоровой тройки (a, b, c) будем строить все Пифагоровы тройки с кратными катетами (k\*a, k\*b, k\*c) 
до тех пор, пока количество решений для таких троек больше нуля.
Очевидно, что если для \\(k\_{1}\\) количество решений равно нулю, то для всех больших \\(k\_{2}\\) 
количество решений также будет равняться нулю, т.к. катеты вышли за пределы лимита. 

К тому же сумма меньших сторон исходного параллелепипеда (x + y) может равняться 
либо одному катету Пифагоровой тройки (a), либо другому (b).

**Код:**

```dotty
def getCountForPythagoreanTriplet(
    triplet: PythagoreanTriplet,
    limit: Int
): Long =
  getCountForSimilarLegs(triplet.a, triplet.b, limit) + getCountForSimilarLegs(triplet.b, triplet.a, limit)

def getCountForSimilarLegs(a: Long, b: Long, limit: Long): Long =
  @tailrec
  def loop(aa: Long, bb: Long, acc: Long): Long =
    val count = getCountForLegs(aa, bb, limit)
    if count == 0 then acc
    else loop(aa + a, bb + b, acc + count)

  loop(a, b, 0)
```

[Примитивные Пифагоровы тройки генерятся согласно заданному алгоритму](../../algorithms/algorithms/fibonacci.md).

Пройдемся по всем текущим пифагоровым тройкам, начиная с исходной (3, 4, 5) и если среди текущих есть те, 
что генерят положительную сумму, то оставляем их для подсчета следующих троек.
Если нет, то заканчиваем подсчет.

**Код:**

```dotty
private val primitivePythagoreanTriplet: PythagoreanTriplet =
  PythagoreanTriplet(3, 4, 5)

def getCountForM(limit: Int): Long =
  @tailrec
  def loop(triplets: List[PythagoreanTriplet], acc: Long): Long =
    val (validTriplets, sum) = getCountForPythagoreanTriplets(triplets, limit)

    val newAcc = acc + sum
    if validTriplets.isEmpty then newAcc
    else
      val next = validTriplets.flatMap(_.nextPythagoreanTriplet)
      loop(next, newAcc)

  loop(List(primitivePythagoreanTriplet), 0)

def getCountForPythagoreanTriplets(
    triplets: List[PythagoreanTriplet],
    limit: Int
): (List[PythagoreanTriplet], Long) =
  triplets.foldLeft((List.empty[PythagoreanTriplet], 0L)) {
    case ((validTriplets, sum), triplet) =>
      val count = getCountForPythagoreanTriplet(triplet, limit)
      if count == 0 then (validTriplets, sum)
      else (triplet :: validTriplets, sum + count)
  }
  
getCountForM(99)  // 1975
getCountForM(100) // 2060
```

## Задача №87

[Задача №87 - Prime Power Triples](https://projecteuler.net/problem=87)

> Наименьшее число, которое можно представить в виде суммы квадрата простого числа, 
> куба простого числа и четвертой степени простого числа, равно 28. 
> Между прочим, существует ровно 4 таких числа меньше пятидесяти, 
> которые можно представить в виде суммы указанным способом:
> 
> - \\(28 = 2^2 + 2^3 + 2^4\\)
> - \\(33 = 3^2 + 2^3 + 2^4\\)
> - \\(49 = 5^2 + 2^3 + 2^4\\)
> - \\(47 = 2^2 + 3^3 + 2^4\\)
> 
> Сколько чисел до пятидесяти миллионов можно представить в виде суммы квадрата, куба и четвертой степени простых чисел?

**Алгоритм:**

- Составим список всех простых чисел меньших корня из пятидесяти миллионов (т.к. \\(p^2 + 2^3 + 2^4 < 50000000\\))
- Пойдем по каждому a из списка от меньшего к большему до тех пор, пока \\(a^2 + a^3 + a^4 < 50000000\\)
- Пойдем по каждому b из списка от меньшего к большему, начиная со значения a, 
  до тех пор, пока \\(b^2 + a^3 + a^4 < 50000000\\)
- Пойдем по каждому c из списка от меньшего к большему, начиная со значения b,
  до тех пор, пока \\(c^2 + b^3 + a^4 < 50000000\\)
- Составим все 6 возможных вариантов сумм из этих трех чисел
    - \\(a^2 + b^3 + c^4\\)
    - \\(a^2 + c^3 + b^4\\)
    - \\(b^2 + a^3 + c^4\\)
    - \\(b^2 + c^3 + a^4\\)
    - \\(c^2 + a^3 + b^4\\)
    - \\(c^2 + b^3 + a^4\\)
- Сохраним в памяти те суммы, которые меньше пятидесяти миллионов (`memory(number) = true`)
    - При этом, возможно, получение дубликата и такое число уже сохранено в памяти
- Посчитаем количество сохраненных в памяти чисел

**Код:**

```dotty
def getCountOfPossibleSum(limit: Int): Long =
  val maxPrime               = math.sqrt(limit).toInt
  val primes                 = primesNoMoreThanN(maxPrime) // метод определен в Problem 27
  val memory: Array[Boolean] = new Array[Boolean](limit)
  for
    i <- primes.indices
    a = primes(i).toLong
    if a * a + a * a * a + a * a * a * a < limit
    j <- i until primes.length
    b = primes(j).toLong
    if b * b < limit - a * a * a * (1 + a)
    k <- j until primes.length
    c   = primes(k).toLong
    cba = c * c + b * b * b + a * a * a * a
    if cba < limit
  do
    memory(cba.toInt) = true
    val acb = a * a + c * c * c + b * b * b * b
    if acb < limit then memory(acb.toInt) = true
    val bac = b * b + a * a * a + c * c * c * c
    if bac < limit then memory(bac.toInt) = true
    val bca = b * b + c * c * c + a * a * a * a
    if bca < limit then memory(bca.toInt) = true
    val cab = c * c + a * a * a + b * b * b * b
    if cab < limit then memory(cab.toInt) = true
    val abc = a * a + b * b * b + c * c * c * c
    if abc < limit then memory(abc.toInt) = true
  end for
  memory.count(identity)
end getCountOfPossibleSum

getCountOfPossibleSum(50)  // 4
getCountOfPossibleSum(500) // 53
```

## Задача №88

[Задача №88 - Product-sum Numbers](https://projecteuler.net/problem=88)

> Каждое натуральное число N, которое можно записать как в виде суммы, так и в виде произведения элементов множества, 
> состоящего из по крайней мере двух натуральных чисел \\({a\_1, a\_2, ... , a\_k}\\), 
> называется числом произведения-суммы: \\(N = a\_1 + a\_2 + ... + a\_k = a\_1 \times a\_2 \times ... \times a\_k\\).
>
> К примеру, \\(6 = 1 + 2 + 3 = 1 \times 2 \times 3\\).
> 
> Для заданного множества размером k мы будем называть наименьшее число N, обладающее данным свойством, 
> наименьшим числом произведения-суммы. 
> Наименьшими числами произведения-суммы множеств размером k = 2, 3, 4, 5 и 6 являются следующие числа:
> 
> - \\(k = 2: 4 = 2 \times 2 = 2 + 2\\)
> - \\(k = 3: 6 = 1 \times 2 \times 3 = 1 + 2 + 3\\)
> - \\(k = 4: 8 = 1 \times 1 \times 2 \times 4 = 1 + 1 + 2 + 4\\)
> - \\(k = 5: 8 = 1 \times 1 \times 2 \times 2 \times 2 = 1 + 1 + 2 + 2 + 2\\)
> - \\(k = 6: 12 = 1 \times 1 \times 1 \times 1 \times 2 \times 6 = 1 + 1 + 1 + 1 + 2 + 6\\)
> 
> Отсюда следует, что сумма всех минимальных чисел произведения-суммы при \\(2 \leq k \leq 6\\) равна 4 + 6 + 8 + 12 = 30. 
> Обратите внимание на то, что число 8 учитывалось в сумме лишь один раз.
> 
> Т.к. множеством минимальных чисел произведения-суммы при \\(2 \leq k \leq 12\\) является {4, 6, 8, 12, 15, 16}, 
> то их сумма равна 61.
> 
> Какова сумма всех минимальных чисел произведения-суммы при \\(2 \leq k \leq 12000\\)?

**Алгоритм:**

**Код:**

```dotty

```

## Задача №89

[Задача №89 - Roman Numerals](https://projecteuler.net/problem=89)

> Правила записи чисел римскими цифрами позволяют записывать одно и то же число несколькими способами 
> (см. FAQ: [Римские числа](https://euler.jakumo.org/roman.html)). 
> Однако, всегда существует "наилучший" способ записи определенного числа.
> 
> К примеру, ниже представлены все разрешенные способы записи числа шестнадцать:
> 
> - IIIIIIIIIIIIIIII
> - VIIIIIIIIIII
> - VVIIIIII
> - XIIIIII
> - VVVI
> - XVI
> 
> Последнее из них считается наиболее эффективным, поскольку использует наименьшее число римских цифр.
> 
> В текстовом файле roman.txt размером 11KБ тысяча чисел записана римскими цифрами правильным, 
> но не обязательно наилучшим способом, т.е. они отсортированы в порядке убывания 
> и подчиняются правилу вычитания пар (для информации об определяющих правилах данной задачи см. FAQ).
> 
> Найдите число символов, сэкономленных путем перезаписи каждого числа в его наиболее короткий вид.
> 
> Примечание: Можете считать, что все числа в файле содержат не более четырех последовательных одинаковых цифр.

**Алгоритм:**

Достаточно минимизировать цифры от меньшего к большему (\\(I \to V \to X \to ...\\)),
следуя определениям Римских цифр. 
От меньшего к большему, т.к. замена меньшего, например I, добавляет большее число (V),
что может привести к необходимости и его последующей замены.

**Код:**

```dotty
def toMinimalRomanNumeral(roman: String): String =
  roman.toUpperCase
    .replaceAll("IIIII", "V")
    .replaceAll("VV", "X")
    .replaceAll("XXXXX", "L")
    .replaceAll("LL", "C")
    .replaceAll("CCCCC", "D")
    .replaceAll("DD", "M")
    .replaceAll("VIIII", "IX")
    .replaceAll("IIII", "IV")
    .replaceAll("LXXXX", "XC")
    .replaceAll("XXXX", "XL")
    .replaceAll("DCCCC", "CM")
    .replaceAll("CCCC", "CD")

val names = Source.fromResource("p089_roman.txt").getLines.toSeq
val count =
  names.map: number =>
    val minimal = toMinimalRomanNumeral(number)
    number.length - minimal.length
  .sum
```

## Задача №90

[Задача №90 - Cube Digit Pairs](https://projecteuler.net/problem=90)

> На грани кубика нанесены разные цифры (от 0 до 9). То же сделано со вторым кубиком. 
> По-разному располагая два кубика рядом, можно получить различные двузначные числа.
>
> К примеру, можно получить число-квадрат 64:
> 
> ![](https://projecteuler.net/project/images/p090.png)
> 
> Между прочим, внимательно выбирая цифры обоих кубиков, можно получить все возможные квадраты меньше сотни: 
> 01, 04, 09, 16, 25, 36, 49, 64 и 81.
> 
> К примеру, один из способов достижения такой цели - нанести цифры {0, 5, 6, 7, 8, 9} на грани одного кубика, 
> а на грани второго - цифры {1, 2, 3, 4, 8, 9}.
> 
> Однако, в данной задаче мы разрешаем переворачивать 6 и 9, таким образом, 
> нанеся на грани одного кубика цифры {0, 5, 6, 7, 8, 9}, а на грани другого - {1, 2, 3, 4, 6, 7}, 
> можно получить все девять квадратов. В противном случае невозможно получить 09.
> 
> Определяя отдельные порядки нанесения цифр на кубики, нас интересуют только цифры на гранях каждого из них, 
> а не их порядок следования.
> 
> - {1, 2, 3, 4, 5, 6} равносильно {3, 6, 4, 1, 2, 5}
> - {1, 2, 3, 4, 5, 6} отличается от {1, 2, 3, 4, 5, 9}
> 
> Однако, т.к. мы разрешили переворачивать 6 и 9, оба различных множества предыдущего примера представляют 
> расширенное множество {1, 2, 3, 4, 5, 6, 9} для получения двузначных чисел.
> 
> Сколько различных порядков нанесения цифр на кубики дают возможность получения всех квадратов?

**Алгоритм:**

**Код:**

```dotty

```

## Задача №91

[Задача №91 - Right Triangles with Integer Coordinates](https://projecteuler.net/problem=91)

> Точки \\(P(x\_1, y\_1)\\) и \\(Q(x\_2, y\_2)\\) находятся на целых координатах и соединены с началом координат 
> O(0,0), образуя треугольник ΔOPQ.
> 
> ![](https://projecteuler.net/project/images/p091_1.png)
> 
> Существует ровно четырнадцать треугольников с прямым углом, которые можно получить для целых координат 
> в пределах от 0 до 2 включительно, т.е. \\(0 \leq x\_1, y\_1 , x\_2, y\_2 \leq 2\\).
> 
> ![](https://projecteuler.net/project/images/p091_2.png)
> 
> При данных \\(0 \leq x\_1, y\_1 , x\_2, y\_2 \leq 50\\), сколько прямоугольных треугольников можно построить?

**Алгоритм:**

Обозначим (a, b, c) как квадраты сторон треугольника \\((0, 0), (x\_1, y\_1), (x\_2, y\_2)\\).
Этот треугольник является прямоугольным, если сумма двух квадратов равна третьему:

**Код:**

```dotty
def isRightTriangle(x1: Int, y1: Int, x2: Int, y2: Int): Boolean =
  val a = x1 * x1 + y1 * y1
  val b = x2 * x2 + y2 * y2
  val c = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
  (a + b == c || a + c == b || b + c == a)
```

Чтобы не рассматривать каждый треугольник дважды (в случаях \\((x\_1, y\_1), (x\_2, y\_2)\\) и \\((x\_2, y\_2), (x\_1, y\_1)\\))
условимся, что \\(x\_1 \leq x\_2\\), а если \\(x\_1 = x\_2\\), то \\(y\_1 < y\_2\\).

```dotty
def trianglesCount(limit: Int): Long =
  val triangles =
    for
      x1 <- 0 to limit
      y1 <- 0 to limit
      if x1 + y1 > 0
      x2 <- x1 to limit
      y2 <- (if x1 == x2 then y1 + 1 else 0) to limit
      if isRightTriangle(x1, y1, x2, y2)
    yield 1
  triangles.sum

trianglesCount(2) // 14
```

## Задача №92

[Задача №92 - Square Digit Chains](https://projecteuler.net/problem=92)

> Последовательность чисел получается путем сложения квадратов цифр предыдущего числа до тех пор, 
> пока не получится уже встречавшееся ранее число.
> 
> К примеру,
> 
> - \\(44 \to 32 \to 13 \to 10 \to 1 \to 1\\)
> - \\(85 \to 89 \to 145 \to 42 \to 20 \to 4 \to 16 \to 37 \to 58 \to 89\\)
> 
> Таким образом, любая последовательность, приводящая к получению 1 или 89, замкнется в бесконечный цикл. 
> Самое удивительное, что ЛЮБОЕ начальное число рано или поздно даст 1 или 89.
> 
> Сколько начальных чисел меньше десяти миллионов приведут к получению 89?

**Алгоритм:**

Для каждого числа от одного до десяти миллионов считаем следующий элемент в последовательности - 
сумма квадратов цифр текущего числа.
Если наткнулись на 1 или 89, то сохраняем в памяти результат для всех чисел из последовательности,
чтобы не вычислять дважды для одного и того же числа.

**Код:**

```dotty
@tailrec
def getSquareSum(n: Int, acc: Int = 0): Int =
  if n <= 0 then acc
  else
    val r = n % 10
    getSquareSum(n / 10, acc + r * r)

val limit: Int = 10_000_000

val memory: Array[Byte] = new Array[Byte](limit)
memory(1) = 1
memory(89) = 89

def getCycleNumber(n: Int): Byte =
  if memory(n) > 0 then memory(n)
  else
    val m = getSquareSum(n)
    memory(n) = getCycleNumber(m)
    memory(n)

val count = (1 until limit).count(n => getCycleNumber(n) == 89)
```

## Задача №93

[Задача №93 - Arithmetic Expressions](https://projecteuler.net/problem=93)

> Используя каждую из цифр множества {1, 2, 3, 4} только один раз, с помощью четырех арифметических действий 
> \\((+, −, *, /)\\) и скобок можно получить различные натуральные числа.
> 
> К примеру,
> 
> - \\(8 = (4 * (1 + 3)) / 2\\)
> - \\(14 = 4 * (3 + 1 / 2)\\)
> - \\(19 = 4 * (2 + 3) − 1\\)
> - \\(36 = 3 * 4 * (2 + 1)\\)
> 
> Обратите внимание, что объединять цифры, вроде 12 + 34, не разрешается.
> 
> Используя множество {1, 2, 3, 4}, можно получить тридцать одно отличное число, среди которых наибольшим является 36. 
> Помимо этого, до обнаружения первого числа, которое нельзя выразить данным способом, были получены все числа от 1 до 28.
> 
> Найдите множество четырех отличных цифр a < b < c < d, с помощью которых можно получить максимально 
> длинное множество последовательных натуральных чисел от 1 до n. Ответ дайте объединив числа в строку: abcd.

**Алгоритм:**

Определим необходимые операции: сложение, вычитание, умножение и деление. 
При этом надо иметь в виду, что операции должны производиться над `Double`, 
т.к. только итоговое число после выполнения всех трех операции должно быть целым.
Например, как в случае с \\(4 * (3 + \frac{1}{2}) = 4 * 3.5 = 14\\).

**Код:**

```dotty
enum Oper:
  case Add, Sub, Mul, Div

def calculate(a: Double, b: Double, op: Oper): Double =
  op match
    case Oper.Add => a + b
    case Oper.Sub => a - b
    case Oper.Mul => a * b
    case Oper.Div => a / b
```

Для уже заданных чисел и операций (a op1 b op2 c op3 d) определим все возможные комбинации скобок, 
задающих порядок выполнения:

- ((a op1 b) op2 (c op3 d))
- (((a op1 b) op2 c) op3 d)
- (a op1 ((b op2 c) op3 d))
- ((a op1 (b op2 c)) op3 d)
- (a op1 (b op2 (c op3 d)))

**Код:**

```dotty
def calculateDifferentCompositions(
    a: Int,
    b: Int,
    c: Int,
    d: Int,
    first: Oper,
    second: Oper,
    third: Oper
): SortedSet[Int] =
  val case132 = calculate(
    calculate(a.toDouble, b.toDouble, first),
    calculate(c.toDouble, d.toDouble, third),
    second
  )
  val case123 =
    calculate(
      calculate(calculate(a.toDouble, b.toDouble, first), c.toDouble, second),
      d.toDouble,
      third
    )
  val case312 = calculate(
    a.toDouble,
    calculate(calculate(b.toDouble, c.toDouble, second), d.toDouble, third),
    first
  )
  val case213 =
    calculate(
      calculate(a.toDouble, calculate(b.toDouble, c.toDouble, second), first),
      d.toDouble,
      third
    )
  val case321 = calculate(
    a.toDouble,
    calculate(b.toDouble, calculate(c.toDouble, d.toDouble, third), second),
    first
  )
  SortedSet(case132, case123, case312, case213, case321).collect {
    case x if x.isWhole => x.toInt
  }
end calculateDifferentCompositions
```

Составим все возможные комбинации операций для заданного набора чисел (a, b, c, d):

```dotty
def calculateDifferentOperations(
    a: Int,
    b: Int,
    c: Int,
    d: Int
): SortedSet[Int] =
  val numbers =
    for
      first  <- Oper.values.toSeq
      second <- Oper.values
      third  <- Oper.values
      result <-
        calculateDifferentCompositions(a, b, c, d, first, second, third)
    yield result

  SortedSet.from(numbers)
  
calculateDifferentOperations(4, 3, 1, 2)  
// SortedSet(-12, -7, -5, -4, -2, -1, 0, 
// 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 21, 22, 24, 26, 32, 36)
```

Для заданного набора чисел (a, b, c, d) определим максимальную длину последовательности натуральных чисел,
входящих в результаты, полученные перестановкой этих чисел для различных комбинаций:

> [Перестановку можно получить по стандартному алгоритму](../../algorithms/algorithms/permutation.md).

```dotty
def getMaxLength(a: Int, b: Int, c: Int, d: Int): Int =
  val set = calculateDifferentOperations(a, b, c, d)
  LazyList.from(1).takeWhile(i => set.contains(i)).length
  
private def calculateDifferentPositions(
    a: Int,
    b: Int,
    c: Int,
    d: Int
): SortedSet[Int] =
  val result = xpermutations(List(a, b, c, d)).collect {
    case a :: b :: c :: d :: Nil => calculateDifferentOperations(a, b, c, d)
  }.flatten
  SortedSet.from(result)

private def xpermutations[A](l: List[A]): List[List[A]] = ...

getMaxLength(1, 2, 3, 4) // 28  
```

Искомое множество четырех отличных цифр a < b < c < d, с помощью которых можно получить 
максимально длинное множество последовательных натуральных чисел от 1 до n можно найти так:

```dotty
lazy val getSetWithMaxLength: (Int, Int, Int, Int) =
  val sets =
    for
      a <- 1 to 6
      b <- a + 1 to 7
      c <- b + 1 to 8
      d <- c + 1 to 9
    yield (a, b, c, d)
  sets.maxBy { case (a, b, c, d) => getMaxLength(a, b, c, d) }
```

## Задача №94

[Задача №94 - Almost Equilateral Triangles](https://projecteuler.net/problem=94)

> Легко показать, что не существует равносторонних треугольников с целочисленными сторонами и площадью. 
> Однако, площадь почти равностороннего треугольника со сторонами 5-5-6 равна 12 квадратным единицам.
> 
> Определим почти равносторонний треугольник как равнобедренный треугольник, 
> у которого основание отличается от боковой стороны не более чем на одну единицу.
> 
> Найдите сумму периметров всех почти равносторонних треугольников с целыми боковыми сторонами и площадью, 
> периметр каждого из которых не превышает один миллиард (1 000 000 000).

**Алгоритм:**

Согласно условиям задачи возможны [два равнобедренных треугольника](https://ru.wikipedia.org/wiki/%D0%A0%D0%B0%D0%B2%D0%BD%D0%BE%D0%B1%D0%B5%D0%B4%D1%80%D0%B5%D0%BD%D0%BD%D1%8B%D0%B9_%D1%82%D1%80%D0%B5%D1%83%D0%B3%D0%BE%D0%BB%D1%8C%D0%BD%D0%B8%D0%BA), 
где `a` - одна из двух равных сторон:

- \\((a, a, a - 1)\\): периметр равен \\(3a - 1\\), площадь - \\(\frac{(a - 1) \times \sqrt{(3a - 1)(a + 1)}}{4}\\)
- \\((a, a, a + 1)\\): периметр равен \\(3a + 1\\), площадь - \\(\frac{(a + 1) \times \sqrt{(3a + 1)(a - 1)}}{4}\\)

Заметим, что a - нечетное число (2k + 1), т.к. в противном случае площадь не будет целым числом, 
т.к. числитель не будет кратным 2.

В таком случае получим:

- \\((2k + 1, 2k + 1, 2k)\\): периметр равен \\(6k + 2\\), площадь - \\(k \times \sqrt{(3k + 1)(k + 1)}\\)
- \\((2k + 1, 2k + 1, 2k + 2)\\): периметр равен \\(6k + 4\\), площадь - \\((k + 1) \times \sqrt{(3k + 2)k}\\)

**Код:**

```dotty
def isBigTriangleCorrect(k: Long): Boolean =
  val p = k * (3 * k + 2)
  val s = math.sqrt(p).toLong
  s * s == p

def isSmallTriangleCorrect(k: Long): Boolean =
  val p = (k + 1) * (3 * k + 1)
  val s = math.sqrt(p).toLong
  s * s == p

def getSumOfPerimeters(k: Long): Long =
  (if isBigTriangleCorrect(k) then 6 * k + 4 else 0) +
    (if isSmallTriangleCorrect(k) then 6 * k + 2 else 0)

val limit = 1_000_000_000L
val last  = (limit - 4) / 6
val count = (1L to last).foldLeft(0L)((acc, k) => acc + getSumOfPerimeters(k))
```

## Задача №95

[Задача №95 - Amicable Chains](https://projecteuler.net/problem=95)

> Собственными делителями числа являются все его делители, за исключением самого числа. 
> К примеру, собственными делителями числа 28 являются 1, 2, 4, 7 и 14. 
> Т.к. сумма этих делителей равна 28, будем называть такое число идеальным.
> 
> Интересно, что сумма всех собственных делителей числа 220 равна 284, 
> а сумма всех собственных делителей числа 284 равна 220, образуя цепочку их двух чисел. 
> Поэтому числа 220 и 284 называются парой дружественных чисел.
> 
> Менее известны цепочки большей длины. К примеру, начиная с числа 12496, образуется цепочка из 5 чисел:
> 
> \\(12496 \to 14288 \to 15472 \to 14536 \to 14264 (\to 12496 \to ...)\\)
> 
> Т.к. эта цепочка оканчивается тем же числом, которым она начиналась, ее называют цепочкой дружественных чисел.
> 
> Найдите наименьший член самой длинной цепочки дружественных чисел, 
> ни один элемент которой не превышает один миллион.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №96

[Задача №96 - Su Doku](https://projecteuler.net/problem=96)

> Су Доку (по-японски значит _место числа_) - название популярной головоломки. 
> Ее происхождение неизвестно, однако нужно отдать должное Леонарду Эйлеру, который придумал идею похожей, 
> но более сложной головоломки под названием Латинские Квадраты. 
> Целью Су Доку является заменить пустые места (или нули) в сетке 9 x 9 цифрами таким образом, 
> чтобы в каждой строке, колонке и квадрате 3 x 3 содержались все цифры от 1 до 9. 
> Ниже приведен пример типичной исходной головоломки и ее решение.
> 
> ![](https://projecteuler.net/project/images/p096_1.png)
> ![](https://projecteuler.net/project/images/p096_2.png)
> 
> Правильно составленная головоломка Су Доку имеет единственное решение и может быть решена с помощью логики, 
> однако иногда необходимо применять метод "гадай и проверяй", чтобы исключить неверные варианты 
> (существует очень спорное мнение по этому поводу). Сложность поиска определяет уровень головоломки. 
> Приведенный выше пример считается легким, так как его можно решить прямой дедукцией.
> 
> 6 КБ текстовый файл sudoku.txt содержит пятьдесят разных головоломок Су Доку различной сложности, 
> но каждая имеет единственное решение (первая головоломка в файле рассмотрена выше).
> 
> Решив все пятьдесят головоломок, найдите сумму трехзначных чисел, находящихся в верхнем левом углу каждого решения. 
> Например, 483 является трехзначным числом, находящимся в верхнем левом углу приведенного выше решения.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №97

[Задача №97 - Large Non-Mersenne Prime](https://projecteuler.net/problem=97)

> Первое из известных простых чисел, длина которого превышает миллион цифр, было открыто в 1999 году. 
> Это - простое число Мерсенна, имеющее вид \\(2^{6972593} − 1\\); оно состоит ровно из 2 098 960 цифр. 
> Позже были найдены другие простые числа Мерсенна, имеющие вид \\(2^p − 1\\), длина которых еще больше.
> 
> Однако, в 2004 году было найдено огромное простое число, не являющееся числом Мессена, 
> которое состоит из 2 357 207 цифр: \\(28433 \times 2^{7830457} + 1\\).
> 
> Найдите последние десять цифр этого простого числа.

**Алгоритм:**

**Код:**

```dotty
val module = BigInt("10000000000")
val lastTenDigits = (BigInt(2).modPow(BigInt(7830457), module) * 28433 + 1).mod(module).toLong
```

## Задача №98

[Задача №98 - Anagramic Squares](https://projecteuler.net/problem=98)

> Заменяя каждую из букв слова "CARE" цифрами 1, 2, 9 и 6, соответственно, получим квадратное число: \\(1296 = 36^2\\). 
> Примечательно, что после осуществления такой подстановки в слове "RACE", также получается квадратное число: \\(9216 = 96^2\\). 
> Слова "CARE" и "RACE" впредь будем называть парой слов - квадратных анаграмм. 
> Помимо этого, решим, что ведущие нули не разрешены, а также что ни одной букве нельзя присвоить цифру, 
> уже присвоенную другой букве.
> 
> В текстовом файле words.txt размером 16KБ содержится около двух тысяч распространенных английских слов. 
> Найдите все пары слов - квадратных анаграмм (слово-палиндром не считается своей же анаграммой).
> 
> Каким будет наибольшее квадратное число, полученное из любого слова такой пары?
> 
> Примечание: Все образованные анаграммы должны содержаться в указанном текстовом файле.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №99

[Задача №99 - Largest Exponential](https://projecteuler.net/problem=99)

> Сравнить два числа, записанных в виде степени, как, например, \\(2^{11}\\) и \\(3^7\\), не так уж трудно, 
> поскольку любой калькулятор подтвердит, что \\(2^{11} = 2048 < 3^7 = 2187\\).
>
> Однако, гораздо сложнее подтвердить, что \\(632382^{518061} > 519432^{525806}\\), 
> поскольку каждое из чисел содержит более трех миллионов цифр.
> 
> Текстовый файл base_exp.txt размером 22КБ содержит тысячу строк с парами основание/показатель степени на каждой строке. 
> Определите номер строки, в которой записано самое большое по своему значению число.
> 
> Примечание: Первые две строки файла - числа из примера, приведенного выше.

**Алгоритм:**

Для сравнения чисел достаточно взять логарифм от предполагаемого числа 
вместо попытки вычислить его возведением в большую степень.

**Код:**

```dotty
def parseString(row: String): Double =
  val (first, second) = row.splitAt(row.indexOf(","))
  math.log(first.toLong) * second.tail.toDouble

val lines   = Source.fromResource("p099_base_exp.txt").getLines.toArray
val maxLine = lines.indices.maxBy(i => parseString(lines(i))) + 1
```

## Задача №100

[Задача №100 - Arranged Probability](https://projecteuler.net/problem=100)

> Пусть в коробке лежит двадцать один диск, среди которых пятнадцать окрашены в синий цвет и остальные шесть - в красный. 
> Из коробки случайным образом взяли два диска. Нетрудно показать, 
> что вероятность достать два синих диска равна \\(P(BB) = (\frac{15}{21}) \times (\frac{14}{20}) = \frac{1}{2}\\).
> 
> Следующая комбинация из наименьшего возможного количества дисков с ровно 50%-ным шансом случайным образом достать 
> 2 синих диска - это коробка с 85 синими дисками и 35 красными.
> 
> Найдите такую комбинацию с наименьшим возможным суммарным количеством дисков, 
> превышающим \\(10^{12} = 1 000 000 000 000\\), 
> и определите количество синих дисков, находящихся в коробке.

**Алгоритм:**

Пусть b - количество синих шаров, а r - красных. Тогда по условию задачи:
\\(\frac{b}{b + r} \times \frac{b - 1}{b + r - 1} = \frac{1}{2}\\).

Проведем некоторую последовательность вычислений:

1. \\(2b^2 - 2b = b^2 + br - b + br + r^2 - r\\)
2. \\(b^2 - b = r(2b + r - 1)\\)
3. Пусть \\(x = 2b + r - 1\\), тогда \\(r = x - 2b + 1\\)
4. \\(b^2 - b = x^2 - 2bx + x\\)
5. \\(b(2x + b) = x^2 + x + b\\)
6. Пусть \\(y = 2x + b\\), тогда \\(b = y - 2x\\)
7. \\(y^2 - 2xy = x^2 + y - x\\)
8. \\(y^2 - y = x(2y + x - 1)\\) - то же самое уравнение, что и во втором пункте

Получается, что если пара (b, r) удовлетворяет уравнению, то и следующая пара (5b + 2r - 2, 2b + r - 1) (с б**о**льшими числами)
также будет удовлетворять уравнению.

Самая меньшая возможная пара (b, r) - это пара с r = 1 и следовательно \\(\frac{2b(b - 1)}{(b + 1)b} = 1 \Rightarrow b = 3\\).

Первые пары: \\((3, 1) \Rightarrow (15, 6) \Rightarrow (85, 35) \Rightarrow (493, 204) \Rightarrow ... \\)

> Правда, нет гарантии, что это единственный способ генерации следующих пар

**Код:**

```dotty
@tailrec
def getBlueDiscs(blue: Long, red: Long): Long =
  if blue + red > 1_000_000_000_000L then blue
  else getBlueDiscs(5 * blue + 2 * red - 2, 2 * blue + red - 1)

val count = getBlueDiscs(3, 1)
```

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]
- [Проект Эйлера на русском](https://euler.jakumo.org/problems.html)

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
