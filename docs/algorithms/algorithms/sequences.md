# Последовательности

## Числа Фибоначчи

[Числа Фибоначчи](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8) — 
элементы числовой последовательности `0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...`,
в которой первые два числа равны `0` и `1`, а каждое последующее число равно сумме двух предыдущих чисел.

Члены этой последовательности обозначаются через \\(F\_{n}\\).
Формальное определение: \\(F\_{0} = 0; F\_{1} = 1; F\_{n + 2} = F\_{n+1} + F\_{n}, n \geq 0.\\)

### Некоторые свойства

- **Формула 1**: \\(F\_{n+1}F\_{n-1} - F\_{n}^{2} = (-1)^{n}\\)
- Из **формулы 1** следует, что числа \\(F\_{n}\\) и \\(F\_{n+1}\\) являются взаимно простыми, 
  так как любой их общий делитель должен быть также делителем \\((−1)^{n}\\).
- **Формула 2**: \\(\begin{pmatrix} F\_{n+1} & F\_{n} \\\\ F\_{n} & F\_{n-1} \\\\ \end{pmatrix} = \begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\\\ \end{pmatrix}^{n}\\)
- **Формула 3**: \\(F\_{n+m} = F\_{m}F\_{n+1} + F\_{m-1}F\_{n}\\)
- **Теорема A** (Э. Люка, 1876). Некоторое целое число делит и \\(F\_{m}\\), и \\(F\_{n}\\) тогда и только тогда, 
  когда оно является делителем \\(F\_{d}\\), где \\(d = gcd(m, n)\\); в частности, \\(gcd(F\_{m}, F\_{n}) = F\_{gcd(m,n)}\\).
- **Формула 4**: \\(F\_{n} = \frac{1}{\sqrt{5}}(\phi^{n} - \hat{\phi}^{n})\\), 
  где \\(\phi = \frac{1 + \sqrt{5}}{2}\\) и \\(\hat{\phi} = 1 - \phi = \frac{1 - \sqrt{5}}{2}\\).
- **Формула 5**: \\(F\_{n} = \frac{\phi^{n}}{\sqrt{5}}\\), округленное до ближайшего целого числа.
- **Формула 6**: \\(\sum\_{k=0}^{n}F\_{k}F\_{n-k} = \frac{n - 1}{5}F\_{n} + \frac{2n}{5}F\_{n-1}\\)


### Точное значение

> Вычислить n-е число Фибоначчи.

**Алгоритм:**

- **Шаг 1** (Базовый случай). Если `n` равно `0`, то возвращаем `0` (первое число Фибоначчи).
- **Шаг 2** (Определение переменных).
    - `a`: Предыдущее число Фибоначчи \\(F\_{n-2}\\). Начальное значение: \\(F\_0 = 0\\).
    - `b`: Текущее число Фибоначчи \\(F\_{n-1}\\). Начальное значение: \\(F\_1 = 1\\).
    - `count`: Количество оставшихся итераций. Начальное значение: `n - 1`.
- **Шаг 3** (Итерация). 
    - Если `count` равно `0`, то возвращаем `b` (текущее число Фибоначчи).
    - Иначе, возвращаемся на предыдущий шаг с новыми значениями:
        - `a` присваиваем значение `b` (следующее число Фибоначчи становится предыдущим).
        - `b` присваиваем значение `a + b` (вычисляем следующее число Фибоначчи).
        - Уменьшаем `count` на `1`.

**Пример:**

Пример выполнения алгоритма для `n = 5`

- **Инициализация:**: `n = 5`
- **Первая итерация:**: `a = 0`, `b = 1`, `count = 4`
- **Вторая итерация:**: `a = 1`, `b = 1`, `count = 3`
- **Третья итерация:**: `a = 1`, `b = 2`, `count = 2`
- **Четвертая итерация:**: `a = 2`, `b = 3`, `count = 1`
- **Пятая итерация:**: `a = 3`, `b = 5`, `count = 0`
- **Результат:**: 5-ое число Фибоначчи равно `5`.

**Оценка:**

Алгоритм имеют линейную сложность.

- **Время** - O(n)
- **Память** - O(1)

**Код:**

> Алгоритм основан на хвостовой рекурсии, что позволяет избежать переполнения стека при больших значениях `n`.

```dotty
def iterativeFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, count: Int): BigInt =
    if count == 0 then b
    else loop(b, a + b, count - 1)

  if n == 0 then BigInt(0) else loop(BigInt(0), BigInt(1), n - 1)

iterativeFib(100)
// 354224848179261915075  
```

**Метрики:**

Вычисление миллионного числа Фибоначчи занимает 11 секунд.

```dotty
iterativeFib(1_000_000)
      
// Число тестовых запусков = 10
// Среднее время = 11,298 секунд  
```

### Числа Фибоначчи с логарифмической сложностью

> Вычислить число Фибоначчи с логарифмической сложностью

**Алгоритм:**

Алгоритм взят из **Exercise 1.19** из [SICP][sicp]:

- Трансформация переменных состояния a и b: \\(\acute{a} ← a + b\\) и \\(\acute{b} ← a\\). 
- Назовем эту трансформацию T и заметим, что n-кратное применение T, начиная с 1 и 0, 
  дает нам пару \\(F\_{n+1}\\) и \\(F\_{n}\\). 
  Другими словами, числа Фибоначчи получаются путем применения \\(T^{n}\\), n-ой степени трансформации T, к паре (1,0). 
- Теперь рассмотрим T как частный случай p = 0, q = 1 в семействе трансформаций \\(T\_{pq}\\), 
  где \\(T\_{pq}\\) преобразует пару (a,b) по правилу \\(a \leftarrow bq + aq + ap\\), \\(b \leftarrow bp + aq\\). 
- Двукратное применение трансформации \\(T\_{pq}\\) равносильно однократному применению трансформации
  \\(T\_{\acute{p}\acute{q}}\\) того же типа, где \\(\acute{p} = p^{2} + q^{2}\\) и \\(\acute{q} = 2pq + q^{2}\\)
- Это дает прямой способ возводить такие трансформации в квадрат, и таким образом, можно вычислить \\(T^{n}\\)
  с помощью последовательного возведения в квадрат

**Пример:**

Рассмотрим пример выполнения кода для вычисления 10-го числа Фибоначчи (`n = 10`).

- **Шаг 1: Инициализация**
    - `a = 1`, `b = 0`, `p = 0`, `q = 1`, `count = 10`
- **Шаг 2: Итеративный процесс**
    - **Итерация 1:**
        - Проверка условия `count == 0`: `10 != 0` — продолжаем.
        - Проверка условия `count % 2 == 0`: `10 % 2 == 0` — условие истинно.
        - Вызов цикла с новыми значениями:
        - `a = 1`, `b = 0`, `p = p * p + q * q = 0 * 0 + 1 * 1 = 1`, `q = 2 * p * q + q * q = 2 * 0 * 1 + 1 * 1 = 1`, `count = count / 2 = 10 / 2 = 5`
    - **Итерация 2:**
       - Проверка условия `count == 0`: `5 != 0` — продолжаем.
       - Проверка условия `count % 2 == 0`: `5 % 2 != 0` — условие ложно.
       - Вызов цикла с новыми значениями:
        - `a = b * q + a * (p + q) = 0 * 1 + 1 * (1 + 1) = 2`, `b = b * p + a * q = 0 * 1 + 1 * 1 = 1`, `p = 1`, `q = 1`, `count = count - 1 = 5 - 1 = 4`
    - **Итерация 3:**
        - Проверка условия `count == 0`: `4 != 0` — продолжаем.
        - Проверка условия `count % 2 == 0`: `4 % 2 == 0` — условие истинно.
        - Вызов цикла с новыми значениями:
        - `a = 2`, `b = 1`, `p = p * p + q * q = 1 * 1 + 1 * 1 = 2`, `q = 2 * p * q + q * q = 2 * 1 * 1 + 1 * 1 = 3`, `count = count / 2 = 4 / 2 = 2`
    - **Итерация 4:**
        - Проверка условия `count == 0`: `2 != 0` — продолжаем.
        - Проверка условия `count % 2 == 0`: `2 % 2 == 0` — условие истинно.
        - Вызов цикла с новыми значениями:
        - `a = 2`, `b = 1`, `p = p * p + q * q = 2 * 2 + 3 * 3 = 13`, `q = 2 * p * q + q * q = 2 * 2 * 3 + 3 * 3 = 21`, `count = count / 2 = 2 / 2 = 1`
    - **Итерация 5:**
        - Проверка условия `count == 0`: `1 != 0` — продолжаем.
        - Проверка условия `count % 2 == 0`: `1 % 2 != 0` — условие ложно.
        - Вызов цикла с новыми значениями:
        - `a = b * q + a * (p + q) = 1 * 21 + 2 * (13 + 21) = 109`, `b = b * p + a * q = 1 * 13 + 2 * 21 = 55`, `p = 13`, `q = 21`, `count = count - 1 = 1 - 1 = 0`
    - **Итерация 6:**
        -  Проверка условия `count == 0`: `0 == 0` — условие истинно.
        -  Возврат значения `b = 55`.

**Оценка:**

- **Время** - O(log(n))
- **Память** - O(1)

**Код:**

```dotty
def fastFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, p: BigInt, q: BigInt, count: Int): BigInt =
    if count == 0 then b
    else if count % 2 == 0 then
      loop(a, b, p * p + q * q, 2 * p * q + q * q, count / 2)
    else loop(b * q + a * (p + q), b * p + a * q, p, q, count - 1)

  loop(1, 0, 0, 1, n)

fastFib(100)
// 354224848179261915075  
```

**Метрики:**

Вычисление миллионного числа Фибоначчи занимает меньше секунды.

```dotty
fastFib(1_000_000)
      
// Число тестовых запусков = 10
// Среднее время = 0,100 секунд     
```

### Приближенное значение

**Алгоритм:**

Приблизительное значение числа Фибоначчи можно вычислить [по формуле Бине](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

\\(F\_{n} = \frac{(\frac{1+\sqrt{5}}{2})^{n} - (\frac{1-\sqrt{5}}{2})^{n}}{\sqrt{5}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{\varphi - (-\varphi)^{-1}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{2\varphi - 1}\\)

**Оценка:**

- **Время** - O(log n)
- **Память** - O(1)

**Код:**

```dotty
private val PHI: BigDecimal = BigDecimal((1.0 + math.sqrt(5.0)) / 2.0)

def approximateFibonacci(n: Int): BigInt =
  (PHI.pow(n) / BigDecimal(math.sqrt(5)))
    .setScale(0, BigDecimal.RoundingMode.HALF_UP)
    .toBigInt
```

**Метрики:**

Вычисление приближенного значения миллионного числа Фибоначчи занимает меньше секунды.

```dotty
approximateFibonacci(1_000_000)
      
// Число тестовых запусков = 10
// Среднее время = 0,044 секунд  
```

## Пифагорова тройка

> [Пифагорова тройка](https://ru.wikipedia.org/wiki/%D0%9F%D0%B8%D1%84%D0%B0%D0%B3%D0%BE%D1%80%D0%BE%D0%B2%D0%B0_%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B0) — 
> это набор трех натуральных чисел \\(a < b < c\\), 
> для которых \\(a^{2} + b^{2} = c^{2}\\).
> Представим \\(a = m^{2} - n^{2}, b = 2\*m\*n, c = m^{2} + n^{2}\\). Тогда \\((m,n)\\) – пифагорова двойка.
> 
> [Дерево примитивных пифагоровых троек](https://ru.wikipedia.org/wiki/%D0%94%D0%B5%D1%80%D0%B5%D0%B2%D0%BE_%D0%BF%D1%80%D0%B8%D0%BC%D0%B8%D1%82%D0%B8%D0%B2%D0%BD%D1%8B%D1%85_%D0%BF%D0%B8%D1%84%D0%B0%D0%B3%D0%BE%D1%80%D0%BE%D0%B2%D1%8B%D1%85_%D1%82%D1%80%D0%BE%D0%B5%D0%BA)
> можно построить используя как пифагорову двойку, так и пифагорову тройку.

**Код:**

Пифагорова двойка:

```dotty
final case class PythagoreanDeuce(m: Long, n: Long):
  lazy val a: Long                     = m * m - n * n
  lazy val b: Long                     = 2 * m * n
  lazy val c: Long                     = m * m + n * n
  lazy val triplet: PythagoreanTriplet = PythagoreanTriplet(a, b, c)
  lazy val nextPythagoreanDeuce: List[PythagoreanDeuce] =
    List(
      PythagoreanDeuce(2 * m - n, m),
      PythagoreanDeuce(2 * m + n, m),
      PythagoreanDeuce(m + 2 * n, n)
    )
end PythagoreanDeuce

object PythagoreanDeuce:
  val primitivePythagoreanDeuce: PythagoreanDeuce = PythagoreanDeuce(2, 1)

import PythagoreanDeuce.primitivePythagoreanDeuce

primitivePythagoreanDeuce.triplet
// PythagoreanTriplet(3,4,5)
primitivePythagoreanDeuce.nextPythagoreanDeuce
// List(PythagoreanDeuce(3,2), PythagoreanDeuce(5,2), PythagoreanDeuce(4,1))
```

Пифагорова тройка:

```dotty
final case class PythagoreanTriplet(a: Long, b: Long, c: Long):
  lazy val nextPythagoreanTriplet: List[PythagoreanTriplet] =
    List(
      PythagoreanTriplet(
        a - 2 * b + 2 * c,
        2 * a - b + 2 * c,
        2 * a - 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        a + 2 * b + 2 * c,
        2 * a + b + 2 * c,
        2 * a + 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        -a + 2 * b + 2 * c,
        -2 * a + b + 2 * c,
        -2 * a + 2 * b + 3 * c
      )
    )
end PythagoreanTriplet

object PythagoreanTriplet:
  val primitivePythagoreanTriplet: PythagoreanTriplet =
    PythagoreanTriplet(3, 4, 5)

import PythagoreanTriplet.primitivePythagoreanTriplet

primitivePythagoreanTriplet.nextPythagoreanTriplet
// List(PythagoreanTriplet(5,12,13), PythagoreanTriplet(21,20,29), PythagoreanTriplet(15,8,17))
```

Нахождение [пифагоровых троек с заданной суммой](https://projecteuler.net/problem=9):

```dotty
def pythagoreanTripletsWithGivenSum(sum: Long): Array[PythagoreanTriplet] =
  if sum % 2 == 1 then new Array(0)
  else
    val arrayBuffer = mutable.ArrayBuffer.empty[PythagoreanTriplet]
    val s2          = sum / 2
    val sqrt        = math.sqrt(s2.toDouble).toLong
    val mLimit      = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    for m <- 2L to mLimit do
      if s2 % m == 0 then
        // сократим пространство поиска, удалив все делители 2
        val sm = reduceLoop(s2 / m, 2)

        @tailrec
        def loop(k: Long): Unit =
          if k >= 2 * m || k > sm then ()
          else if sm % k == 0 && gcd(k, m) == 1 then
            val d = s2 / (k * m)
            val n = k - m
            val a = d * (m * m - n * n)
            val b = 2 * d * m * n
            val c = d * (m * m + n * n)
            arrayBuffer += PythagoreanTriplet(a, b, c)
            loop(k + 2)
          else loop(k + 2)

        loop(if m % 2 == 1 then m + 2 else m + 1)

    arrayBuffer.toArray

Array(PythagoreanTriplet(375,200,425))    
```

**Метрики:**

Вычисление всех пифагоровых троек, сумма которых равна 1000 выполняется несколько миллисекунд.

```dotty
pythagoreanTripletsWithGivenSum(1000).mkString("Array(", ", ", ")")
     
// Число тестовых запусков = 10
// Среднее время = 7,096 миллисекунд   
```

## Циклические последовательности

Циклические последовательности:

- Треугольные числа; \\(T\_{n} = \frac{n(n+1)}{2}\\); 1, 3, 6, 10, 15, ...
- Квадратные числа; \\(P\_{4,n} = n^{2}\\); 1, 4, 9, 16, 25, ...
- Пятиугольные числа; \\(P\_{5,n} = \frac{n(3n−1)}{2}\\); 1, 5, 12, 22, 35, ...
- Шестиугольные числа; \\(H\_{n} = n(2n−1)\\); 1, 6, 15, 28, 45, ...
- Семиугольные числа; \\(P\_{7,n} = \frac{n(5n−3)}{2}\\); 1, 7, 18, 34, 55, ...
- Восьмиугольные числа; \\(P\_{8,n} = n(3n−2)\\); 1, 8, 21, 40, 65, ...

**Код:**

```dotty
object CyclicNumbers:
  def triangleNumber(n: Long): Long = n * (n + 1) / 2

  def isTriangle(n: Long): Boolean =
    val sol = math.round(-1.0 / 2.0 + math.sqrt(2.0 * n + 1.0 / 4.0))
    triangleNumber(sol) == n

  def squareNumber(n: Long): Long = n * n

  def isSquare(n: Long): Boolean = math.sqrt(n.toDouble).isWhole

  def pentagonalNumber(n: Long): Long = n * (3 * n - 1) / 2

  def isPentagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 6.0 + math.sqrt(2.0 * n / 3.0 + 1.0 / 36.0))
    pentagonalNumber(sol) == n

  def hexagonalNumber(n: Long): Long = n * (2 * n - 1)

  def isHexagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 4.0 + math.sqrt(n / 2.0 + 1.0 / 16.0))
    hexagonalNumber(sol) == n

  def heptagonalNumber(n: Long): Long = n * (5 * n - 3) / 2

  def isHeptagonal(n: Long): Boolean =
    val sol = math.round(3.0 / 10.0 + math.sqrt(2.0 * n / 5.0 + 9.0 / 100.0))
    heptagonalNumber(sol) == n

  def octagonalNumber(n: Long): Long = n * (3 * n - 2)

  def isOctagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 3.0 + math.sqrt(n / 3.0 + 1.0 / 9.0))
    octagonalNumber(sol) == n
end CyclicNumbers

triangleNumber(1_000_000)    // 500000500000
squareNumber(1_000_000)      // 1000000000000
pentagonalNumber(1_000_000)  // 1499999500000
hexagonalNumber(1_000_000)   // 1999999000000
heptagonalNumber(1_000_000)  // 2499998500000
octagonalNumber(1_000_000)   // 2999998000000
```

## Гипотеза Коллатца

> Берём любое натуральное число n. Если оно чётное, то делим его на 2, 
> а если нечётное, то умножаем на 3 и прибавляем 1 (получаем 3n + 1). 
> Над полученным числом выполняем те же самые действия, и так далее.
>
> [Гипотеза Коллатца](https://ru.wikipedia.org/wiki/%D0%93%D0%B8%D0%BF%D0%BE%D1%82%D0%B5%D0%B7%D0%B0_%D0%9A%D0%BE%D0%BB%D0%BB%D0%B0%D1%82%D1%86%D0%B0)
> заключается в том, что какое бы начальное число n мы ни взяли, рано или поздно мы получим единицу.

**Алгоритм:**

Подсчитать `collatz` - количество итераций до получения `1` от заданного `n`:

- Если `n` - четное, то `collatz(n) = collatz(n / 2) + 1`
- Если `n` - нечетное, то `3n + 1` - четное (и не равно `1`), то `collatz(n) = collatz((3 * n + 1) / 2) + 2`

**Пример:**

Рассмотрим, как работает этот алгоритм на примере числа `6`.

- Начнем с `n = 6`.
- Поскольку `6` четное, мы делим его на `2`: `n = 6 / 2 = 3`. Увеличиваем счетчик на `1` (`count = 1`).
- Теперь у нас `n = 3`, которое нечетное. Применяем формулу `3n + 1: n = (3 * 3 + 1) / 2 = 5`. Увеличиваем счетчик на `2` (`count = 3`).
- Теперь у нас `n = 5`, которое нечетное. Применяем формулу: `n = (3 * 5 + 1) / 2 = 8`. Увеличиваем счетчик на `2` (`count = 5`).
- Теперь `n = 8`, четное. Делим на `2`: `n = 8 / 2 = 4`. Увеличиваем счетчик на `1` (`count = 6`).
- `n = 4`, четное. Делим на `2`: `n = 4 / 2 = 2`. Увеличиваем счетчик на `1` (`count = 7`).
- `n = 2`, четное. Делим на `2`: `n = 2 / 2 = 1`. Увеличиваем счетчик на `1` (`count = 8`).
- Теперь мы достигли `n = 1`, и алгоритм завершает выполнение. 
- Общее количество шагов (или итераций), необходимых для достижения `1`, составляет `8`.

**Оценка:**

- Временная сложность:
    - Худший случай: Последовательность Коллатца может расти экспоненциально до достижения 1. 
      В худшем случае временная сложность алгоритма может быть экспоненциальной \\(O(2^{n})\\), но это зависит от конкретных значений n.
    - Средний случай: В среднем, временная сложность алгоритма оценивается как O(log n), 
      так как последовательность Коллатца обычно быстро сходится к 1.

Итого:

- **Время** - в среднем - O(log n), в худшем - \\(O(2^{n})\\)
- **Память** - O(1)

**Код:**

```dotty
@tailrec
def nextCollatz(n: Long, count: Long = 0): Long =
  if n == 1 then count
  else if n % 2 == 0 then nextCollatz(n / 2, count + 1)
  else nextCollatz((3 * n + 1) / 2, count + 2)

nextCollatz(27)
// 111    
```

## Числа Хэмминга

> Положительное число number является обобщенным числом Хэмминга типа n, 
> если все его простые делители не превышают n.

**Алгоритм:**

- **Шаг 1** (Проверка начальных условий). 
    - Если number меньше 2 или меньше или равно n, то это число Хэмминга, т.к. все простые делители числа меньше или равны n.
    - Если n меньше 2, то number - не число Хэмминга, так как не существует простых чисел меньше 2.
- **Шаг 2** (Получение списка простых чисел). 
    - Сгенерировать список простых чисел до n с помощью ["решета Эратосфена"](number_theory/primes.md).
- **Шаг 3** (Проверка простых делителей).
    - Для каждого простого числа в списке:
        - Если простое число больше n, то number не является числом Хэмминга.
        - Если number делится на простое число, то сокращаем его. Присваиваем number значения после сокращения.
        - Если number стал равен 1, то это число Хэмминга.
        - Если number все ещё больше 1, то повторяем шаг 3 для следующего простого числа.

**Пример:**

Рассмотрим пример для числа 30 и n = 5:

- **Шаг 1**: Проверка начальных условий: number = 30, n = 5
    - 30 больше 2 и больше 5.
- **Шаг 2**: Получение списка простых чисел
    - Генерируем список простых чисел до 5. Это: 2, 3, 5.
- **Шаг 3**: Проверка простых делителей
    - Первое простое число: 2. 30 делится на 2: 30 / 2 = 15. Теперь number = 15.
    - Второе простое число: 3. 15 делится на 3: 15 / 3 = 5. Теперь number = 5.
    - Третье простое число: 5. 5 делится на 5: 5 / 5 = 1. Теперь number = 1.
- **Заключение**: Поскольку number стал равен 1, мы можем заключить, что 30 является числом Хэмминга относительно n = 5.

**Оценка:**

- Ассимптотическая сложность
- Генерация простых чисел: Если использовать решето Эратосфена, то это \\(O(n \log \log n)\\).
- Цикл: В худшем случае для каждого простого числа мы можем выполнить деление, что в общем случае может занять \\(O(\log(\text{number}))\\) для каждого простого числа. 
  Если k — количество простых чисел до n, то это будет \\(O(k \cdot \log(\text{number}))\\).
- Таким образом, общая ассимптотическая сложность алгоритма будет \\(O(n \log \log n + k \cdot \log(\text{number}))\\), где k — количество простых чисел до n.

Итого:

- **Время** - \\(O(n \log \log n + k \cdot \log(\text{number}))\\), где k — количество простых чисел до n.
- **Память** - \\(O(k)\\).

**Код:**

```dotty
def isHammingNumber(number: Long, n: Int): Boolean =
  if number < 2 || number <= n then true
  else if n < 2 then false
  else
    val primesList = primesNoMoreThanN(n)

    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    @tailrec
    def loop(n: Long, i: Int): Boolean =
      if i >= primesList.length then false
      else
        val newN = reduceLoop(n, primesList(i))
        if newN == 1 then true
        else loop(newN, i + 1)

    loop(number, 0)

private def primesNoMoreThanN(n: Int): Array[Int] = ... // Описано в главе "Факторизация"
    
isHammingNumber(2125764000, 5)
// true    
```

## Числа харшад

**Алгоритм:**

[_Числа харшад_](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D1%85%D0%B0%D1%80%D1%88%D0%B0%D0%B4), 
или числа Нивена, — натуральные числа, делящиеся нацело на сумму своих цифр. 
Таким числом является, например, 1729, так как 1729 = (1 + 7 + 2 + 9) × 91.

_Сильное число Харшада_ — это число Харшада, которое при делении на сумму своих цифр дает простое число.

201 — это число Харшада, поскольку оно делится на 3 (сумму своих цифр). 
Когда мы отсекаем последнюю цифру от 201, то получаем 20, что является числом Харшада. 
Когда мы отсекаем последнюю цифру от 20, то получаем 2, что также является числом Харшада. 
Назовем число Харшада, которое при рекурсивном отсечении последней цифры всегда приводит к числу Харшада, 
_отсекаемым вправо числом Харшада_.

Возьмем число 2011, которое является простым. 
Если отсечь из него последнюю цифру, то получаем 201 - сильное число Харшада, которое также отсекается вправо. 
Назовем такие простые числа _сильными, отсекаемыми справа простыми числами Харшада_.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
object HarshadNumber:
  def isHarshadNumber(number: Long): Boolean = number % sumOfDigits(number) == 0

  def isStrongHarshadNumber(number: Long): Boolean =
    val sod = sumOfDigits(number)
    number % sod == 0 && isPrime(number / sod)

  def getStrongRightTruncatableHarshadPrimes(p: Int): Seq[Long] =
    var harshadNumbers = Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
    (1 until p - 1).flatMap { _ =>
      harshadNumbers = rightTruncatableHarshadNumbers(harshadNumbers)
      harshadNumbers
        .withFilter(isStrongHarshadNumber)
        .flatMap(strongRightTruncatableHarshadPrimes)
    }

  private def strongRightTruncatableHarshadPrimes(
      strongHarshadNumber: Long
  ): Seq[Long] =
    (1 to 9 by 2)
      .withFilter(i => isPrime(strongHarshadNumber * 10 + i))
      .map(strongHarshadNumber * 10 + _)

  private def rightTruncatableHarshadNumbers(
      harshadNumbers: Seq[Long]
  ): Seq[Long] =
    harshadNumbers.flatMap(rightTruncatableHarshadNumbers)

  private def rightTruncatableHarshadNumbers(harshadNumber: Long): Seq[Long] =
    val sod       = sumOfDigits(harshadNumber)
    val newNumber = harshadNumber * 10
    (0 to 9)
      .withFilter(i => (newNumber + i) % (sod + i) == 0)
      .map(newNumber + _)

  private def sumOfDigits(number: BigInt): Int =
    number.toString.foldLeft(0)((s, ch) => s + ch.asDigit)
end HarshadNumber
    
getStrongRightTruncatableHarshadPrimes(3)
// Vector(181, 211, 271, 277, 421, 457, 631)    
```

**Метрики:**

Вычисление всех сильных, усекаемых справа простых чисел Харшада меньше \\(10^{9}\\)
занимает меньше секунды.

```dotty
@main def getStrongRightTruncatableHarshadPrimesBench(): Unit =
  println(getStrongRightTruncatableHarshadPrimes(9))
      
// Vector(181, 211, 271, 277, 421, 457, 631, 2011, 2017, 2099, 2473, 2477, 4021, 4027, 4073, 4079, 4231, 4813, 4817, 
// 6037, 8011, 8017, 8039, 8461, 8467, 20071, 20431, 40867, 48091, 84061, 84067, 400237, 400277, 4008271, 4860013, 
// 40000021, 80402071, 200400073, 200400077, 240840013, 400002073, 480006031)
      
// CPU Time: 564 ms
// Allocation memory size: 13,65 MB      
```

## Числа Бернулли

**Алгоритм:**

[Числа Бернулли](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%91%D0%B5%D1%80%D0%BD%D1%83%D0%BB%D0%BB%D0%B8)

Для чисел Бернулли существует следующая рекуррентная формула:

- \\(B\_{0} = 1\\)
- \\(B\_{n}={\frac{-1}{n+1}}\sum\_{k=1}^{n}{\binom {n+1}{k+1}}B\_{n-k}, \quad n \in \mathbb {N} \\)

Все числа Бернулли с нечётными номерами, кроме \\(B\_{1} = -\frac{1}{2}\\), равны нулю, 
а знаки чисел Бернулли с чётными номерами чередуются.

Поэтому формулу вычисления \\(B\_{n}\\) для четных \\(n \geq 2\\) можно упростить:
\\(B\_{n} = \frac{1}{2} + {\frac{-1}{n+1}}\sum\_{k=2,4,...}^{n}{\binom {n+1}{k+1}}B\_{n-k}, \quad n \in \mathbb {N} \\)

[Генератор чисел Бернулли](https://ru.numberempire.com/bernoullinumbers.php)

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
import scalaz.Memo
  
val bernoulli: Int => Rational =
  Memo.mutableHashMapMemo {
    case 0 => Rational(1)
    case 1 => Rational(-1, 2)
    case n if n % 2 == 1 => Rational(0)
    case n =>
      val s =
        (2 to n by 2)
          .map: l =>
            bernoulli(n - l) * Rational(binomialCoefficient(n + 1, l + 1))
          .sum
      Rational(1, 2) - (s / (n + 1))
  }
        
bernoulli(50)
// 495057205241079648212477525/66 
```

**Метрики:**

500 число Бернулли рассчитывается за 1,5 секунды.

```dotty
@main def bernoulliBench(): Unit =
  println(bernoulli(500))
     
// CPU Time: 1500 ms
// Allocation memory size: 1,37 MB      
```

## Числа Падована

**Алгоритм:**

[Последовательность Падована (или числа Падована)](https://oeis.org/A000931) рассчитывается по формуле:

- \\(a\_{0} = 1, a\_{1} = a\_{2} = 0\\)
- \\(a\_{n} = a\_{n-2} + a\_{n-3}\\)

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def padovan(n: Int): SafeLong =
  @tailrec
  def loop(a: SafeLong, b: SafeLong, c: SafeLong, i: Int): SafeLong =
    if i == 0 then c
    else loop(b, c, a + b, i - 1)
  if n == 0 then 1
  else if n <= 2 then 0
  else loop(1, 0, 0, n - 2)
    
padovan(100)
// 288627200960 
```

**Метрики:**

Миллионное число Падована вычисляется за 12,5 секунд.

```dotty
@main def padovanBench(): SafeLong =
  padovan(1_000_000)
     
// CPU Time: 12576 ms
// Allocation memory size: 26,2 GB      
```

## Числа Якобсталя

**Алгоритм:**

[Последовательность Якобсталя (или числа Якобсталя)](https://oeis.org/A001045): 
\\(a\_{n} = a\_{n-1} + 2*a\_{n-2}\\), где \\(a\_{0} = 0, a\_{1} = 1\\); 
также \\(a\_{n}\\) - ближайшее целое число к \\(\frac{2^n}{3}\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def jacobsthal(i: Int): SafeLong =
  @tailrec
  def loop(a: SafeLong, b: SafeLong, i: Int): SafeLong =
    if i == 0 then b
    else loop(b, b + 2 * a, i - 1)
  if i <= 1 then i
  else loop(0, 1, i - 1)
    
jacobsthal(100)
// 422550200076076467165567735125
```

**Метрики:**

Миллионное число Якобсталя вычисляется за 55 секунд.

```dotty
@main def jacobsthalBench(): SafeLong =
  jacobsthal(1_000_000)
     
// CPU Time: 55346 ms
// Allocation memory size: 127,42 GB      
```

## Числа Пелля

**Алгоритм:**

[Числа Пелля](https://oeis.org/A000129):
\\(a\_{n} = 2*a\_{n-1} + a\_{n-2}\\), где \\(a\_{0} = 0, a\_{1} = 1\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def pell(i: Int): SafeLong =
  @tailrec
  def loop(a: SafeLong, b: SafeLong, i: Int): SafeLong =
    if i == 0 then b
    else loop(b, 2 * b + a, i - 1)
  if i <= 1 then i
  else loop(0, 1, i - 1)
    
pell(100)
// 66992092050551637663438906713182313772
```

**Метрики:**

Миллионное число Пелля вычисляется за 72 секунды.

```dotty
@main def pellBench(): SafeLong =
  pell(1_000_000)
     
// CPU Time: 71481 ms
// Allocation memory size: 160,09 GB      
```

## Числа Трибоначчи

**Алгоритм:**

[Числа Трибоначчи](https://oeis.org/A000073): 
\\(a\_{n} = a\_{n-1} + a\_{n-2} + a\_{n-3}\\) для \\(n \geq 3\\), с \\(a\_{0} = a\_{1} = 0, a\_{2} = 1\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def tribonacci(i: Int): SafeLong =
  @tailrec
  def loop(a: SafeLong, b: SafeLong, c: SafeLong, i: Int): SafeLong =
    if i == 0 then c
    else loop(b, c, a + b + c, i - 1)

  if i <= 1 then 0
  else if i == 2 then 1
  else loop(0, 0, 1, i - 2)
    
tribonacci(100)
// 53324762928098149064722658  
```

**Метрики:**

Миллионное число Трибоначчи вычисляется за 55 секунд.

```dotty
@main def tribonacciBench(): SafeLong =
  tribonacci(1_000_000)
     
// CPU Time: 54891 ms
// Allocation memory size: 112,9 GB      
```

## Числа Тетраначчи

**Алгоритм:**

[Числа Тетраначчи](https://oeis.org/A000078):
\\(a\_{n} = a\_{n-1} + a\_{n-2} + a\_{n-3} + a\_{n-4}\\) 
для \\(n \geq 4\\), с \\(a\_{0} = a\_{1} = a\_{2} = 0, a\_{3} = 1\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def tetranacci(i: Int): SafeLong =
  @tailrec
  def loop(
      a: SafeLong,
      b: SafeLong,
      c: SafeLong,
      d: SafeLong,
      i: Int
  ): SafeLong =
    if i == 0 then d
    else loop(b, c, d, a + b + c + d, i - 1)

  if i <= 2 then 0
  else if i == 3 then 1
  else loop(0, 0, 0, 1, i - 3)
    
tetranacci(100)
// 2505471397838180985096739296
```

**Метрики:**

Миллионное число Тетраначчи вычисляется за 80 секунд.

```dotty
@main def tetranacciBench(): SafeLong =
  tetranacci(1_000_000)
     
// CPU Time: 79549 ms
// Allocation memory size: 182,55 GB      
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.8](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
