{%
showPrev = true
prevPage = ../combinatorics.html
showNext = true
nextPage = permutation.html
%}

# Факториал

_n-факториал_ - величина, записанная следующим образом: \\(n! = 1 · 2 · ... · n = \prod_{k=1}^{n}k \\).

Например, 5-факториал равен 5! = 120.

При этом \\(0! = 1\\) - учитывая это соглашение, видно, 
что основное тождество \\(n! = (n − 1)!n\\) справедливо для всех целых положительных n.

## Вычисление факториала

> Дано число n. Вычислите n!.

**Алгоритм:**

- **Шаг 1**. Присвоить `acc` значение `1`. Присвоить `i` значение `2`. 
- **Шаг 2**. Если `i` не больше `n`, то перейти к шагу 3. В противном случае вывести `acc`.
- **Шаг 3**. Присвоить `acc` значение `acc * i`, а `i` увеличить на `1`. Вернуться к шагу 2.

**Пример:**

Пример вычисления для `n = 5`:

- Начальное значение аккумулятора: `1`.
- Последовательное умножение: `1 * 2 = 2`, `2 * 3 = 6`, `6 * 4 = 24`, `24 * 5 = 120`.
- **Результат**: Факториал числа 5 равен 120.

**Оценка:**

- **Время** - O(n)
- **Память** - O(1)

**Код:**

```dotty
def factorial(n: Int): Long =
  (2 to n).foldLeft(1L)(_ * _)
    
factorial(8)
// 40320
```

**Метрики:**

Вычисление `100000!` занимает 2 секунды.

```dotty
factorial(100000)
      
// Число тестовых запусков = 5
// Среднее время = 2,356 секунд  
```

## Приближенное значение по формуле Стирлинга

**Алгоритм:**

Формула Стирлинга позволяет вычислить приближенное значение факториала:
\\(n!\approx \sqrt{2\pi n}\left ( \frac{n}{e} \right )^{n}\\)

> Относительная погрешность расчетов по этой формуле приблизительно равна \\(\frac{1}{12n}\\).

**Пример:**

Пример вычисления факториала по формуле Стирлинга для \\(n = 10\\):

- Шаг 1: Вычислить \\(\sqrt{2 \pi n}\\):
    - \\(\sqrt{2 \pi \cdot 10} = \sqrt{2 \cdot 3.14159 \cdot 10} = \sqrt{62.8318} \approx 7.926\\)
- Шаг 2: Вычислить \\(\left( \frac{n}{e} \right)^n\\):
    - \\(\left( \frac{10}{2.71828} \right)^{10} = \left( 3.6788 \right)^{10} \approx 453999.29\\)
- Шаг 3: Перемножить результаты шагов 1 и 2:
    - \\(7.926 \cdot 453999.29 \approx 3628800\\)
- Результат: Приближенное значение факториала числа \\(10\\) по формуле Стирлинга равно \\(3628800\\).
- Точное значение: \\(10! = 3628800\\).

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
approximateFactorial(100000)
      
// Число тестовых запусков = 10
// Среднее время = 0,078 секунд    
```

### Более точное приближение

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
approximateFactorial2(100000)  
      
// Число тестовых запусков = 10
// Среднее время = 0,081 секунд     
```

## Разложение факториала на простые множители

**Алгоритм:**

Простое число \\(p\\) является делителем \\(n!\\) кратности
\\(\mu = \left \lfloor \frac{n}{p} \right \rfloor + \left \lfloor \frac{n}{p^{2}} \right \rfloor + \left \lfloor \frac{n}{p^{3}} \right \rfloor + ... = \sum_{k > 0}^{}\left \lfloor \frac{n}{p^{k}} \right \rfloor\\).

Эта формула позволяет разложить факториал на простые множители.

**Пример:**

Рассмотрим пример вычисления кратности простого числа \\(p = 3\\) в факториале \\( n = 10\\).

Формула:

\\(\mu = \left \lfloor \frac{n}{p} \right \rfloor + \left \lfloor \frac{n}{p^{2}} \right \rfloor + \left \lfloor \frac{n}{p^{3}} \right \rfloor + ... = \sum_{k > 0}^{}\left \lfloor \frac{n}{p^{k}} \right \rfloor \\)

- Шаг 1: Вычислить \\(\left \lfloor \frac{n}{p} \right \rfloor \\): \\(\left \lfloor \frac{10}{3} \right \rfloor = \left \lfloor 3.333 \right \rfloor = 3\\)
- Шаг 2: Вычислить \\(\left \lfloor \frac{n}{p^{2}} \right \rfloor \\): \\(\left \lfloor \frac{10}{3^{2}} \right \rfloor = \left \lfloor \frac{10}{9} \right \rfloor = \left \lfloor 1.111 \right \rfloor = 1\\)
- Шаг 3: Вычислить \\(\left \lfloor \frac{n}{p^{3}} \right \rfloor\\): \\(\left \lfloor \frac{10}{3^{3}} \right \rfloor = \left \lfloor \frac{10}{27} \right \rfloor = \left \lfloor 0.370 \right \rfloor = 0\\)
- Шаг 4: Суммировать все полученные значения: \\(\mu = 3 + 1 + 0 = 4\\)
- Результат: Простое число \\(3\\) является делителем \\(10!\\) кратности \\(4\\).

**Оценка:**

- **Время** - O(log(p))
- **Память** - O(1)

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

Вычисление разложения `100000!` на простые множители занимает примерно 0,166 секунды,
но здесь используется [алгоритм нахождения простых чисел от 2 до заданного лимита](../number_theory/primes.md),
который, согласно приведенным метрикам, занимает примерно 0,152 секунды.

Выходит, практически все ресурсы занимает процесс нахождения простых чисел.

```dotty
val limit = 100_000
primes.take(limit).toList.foreach: p =>
  primeFactor(limit, p)
      
// Число тестовых запусков = 10
// Среднее время = 0,166 секунд
```

---

**Ссылки:**

- [Исходный код][sample]
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
[sample]: https://gitverse.ru/scalabook/algorithms
