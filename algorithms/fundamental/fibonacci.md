# Числа Фибоначчи

[Числа Фибоначчи](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8) — 
элементы числовой последовательности `0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...`,
в которой первые два числа равны `0` и `1`, а каждое последующее число равно сумме двух предыдущих чисел.


## Заданное число Фибоначчи

Заданное число Фибоначчи можно получить с помощью меморизации из библиотеки [scalaz](https://github.com/scalaz/scalaz):

```scala
import scalaz.*

object Fibonacci:
  val memoizedFib: Int => BigInt = Memo.mutableHashMapMemo {
    case 0 => BigInt(0)
    case 1 => BigInt(1)
    case n => memoizedFib(n - 2) + memoizedFib(n - 1)
  }

memoizedFib(60)  // 1548008755920
```

Либо число Фибоначчи можно вычислять итеративно:

```scala
def iterativeFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, count: Int): BigInt =
    if count == 0 then b
    else loop(b, a + b, count - 1)

  loop(BigInt(0), BigInt(1), n - 1)
```

Но вышеперечисленные алгоритмы имеют линейную сложность.

#### Числа Фибоначчи с логарифмической сложностью

Вычислить числа Фибоначчи с логарифмической сложностью можно так (**Exercise 1.19** из [SICP][sicp]):

```scala
def fastFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, p: BigInt, q: BigInt, count: Int): BigInt =
    if count == 0 then b
    else if count % 2 == 0 then
      loop(a, b, p * p + q * q, 2 * p * q + q * q, count / 2)
    else loop(b * q + a * (p + q), b * p + a * q, p, q, count - 1)

  loop(1, 0, 0, 1, n)
```

## Приблизительное значение Фибоначчи

Приблизительное значение числа Фибоначчи можно вычислить [по формуле Бине](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/7e13a8a7c316c95f60118e8a3dc9641eb1680dc2)

```scala
private val PHI: BigDecimal = BigDecimal((1.0 + math.sqrt(5.0)) / 2.0)

def approximateFibonacci(n: Int): BigInt =
  (PHI.pow(n) / BigDecimal(math.sqrt(5)))
    .setScale(0, BigDecimal.RoundingMode.HALF_UP)
    .toBigInt
```


---

## Ссылки

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FFibonacci.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FFibonacciSuite.scala)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
