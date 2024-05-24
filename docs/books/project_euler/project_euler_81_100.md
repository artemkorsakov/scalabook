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

>

**Алгоритм:**

**Код:**

```dotty

```

## Задача №84

[Задача №84 - Monopoly Odds](https://projecteuler.net/problem=84)

>

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

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
