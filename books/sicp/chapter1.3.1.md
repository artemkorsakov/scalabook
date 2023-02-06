# Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.1. Процедуры в качестве аргументов

#### Упражнение 1.29

> Правило Симпсона — более точный метод численного интегрирования, чем представленный выше. 
> С помощью правила Симпсона интеграл функции **f** между **a** и **b** приближенно вычисляется в виде 
> **(h / 3) * [y<sub>0</sub> + 4y<sub>1</sub> + 2y<sub>2</sub> + 4y<sub>3</sub> + 2y<sub>4</sub> + . . . + 2y<sub>n-2</sub> + 4y<sub>n-1</sub> + y<sub>n</sub>]** 
> , где **h = (b − a)/n**, для какого-то четного целого числа **n**, а **y<sub>k</sub> = f(a + kh)**. 
> (Увеличение **n** повышает точность приближенного вычисления.) 
> Определите процедуру, которая принимает в качестве аргументов **f**, **a**, **b** и **n**, 
> и возвращает значение интеграла, вычисленное по правилу Симпсона. 
> С помощью этой процедуры проинтегрируйте **cube** между **0** и **1** (с **n = 100** и **n = 1000**) 
> и сравните результаты с процедурой **integral**, приведенной выше.


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

> Процедура **sum** порождает линейную рекурсию. 
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

#### Упражнение 1.32

#### Упражнение 1.33


---

**Ссылки:**
- [Упражнение 1.29 - 1.33](https://web.mit.edu/6.001/6.037/sicp.pdf#page=108)
