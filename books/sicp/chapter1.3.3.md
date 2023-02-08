# Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.3. Процедуры как обобщенные методы

#### Упражнение 1.35

> Покажите, что золотое сечение **φ** (раздел 1.2.2) есть неподвижная точка трансформации **x → 1 + 1/x**, 
> и используйте этот факт для вычисления **φ** с помощью процедуры **fixed-point**.

**φ<sup>2</sup> = φ + 1 => φ = 1 + 1/φ**

Решение на Scala:

```scala
val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => 1 + (1.0 / x), 1.0)  // 1.6180327868852458
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-35.worksheet.sc)

#### Упражнение 1.36

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-33.worksheet.sc)

#### Упражнение 1.37

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-33.worksheet.sc)

#### Упражнение 1.38

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-33.worksheet.sc)

#### Упражнение 1.39

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-33.worksheet.sc)


---

**Ссылки:**
- [Упражнение 1.35 - 1.39](https://web.mit.edu/6.001/6.037/sicp.pdf#page=122)
