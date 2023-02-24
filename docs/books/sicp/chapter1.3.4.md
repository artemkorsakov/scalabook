# Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.4. Процедуры как возвращаемые значения

#### Упражнение 1.40

> Определите процедуру **cubic**, которую можно было бы использовать совместно с процедурой **newtons-method** 
> в выражениях вида **(newtons-method (cubic a b c) 1)** для приближенного вычисления нулей 
> кубических уравнений **x<sup>3</sup> + ax<sup>2</sup> + bx + c**.


Решение на Scala:

```scala
def square(x: Double): Double = x * x

def cube(x: Double): Double = x * x * x

def cubic(a: Double, b: Double, c: Double): Double => Double = x =>
  cube(x) + a * square(x) + b * x + c
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-40.worksheet.sc)


#### Упражнение 1.41

> Определите процедуру **double**, которая принимает как аргумент процедуру с одним аргументом 
> и возвращает процедуру, которая применяет исходную процедуру дважды. 
> Например, если процедура **inc** добавляет к своему аргументу **1**, 
> то **(double inc)** должна быть процедурой, которая добавляет **2**. 
> 
> Скажите, какое значение возвращает **(((double (double double)) inc) 5)**


Решение на Scala:

```scala
val inc: Int => Int = _ + 1

def double[T](f: T => T): T => T = x => f(f(x))

// (((double (double double)) inc) 5)
val f: (Int => Int) => (Int => Int) = double(double(double))
f(inc)(5) // 21
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-41.worksheet.sc)


#### Упражнение 1.42

> Пусть **f** и **g** — две одноаргументные функции. 
> По определению, композиция (*composition*) **f** и **g** есть функция **x → f(g(x))**. 
> Определите процедуру **compose** которая реализует композицию.
> Например, если **inc** — процедура, добавляющая к своему аргументу **1**,
>
> ```
> ((compose square inc) 6)
> 49
> ```


Решение на Scala:

```scala
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

val square: Int => Int = x => x * x
val inc: Int => Int = x => x + 1

compose(square, inc)(6) // 49
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-42.worksheet.sc)


#### Упражнение 1.43

> Если **f** есть численная функция, а **n** — положительное целое число, 
> то мы можем построить **n**-кратное применение **f**, 
> которое определяется как функция, значение которой в точке **x** равно **f(f(... (f(x)) ...))**. 
> Например, если **f** есть функция **x → x + 1**, то **n**-кратным применением **f** будет функция **x → x + n**. 
> Если **f** есть операция возведения в квадрат, 
> то **n**-кратное применение **f** есть функция, которая возводит свой аргумент в **2<sup>n</sup>**-ю степень. 
> 
> Напишите процедуру, которая принимает в качестве ввода процедуру, вычисляющую **f**, и положительное целое **n**, 
> и возвращает процедуру, вычисляющую **n**-кратное применение **f**. 
> 
> Требуется, чтобы Вашу процедуру можно было использовать в таких контекстах:
>
> ```
> ((repeated square 2) 5)
> 625
> ```
>
> Подсказка: может оказаться удобно использовать **compose** из упражнения 1.42

Решение на Scala:

```scala
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

val square: Int => Int = x => x * x

repeated(square, 2)(5) // 625
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-43.worksheet.sc)


#### Упражнение 1.44

> Идея сглаживания (*smoothing a function*) играет важную роль в обработке сигналов. 
> Если **f** — функция, а **dx** — некоторое малое число, то сглаженная версия **f** есть функция, 
> значение которой в точке **x** есть среднее между **f(x − dx)**, **f(x)** и **f(x + dx)**. 
> Напишите процедуру **smooth**, которая в качестве ввода принимает процедуру, вычисляющую **f**, 
> и возвращает процедуру, вычисляющую сглаженную версию **f**. 
> 
> Иногда бывает удобно проводить повторное сглаживание (то есть сглаживать сглаженную функцию и т.д.), 
> получая **n**-кратно сглаженную функцию (*n-fold smoothed function*). 
> Покажите, как породить **n**-кратно сглаженную функцию с помощью **smooth** и **repeated** из упражнения 1.43.


Решение на Scala:

```scala
val dx: Double = 1e-6

def smooth(f: Double => Double): Double => Double =
  x => (f(x - dx) + f(x) + f(x + dx)) / 3

def nFoldSmoothedFunction(f: Double => Double, n: Int): Double => Double =
  repeated(smooth, n)(f)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-44.worksheet.sc)


#### Упражнение 1.45

> В разделе 1.3.3 мы видели, что попытка вычисления квадратных корней путем наивного поиска неподвижной точки **y → x/y** не сходится, 
> и что это можно исправить путем торможения усреднением. 
> Тот же самый метод работает для нахождения кубического корня как неподвижной точки **y → x/y<sup>2</sup>**, заторможенной усреднением. 
> К сожалению, этот процесс не работает для корней четвертой степени — однажды примененного торможения усреднением недостаточно, 
> чтобы заставить сходиться процесс поиска неподвижной точки **y → x/y<sup>3</sup>**. 
> С другой стороны, если мы применим торможение усреднением дважды 
> (т.е. применим торможение усреднением к результату торможения усреднением от **y → x/y<sup>3</sup>**), 
> то поиск неподвижной точки начнет сходиться. 
> Проделайте эксперименты, чтобы понять, сколько торможений усреднением нужно, 
> чтобы вычислить корень **n**-ой степени как неподвижную точку 
> на основе многократного торможения усреднением функции **y → x/y<sup>n−1</sup>**. 
> Используя свои результаты для написания простой процедуры вычисления корней **n**-ой степени с помощью процедур 
> **fixed-point**, **average-damp** и **repeated** из упражнения 1.43. 
> Считайте, что все арифметические операции, какие Вам понадобятся, присутствуют в языке как примитивы.

Судя по наблюдениям, для вычисления корней **n**-ой степени достаточно **log(n) + 1** торможений.

Решение на Scala:

```scala
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

def average(a: Double, b: Double): Double = (a + b) / 2

def averageDamp(f: Double => Double): Double => Double =
  x => average(x, f(x))

val tolerance = 1e-5

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

def nFoldAverageFunction(f: Double => Double, n: Int): Double => Double =
  repeated(averageDamp, n)(f)

def nRoot(x: Double, n: Int): Double =
  val f: Double => Double = y => (x / math.pow(y, n - 1))
  fixedPoint(nFoldAverageFunction(f, math.log(n).toInt + 1), 1.0)

nRoot(math.pow(2, 100), 100) // 2.0079006560360497
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-45.worksheet.sc)


#### Упражнение 1.46

> Некоторые из вычислительных методов, описанных в этой главе, 
> являются примерами чрезвычайно общей вычислительной стратегии, называемой пошаговое улучшение (*iterative improvement*). 
> Пошаговое улучшение состоит в следующем: чтобы что-то вычислить, нужно взять какое-то начальное значение, 
> проверить, достаточно ли оно хорошо, чтобы служить ответом, 
> и если нет, то улучшить это значение и продолжить процесс с новым значением. 
> 
> Напишите процедуру **iterative-improve**, которая принимает в качестве аргументов две процедуры: 
> проверку, достаточно ли хорошо значение, и метод улучшения значения. 
> **Iterative-improve** должна возвращать процедуру, которая принимает начальное значение в качестве аргумента 
> и улучшает его, пока оно не станет достаточно хорошим. 
> 
> Перепишите процедуру **sqrt** из раздела 1.1.7 и 
> процедуру **fixed-point** из раздела 1.3.3 в терминах **iterative-improve**.

Решение на Scala:

```scala
def iterativeImprove(
    doesCloseEnough: (Double, Double) => Boolean,
    next: Double => Double
): Double => Double = firstGuess =>
  def tryGuess(guess: Double): Double =
    val nextGuess = next(guess)
    if doesCloseEnough(nextGuess, guess) then nextGuess
    else tryGuess(nextGuess)
  tryGuess(firstGuess)

def doesCloseEnough(a: Double, b: Double): Boolean =
  math.abs(a - b) < 1e-5

def average(x: Double, y: Double): Double = (x + y) / 2

def sqrt(x: Double): Double =
  iterativeImprove(doesCloseEnough, y => average(y, x / y))(1.0)

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  iterativeImprove(doesCloseEnough, f)(firstGuess)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-46.worksheet.sc)


---

**Ссылки:**
- [Упражнение 1.40 - 1.46](https://web.mit.edu/6.001/6.037/sicp.pdf#page=131)
