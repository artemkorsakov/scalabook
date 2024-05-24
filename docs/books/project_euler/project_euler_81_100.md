# Задачи №81-№100

## Задача №81

[Задача №81 - Path Sum: Two Ways](https://projecteuler.net/problem=81)

> In the 5 by 5 matrix below, the minimal path sum from the top left to the bottom right, 
> by **only moving to the right and down**, is indicated in bold red and is equal to 2427.
>
> Find the minimal path sum from the top left to the bottom right by only moving right and down in matrix.txt, 
> a 31K text file containing an 80 by 80 matrix.

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

> > NOTE: This problem is a more challenging version of Problem 81.
> 
> The minimal path sum in the 5 by 5 matrix below, by starting in any cell in the left column and finishing 
> in any cell in the right column, and only moving up, down, and right, is indicated in red and bold; 
> the sum is equal to 994.
>
> Find the minimal path sum from the left column to the right column in matrix.txt, 
> a 31K text file containing an 80 by 80 matrix.

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

> > NOTE: This problem is a significantly more challenging version of Problem 81.
> 
> In the 5 by 5 matrix below, the minimal path sum from the top left to the bottom right, 
> by moving left, right, up, and down, is indicated in bold red and is equal to 2297.
> 
> Find the minimal path sum from the top left to the bottom right by moving left, right, up, and down 
> in matrix.txt, a 31K text file containing an 80 by 80 matrix.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №84

[Задача №84 - Monopoly Odds](https://projecteuler.net/problem=84)

> In the game, **Monopoly**, the standard board is set up in the following way:
> 
> ![Monopoly](https://projecteuler.net/resources/images/0084_monopoly_board.png?1678992052)
>
> A player starts on the GO square and adds the scores on two 6-sided dice to determine the number of squares they 
> advance in a clockwise direction. 
> Without any further rules we would expect to visit each square with equal probability: 2.5%. 
> However, landing on G2J (Go To Jail), CC (community chest), and CH (chance) changes this distribution.
> 
> In addition to G2J, and one card from each of CC and CH, that orders the player to go directly to jail, 
> if a player rolls three consecutive doubles, they do not advance the result of their 3rd roll. 
> Instead they proceed directly to jail.
> 
> At the beginning of the game, the CC and CH cards are shuffled. 
> When a player lands on CC or CH they take a card from the top of the respective pile and, 
> after following the instructions, it is returned to the bottom of the pile. 
> There are sixteen cards in each pile, but for the purpose of this problem we are only concerned with cards 
> that order a movement; any instruction not concerned with movement will be ignored and 
> the player will remain on the CC/CH square.
> 
> - Community Chest (2/16 cards):
>     - Advance to GO
>     - Go to JAIL
> - Chance (10/16 cards):
>     - Advance to GO
>     - Go to JAIL
>     - Go to C1
>     - Go to E3
>     - Go to H2
>     - Go to R1
>     - Go to next R (railway company)
>     - Go to next R
>     - Go to next U (utility company)
>     - Go back 3 squares.
>
> The heart of this problem concerns the likelihood of visiting a particular square. 
> That is, the probability of finishing at that square after a roll. 
> For this reason it should be clear that, with the exception of G2J for which the probability of finishing on it is zero, 
> the CH squares will have the lowest probabilities, as 5/8 request a movement to another square, 
> and it is the final square that the player finishes at on each roll that we are interested in. 
> We shall make no distinction between "Just Visiting" and being sent to JAIL, 
> and we shall also ignore the rule about requiring a double to "get out of jail", 
> assuming that they pay to get out on their next turn.
> 
> By starting at GO and numbering the squares sequentially from 00 to 39 we can concatenate these two-digit numbers 
> to produce strings that correspond with sets of squares.
> 
> Statistically it can be shown that the three most popular squares, in order, 
> are JAIL (6.24%) = Square 10, E3 (3.18%) = Square 24, and GO (3.09%) = Square 00. 
> So these three most popular squares can be listed with the six-digit modal string: 102400.
> 
> If, instead of using two 6-sided dice, two 4-sided dice are used, find the six-digit modal string.

**Алгоритм:**

**Код:**

```dotty

```

## Задача №85

[Задача №85 - Counting Rectangles](https://projecteuler.net/problem=85)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №86

[Задача №86 - Cuboid Route](https://projecteuler.net/problem=86)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №87

[Задача №87 - Prime Power Triples](https://projecteuler.net/problem=87)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №88

[Задача №88 - Product-sum Numbers](https://projecteuler.net/problem=88)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №89

[Задача №89 - Roman Numerals](https://projecteuler.net/problem=89)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №90

[Задача №90 - Cube Digit Pairs](https://projecteuler.net/problem=90)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №91

[Задача №91 - Right Triangles with Integer Coordinates](https://projecteuler.net/problem=91)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №92

[Задача №92 - Square Digit Chains](https://projecteuler.net/problem=92)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №93

[Задача №93 - Arithmetic Expressions](https://projecteuler.net/problem=93)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №94

[Задача №94 - Almost Equilateral Triangles](https://projecteuler.net/problem=94)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №95

[Задача №95 - Amicable Chains](https://projecteuler.net/problem=95)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №96

[Задача №96 - Su Doku](https://projecteuler.net/problem=96)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №97

[Задача №97 - Large Non-Mersenne Prime](https://projecteuler.net/problem=97)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №98

[Задача №98 - Anagramic Squares](https://projecteuler.net/problem=98)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №99

[Задача №99 - Largest Exponential](https://projecteuler.net/problem=99)

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №100

[Задача №100 - Arranged Probability](https://projecteuler.net/problem=100)

>

**Алгоритм:**

**Код:**

```dotty

```

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]
- [Проект Эйлера на русском](https://euler.jakumo.org/problems.html)

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
