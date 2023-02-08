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

> Измените процедуру **fixed-point** так, чтобы она печатала последовательность приближений, 
> которые порождает, с помощью примитивов **newline** и **display**, показанных в упражнении 1.22. 
> Затем найдите решение уравнения **x<sup>x</sup> = 1000** путем поиска неподвижной точки **x → log(1000)/ log(x)**. 
> (Используйте встроенную процедуру **Scheme log**, которая вычисляет натуральные логарифмы.) 
> Посчитайте, сколько шагов это занимает при использовании торможения усреднением и без него. 
> (Учтите, что нельзя начинать **fixed-point** со значения **1**, поскольку это вызовет деление на **log(1) = 0**.)

Решение на Scala:

```scala
def average(a: Double, b: Double): Double = (a + b) / 2

val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    println(next)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => math.log(1000) / math.log(x), 10.0)

fixedPoint(x => average(x, math.log(1000) / math.log(x)), 10.0)
```

В первом случае мы получаем такое приближение (33 итерации):

```text
res0: Double = 4.555532257016376
// 2.9999999999999996
// 6.2877098228681545
// 3.7570797902002955
// 5.218748919675316
// 4.1807977460633134
// 4.828902657081293
// 4.386936895811029
// 4.671722808746095
// 4.481109436117821
// 4.605567315585735
// 4.522955348093164
// 4.577201597629606
// 4.541325786357399
// 4.564940905198754
// 4.549347961475409
// 4.5596228442307565
// 4.552843114094703
// 4.55731263660315
// 4.554364381825887
// 4.556308401465587
// 4.555026226620339
// 4.55587174038325
// 4.555314115211184
// 4.555681847896976
// 4.555439330395129
// 4.555599264136406
// 4.555493789937456
// 4.555563347820309
// 4.555517475527901
// 4.555547727376273
// 4.555527776815261
// 4.555540933824255
// 4.555532257016376
```

Во втором - 9:

```text
res1: Double = 4.555536206185039
// 6.5
// 5.095215099176933
// 4.668760681281611
// 4.57585730576714
// 4.559030116711325
// 4.55613168520593
// 4.555637206157649
// 4.55555298754564
// 4.555538647701617
// 4.555536206185039
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-36.worksheet.sc)

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
