# Численные алгоритмы

> Для снятия метрик использовалась библиотека [Metrics-Scala](https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Manual.md).

## Алгоритм Евклида

> Даны два целых положительных числа `m` и `n`. 
> Требуется найти их наибольший общий делитель, т.е. наибольшее целое положительное число, 
> которое нацело делит оба числа `m` и `n`.

**Алгоритм:**

- **Шаг 1** (Сравнение с нулем). Если `n = 0`, то выполнение алгоритма прекращается; `m` — искомое значение.
- **Шаг 2** (Замещение). Присвоить `r ← m % n, m ← n, n ← r` и вернуться к **шагу 1**.

**Пример:**

\\((m = 4851, n = 3003) \longmapsto (m = 3003, n = 1848) \longmapsto (m = 1848, n = 1155) \longmapsto\\)
\\((m = 1155, n = 693) \longmapsto (m = 693, n = 462) \longmapsto (m = 462, n = 231) \longmapsto (m = 231, n = 0)\\)

**Пояснение:** 

Наибольший общий делитель (НОД) с помощью алгоритма Евклида вычисляется исходя из следующих соображений:

- `НОД(r, 0) = r` для любого ненулевого `r`, так как `0` делится на любое целое число.
- Пусть `a = b*q + r`, тогда `НОД (a, b) = НОД (b, r)`, так как `НОД (b, r)` делит `b*q + r`.

**Оценка:**

- **Время** - O(log n)
- **Память** - O(1)

Временная сложность алгоритма Евклида составляет: \\(O(\log(\min(a, b)))\\), где a и b — это исходные числа.
Это связано с тем, что каждое деление уменьшает одно из чисел как минимум в два раза (в среднем), 
что приводит к логарифмическому количеству шагов.

**Код:**

```dotty
@annotation.tailrec
final def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)
  
gcd(4851, 3003)
// 231
```

**Метрики:**

Вычисление НОД всех пар первых 10000 чисел, начиная с миллиона, 
с помощью алгоритма Евклида занимает примерно 5,5 секунд. 

```dotty
val limit = 10_000
val start = 1_000_000
val end   = start + limit
(start until end).foreach: i =>
  (i + 1 to end).foreach: j =>
    gcdByEuclideanAlgorithm(j, i)

// Число тестовых запусков = 10
// Среднее время = 5,485 секунд
```

## Алгоритм вычисления НОД на основе четности чисел

Алгоритм нахождения наибольшего общего делителя (НОД), учитывающий четность чисел, 
является оптимизированной версией классического алгоритма Евклида. 
Он использует свойства четности и делимости для уменьшения количества операций, 
что может сделать его более эффективным в некоторых случаях. 
Однако, в среднем, он не лучше, чем классический алгоритм.

**Алгоритм:**

Алгоритм, нахождения НОД, учитывающий четность чисел:

- **Шаг 1**: Если `u == v`, то `НОД(u, v) = u`.
- **Шаг 2**: `НОД(r, 0) = r` для любого ненулевого `r`, так как `0` делится на любое целое число.
- **Шаг 3**: Если `u` и `v` - четные, то `НОД(u, v) = 2 * НОД(u/2, v/2)`.
- **Шаг 4**: Если числа имеют разную четность, то НОД этих двух чисел равен НОД нечетному и четному, разделенному на `2`.
- **Шаг 5**: Если оба нечетные, то вычтем из большего меньшее и продолжим (разница будет четная, можно сразу поделить на `2`)

**Пример:**

\\((u = 4851, v = 3003) \longmapsto (u = 924, v = 3003) \longmapsto (u = 462, v = 3003) \longmapsto\\)
\\((u = 231, v = 3003) \longmapsto (u = 1386, v = 231) \longmapsto (u = 693, v = 231) \longmapsto (u = 231, v = 231)\\)

**Пояснение:** 

Наибольший общий делитель (НОД) вычисляется исходя из следующих соображений:

- **Шаг 1**: Если оба числа равны, то их НОД равен любому из них.
- **Шаг 2**: Если одно из чисел равно нулю, то НОД равен другому числу, так как любое число делит ноль.
- **Шаг 3**: Если оба числа четные, то их НОД также будет четным. Мы можем упростить задачу, разделив оба числа на 2 и умножив результат на 2.
- **Шаг 4**: Если одно число четное, а другое нечетное, то НОД также нечетный. Его можно найти, разделив четное число на 2 и вычислив НОД с нечетным числом.
- **Шаг 5**: Если оба числа нечетные, мы можем вычесть меньшее из большего. Разница нечетных будет четной, и мы можем делить на 2, чтобы упростить числа.

**Оценка:**

- **Время** - O(log n)
- **Память** - O(1)

**Код:**

```dotty
def gcdBasedOnParityOfNumbers(a: Long, b: Long): Long =
  val u = math.abs(a)
  val v = math.abs(b)
  if u == v then u
  else if u == 0 then v
  else if v == 0 then u
  else
    (~u & 1, ~v & 1) match
      case (1, 1) => gcdBasedOnParityOfNumbers(u >> 1, v >> 1) << 1
      case (1, 0) => gcdBasedOnParityOfNumbers(u >> 1, v)
      case (0, 1) => gcdBasedOnParityOfNumbers(u, v >> 1)
      case (_, _) =>
        if u > v then gcdBasedOnParityOfNumbers((u - v) >> 1, v)
        else gcdBasedOnParityOfNumbers((v - u) >> 1, u)
  
gcdBasedOnParityOfNumbers(4851, 3003)  
// 231
```

**Метрики:**

Вычисление НОД всех пар первых 10000 чисел, начиная с миллиона,
с помощью алгоритма на основе четности чисел занимает примерно 13,5 секунды.

```dotty
val limit = 10_000
val start = 1_000_000
val end   = start + limit
(start until end).foreach: i =>
  (i + 1 to end).foreach: j =>
    gcdBasedOnParityOfNumbers(j, i)
      
// Число тестовых запусков = 5
// Среднее время = 13,468 секунд  
```

## Обобщенный алгоритм Евклида

> Даны два целых положительных числа `m` и `n`. 
> Требуется найти их наибольший общий делитель `d` и два целых числа `a` и `b`, таких, что `am + bn = d`.

**Алгоритм:**

- **Шаг 1**: (Инициализация). Присвоить \\(\acute{a} \leftarrow b \leftarrow 1, a \leftarrow \acute{b} \leftarrow 0, c \leftarrow m, d \leftarrow n\\).
- **Шаг 2**: (Деление). Пусть \\(q\\) и \\(r\\) — это частное и остаток от деления \\(c\\) на \\(d\\) соответственно. 
  (Тогда \\(c = qd + r\\), где \\(0 \leq r < d\\)).
- **Шаг 3**: (Остаток — нуль?). Если \\(r = 0\\), то выполнение алгоритма прекращается; 
  в этом случае имеем \\(am + bn = d\\), как и требовалось.
- **Шаг 4**: (Повторение цикла). Присвоить \\(c \leftarrow d, d \leftarrow r, t \leftarrow \acute{a}, \acute{a} \leftarrow a, a \leftarrow t − qa, t \leftarrow \acute{b}, \acute{b} \leftarrow b, b \leftarrow t − qb\\) и вернуться к шагу **Шаг 2**.

**Пример:**

Предположим, даны два числа `m = 1769` и `n = 551`.

Рассмотрим итерации алгоритма:

- \\(\acute{a} = 1, b = 1, a = 0, \acute{b} = 0, c = 1769, d = 551, q = 3, r = 116\\)
- \\(\acute{a} = 0, b = -3, a = 1, \acute{b} = 1, c = 551, d = 116, q = 4, r = 87\\)
- \\(\acute{a} = 1, b = 13, a = -4, \acute{b} = -3, c = 116, d = 87, q = 1, r = 29\\)
- \\(\acute{a} = -4, b = -16, a = 5, \acute{b} = 13, c = 87, d = 29, q = 3, r = 0\\)

Итоговый результат: \\(5 \times 1769 + (-16) \times 551 = 29\\)

**Пояснение:** 

- Алгоритм конечен, т.к. c и d постоянно уменьшаются (с логарифмической скоростью). 
  А значит, наступит момент, когда остаток от деления c на d будет равен 0 (в крайнем случае, когда d уменьшится до 1).

**Оценка:**

- **Время** - O(log n)
- **Память** - O(1)

**Код:**

```dotty
def generalizedEuclideanAlgorithm(m: Long,n: Long): (Long, Long, Long) =
  @annotation.tailrec
  def loop(
      aa: Long,
      b: Long,
      a: Long,
      bb: Long,
      c: Long,
      d: Long
  ): (Long, Long, Long) =
    val r = c % d
    if r == 0 then (d, a, b)
    else
      val q = c / d
      loop(aa = a, b = bb - q * b, a = aa - q * a, bb = b, c = d, d = r)

  loop(aa = 1, b = 1, a = 0, bb = 0, c = m, d = n)
end generalizedEuclideanAlgorithm

generalizedEuclideanAlgorithm(1769, 551)
// (d = 29, a = 5, b = -16)    
```

**Метрики:**

Вычисление обобщенного алгоритма Евклида всех пар первых 10000 чисел, начиная с 2, 
занимает примерно 17 секунд.

```dotty
val limit = 10_000
(2 until limit).foreach: i =>
  (i + 1 to limit).foreach: j =>
    generalizedEuclideanAlgorithm(j, i)
      
// Число тестовых запусков = 5
// Среднее время = 16,874 секунд
```

## Обратное по модулю число

**Алгоритм:**

[Обратное по модулю целого `m`](https://ru.wikipedia.org/wiki/%D0%9E%D0%B1%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D0%BF%D0%BE_%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8E_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) — 
это такое целое число `x`, что произведение `m*x` сравнимо с `1` по модулю `n`: \\(mx \equiv 1\\) (mod n).

При вычислении обратного по модулю числа достаточно воспользоваться "Обобщенным алгоритмом Евклида".
Если два числа `m` и `n` взаимно простые (`d = 1`), то `a` из формулы `am + bn = 1`
и есть искомое значение.

Если числа не взаимно простые, то такого числа `x` не существует.

**Оценка:**

- **Время** - O(log n)
- **Память** - O(1)

**Код:**

```dotty
def gcdInverse(m: Long, n: Long): Option[Long] =
  val (d, a, _) = generalizedEuclideanAlgorithm(m, n)
  if d == 1 then Some((a % n + n) % n) else None

gcdInverse(7, 26) // Some(15)
```

**Метрики:**

Метрики аналогичны метрикам Обобщенного алгоритма Евклида.

```dotty
val limit = 10_000
(2 until limit).foreach: i =>
  (i + 1 to limit).foreach: j =>
    gcdInverse(j, i)

// Число тестовых запусков = 5
// Среднее время = 18,834 секунд
```

## Китайская теорема об остатках

[Китайская теорема об остатках](https://ru.wikipedia.org/wiki/%D0%9A%D0%B8%D1%82%D0%B0%D0%B9%D1%81%D0%BA%D0%B0%D1%8F_%D1%82%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%BE%D0%B1_%D0%BE%D1%81%D1%82%D0%B0%D1%82%D0%BA%D0%B0%D1%85):

Если натуральные числа \\(a\_1, a\_2, \dots, a\_n\\) попарно взаимно просты, то для любых целых \\(r\_1, r\_2, \dots, r\_n\\) таких, 
что \\(0 \le r\_i < a\_i\\) при всех \\(i \in \{1, 2, \dots, n\}\\) найдётся число N, 
которое при делении на \\(a\_i\\) даёт остаток \\(r\_i\\) при всех \\(i \in \{1, 2, \dots, n\}\\). 
Более того, если найдутся два таких числа \\(N\_{1}\\) и \\(N_{2}\\) (соответствующих утверждению выше), 
то \\(N\_1 \equiv N\_2\\) (mod \\(a\_{1} \cdot a\_{2} \cdot \ldots \cdot a\_{n}\\)).

**Алгоритм:**

**Код:**

```dotty
object ChineseRemainderTheorem:
  def solution(aArray: Array[Long],rArray: Array[Long]): Option[Long] =
    val m      = aArray.product         // Шаг 1
    val mArray = aArray.map(a => m / a) // Шаг 2
    val maybeMMinus1Array =
      mArray.indices.foldLeft(Option(ListBuffer.empty[Long])): (acc, i) => // Шаг 3
        (acc, gcdInverse(mArray(i), aArray(i))) match
          case (Some(list), Some(mMinus)) => Some(list :+ mMinus)
          case _ => None
    maybeMMinus1Array.map: mMinus1Array =>
      mArray.indices.foldLeft(0L): (x, i) => // Шаг 4
        x + (((rArray(i) * mArray(i)) % m) * mMinus1Array(i)) % m
     
solution(Array(2, 3), Array(1, 2))
// 5, потому что 5 % 2 = 1, 5 % 3 = 2

solution(Array(707, 527), Array(0, 5))
// 258762, потому что 258762 % 707 = 0, 258762 % 527 = 5   
```

**Метрики:**

Решение задачи для достаточно больших чисел вычисляется меньше 100 миллисекунд.

```dotty
ChineseRemainderTheorem.solution(
  Array(1504170715041707L, 4503599627370517L),
  Array(0L, 8912517754604L)
)

// Число тестовых запусков = 5
// Среднее время = 32,764 миллисекунд  
```

## Наименьшее общее кратное

**Алгоритм:**

Наименьшее общее кратное вычисляется как произведение двух чисел, деленное на их НОД.

**Оценка:**

Оценка аналогична оценке вычислению НОД.

**Код:**

```dotty
def lcm(x: Long, y: Long): Long = math.abs(x * y) / gcd(x, y)
```

## Возведение числа в степень

**Алгоритм:**

Возведение числа в степень основывается на двух ключевых формулах:

- \\(A^{2*M} = (A^{2})^{M}\\)
- \\(A^{M+N} = A^{M} * A^{N}\\)

Первая позволяет быстро вычислить степень числа `А`, возводя квадрат `A` в половину исходной степени;
вторая помогает комбинировать степени любым удобным образом.

**Оценка:**

Временная сложность составляет O(log n). 
Это связано с тем, что каждое возведение в степень уменьшает степень в два раза, 
что приводит к логарифмическому количеству шагов.

- **Время** - O(log n)
- **Память** - O(1)

**Код:**

Реализация алгоритма (_Exercise 1.16_ из [SICP][sicp]):

```dotty
def power(a: Long, n: Long): Long =
  @annotation.tailrec
  def loop(base: Long, power: Long, acc: Long): Long =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(a, n, 1)
```

**Метрики:**

Поочередное возведение первых 100 чисел в степени от 1 до 10 занимает несколько миллисекунд.

```dotty
(1 to 100).foreach: i =>
  (1 to 10).foreach: j =>
    power(j, i)

// Число тестовых запусков = 10
// Среднее время = 7,605 миллисекунд
```

## Нахождение корня

**Алгоритм:**

Вычисление квадратного корня методом Ньютона хорошо описана [в главе "Пример: вычисление квадратного корня методом Ньютона" 
книги "Структура и интерпретация компьютерных программ"][sicp].

Метод Ньютона основывается на том, что имея некоторое приближенное значение `y` для квадратного корня числа `x`,
мы можем получить более точное значение, если возьмем среднее между `y` и `x/y`.

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

_Exercise 1.7_ из [SICP][sicp]:

```dotty
def sqrt(x: Double): Double       =
  def isGoodEnough(guess: Double): Boolean =
    math.abs(guess * guess - x) < delta

  def improve(guess: Double): Double = (guess + x / guess) / 2

  def sqrtIter(guess: Double): Double =
    if isGoodEnough(guess) then guess
    else sqrtIter(improve(guess))

  sqrtIter(1.0)
```

**Метрики:**

Вычисление квадратного корня первых миллиона чисел занимает меньше секунды.

```dotty
(1 to 1_000_000).foreach: i =>
  sqrt(i)

// Число тестовых запусков = 10
// Среднее время = 68,382 миллисекунд
```

## Нахождение кубического корня

**Алгоритм:**

Метод нахождения кубического корня основывается на том, 
что имея некоторое приближенное значение `y` для кубического корня числа `x`,
можно получить более точное значение по формуле:
\\((\frac{x}{y^{2}} + 2y) / 3\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

_Exercise 1.8_ из [SICP][sicp]:

```dotty
def cubeRootOf(x: Double): Double =
  def isGoodEnough(guess: Double): Boolean =
    math.abs(guess * guess * guess - x) < delta

  def improve(guess: Double): Double = (x / (guess * guess) + 2 * guess) / 3

  def sqrtIter(guess: Double): Double =
    if isGoodEnough(guess) then guess
    else sqrtIter(improve(guess))

  sqrtIter(1.0)
```

**Метрики:**

Вычисление кубического корня первых миллиона чисел занимает меньше секунды.
Результаты аналогичны метрикам вычисления квадратного корня, т.к. алгоритмы похожие.

```dotty
(1 to 1_000_000).foreach: i =>
  cubeRootOf(i)

// Число тестовых запусков = 10
// Среднее время = 194,439 миллисекунд  
```

## Степень двойки

**Алгоритм:**

Проверка, является ли заданное число степенью двойки.

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
def isPowerOfTwo(x: Int): Boolean = (x & (x - 1)) == 0

isPowerOfTwo(16) // true
```

**Метрики:**

Определение, является ли число степенью двойки, для первых миллиарда чисел занимает 4,5 секунды.
Результаты аналогичны тому, как если бы мы просто прошлись по коллекции в миллиард элементов и ничего не делали.

```dotty
@main def isPowerOfTwoBench(): Unit =
  (1 to 1_000_000_000).foreach: i =>
    isPowerOfTwo(i)

// CPU Time: 4658 ms
// Allocation memory size: 16,04 GB      
```

## Количество битовых единиц

**Алгоритм:**

Проверяет, содержат ли данные числа `x` и `y` одинаковое количество битовых единиц или нет.

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
@tailrec
def isSnoob(x: Int, y: Int): Boolean = 
  if x == 0 && y == 0 then true
  else if x == 0 || y == 0 then false
  else if x % 2 == 1 && y % 2 == 0 then isSnoob(x, y >> 1)
  else if x % 2 == 0 && y % 2 == 1 then isSnoob(x >> 1, y)
  else isSnoob(x >> 1, y >> 1)
  
// isSnoob(11, 13) // true: "1101", "1011"  
```

**Метрики:**

Проверка для первых 10000 пар занимает 4 секунды.

```dotty
@main def isSnoobBench(): Unit =
  val limit = 10_000
  (1 until limit).foreach: i =>
    (i + 1 to limit).foreach: j =>
      isSnoob(j, i)

// CPU Time: 3937 ms
// Allocation memory size: 810 MB      
```

## Делители числа

**Алгоритм:**

Согласно [функциям делителей][divisorfunc] сумму делителей числа, [разложенного на простые множители со степенями](primes.md):
\\(n = \prod\_{i=1}^{r}\rho\_{i}^{\alpha\_{i}}\\) можно посчитать по формуле:

\\(\sigma\_{1}(n) = \prod\_{i=1}^{r}\frac{\rho\_{i}^{\alpha\_{i} + 1} - 1}{\rho\_{i} - 1}\\)

А количество делителей по формуле:

\\(\sigma\_{0}(n) = \prod\_{i=1}^{r}(\alpha\_{i} + 1)\\)

### Сумма делителей числа

**Алгоритм:**

\\(\sigma\_{1}(n) = \prod\_{i=1}^{r}\frac{\rho\_{i}^{\alpha\_{i} + 1} - 1}{\rho\_{i} - 1}\\)

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def sumOfDivisors(number: Long): SafeLong =
  val primeDivisors = primeFactorsWithPow(number)
  primeDivisors.foldLeft(SafeLong(1)) { case (mul, (prime, power)) =>
    val num = SafeLong(prime).pow(power + 1) - 1
    val den = prime - 1
    mul * (num / den)
  }
  
sumOfDivisors(220) // 504  
sumOfDivisors(284) // 504
```

**Метрики:**

Вычисление суммы делителей первых миллиона чисел занимает 2 секунды.

```dotty
@main def sumOfDivisorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfDivisors(i)

// CPU Time: 1867 ms
// Allocation memory size: 1,18 GB      
```

### Количество делителей числа

**Алгоритм:**

\\(\sigma\_{0}(n) = \prod\_{i=1}^{r}(\alpha\_{i} + 1)\\)

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def countOfDivisors(number: Long): SafeLong =
  primeFactorsWithPow(number).values.foldLeft(SafeLong(1)): (mul, a) =>
    mul * (a + 1)
    
countOfDivisors(100) // 9    
```

**Метрики:**

Вычисление количества делителей первых миллиона чисел занимает примерно 1 секунду.

```dotty
@main def countOfDivisorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    countOfDivisors(i)  

// CPU Time: 1216 ms
// Allocation memory size: 435 MB      
```

### Совершенное число

**Алгоритм:**

[Совершенное число](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D0%B2%D0%B5%D1%80%D1%88%D0%B5%D0%BD%D0%BD%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) — 
это целое положительное число, равное сумме своих положительных делителей, исключая само число.

[Недостаточное число](https://ru.wikipedia.org/wiki/%D0%9D%D0%B5%D0%B4%D0%BE%D1%81%D1%82%D0%B0%D1%82%D0%BE%D1%87%D0%BD%D1%8B%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0) — 
это целое положительное число, которое меньше суммы своих положительных делителей, исключая само число.

[Избыточное число](https://ru.wikipedia.org/wiki/%D0%98%D0%B7%D0%B1%D1%8B%D1%82%D0%BE%D1%87%D0%BD%D1%8B%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0) — 
это целое положительное число, которое больше суммы своих положительных делителей, исключая само число.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
enum PerfectNumbersType:
  case Perfect, Deficient, Abundant
  
def perfectNumbersType(n: Long): PerfectNumbersType =
  val sum = sumOfDivisors(n) - n
  if sum == n then Perfect
  else if sum < n then Deficient
  else Abundant
  
perfectNumbersType(6)  // Perfect
perfectNumbersType(7)  // Deficient
perfectNumbersType(12) // Abundant
```

**Метрики:**

Вычисление типа числа (совершенное/избыточное/недостаточное) первых миллиона чисел занимает 2 секунды.

```dotty
@main def perfectNumbersTypeBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    perfectNumbersType(i)

// CPU Time: 2013 ms
// Allocation memory size: 1,27 GB      
```

## Сумма первых натуральных чисел

### Сумма первых натуральных чисел до заданного

**Алгоритм:**

Сумму первых натуральных чисел можно посчитать по формуле:
\\(\frac{n * (n + 1)}{2}\\)

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
def sumToGiven(n: Long): SafeLong = SafeLong(n) * SafeLong(n + 1) / 2

sumToGiven(1000000) 
// 500000500000L
```

**Метрики:**

Вычисление суммы от 1 до заданного числа для первого миллиона чисел занимает меньше секунды.
Результаты аналогичны тому, как если бы мы просто прошлись по коллекции в миллион элементов и ничего не делали.

```dotty
@main def sumToGivenBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumToGiven(i)

// CPU Time: 309 ms
// Allocation memory size: 122,85 MB      
```

### Сумма квадратов первых натуральных чисел до заданного

**Алгоритм:**

Сумму квадратов первых натуральных чисел можно посчитать [по формуле](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D0%BD%D1%8B%D0%B9_%D0%BA%D0%B2%D0%B0%D0%B4%D1%80%D0%B0%D1%82): 
\\(\frac{n * (n + 1) * (2 * n + 1)}{6}\\)

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
def sumOfSquaresTo(n: Long): SafeLong = SafeLong(n) * SafeLong(n + 1) * SafeLong(2 * n + 1) / 6

sumOfSquaresTo(1000) 
// 333833500L
```

**Метрики:**

Вычисление суммы квадратов от 1 до заданного числа для первого миллиона чисел занимает меньше секунды.
Результаты аналогичны тому, как если бы мы просто прошлись по коллекции в миллион элементов и ничего не делали.

```dotty
@main def sumOfSquaresToBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfSquaresTo(i)

// CPU Time: 418 ms
// Allocation memory size: 170,55 MB      
```

### Сумма кубов первых натуральных чисел до заданного

**Алгоритм:**

Сумму кубов первых натуральных чисел можно посчитать [по формуле](https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D0%B1_(%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0)):
\\(\left ( \frac{n * (n + 1)}{2} \right )^{2}\\)

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
def sumOfCubesTo(n: Long): SafeLong =
  val s = sumToGiven(n)
  s * s
  
sumOfCubesTo(1000) 
// 250500250000L  
```

**Метрики:**

Вычисление суммы кубов от 1 до заданного числа для первого миллиона чисел занимает 2 секунды.
Результаты аналогичны тому, как если бы мы просто прошлись по коллекции в миллион элементов и ничего не делали.

```dotty
@main def sumOfCubesToBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    sumOfCubesTo(i)  

// CPU Time: 1795 ms
// Allocation memory size: 1,06 GB      
```

## Рандомизация данных - Линейный конгруэнтный генератор

**Алгоритм:**

Линейный конгруэнтный генератор использует следующую зависимость
для формирования псевдослучайных величин

\\(X\_{n+1}\\) = (A * \\(X\_{n}\\) + B) % M, где A, B и M - константы.

Величина \\(X_{0}\\) называется _начальным_ числом.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
val A = 7
val B = 5
val M = 11
val generator = LazyList.iterate(0)(x => (A * x + B) % M)
generator.take(11).toList
// List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)
```

## Преобразование десятичного числа в двоичное

**Алгоритм:**

Мы переводим числа с основанием 10 в числа с основанием 2.
В этих двух системах счисления процедура счета различается главным образом из-за используемых символов.

Сначала опишем алгоритм конвертации:

1. Начиная с заданного числа, составить последовательность чисел таким образом, чтобы последующее число было половиной предыдущего,
   отбрасывая десятичную часть. Продолжить до тех пор, пока последний элемент не будет удовлетворять условию 2 > x > 0,
   где x — последнее число в последовательности.
   Формально S = {\\(x\_{1}\\), \\(x\_{2}\\),..., \\(x\_{n}\\)},
   где \\(x\_{2}\\) = \\(x\_{1}\\)/2, \\(x\_{3}\\) = \\(x\_{2}\\)/2 и т.д., и 2 > \\(x\_{n}\\) > 0.
2. Для каждого числа из приведенного выше списка разделить на 2 и хранить остаток в контейнере.
3. Теперь контейнер содержит двоичные эквивалентные биты в обратном порядке, \\(b\_{1}b\_{2}...b\_{n}\\),
   поэтому надо изменить порядок, чтобы получить двоичное эквивалентное число, \\(b\_{n}...b\_{2}b\_{1}\\).

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def decToBinConv(x: Int): String =
  val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
  val binList = seqOfDivByTwo
    .takeWhile(a => a > 0)
    .map(a => a % 2)
  binList.mkString.reverse

decToBinConv(8) // "1000"
decToBinConv(7) // "111"  
```

**Метрики:**

Преобразование из десятичного в двоичный формат для первого миллиона чисел занимает 2 секунды.

```dotty
@main def decToBinConvBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    decToBinConv(i)

// CPU Time: 1905 ms
// Allocation memory size: 1,59 GB      
```

## Палиндром

**Алгоритм:**

[Палиндром](https://ru.wikipedia.org/wiki/%D0%9F%D0%B0%D0%BB%D0%B8%D0%BD%D0%B4%D1%80%D0%BE%D0%BC) -
число, буквосочетание, слово или текст, одинаково читающееся в обоих направлениях.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def isPalindrome(number: Long): Boolean = isPalindrome(number, 10)

// Является ли заданное число палиндромом в системе исчисления по заданному состоянию
def isPalindrome(number: Long, base: Int): Boolean =
  number == 0 || number > 0 && number % base != 0 && {
    var revertedNumber = 0L
    var k              = number
    while k > revertedNumber
    do
      revertedNumber = revertedNumber * base + k % base
      k /= base
    k == revertedNumber || k == revertedNumber / base
  }
```

**Метрики:**

Количество палиндромов в первом миллионе чисел (1998) рассчитывается меньше секунды.

```dotty
@main def isPalindromeBench(): Unit =
  println((1 to 1000000).count(isPalindrome))
      
// CPU Time: 312 ms
// Allocation memory size: 24,15 MB      
```

---

**Ссылки:**

- [Алгоритм Евклида](https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%95%D0%B2%D0%BA%D0%BB%D0%B8%D0%B4%D0%B0)
- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Функция делителей][divisorfunc]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[divisorfunc]: https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%B4%D0%B5%D0%BB%D0%B8%D1%82%D0%B5%D0%BB%D0%B5%D0%B9
