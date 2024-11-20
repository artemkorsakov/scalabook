# Факториал

## Факториал

**Алгоритм:**

_n-факториал_ - величина, записанная следующим образом: \\(n! = 1 · 2 · ... · n = \prod_{k=1}^{n}k \\).
При этом \\(0! = 1\\) - учитывая это соглашение, видно, 
что основное тождество \\(n! = (n − 1)!n\\) справедливо для всех целых положительных n.

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def factorial(n: Int): SafeLong =
  (2 to n).foldLeft(SafeLong(1))(_ * _)
  
factorial(8)
// 40320
```

**Метрики:**

Вычисление `100000!` занимает примерно 4 секунды.

```dotty
@main def factorialBench(): SafeLong =
  factorial(100000)
      
// CPU Time: 3790 ms
// Allocation memory size: 13,61 GB      
```

#### Приближенное значение по формуле Стирлинга

**Алгоритм:**

Формула Стирлинга позволяет вычислить приближенное значение факториала:
\\(n!\approx \sqrt{2\pi n}\left ( \frac{n}{e} \right )^{n}\\)

> Относительная погрешность расчетов по этой формуле приблизительно равна \\(\frac{1}{12n}\\).

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def approximateFactorial(n: Int): BigInt =
  (BigDecimal(math.sqrt(2 * math.Pi * n)) *
    BigDecimal(n / math.E).pow(n)).toBigInt
      
approximateFactorial(8)
// BigInt(39902)      
```

**Метрики:**

Вычисление приближенного значения `100000!` занимает меньше секунды.

```dotty
@main def approximateFactorialBench(): BigInt =
  approximateFactorial(100000)
      
// CPU Time: 428 ms
// Allocation memory size: 83,58 MB      
```

**Алгоритм:**

Более точная формула:

\\(n! = \sqrt{2\pi n}\left ( \frac{n}{e} \right )^{n}(1 + \frac{1}{12n} + \frac{1}{288n^{2}} - \frac{139}{51840n^{3}} - \frac{571}{2488320n^{4}} + O(\frac{1}{n^{5}}))\\)

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def approximateFactorial2(n: Int): BigInt =
  (BigDecimal(math.sqrt(2 * math.Pi * n)) *
    BigDecimal(n / math.E).pow(n) *
    (1 + 1 / BigDecimal(12.0 * n) + 1 / BigDecimal(288.0 * n * n) -
      139 / BigDecimal(51840.0 * n * n * n) -
      571 / BigDecimal(2488320.0 * n * n * n * n))).toBigInt
 
approximateFactorial2(8)
// BigInt(40319)      
```

**Метрики:**

Вычисление приближенного значения `100000!` по уточненной формуле занимает меньше секунды.

```dotty
@main def approximateFactorial2Bench(): BigInt =
  approximateFactorial2(100000)  
      
// CPU Time: 396 ms
// Allocation memory size: 83,55 MB      
```

#### Разложение факториала на простые множители

**Алгоритм:**

Простое число \\(p\\) является делителем \\(n!\\) кратности
\\(\mu = \left \lfloor \frac{n}{p} \right \rfloor + \left \lfloor \frac{n}{p^{2}} \right \rfloor + \left \lfloor \frac{n}{p^{3}} \right \rfloor + ... = \sum_{k > 0}^{}\left \lfloor \frac{n}{p^{k}} \right \rfloor\\).

Эта формула позволяет разложить факториал на простые множители.

**Оценка:**

- **Время** - O(log(p))
- **Память** - ???

**Код:**

```dotty
def primeFactor(n: Int, p: Int): Int =
  @tailrec
  def loop(pj: Int, acc: Int): Int =
    if pj == 0 then acc
    else
      val next = pj / p
      loop(next, acc + next)
  loop(n, 0)
  
primeFactor(1000, 3)
// 498  
```

**Метрики:**

Вычисление разложения `100000!` на простые множители занимает примерно 1,6 секунды,
но здесь используется [алгоритм нахождения простых чисел от 2 до заданного лимита](../number_theory/primes.md),
который, согласно приведенным метрикам, тоже занимает 1,6 секунды.

Выходит, что практически все ресурсы занимает процесс нахождения простых чисел.

```dotty
@main def primeFactorBench(): Unit =
  val limit = 100_000
  primes.take(limit).toList.foreach: p =>
    primeFactor(limit, p)
      
// CPU Time: 1633 ms                | 1598 ms | разница: 35 ms
// Allocation memory size: 1,67 GB  | 1,68 GB |     
```

---

**Ссылки:**

- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
