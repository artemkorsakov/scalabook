# Перестановки и факториалы

_Перестановкой n объектов_ называется способ последовательного расположения n различных объектов с учетом порядка. 
Например, для трех объектов \\({a, b, c}\\) существует шесть перестановок: \\(abc, acb, bac, bca, cab, cba\\).

Обозначив через \\(p\_{nk}\\) количество способов выбора \\(k\\) объектов из \\(n\\) с учетом порядка, 
получим \\(p\_{nk} = n(n − 1)...(n − k + 1)\\). 
Отсюда вытекает, что общее число перестановок выражается формулой \\(p\_{nn} = n(n − 1) ... (1) = n!\\).

## Построение перестановок

Существует два основных метода перехода от перестановок из n − 1 объектов к перестановкам из n объектов.

#### Метод 1

**Алгоритм:**

Для каждой перестановки \\(a\_{1} a\_{2} ... a\_{n−1}\\) из \\({1, 2,...,n−1}\\) объектов построим еще n перестановок, 
помещая число n на все возможные места, в результате чего получим 
\\(n a\_{1} a\_{2} ... a\_{n−1}, a\_{1} n a\_{2} ... a\_{n−1}, ..., a\_{1} a\_{2} ... n a\_{n−1}, a\_{1} a\_{2} ... a\_{n−1} n\\).
Очевидно, это все возможные перестановки из n объектов, причем ни одна из них не повторяется.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def constructByMethod1[A](source: Vector[A], a: A): Vector[Vector[A]] =
  (0 to source.length).map { i =>
    val (before, after) = source.splitAt(i)
    before ++ Vector(a) ++ after
  }.toVector
  
constructByMethod1(Vector(2, 3, 1), 4),
// Vector(Vector(4, 2, 3, 1), Vector(2, 4, 3, 1), Vector(2, 3, 4, 1), Vector(2, 3, 1, 4))
```

**Метрики:**

Переход от перестановки в 19999 элементов к перестановке в 20000 элементов занимает примерно 1 секунду.

```dotty
@main def constructByMethod1Bench(): Vector[Vector[Int]] =
  constructByMethod1[Int]((1 to 19_999).toVector, 20_000)
      
// CPU Time: 1118 ms
// Allocation memory size: 942,3 MB      
```

#### Метод 2. 

**Алгоритм:**

Для каждой перестановки \\(a\_{1} a\_{2} ... a\_{n−1}\\) из \\({1, 2,...,n−1}\\) объектов 
построим ещё n перестановок следующим образом. 
Сначала построим набор 
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{1}{2}\\),
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{3}{2}\\),
...
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{n-1}{2}\\). 
Затем заменим элементы каждой перестановки цифрами \\({1, 2,...,n}\\), _сохраняя порядок_.
Например, для перестановки \\(2 3 1\\) получим
\\(231\frac{1}{2}\\),
\\(231\frac{3}{2}\\),
\\(231\frac{5}{2}\\),
\\(231\frac{7}{2}\\)
и после замены получим \\(3421, 3412, 2413, 2314\\).

Есть еще один способ описания этого процесса. 
Возьмем перестановку \\(a\_{1}a\_{2}...a\_{n−1}\\) и число \\(k, 1 \leq k \leq n\\); 
добавим единицу к каждому \\(a\_{j}\\), значение которого \\(\geq k\\).
В результате получим перестановку \\(b\_{1}b\_{2}...b\_{n−1}\\) 
из элементов \\({1, ..., k − 1, k + 1, ..., n}\\); 
значит, \\(b\_{1}b\_{2}...b\_{n−1}k\\)  — это перестановка чисел \\({1,...,n}\\).
При таком способе построения тоже очевидно, что каждая перестановка из \\(n\\) элементов встречается только один раз. 
Аналогичные построения можно выполнить, помещая \\(k\\) не справа, а слева, либо в любой другой фиксированной позиции.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def constructByMethod2(source: Vector[Int]): Vector[Vector[Int]] =
  (1 to source.length + 1).map { k =>
    source.map { v => if v >= k then v + 1 else v } :+ k
  }.toVector
  
constructByMethod2(Vector(2, 3, 1)),
// Vector(Vector(3, 4, 2, 1), Vector(3, 4, 1, 2), Vector(2, 4, 1, 3), Vector(2, 3, 1, 4))
```

**Метрики:**

Переход от перестановки в 4999 элементов к перестановке в 5000 элементов занимает примерно 1 секунду.
Метод 1 является более эффективным по сравнению с методом 2.

```dotty
@main def constructByMethod2Bench(): Vector[Vector[Int]] =
  constructByMethod2((1 to 4_999).toVector)
      
// CPU Time: 1038 ms
// Allocation memory size: 516,11 MB      
```

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
но здесь используется [алгоритм нахождения простых чисел от 2 до заданного лимита](primes.md),
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
