# Числа Фибоначчи и другие последовательности

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

#### По формуле: сумма двух предыдущих

**Алгоритм:**

Заданное число Фибоначчи можно получить с помощью меморизации из библиотеки [scalaz](https://github.com/scalaz/scalaz):

**Оценка:**

- **Время** - O(n) (с запоминанием предыдущих вычислений)
- **Память** - ???

**Код:**

```dotty
import scalaz.*

object Fibonacci:
  val memoizedFib: Int => BigInt = Memo.mutableHashMapMemo {
    case 0 => BigInt(0)
    case 1 => BigInt(1)
    case n => memoizedFib(n - 2) + memoizedFib(n - 1)
  }

memoizedFib(60)  // 1548008755920
```

**Метрики:**

Вычисление 2000 числа Фибоначчи занимает меньше секунды.
Но при этом уже примерно на 2500 числе начинает выдаваться ошибка `StackOverflowError`.

```dotty
@main def memoizedFibBench(): BigInt =
  memoizedFib(2000)
      
// CPU Time: 238 ms
// Allocation memory size: 10,03 MB      
```

#### Итеративно

**Алгоритм:**

Либо число Фибоначчи можно вычислять итеративно:

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def iterativeFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, count: Int): BigInt =
    if count == 0 then b
    else loop(b, a + b, count - 1)

  loop(BigInt(0), BigInt(1), n - 1)
```

**Метрики:**

Вычисление миллионного числа Фибоначчи занимает 23 секунды.

```dotty
@main def iterativeFibBench(): BigInt =
  iterativeFib(1_000_000)
      
// CPU Time: 22898 ms
// Allocation memory size: 44,56 MB      
```

> Вышеперечисленные алгоритмы имеют линейную сложность.

#### Числа Фибоначчи с логарифмической сложностью

**Алгоритм:**

Вычислить числа Фибоначчи с логарифмической сложностью можно так (**Exercise 1.19** из [SICP][sicp]):

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

**Оценка:**

- **Время** - O(log(n))
- **Память** - ???

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
```

**Метрики:**

Вычисление миллионного числа Фибоначчи занимает меньше секунды.

```dotty
@main def fastFibBench(): BigInt =
  fastFib(1_000_000)
      
// CPU Time: 528 ms
// Allocation memory size: 160,60 MB      
```

### Приближенное значение

**Алгоритм:**

Приблизительное значение числа Фибоначчи можно вычислить [по формуле Бине](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

\\(F\_{n} = \frac{(\frac{1+\sqrt{5}}{2})^{n} - (\frac{1-\sqrt{5}}{2})^{n}}{\sqrt{5}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{\varphi - (-\varphi)^{-1}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{2\varphi - 1}\\)

**Оценка:**

- **Время** - ???
- **Память** - ???

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
@main def approximateFibonacciBench(): BigInt =
  approximateFibonacci(1_000_000)
      
// CPU Time: 341 ms
// Allocation memory size: 32,74 MB      
```

## Циклические последовательности

**Алгоритм:**

Циклические последовательности:

- Треугольные числа; \\(T\_{n} = \frac{n(n+1)}{2}\\); 1, 3, 6, 10, 15, ...
- Квадратные числа; \\(P\_{4,n} = n^{2}\\); 1, 4, 9, 16, 25, ...
- Пятиугольные числа; \\(P\_{5,n} = \frac{n(3n−1)}{2}\\); 1, 5, 12, 22, 35, ...
- Шестиугольные числа; \\(H\_{n} = n(2n−1)\\); 1, 6, 15, 28, 45, ...
- Семиугольные числа; \\(P\_{7,n} = \frac{n(5n−3)}{2}\\); 1, 7, 18, 34, 55, ...
- Восьмиугольные числа; \\(P\_{8,n} = n(3n−2)\\); 1, 8, 21, 40, 65, ...

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
object CyclicNumbers:
  def triangleNumber(n: SafeLong): SafeLong = n * (n + 1) / 2

  def isTriangle(n: Long): Boolean =
    val sol = math.round(-1.0 / 2.0 + sqrt(2.0 * n + 1.0 / 4.0))
    triangleNumber(sol) == n

  def squareNumber(n: SafeLong): SafeLong = n * n

  def isSquare(n: Long): Boolean = sqrt(n.toDouble).isWhole

  def pentagonalNumber(n: SafeLong): SafeLong = n * (3 * n - 1) / 2

  def isPentagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 6.0 + sqrt(2.0 * n / 3.0 + 1.0 / 36.0))
    pentagonalNumber(sol) == n

  def hexagonalNumber(n: SafeLong): SafeLong = n * (2 * n - 1)

  def isHexagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 4.0 + sqrt(n / 2.0 + 1.0 / 16.0))
    hexagonalNumber(sol) == n

  def heptagonalNumber(n: SafeLong): SafeLong = n * (5 * n - 3) / 2

  def isHeptagonal(n: Long): Boolean =
    val sol = math.round(3.0 / 10.0 + sqrt(2.0 * n / 5.0 + 9.0 / 100.0))
    heptagonalNumber(sol) == n

  def octagonalNumber(n: SafeLong): SafeLong = n * (3 * n - 2)

  def isOctagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 3.0 + sqrt(n / 3.0 + 1.0 / 9.0))
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

**Алгоритм:**

Берём любое натуральное число n. Если оно чётное, то делим его на 2, 
а если нечётное, то умножаем на 3 и прибавляем 1 (получаем 3n + 1). 
Над полученным числом выполняем те же самые действия, и так далее.
[Гипотеза Коллатца](https://ru.wikipedia.org/wiki/%D0%93%D0%B8%D0%BF%D0%BE%D1%82%D0%B5%D0%B7%D0%B0_%D0%9A%D0%BE%D0%BB%D0%BB%D0%B0%D1%82%D1%86%D0%B0)
заключается в том, что какое бы начальное число n мы ни взяли, рано или поздно мы получим единицу.

Подсчитать `collatz` - количество итераций до получения `1` от заданного `n`:

- Если `n` - четное, то `collatz(n) = collatz(n / 2) + 1`
- Если `n` - нечетное, то `3n + 1` - четное (и не равно `1`), то `collatz(n) = collatz((3 * n + 1) / 2) + 2`

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
final case class CollatzNumber(cacheLimit: Int):
  private val cache: Array[SafeLong] = new Array[SafeLong](cacheLimit)
  cache(1) = SafeLong(1)

  def collatz(n: Long): SafeLong =
    if n >= cacheLimit then nextCollatz(n)
    else
      val m = n.toInt
      if Option(cache(m)).isEmpty then cache(m) = nextCollatz(n)
      cache(m)

  private def nextCollatz(n: Long): SafeLong =
    if n % 2 == 0 then collatz(n / 2) + 1
    else collatz((3 * n + 1) / 2) + 2
    
collatzNumber.collatz(27) 
// 112    
```

**Метрики:**

```dotty
private val collatzNumber = CollatzNumber(1_000_000)

@main def collatzNumberBench(): Unit =
  (1 to 10_000).foreach: i =>
    collatzNumber.collatz(i)
      
// CPU Time: 288 ms
// Allocation memory size: 16,59 MB      
```

## Числа Хэмминга

**Алгоритм:**

Положительное число является обобщенным числом Хэмминга типа n, если все его простые делители не превышают n.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def isHammingNumber(number: Long, n: Int): Boolean =
  if number < 2 || number <= n then true
  else if n < 2 then false
  else
    var temp       = number
    val primesList = primesNoMoreThanN(n)
    var isHamming  = false
    var i          = 0
    while i < primesList.length && !isHamming
    do
      val prime = primesList(i)
      while temp % prime == 0
      do temp /= prime
      if temp == 1 then isHamming = true
      i += 1
    isHamming
    
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

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.8](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
