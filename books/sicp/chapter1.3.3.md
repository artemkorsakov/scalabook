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

> а. Бесконечная цепная дробь (**continued fraction**) есть выражение вида
> **f = N<sub>1</sub> / (D<sub>1</sub> + N<sub>2</sub> / (D<sub>2</sub> + N<sub>3</sub> /(D<sub>3</sub> + ...)))**
> В качестве примера можно показать, что расширение бесконечной цепной дроби при всех **N<sub>i</sub>** и 
> **D<sub>i</sub>**, равных **1**, дает **1/φ**, где **φ** — золотое сечение (описанное в разделе 1.2.2). 
> Один из способов вычислить цепную дробь состоит в том, чтобы после заданного количества термов оборвать вычисление. 
> Такой обрыв — так называемая конечная цепная дробь (**finite continued fraction**) из **k** элементов, — 
> имеет вид **f = N<sub>1</sub> / (D<sub>1</sub> + N<sub>2</sub> / (D<sub>2</sub> + N<sub>3</sub> / (D<sub>3</sub> + ... + N<sub>k</sub> / D<sub>k</sub>)))**.
> Предположим, что **n** и **d** — процедуры одного аргумента (номера элемента **i**), 
> возвращающие **N<sub>i</sub>** и **D<sub>i</sub>** элементов цепной дроби. 
> Определите процедуру **cont-frac** так, чтобы вычисление **(cont-frac n d k)** давало значение **k**-элементной конечной цепной дроби.
> Проверьте свою процедуру, вычисляя приближения к **1/φ** с помощью
>
> ```
> (cont-frac (lambda (i) 1.0)
>            (lambda (i) 1.0)
>            k)
> ```
> 
> для последовательных значений **k**. 
> 
> Насколько большим пришлось сделать **k**, чтобы получить приближение, верное с точностью 4 цифры после запятой?
> 
> б. Если Ваша процедура **cont-frac** порождает рекурсивный процесс, напишите вариант, который порождает итеративный процесс. 
> Если она порождает итеративный процесс, напишите вариант, порождающий рекурсивный процесс.

Для k == 11 точность составляет 4 знака.

Решение на Scala:

Рекурсивный процесс: 

```scala
def contFracRec(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int): Double =
    if i == k then n(i) / d(i)
    else n(i) / (d(i) + loop(i + 1))
  loop(1)

contFracRec(_ => 1.0, _ => 1.0, 11) // 0.6180(5)
```

Итеративный процесс:

```scala
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

contFracIter(_ => 1.0, _ => 1.0, 11) // 0.6180(5)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-37.worksheet.sc)

#### Упражнение 1.38

> В 1737 году швейцарский математик Леонард Эйлер опубликовал статью *De functionibus Continuis*, 
> которая содержала расширение цепной дроби для **e−2**, где **e** — основание натуральных логарифмов. 
> В этой дроби все **N<sub>i</sub>** равны **1**, а **D<sub>i</sub>** последовательно равны **1, 2, 1, 1, 4, 1, 1, 6, 1, 1, 8, ...**
> 
> Напишите программу, использующую Вашу процедуру **cont-frac** из упражнения 1.37 
> для вычисления **e** на основании формулы Эйлера

Решение на Scala:

```scala
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

val n: Int => Double = _ => 1.0

val d: Int => Double = i =>
  if i % 3 == 2 then (i / 3 + 1) * 2
  else 1.0

contFracIter(n, d, 1000) // 0.7182818284590453
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-38.worksheet.sc)

#### Упражнение 1.39

> Представление тангенса в виде цепной дроби было опубликовано в 1770 году немецким математиком Й.Х. Ламбертом:
>
> **tg x = x / (1 − x<sup>2</sup> / (3 − x<sup>2</sup> / (5 − ...)))**
>
> где **x** дан в радианах. 
> 
> Определите процедуру **(tan-cf x k)**, которая вычисляет приближение к тангенсу на основе формулы Ламберта. 
> **K** указывает количество термов, которые требуется вычислить, как в упражнении 1.37.


Решение на Scala:

```scala
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

def n(x: Double)(i: Int): Double =
  if i == 1 then x
  else -x * x

val d: Int => Double = i => 2 * (i - 1) + 1

def tanCf(x: Double, k: Int): Double =
  contFracIter(n(x), d, k)

tanCf(1.0, 100) // 1.557407724654902
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-39.worksheet.sc)


---

**Ссылки:**
- [Упражнение 1.35 - 1.39](https://web.mit.edu/6.001/6.037/sicp.pdf#page=122)
