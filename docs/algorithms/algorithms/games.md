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

---

**Ссылки:**

- [Darts - Project Euler](https://projecteuler.net/problem=109)
