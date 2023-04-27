# Глава 1.3.1

## Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.1. Процедуры в качестве аргументов

#### Упражнение 1.29

> Правило Симпсона — более точный метод численного интегрирования, чем представленный выше. 
> С помощью правила Симпсона интеграл функции `f` между `a` и `b` приближенно вычисляется в виде 
> `(h / 3) * [y[0] + 4y[1] + 2y[2] + 4y[3] + 2y[4] + . . . + 2y[n-2] + 4y[n-1] + y[n]]` 
> , где `h = (b − a)/n`, для какого-то четного целого числа `n`, а `y[k] = f(a + kh)`. 
> (Увеличение `n` повышает точность приближенного вычисления.) 
> Определите процедуру, которая принимает в качестве аргументов `f`, `a`, `b` и `n`, 
> и возвращает значение интеграла, вычисленное по правилу Симпсона. 
> С помощью этой процедуры проинтегрируйте `cube` между `0` и `1` (с `n = 100` и `n = 1000`) 
> и сравните результаты с процедурой `integral`, приведенной выше.


Решение на Scala:

```scala
def simpsonRule(f: Double => Double, a: Double, b: Double, n: Int): Double =
  val h = (b - a) / n
  def y(k: Int): Double =
    val co = if k == 0 || k == n then 1 else if k % 2 == 0 then 2 else 4
    co * f(a + k * h)

  (0 to n).foldLeft(0.0)((acc, k) => acc + y(k)) * h / 3

simpsonRule(cube, 0.0, 1.0, 100)   // 0.25000000000000006
simpsonRule(cube, 0.0, 1.0, 1000)  // 0.25000000000000006
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-29.worksheet.sc)


#### Упражнение 1.30

> Процедура `sum` порождает линейную рекурсию. 
> Ее можно переписать так, чтобы суммирование выполнялось итеративно. 
> Покажите, как сделать это, заполнив пропущенные выражения в следующем определении:
>
> ```
> (define (sum term a next b)
>   (define (iter a result)
>      (if <??>
>          <??>
>          (iter <??> <??>)))
>   (iter <??> <??>))
> ```

Решение на Scala:

```scala
def cube(a: Double): Double = a * a * a

def sum(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result + term(a))
  iter(a, 0.0)

def integral(f: Double => Double, a: Double, b: Double, dx: Double): Double =
  def addDx(x: Double): Double = x + dx
  sum(f, a + dx / 2, addDx, b) * dx

integral(cube, 0.0, 1.0, 0.01)  // 0.24998750000000042
integral(cube, 0.0, 1.0, 0.001) // 0.24999987500000073
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-30.worksheet.sc)


#### Упражнение 1.31

> а. Процедура `sum` — всего лишь простейшая из обширного множества подобных абстракций, 
> которые можно выразить через процедуры высших порядков. 
> Напишите аналогичную процедуру под названием `product`, 
> которая вычисляет произведение значений функции в точках на указанном интервале. 
> Покажите, как с помощью этой процедуры определить `factorial`. 
> Кроме того, при помощи `product` вычислите приближенное значение `π` по формуле:
> `π / 4 = (2 / 3) * (4 / 3) * (4 / 5) * (6 / 5) * (6 / 7) * (8 / 7) * ...`
> 
> б. Если Ваша процедура `product` порождает рекурсивный процесс, перепишите ее так, чтобы она порождала итеративный. 
> Если она порождает итеративный процесс, перепишите ее так, чтобы она порождала рекурсивный.

Решение на Scala:

```scala
val identity: Double => Double = x => x
val nextNum: Double => Double = x => x + 1
val square: Double => Double = x => x * x
val piFraction: Double => Double = k =>
  (2 * k) * (2 * k + 2) / square(2 * k + 1)

def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then 1.0
  else term(a) * productRec(term, next(a), next, b)

def factorialRec(n: Int): Int =
  productRec(identity, 1, nextNum, n).toInt

def calculatePiRec(n: Int): Double =
  4 * productRec(piFraction, 1, nextNum, n)

factorialRec(10)     // 3628800
calculatePiRec(1000) // 3.142377365093882

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result * term(a))
  iter(a, 1.0)

def factorialIter(n: Int): Int =
  productIter(identity, 1, nextNum, n).toInt

def calculatePiIter(n: Int): Double =
  4 * productIter(piFraction, 1, nextNum, n)

factorialIter(10)     // 3628800
calculatePiIter(1000) // 3.1423773650938855
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-31.worksheet.sc)


#### Упражнение 1.32

> а. Покажите, что **sum** и **product** (упражнение 1.31) являются частными случаями еще более общего понятия, 
> называемого накопление (**accumulation**), которое комбинирует множество термов 
> с помощью некоторой общей функции накопления **(accumulate combiner null-value term a next b)**
>
> **Accumulate** принимает в качестве аргументов те же описания термов и диапазона, что и **sum** с **product**, 
> а еще процедуру **combiner** (двух аргументов), которая указывает, 
> как нужно присоединить текущий терм к результату накопления предыдущих, 
> и **null-value**, базовое значение, которое нужно использовать, когда термы закончатся. 
> Напишите **accumulate** и покажите, как и **sum**, и **product** можно определить в виде простых вызовов **accumulate**.
> 
> б. Если Ваша процедура **accumulate** порождает рекурсивный процесс, перепишите ее так, чтобы она порождала итеративный. 
> Если она порождает итеративный процесс, перепишите ее так, чтобы она порождала рекурсивный

Решение на Scala:

```scala
def accumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then nullValue
  else
    combiner(
      term(a),
      accumulateRec(combiner, nullValue, term, next(a), next, b)
    )

def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateRec(_ * _, 1.0, term, a, next, b)

def accumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), combiner(result, term(a)))
  iter(a, nullValue)

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateIter(_ * _, 1.0, term, a, next, b)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-32.worksheet.sc)


#### Упражнение 1.33

> Можно получить еще более общую версию **accumulate** (упражнение 1.32), 
> если ввести понятие фильтра (**filter**) на комбинируемые термы. 
> То есть комбинировать только те термы, порожденные из значений диапазона, которые удовлетворяют указанному условию. 
> 
> Получающаяся абстракция **filtered-accumulate** получает те же аргументы, что и **accumulate**, 
> плюс дополнительный одноаргументный предикат, который определяет фильтр. 
> Запишите **filtered-accumulate** в виде процедуры. 
> Покажите, как с помощью **filtered-accumulate** выразить следующее:
> 
> а. сумму квадратов простых чисел в интервале от **a** до **b** 
> (в предположении, что процедура **prime?** уже написана);
> 
> б. произведение всех положительных целых чисел меньше **n**, 
> которые просты по отношению к **n** (то есть всех таких положительных целых чисел **i < n**, что **НОД(i, n) = 1**).

Решение на Scala:

```scala
def filteredAccumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  if a > b then nullValue
  else
    val nextA = term(a)
    lazy val res =
      filteredAccumulateRec(combiner, nullValue, term, next(a), next, b, filter)
    if filter(nextA) then combiner(nextA, res)
    else res

def filteredAccumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else
      val nextA = term(a)
      val nextResult =
        if filter(nextA) then combiner(result, nextA)
        else result
      iter(next(a), nextResult)
  iter(a, nullValue)

def filteredAccumulate(
    combiner: (Int, Int) => Int,
    nullValue: Int,
    term: Int => Int,
    a: Int,
    next: Int => Int,
    b: Int,
    filter: Int => Boolean
): Int = ???

def isPrime(n: Int): Boolean = ???

def sumPrimeSquares(a: Int, b: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x + y,
    nullValue = 0,
    term = x => x * x,
    a = a,
    next = x => x + 1,
    b = b,
    filter = isPrime
  )

def gcd(a: Int, b: Int): Int = ???

def productPrimesLessThanN(n: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x * y,
    nullValue = 1,
    term = x => x,
    a = 1,
    next = x => x + 1,
    b = n - 1,
    filter = i => gcd(i, n) == 1
  )
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-33.worksheet.sc)


---

**Ссылки:**

- [Упражнение 1.29 - 1.33](https://web.mit.edu/6.001/6.037/sicp.pdf#page=108)
