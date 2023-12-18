# Числа Фибоначчи

[Числа Фибоначчи](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8) — 
элементы числовой последовательности `0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...`,
в которой первые два числа равны `0` и `1`, а каждое последующее число равно сумме двух предыдущих чисел.

Члены этой последовательности обозначаются через \\(F\_{n}\\).
Формальное определение: \\(F\_{0} = 0; F\_{1} = 1; F\_{n + 2} = F\_{n+1} + F\_{n}, n \geq 0.\\)

## Некоторые свойства

- **Формула 1**: \\(F\_{n+1}F\_{n-1} - F\_{n}^{2} = (-1)^{n}\\)
- **Формула 2**: \\(\begin{pmatrix} F\_{n+1} & F\_{n} \\\\ F\_{n} & F\_{n-1} \\\\ \end{pmatrix} = \begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\\\ \end{pmatrix}^{n}\\)
- Из **формулы 1** следует, что числа \\(F\_{n}\\) и \\(F\_{n+1}\\) являются взаимно простыми, 
  так как любой их общий делитель должен быть также делителем \\((−1)^{n}\\).
- **Формула 3**: \\(F\_{n+m} = F\_{m}F\_{n+1} + F\_{m-1}F\_{n}\\)
- **Теорема A** (Э. Люка, 1876). Некоторое целое число делит и \\(F\_{m}\\), и \\(F\_{n}\\) тогда и только тогда, 
  когда оно является делителем \\(F\_{d}\\), где \\(d = gcd(m, n)\\); в частности, \\(gcd(F\_{m}, F\_{n}) = F\_{gcd(m,n)}\\).
- **Формула 4**: \\(F\_{n} = \frac{1}{\sqrt{5}}(\phi^{n} - \hat{\phi}^{n})\\), 
  где \\(\phi = \frac{1 + \sqrt{5}}{2}\\) и \\(\hat{\phi} = 1 - \phi = \frac{1 - \sqrt{5}}{2}\\).
- **Формула 5**: \\(F\_{n} = \frac{\phi^{n}}{\sqrt{5}}\\), округленное до ближайшего целого числа.
- **Формула 6**: \\(\sum\_{k=0}^{n}F\_{k}F\_{n-k} = \frac{n - 1}{5}F\_{n} + \frac{2n}{5}F\_{n-1}\\)


## Точное значение

Заданное число Фибоначчи можно получить с помощью меморизации из библиотеки [scalaz](https://github.com/scalaz/scalaz):

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

Либо число Фибоначчи можно вычислять итеративно:

```dotty
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

## Приближенное значение

Приблизительное значение числа Фибоначчи можно вычислить [по формуле Бине](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

\\(F\_{n} = \frac{(\frac{1+\sqrt{5}}{2})^{n} - (\frac{1-\sqrt{5}}{2})^{n}}{\sqrt{5}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{\varphi - (-\varphi)^{-1}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{2\varphi - 1}\\)

```dotty
private val PHI: BigDecimal = BigDecimal((1.0 + math.sqrt(5.0)) / 2.0)

def approximateFibonacci(n: Int): BigInt =
  (PHI.pow(n) / BigDecimal(math.sqrt(5)))
    .setScale(0, BigDecimal.RoundingMode.HALF_UP)
    .toBigInt
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.8](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
