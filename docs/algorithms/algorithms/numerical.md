# Численные алгоритмы

## Наибольший общий делитель

### Алгоритм Евклида

> Даны два целых положительных числа `m` и `n`. 
> Требуется найти их наибольший общий делитель, т.е. наибольшее целое положительное число, 
> которое нацело делит оба числа `m` и `n`.

**Алгоритм:**

- **E1** (Сравнение с нулем). Если `n = 0`, то выполнение алгоритма прекращается; `m` — искомое значение.
- **E2** (Замещение). Присвоить `r ← m % n, m ← n, n ← r` и вернуться к шагу **E1**.

**Пояснение:** 

Наибольший общий делитель (НОД) с помощью алгоритма Евклида вычисляется исходя из следующих соображений:

- `НОД(r, 0) = r` для любого ненулевого `r` (так как `0` делится на любое целое число).
- Пусть `a = b*q + r`, тогда `НОД (a, b) = НОД (b, r)`.

**Оценка:**

- **Время** - O(log n)
- **Память** - O(log n)

**Код:**

```dotty
@tailrec
final def gcdByEuclideanAlgorithm(a: Long, b: Long): Long =
  if b == 0 then a
  else gcdByEuclideanAlgorithm(b, a % b)
  
gcdByEuclideanAlgorithm(4851, 3003)
// res0: Long = 231L
```

### Алгоритм НОД на основе четности чисел

**Алгоритм:**

Алгоритм, нахождения НОД, учитывающий четность чисел:

- Если `u == v`, то `НОД(u, v) = u`.
- `НОД(r, 0) = r` для любого ненулевого `r` (так как 0 делится на любое целое число).
- Если `u` и `v` - четные, то `НОД(u, v) = 2 * НОД(u/2, v/2)`.
- Если числа имеют разную четность, то НОД этих двух чисел равен НОД нечетному и четному, разделенному на 2.
- Если оба нечетные, то можно воспользоваться алгоритмом Евклида.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def gcd(a: Long, b: Long): Long =
  val u = math.abs(a)
  val v = math.abs(b)
  if u == v then u
  else if u == 0 then v
  else if v == 0 then u
  else
    (~u & 1, ~v & 1) match
      case (1, 1) => gcd(u >> 1, v >> 1) << 1
      case (1, 0) => gcd(u >> 1, v)
      case (0, 1) => gcd(u, v >> 1)
      case (_, _) => if u > v then gcd(u - v, v) else gcd(v - u, u)
  
gcd(4851, 3003)  
// res1: Long = 231L
```

### Обобщенный алгоритм Евклида

> Даны два целых положительных числа `m` и `n`. 
> Требуется найти их наибольший общий делитель `d` и два целых числа `a` и `b`, таких, что `am + bn = d`.

**Алгоритм:**

- **E1** (Инициализация). Присвоить \\(\acute{a} \leftarrow b \leftarrow 1, a \leftarrow \acute{b} \leftarrow 0, c \leftarrow m, d \leftarrow n\\).
- **E2** (Деление). Пусть \\(q\\) и \\(r\\) — это частное и остаток от деления \\(c\\) на \\(d\\) соответственно. 
  (Тогда \\(c = qd + r\\), где \\(0 \leq r < d\\)).
- **E3** (Остаток — нуль?). Если \\(r = 0\\), то выполнение алгоритма прекращается; 
  в этом случае имеем \\(am + bn = d\\), как и требовалось.
- **E4** (Повторение цикла). Присвоить \\(c \leftarrow d, d \leftarrow r, t \leftarrow \acute{a}, \acute{a} \leftarrow a, a \leftarrow t − qa, t \leftarrow \acute{b}, \acute{b} \leftarrow b, b \leftarrow t − qb\\) и вернуться к шагу **E2**.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def generalizedEuclideanAlgorithm(m: Long, n: Long): (Long, Long, Long) =
  @tailrec
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

generalizedEuclideanAlgorithm(1769, 551)
// (29, 5, -16)    
```

## Обратное по модулю число

**Алгоритм:**

[Обратное по модулю целого `m`](https://ru.wikipedia.org/wiki/%D0%9E%D0%B1%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D0%BF%D0%BE_%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8E_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) — 
это такое целое число `x`, что произведение `m*x` сравнимо с `1` по модулю `n`: \\(mx \equiv 1\\) (mod n).

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
import spire.math.SafeLong

def gcdInverse(m: SafeLong, n: SafeLong): Option[SafeLong] =
  val (d, a, _) = generalizedEuclideanAlgorithm(m, n)
  if d == 1 then Some((a % n + n) % n) else None
```

## Наименьшее общее кратное

**Алгоритм:**

Наименьшее общее кратное вычисляется как произведение двух чисел, деленное на их НОД.

**Оценка:**

- **Время** - O(log n)
- **Память** - O(log n)

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

- **Время** - O(log n)
- **Память** - O(log n)

**Код:**

Реализация алгоритма (_Exercise 1.16_ из [SICP][sicp]):

```dotty
def power(a: SafeLong, n: SafeLong): SafeLong =
  @tailrec
  def loop(base: SafeLong, power: SafeLong, acc: SafeLong): SafeLong =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(a, n, 1)
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

## Нахождение кубического корня

**Алгоритм:**

Метод нахождения кубического корня основывается на том, 
что имея некоторое приближенное значение `y` для кубического корня числа `x`,
можно получить более точное значение по формуле:
\\((\frac{x}{y^{2}} + 2y) / 3\\).

**Оценка:**

- **Время** - ???
- **Память** - ???

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
def sumToGiven(n: Long): Long = n * (n + 1) / 2

sumToGiven(1000000) 
// 500000500000L
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
def sumOfSquaresTo(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

sumOfSquaresTo(1000) 
// 333833500L
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
def sumOfCubesTo(n: Long): Long =
  val s = sumToGiven(n)
  s * s
  
sumOfCubesTo(1000) 
// 250500250000L  
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
