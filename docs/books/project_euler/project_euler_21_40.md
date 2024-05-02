# Задачи №21-№40

## Задача №21

[Задача №21 - Amicable Numbers](https://projecteuler.net/problem=21)

> Let \\(d(n)\\) be defined as the sum of proper divisors of n (numbers less than n which divide evenly into n).
> If \\(d(a) = b\\) and \\(d(b) = a\\), where \\(a \neq b\\), then a and b are an amicable pair 
> and each of a and b are called amicable numbers.
>
> For example, the proper divisors of 220 are 1, 2, 4, 5, 10, 11, 20, 22, 44, 55 and 110; therefore \\(d(220) = 284\\). 
> The proper divisors of 284 are 1, 2, 4, 71 and 142; so \\(d(284) = 220\\).
> 
> Evaluate the sum of all the amicable numbers under 10000.

**Алгоритм:**

Следующий код на Scala находит сумму "дружественных" чисел меньше 10000. 
Числа a и b называются дружественными, если сумма делителей числа a (не считая самого числа) равна b, 
и сумма делителей числа b равна a.

1. `primeFactorsWithPow(n: Long): Map[Long, Int]` - функция, которая находит простые делители числа `n` и их степени. 
   Описание функции приведено в Problem 5.
2. `sumOfDivisors(number: Long): Long` - функция, которая находит сумму делителей числа `number`. 
   Она использует `primeFactorsWithPow` для получения простых делителей и их степеней, 
   а затем использует `foldLeft` для вычисления суммы делителей.
3. `amicableNumbers` - выражение, которое находит все "дружественные" числа меньше 10000. 

**Код:**

```dotty
def primeFactorsWithPow(n: Long): Map[Long, Int] = ... // Из Problem 5

def sumOfDivisors(number: Long): Long =
  val primeDivisors = primeFactorsWithPow(number)
  primeDivisors.foldLeft(1L) { case (mul, (prime, power)) =>
    val num = math.pow(prime, power + 1).toLong - 1
    val den = prime - 1
    mul * (num / den)
  }

val amicableNumbers =
  (2 until 10000).flatMap: a =>
    val b = sumOfDivisors(a) - a
    if b > a && sumOfDivisors(b) - b == a then Some(a + b) else None
  .sum
```

## Задача №22

[Задача №22 - Names Scores](https://projecteuler.net/problem=22)

**Задача:**

> Using names.txt, a 46K text file containing over five-thousand first names, begin by sorting it into alphabetical order. 
> Then working out the alphabetical value for each name, multiply this value by its alphabetical position 
> in the list to obtain a name score.
> 
> For example, when the list is sorted into alphabetical order, COLIN, which is worth 3 + 15 + 12 + 9 + 14 = 53, 
> is the 938th name in the list. So, COLIN would obtain a score of \\(938 \times 53 = 49714\\).
>
> What is the total of all the name scores in the file?

**Алгоритм:**

Следующий код на Scala используется для вычисления алфавитной стоимости имен, хранящихся в файле "p022_names.txt".

1. Определение функции `alphabeticalValue`, которая принимает символ `char` и возвращает его алфавитную стоимость. 
   Алфавитная стоимость определяется как позиция символа в алфавите, если символ является буквой, иначе стоимость равна 0.
2. Чтение имен из файла "p022_names.txt" и сохранение их в переменной `names`. 
   Имена считываются из файла, разделены запятыми, отсортированы и сохранены в виде массива строк.
3. Вычисление алфавитной стоимости каждого имени в отсортированном списке имен. 
   Для каждого имени вычисляется сумма алфавитных значений его букв, умноженная на порядковый номер имени 
   в отсортированном списке (индекс + 1).

**Код:**

```dotty
def alphabeticalValue(char: Char): Int =
  if char.isLetter then char.toUpper.toInt - 'A' + 1 else 0

val names = Source
  .fromResource("p022_names.txt")
  .getLines
  .toSeq
  .mkString
  .split(",")
  .sorted
val count = names.indices.map: i =>
  (i + 1L) * names(i).map(alphabeticalValue).sum
.sum
```

## Задача №23

[Задача №23 - Non-Abundant Sums](https://projecteuler.net/problem=23)

> A perfect number is a number for which the sum of its proper divisors is exactly equal to the number. 
> For example, the sum of the proper divisors of 28 would be 1 + 2 + 4 + 7 + 14 = 28, 
> which means that 28 is a perfect number.
> 
> A number n is called deficient if the sum of its proper divisors is less than n 
> and it is called abundant if this sum exceeds n.
> 
> As 12 is the smallest abundant number, 1 + 2 + 3 + 4 + 6 = 16, 
> the smallest number that can be written as the sum of two abundant numbers is 24. 
> By mathematical analysis, it can be shown that all integers greater than 28123 can be written as 
> the sum of two abundant numbers. However, this upper limit cannot be reduced any further by analysis 
> even though it is known that the greatest number that cannot be expressed as the sum of 
> two abundant numbers is less than this limit.
> 
> Find the sum of all the positive integers which cannot be written as the sum of two abundant numbers.

**Алгоритм:**

Следующий код используется для нахождения суммы всех положительных целых чисел, 
которые не могут быть представлены как сумма двух избыточных чисел. 
Избыточное число - это число, сумма делителей которого превышает само число.

1. Определение функции `sumOfDivisors`, которая вычисляет сумму делителей заданного числа. Описана в Problem 21.
2. Определение функции `isAbundant`, которая проверяет, является ли число избыточным.
3. Определение предела `limit` и нахождение всех избыточных чисел в диапазоне от `12` до `limit - 11`.
4. Создание массива `canBeSumOfTwoAbundant` размером `limit + 1`, который будет использоваться для отметки чисел, 
   которые могут быть представлены как сумма двух избыточных чисел.
5. Для каждого избыточного числа `a` в диапазоне от `12` до `limit - 11`, если `a` меньше или равно половине `limit`, 
   то для каждого избыточного числа `b` в диапазоне от `i` до `abundantNumbers.length`, 
   если сумма `a` и `b` меньше или равна `limit`, то в массиве `canBeSumOfTwoAbundant` 
   помечается число `s`, равное сумме `a` и `b`, как то, которое можно представить в виду суммы избыточных чисел.
6. Нахождение суммы всех чисел от `1` до `limit`, которые не могут быть представлены как сумма двух избыточных чисел, 
   используя массив `canBeSumOfTwoAbundant`.

**Код:**

```dotty
def sumOfDivisors(number: Long): Long = ... // Из Problem 21

def isAbundant(n: Long): Boolean =
  sumOfDivisors(n) > 2 * n

val limit           = 28123
val abundantNumbers = (12 to (limit - 11)).filter(isAbundant)

val half = limit / 2
val sum =
  val canBeSumOfTwoAbundant = new Array[Boolean](limit + 1)
  for
    i <- abundantNumbers.indices
    a = abundantNumbers(i)
    if a <= half
    j <- i until abundantNumbers.length
    s = a + abundantNumbers(j)
    if s <= limit
  do canBeSumOfTwoAbundant(s) = true

  (1 to limit).filterNot(i => canBeSumOfTwoAbundant(i)).sum
```

## Задача №24

[Задача №24 - Lexicographic Permutations](https://projecteuler.net/problem=24)

> A permutation is an ordered arrangement of objects. 
> For example, 3124 is one possible permutation of the digits 1, 2, 3 and 4. 
> If all of the permutations are listed numerically or alphabetically, we call it lexicographic order. 
> The lexicographic permutations of 0, 1 and 2 are:
> 
> 012   021   102   120   201   210
> 
> What is the millionth lexicographic permutation of the digits 0, 1, 2, 3, 4, 5, 6, 7, 8 and 9?

**Алгоритм:**

Следующий код на Scala используется для нахождения i-й перестановки заданного списка чисел.
 
Алгоритм работает следующим образом:

- Первая цифра `i`-й (начиная с `1`) перестановки расположенных по возрастанию цифр (`rest`) - 
  это `k`-й элемент из `rest`, где `k = (i - 1) / countOfRests`, `countOfRests` -  количество 
  перестановок из `rest.length - 1` элементов. Это следует из того, что перед `i`-й перестановкой будет 
  `k * countOfRests` перестановок, которые будут начинаться с цифр, занимающих индексы с `0` до `k - 1`.
  Например, первая цифра `5`-й перестановки из `List(7, 8, 9)` - это `9 = List(7, 8, 9)(k)`, где `k = 4 / 2 = 2`,
  потому что с `7` начинаются первые `2` элемента, с `8` - следующие `2` элемента.
  Итого, после первых `4`-ех элементов последующие элементы начинаюся на `9`.
- Вычислив первую цифру `i`-й перестановки, все последующие цифры можно вычислить применив алгоритм рекурсивно 
  для `j` и `tail`, где `j = i - k * countOfRests` и `tail` - `rest` без вычесленной цифры - найти 
  `j`-ю позицию среди перестановок оставшихся цифр.

**Код:**

```dotty
def factorial(n: Int): Int = (2 to n).product

@tailrec
def getIPerm(i: Int, rest: List[Int], acc: List[Int]): List[Int] =
  if rest.isEmpty then acc
  else
    val countOfRests = factorial(rest.length - 1)
    val k            = (i - 1) / countOfRests
    if k == 0 then getIPerm(i, rest.tail, rest.head :: acc)
    else
      getIPerm(
        i - k * countOfRests,
        rest.filterNot(_ == rest(k)),
        rest(k) :: acc
      )

def getIPermutation(i: Int, rest: List[Int]): Long =
  getIPerm(i, rest, List.empty[Int]).reverse.mkString.toLong

getIPermutation(1, List(0, 1, 2)) // 12
getIPermutation(2, List(0, 1, 2)) // 21
getIPermutation(3, List(0, 1, 2)) // 102
getIPermutation(4, List(0, 1, 2)) // 120
getIPermutation(5, List(0, 1, 2)) // 201
getIPermutation(6, List(0, 1, 2)) // 210
```

## Задача №25

[Задача №25 - 1000-digit Fibonacci Number](https://projecteuler.net/problem=25)

> The Fibonacci sequence is defined by the recurrence relation:
> \\(F\_{n} = F\_{n-1} + F\_{n-2}\\), where \\(F\_{1} = 1\\) and \\(F\_{2} = 1\\).
> Hence the first  terms will be:
> \\(F\_{1} = 1, F\_{2} = 1, F\_{3} = 2, F\_{4} = 3, F\_{5} = 5, F\_{6} = 8, F\_{7} = 13, F\_{8} = 21, F\_{9} = 34, F\_{10} = 55, F\_{11} = 89, F\_{12} = 144\\)
> 
> The 12th term, \\(F\_{12}\\), is the first term to contain three digits. 
> 
> What is the index of the first term in the Fibonacci sequence to contain 1000 digits?

**Алгоритм:**

Значение числа Фибоначчи можно вычислить [по формуле Бине](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

\\(F\_{n} = \frac{(\frac{1+\sqrt{5}}{2})^{n} - (\frac{1-\sqrt{5}}{2})^{n}}{\sqrt{5}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{\varphi - (-\varphi)^{-1}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{2\varphi - 1}\\)

Чтобы найти первое число Фибоначчи, содержащее 1000 цифр, надо найти \\(F\_{n} \simeq \frac{\varphi^{n}}{2\varphi - 1} \geq 10^{999}\\).

Что эквивалентно:

- \\(log\_{10}(\varphi^{n}) - log\_{10}(2\varphi - 1) \geq 999\\)
- \\(n*log\_{10}(\varphi) \geq 999 + log\_{10}(2\varphi - 1)\\)
- \\(n \geq \frac{999 + log\_{10}(2\varphi - 1)}{log\_{10}(\varphi)}\\)

**Код:**

```dotty
val phi: Double = (1.0 + math.sqrt(5.0)) / 2.0
val count: Int =
  ((999 + math.log10(2 * phi - 1)) / math.log10(phi)).toInt + 1
```

## Задача №26

[Задача №26 - Reciprocal Cycles](https://projecteuler.net/problem=26)

> A unit fraction contains 1 in the numerator. 
> The decimal representation of the unit fractions with denominators 2 to n are given:
> 
> 1/2 = 0.5; 1/3 = 0.(3); 1/4 = 0.25; 1/5 = 0.2; 1/6 = 0.1(6); 1/7 = 0.(142857); 1/8 = 0.125; 1/9 = 0.(1); 1/10 = 0.1
> 
> Where 0.1(6) means 0.16666..., and has a 1-digit recurring cycle. It can be seen that \\(\frac{1}{7}\\) 
> has a 6-digit recurring cycle.
> 
> Find the value of d < 1000 for which \\(\frac{1}{d}\\) contains the longest recurring cycle in its decimal fraction part.

**Алгоритм:**

Следующий код реализует алгоритм деления с остатком, который находит цикл в последовательности десятичных дробей.
Функция `getRecurringCycle(d: Int)` принимает целочисленный параметр `d` и возвращает список целых чисел, 
представляющих цикл в десятичной дроби `1/d`.
Внутри функции `getRecurringCycle(d: Int)` определена внутренняя рекурсивная функция 
`loop(a: Int, chain: List[Int], prev: List[Int])`.
Внутри `loop`, если `a` равно `0`, функция возвращает пустой список `Nil` - цикла нет, 
последовательность "остатков" закончилась. 
В противном случае, она ищет индекс `i` числа `a` в списке `prev` (предыдущей цепи "остатков"). 
Если `i` не равно `-1` ("остаток" повторился в `prev`), то функция возвращает перевернутую часть списка 
`chain` от начала до индекса `i + 1`. Это означает, что мы нашли цикл в десятичной дроби.
Если `i` равно `-1` ("остаток" **ещё** не повторился в `prev`), то функция вычисляет `k` и `r`. 
`k` - целая часть от деления `a` на `d` (добавляется в список `chain`), 
а `r` - остаток от деления умноженный на `10` (следующее число для аналаза). 
Затем функция вызывает саму себя с обновленными значениями `a`, `chain` и `prev`.

**Код:**

Вычислить циклическую часть в дроби можно так:

```dotty
def getRecurringCycle(d: Int): List[Int] =
  @tailrec
  def loop(a: Int, chain: List[Int], prev: List[Int]): List[Int] =
    if a == 0 then Nil
    else
      val i = prev.indexOf(a)
      if i != -1 then chain.take(i + 1).reverse
      else
        val k = a / d
        val r = (a - k * d) * 10
        loop(r, k :: chain, a :: prev)

  loop(10, Nil, Nil)

getRecurringCycle(2)  // Nil
getRecurringCycle(3)  // List(3)
getRecurringCycle(4)  // Nil
getRecurringCycle(5)  // Nil
getRecurringCycle(6)  // List(6)
getRecurringCycle(7)  // List(1, 4, 2, 8, 5, 7)
getRecurringCycle(8)  // Nil
getRecurringCycle(9)  // List(1)
getRecurringCycle(10) // Nil
```

Аналогично можно вычислить длину цикла:

```dotty
def getRecurringCycleLength(d: Int): Int =
  @tailrec
  def loop(a: Int, chain: List[Int], prev: List[Int]): Int =
    if a == 0 then 0
    else
      val i = prev.indexOf(a)
      if i != -1 then i + 1
      else
        val k = a / d
        val r = (a - k * d) * 10
        loop(r, k :: chain, a :: prev)

  loop(10, Nil, Nil)
```

## Задача №27

[Задача №27 - Quadratic Primes](https://projecteuler.net/problem=27)

> Euler discovered the remarkable quadratic formula: \\(n^{2} + n + 41\\).
>
> It turns out that the formula will produce 40 primes for the consecutive integer values \\(0 \leq n \leq 39\\). 
> However, when n = 40, \\(40^{2} + 40 + 41 = 40(40 + 1) + 41\\) is divisible by 41, 
> and certainly when \\(n = 41, 41^{2} + 41 + 41\\) is clearly divisible by 41.
> 
> The incredible formula \\(n^{2} - 79n + 1601\\) was discovered, 
> which produces 80 primes for the consecutive values \\(0 \leq n \leq 79\\). 
> The product of the coefficients, -79 and 1601, is -126479.
> 
> Considering quadratics of the form:
> \\(n^{2} + an + b\\), where \\(|a| < 1000\\) and \\(|b| \leq 1000\\).
> where |n| is the modulus/absolute value of n
> e.g. |11| = 11 and |-4| = 4
> Find the product of the coefficients, a and b, for the quadratic expression that produces 
> the maximum number of primes for consecutive values of n, starting with 0.

**Алгоритм:**

1. `isPrime(n: Long): Boolean` - функция, которая проверяет, является ли число простым.
2. `getCountOfPrimes(a: Int, b: Int): Int` - функция, возвращающая количество простых чисел, 
   которые равны \\(n^{2} + an + b\\) для заданных a и b, где n пробегает значения от 0 до такого первого значения k,
   при котором результат применения формулы - составное число. При этом k <= b, 
   т.к. при k = b имеем: \\(b^{2} + ab + b\\) делится на b.
3. `sieveOfEratosthenes(n: Int): Array[Boolean]` - функция, которая реализует алгоритм "Решето Эратосфена" 
   для поиска всех простых чисел до `n`. Функция описана в разделе Problem 10.
4. `primesNoMoreThanN(n: Int): Array[Int]` - функция, которая возвращает массив всех простых чисел, не превышающих `n`.
5. `abWithMaxCountOfPrimes: (Int, Int)` - функция, которая ищет пару `(a, b)`, для которой количество простых чисел, 
   получаемых при подстановке в квадратичную формулу \\(n^{2} + an + b\\), является максимальным. 
   Заметим, что b - простое число, т.к. при n = 0 результат формулы должен быть простым числом.
   При n = 1 получим, что \\(1 + a + b\\) тоже простое число, а значит, \\(1 + a + b \geq 2 \Rightarrow a \geq 1 - b\\).

**Код:**

```dotty
def isPrime(n: Long): Boolean =
  if n < 2 then false
  else if n < 4 then true // 2 и 3 - простые
  else if n % 2 == 0 then false
  else if n < 9 then true // мы уже исключили 4,6 и 8
  else if n % 3 == 0 then false
  else
    val limit = math.ceil(math.sqrt(n.toDouble)).toLong

    @annotation.tailrec
    def loop(f: Long): Boolean =
      if f > limit then true
      else if n % f == 0 || n % (f + 2) == 0 then false
      else loop(f + 6)

    loop(5)

def getCountOfPrimes(a: Int, b: Int): Int =
  @tailrec
  def loop(n: Int, count: Int): Int =
    lazy val m = n * n + a * n + b
    if n < b && isPrime(m) then loop(n + 1, count + 1)
    else count

  loop(0, 0)

getCountOfPrimes(1, 41)     // 40
getCountOfPrimes(-79, 1601) // 80

def sieveOfEratosthenes(n: Int): Array[Boolean] = ... // аналогично методу из Problem 10

def primesNoMoreThanN(n: Int): Array[Int] =
  sieveOfEratosthenes(n).zipWithIndex.collect { case (true, prime) => prime }

def abWithMaxCountOfPrimes: (Int, Int) =
  val pairs =
    for
      b <- primesNoMoreThanN(1000)
      a <- 1 - b to 999
      if isPrime(1 + a + b)
    yield (a, b)

  pairs.maxBy(getCountOfPrimes)
```

## Задача №28

[Задача №28 - Number Spiral Diagonals](https://projecteuler.net/problem=28)

> Starting with the number 1 and moving to the right in a clockwise direction a 5 by 5 spiral is formed as follows:
> 
> 21 22 23 24 25
> 
> 20  7  8  9 10
> 
> 19  6  1  2 11
> 
> 18  5  4  3 12
> 
> 17 16 15 14 13
> 
> It can be verified that the sum of the numbers on the diagonals is 101.
> 
> What is the sum of the numbers on the diagonals in a 1001 by 10001 spiral formed in the same way?

**Алгоритм:**

Определим \\(S\_{k}\\) как искомую сумму чисел по диагонали спирали размера \\((2k + 1) \times (2k + 1)\\).
Тогда \\(S\_{0} = 1, S\_{1} = 25, S\_{2} = 101\\).

Заметим, что \\(S\_{k} = S\_{k - 1} + d\_{1} + d\_{2} + d\_{3} + d\_{4}\\), где \\(d\_{i}\\) - числа по диагонали.
При этом \\(d\_{i} = size\_{k - 1} + i*(row\_{k} - 1)\\), где \\(row\_{k} = 2k + 1\\) - размер стороны k-й спирали, 
а \\(size\_{k} = row\_{k}^{2}\\) - максимальное число (или, что тоже самое, размер квадрата) k-й спирали. 

Получим: 

- \\(S\_{k} = S\_{k - 1} + 4size\_{k - 1} + 10(row\_{k} - 1)\\).
- \\(S\_{k} = 1 + 4\sum\_{i = 0}^{k - 1}size\_{i} + 10\sum\_{i = 1}^{k}row\_{i} - 10k\\)
- \\(S\_{k} = 1 + 4 + 4(4\sum\_{i = 1}^{k - 1}i^{2} + 4\sum\_{i = 1}^{k - 1}i + k - 1) + 20\sum\_{i = 1}^{k}i\\)
- \\(S\_{k} = 1 + 24k + 16\sum\_{i = 1}^{k - 1}i^{2} + 36\sum\_{i = 1}^{k - 1}i\\)

Искомая по задаче сумма - это \\(S\_{500}\\) (т.к. 1001 = 2k + 1).

**Код:**

```dotty
def sumOfSquaresTo(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

def sumToGiven(n: Long): Long = n * (n + 1) / 2

def getCount(n: Int): Long =
  16 * sumOfSquaresTo(n - 1) + 36 * sumToGiven(n - 1) + 24 * n + 1

getCount(0) // 1
getCount(1) // 25
getCount(2) // 101
```

## Задача №29

[Задача №29 - Distinct Powers](https://projecteuler.net/problem=29)

> Consider all integer combinations of \\(a^{b}\\) for \\(2 \leq a \leq 5\\) and \\(2 \leq a \leq 5\\).
> 
> If they are then placed in numerical order, with any repeats removed, we get the following sequence of 15 distinct terms:
> 
> 4, 8, 9, 16, 25, 27, 32, 64, 81, 125, 243, 256, 625, 1024, 3125.
> 
> How many distinct terms are in the sequence generated by \\(a^{b}\\) for \\(2 \leq a \leq 100\\) and \\(2 \leq a \leq 100\\)?

**Алгоритм:**

Следующий код демонстрирует несколько функций:

1. `primeFactorsWithPow(n: Long): Map[Long, Int]` - функция находит простые множители числа `n` 
   и возвращает их в виде `Map`, где ключ - простой множитель, а значение - степень, 
   в которой он встречается в разложении числа `n`.
2. `getAView(a: Int): IndexedSeq[String]` - функция принимает целое число `a` и 
   возвращает последовательность строк, представляющих различные представления числа `a` 
   в виде произведения простых множителей с указанными степенями.
3. `val count = (2 to 100).flatMap(getAView).distinct.length` - код создает последовательность всех представлений чисел 
   от 2 до 100, удаляет дубликаты и считает количество уникальных представлений.

**Код:**

```dotty
def primeFactorsWithPow(n: Long): Map[Long, Int] = ... // Из Problem 5

def getAView(a: Int): IndexedSeq[String] =
  val number = primeFactorsWithPow(a)
  (2 to 100).map: b =>
    number
      .map { case (key, power) => s"$key-${power * b}" }
      .toSeq
      .sorted
      .mkString("--")

val count = (2 to 100).flatMap(getAView).distinct.length
```

## Задача №30

[Задача №30 - Digit Fifth Powers](https://projecteuler.net/problem=30)

> Surprisingly there are only three numbers that can be written as the sum of fourth powers of their digits:
> 
> \\(1634 = 1^{4} + 6^{4} + 3^{4} + 4^{4}, 8208 = 8^{4} + 2^{4} + 0^{4} + 8^{4}, 9474 = 9^{4} + 4^{4} + 7^{4} + 4^{4}\\)
> 
> As \\(1 = 1^{4}\\) is not a sum it is not included.
> 
> The sum of these numbers is 1634 + 8208 + 9474 = 19316.
> 
> Find the sum of all the numbers that can be written as the sum of fifth powers of their digits.

**Алгоритм:**

Следующий метод находит все числа, которые можно представить в виде суммы `p`-х степеней их цифр, 
когда эти числа состоят из четырех цифр.
Для начала, метод использует генератор `for` для создания всех возможных комбинаций четырех цифр. 
Каждая цифра `a`, `b`, `c`, `d` входит в диапазон от 0 до 9.
Затем для каждой комбинации вычисляется `p`-я степень каждой цифры и проверяется, 
что сумма этих степеней не превышает число, составленное из этих цифр - 
для того, чтобы избежать перебора несуществующих чисел.
Наконец, если сумма `p`-х степеней цифр равна числу, составленному из этих цифр, 
то это число добавляется в результирующую последовательность.

Аналогично можно создать методы для чисел, состоящих от 2-х до 6-ти цифр.
Число из одной цифры суммой не является, а число из более чем 6 цифр не может равняться сумме пятых степеней
своих цифр, т.к. \\(k\*9^{5} = k\*59049 \leq 10^{k}\\) для всех k больше 6.

**Код:**

```dotty
def getSumOfPPowersWith4Digits(p: Int): IndexedSeq[Int] =
  for
    a <- 1 to 9
    ap = math.pow(a, p).toInt
    if ap < 1000 * (a + 1)
    b <- 0 to 9
    bp = math.pow(b, p).toInt
    if ap + bp < (1000 * a + 100 * (b + 1))
    c <- 0 to 9
    cp = math.pow(c, p).toInt
    if ap + bp + cp < (1000 * a + 100 * b + 10 * (c + 1))
    d <- 0 to 9
    dp = math.pow(d, p).toInt
    if ap + bp + cp + dp == 1000 * a + 100 * b + 10 * c + d
  yield 1000 * a + 100 * b + 10 * c + d

getSumOfFifthPowersWith4Digits(4) 
// IndexedSeq(1634, 8208, 9474)
```

## Задача №31

[Задача №31 - Coin Sums](https://projecteuler.net/problem=31)

> In the United Kingdom the currency is made up of pound (£) and pence (p). 
> There are eight coins in general circulation:
> 1p, 2p, 5p, 10p, 20p, 50p, £1 (100p), and £2 (200p). 
> 
> It is possible to make £2 in the following way:
> 1×£1 + 1×50p + 2×20p + 1×5p + 1×2p + 3×1p
> 
> How many different ways can £2 be made using any number of coins?

**Алгоритм:**

Следующий код на Scala решает задачу о размене монет.
Он определяет функцию `countWays`, которая принимает два параметра: 
`coins` - массив доступных монет, и `sum` - сумма, которую нужно разменять.

Внутри функции создается массив `ways`, 
который используется для хранения количества способов размена для каждой суммы от 0 до `sum`. 
Изначально все элементы массива инициализируются нулями.
Затем `ways(0)` устанавливается равным `1`, 
так как существует ровно один способ размена нулевой суммы - пустой набор монет.

Далее для каждой монеты из массива `coins` выполняется следующее:

- Для каждой суммы `i` от `coin` до `sum`:
    - `ways(i)` увеличивается на `ways(i - coin)`, что означает, что для текущей суммы `i` 
      количество способов размена увеличивается на количество способов размена суммы `i - coin` без текущей монеты.

В конце функция возвращает `ways(sum)` - количество способов размена суммы `sum`.

**Код:**

```dotty
def countWays(coins: Array[Int], sum: Int): Long =
  val ways = Array.fill(sum + 1)(0L)
  ways(0) = 1
  coins.foreach: coin =>
    (coin to sum).foreach: i =>
      ways(i) += ways(i - coin)
  ways(sum)

countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), 200)
```

## Задача №32

[Задача №32 - Pandigital Products](https://projecteuler.net/problem=32)

> We shall say that an n-digit number is pandigital if it makes use of all the digits 1 to n exactly once; 
> for example, the 5-digit number, 15234, is 1 through 5 pandigital.
>
> The product 7254 is unusual, as the identity, 39 * 186 = 7254, containing multiplicand, multiplier, and product 
> is 1 through 9 pandigital.
>
> Find the sum of all products whose multiplicand/multiplier/product identity can be written as a 1 through 9 pandigital.
>
> HINT: Some products can be obtained in more than one way so be sure to only include it once in your sum.

**Алгоритм:**

1. Определяем функцию `isProductCorrect`, которая проверяет, является ли произведение `product` корректным. 
   Произведение считается корректным, если оно состоит из 4 уникальных цифр (не включая 0) 
   и не содержит цифр, которые входят в состав множителей `multiplicand` и `multiplier`.
2. Определяет функцию `getAllPandigital`, генерирующую все возможные комбинации множителей и произведений, 
   которые могут быть получены путем умножения множителей (двухзначного и трехзначного, либо однозначного и четырехзначного).
3. Внутри `getAllPandigital` также определяется вектор `candidates`, 
   который содержит пару множителей и их произведение, отфильтрованный с помощью `isProductCorrect`.
4. Функция `getDistinctProductSum` использует `getAllPandigital` для получения всех возможных комбинаций, 
   извлекает уникальные произведения, суммирует их и возвращает результат.

**Код:**

```dotty
def isProductCorrect(
    multiplicand: String,
    multiplier: String,
    product: String
): Boolean =
  product.length == 4 &&
    product.distinct.length == 4 &&
    !product.contains('0') &&
    (multiplicand + multiplier).forall(ch => !product.contains(ch))

def getAllPandigital: IndexedSeq[IndexedSeq[(String, String, String)]] =
  for
    a <- 1 to 9
    b <- 1 to 9
    if b != a
    c <- 1 to 9
    if c != a && c != b
    d <- 1 to 9
    if d != a && d != b && d != c
    e <- 1 to 9
    if e != a && e != b && e != c && e != d
  yield
    val multiplicand1 = 10 * a + b
    val multiplier1   = 100 * c + 10 * d + e
    val product1      = (multiplicand1 * multiplier1).toString

    val multiplicand2 = a
    val multiplier2   = 1000 * b + multiplier1
    val product2      = (multiplicand2 * multiplier2).toString

    val candidates: Vector[(String, String, String)] =
      Vector(
        (multiplicand1.toString, multiplier1.toString, product1),
        (multiplicand2.toString, multiplier2.toString, product2)
      )
    candidates.filter(isProductCorrect)

def getDistinctProductSum: Int =
  getAllPandigital.flatten.map(_._3.toInt).distinct.sum
```

## Задача №33

[Задача №33 - Digit Cancelling Fractions](https://projecteuler.net/problem=33)

> The fraction 49/98 is a curious fraction, as an inexperienced mathematician in attempting 
> to simplify it may incorrectly believe that 49/98 = 4/8, which is correct, is obtained by cancelling the 9s.
>
> We shall consider fractions like, 30/50 = 3/5, to be trivial examples.
> 
> There are exactly four non-trivial examples of this type of fraction, less than one in value, 
> and containing two digits in the numerator and denominator.
> 
> If the product of these four fractions is given in its lowest common terms, find the value of the denominator.

**Алгоритм:**

Из условия задачи следует:

- все четыре цифры в дроби больше 0, т.к. если встречается 0, то его вычеркивать нельзя (тривиальный пример),
  а если он останется после вычеркивания второй цифры, то получается либо нулевая дробь (тривиальный пример),
  либо 0 в знаменателе.
- остаются варианты:
    - \\(\frac{ab}{cb} = \frac{a}{c} < 1\\)
    - \\(\frac{ac}{cb} = \frac{a}{b} < 1\\)
    - \\(\frac{ab}{ca} = \frac{b}{c} < 1\\)
    - \\(\frac{ab}{ac} = \frac{b}{c} < 1\\)
- варианта \\(\frac{ab}{cb} = \frac{a}{c} < 1\\) не может быть, т.к. в этом случае 
  \\((10\*a + b)\*c = a\*(10\*c + b) \Rightarrow a = c\\), что противоречит условию, что дробь должна быть меньше 1.
- аналогично не может быть варианта \\(\frac{ab}{ac} = \frac{b}{c} < 1\\), т.к. в этом случае
  \\((10\*a + b)\*c = b\*(10\*a + c) \Rightarrow 10\*a\*c = 10\*a\*b \Rightarrow b = c\\), 
  что противоречит условию, что дробь должна быть меньше 1.
- остаются варианты:
    - \\(\frac{ac}{cb} = \frac{a}{b} < 1\\)
    - \\(\frac{ab}{ca} = \frac{b}{c} < 1\\)
- из варианта \\(\frac{ac}{cb} = \frac{a}{b} < 1\\) следует, что 
  \\(a < b; 9\*a\*b + b\*c = 10\*a\*c \Rightarrow 9\*a\*b + b\*c < 10\*b\*c \Rightarrow a < c\\).
  Предположим, что \\(b \geq c\\), тогда \\(9\*a\*c + c\*c \leq 10\*a\*c \Rightarrow c \leq a\\), что невозможно.
  Значит, \\(a < b < c\\)
- из варианта \\(\frac{ab}{ca} = \frac{b}{c} < 1\\) следует, что
  \\(b < c; 10\*a\*c = 9\*b\*c + a\*b \Rightarrow 10\*a\*c < 9\*c\*c + a\*c \Rightarrow a < c\\).
  Предположим, что \\(a \geq b\\), тогда \\(10\*b\*c \leq 9\*b\*c + a\*b \Rightarrow c \leq a\\), что невозможно.
  Значит, \\(a < b < c\\)
- Варианты \\(\frac{ac}{cb} = \frac{a}{b} < 1\\) и \\(\frac{ab}{ca} = \frac{b}{c} < 1\\) одновременно случиться не могут,
  т.к. в этом случае \\(9\*a\*b + b\*c = 10\*a\*c = 9\*b\*c + a\*b \Rightarrow a = c\\), что невозможно.
- Осталось перебрать все варианты...

**Код:**

```dotty
def getAllFractions: IndexedSeq[(Int, Int)] =
  (
    for
      a <- 1 to 7
      b <- a + 1 to 8
      c <- b + 1 to 9
    yield
      val ac = a * 10 + c
      val cb = c * 10 + b
      val ab = a * 10 + b
      val ca = c * 10 + a
      if ac * b == a * cb then Some((ac, cb))
      else if ab * c == b * ca then Some((ab, ca))
      else None
  ).flatten

@tailrec
def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)

def getFractionDenominator: Long =
  val (n, d) = getAllFractions.foldLeft((1L, 1L)) { case ((n, d), (fn, fd)) =>
    (n * fn, d * fd)
  }
  d / gcd(n, d)
```

## Задача №34

[Задача №34 - Digit Factorials](https://projecteuler.net/problem=34)

> 145 is a curious number, as 1! + 4! + 5! = 1 + 24 + 120 = 145.
>
> Find the sum of all numbers which are equal to the sum of the factorial of their digits.
>
> Note: As 1! = 1 and 2! = 2 are not sums they are not included.

**Алгоритм:**

Заметим, что достаточно рассмотреть только k-значные числа, где 2 < k < 7, т.к. \\(9!\*k < 10^{k}\\) для всех k > 7.
Остальные варианты можно перебрать.

**Код:**

Для трехзначного числа перебор может быть таким, предварительно вычислив все факториалы от 0 до 9,
чтобы не вычислять их более одного раза:

```dotty
def factorial(n: Int): BigInt =
  (2 to n).foldLeft(BigInt(1))(_ * _)

val factorials = (0 to 9).map(factorial)

def getLuckyNumberWithThreeDigits: IndexedSeq[Int] =
  for
    a <- 1 to 9
    limit = (a + 1) * 100
    if factorials(a) < limit
    b <- 0 to 9
    if factorials(a) + factorials(b) < limit
    c <- 0 to 9
    if factorials(a) + factorials(b) + factorials(c) < limit
    abc = a * 100 + b * 10 + c
    if abc == factorials(a) + factorials(b) + factorials(c)
  yield abc
```

## Задача №35

[Задача №35 - Circular Primes](https://projecteuler.net/problem=35)

> The number, 197, is called a circular prime because all rotations of the digits: 197, 971, and 719, are themselves prime.
> 
> There are thirteen such primes below 100: 2, 3, 5, 7, 11, 13, 17, 31, 37, 71, 73, 79, and 97.
> 
> How many circular primes are there below one million?

**Алгоритм:**

- `getCircularRotation(n: Int): IndexedSeq[Int]` - функция принимает целое число n и возвращает последовательность 
  целых чисел, которая является результатом циклического сдвига цифр числа n. 
- `sieveOfEratosthenes(n: Int): Array[Boolean]` - функция реализует алгоритм "Решето Эратосфена".
- `countPrimes(limit: Int): Int` - функция использует функцию `sieveOfEratosthenes` 
  для получения массива простых чисел до `limit`. Затем она подсчитывает количество простых чисел, 
  которые являются циклическими перестановками других простых чисел.

**Код:**

```dotty
def getCircularRotation(n: Int): IndexedSeq[Int] =
  if n < 10 then IndexedSeq(n)
  else
    val m = n.toString
    m.indices.map: i =>
      (m.drop(i) + m.take(i)).toInt

def sieveOfEratosthenes(n: Int): Array[Boolean] = ... // из Problem 10

def countPrimes(limit: Int): Int =
  val isPrimeArr = sieveOfEratosthenes(limit)
  isPrimeArr.indices.count: i =>
    isPrimeArr(i) && getCircularRotation(i).forall(isPrimeArr)

getCircularRotation(197) // Vector(197, 971, 719)
countPrimes(100)         // 13
```

## Задача №36

[Задача №36 - Double-base Palindromes](https://projecteuler.net/problem=36)

> The decimal number, \\(585 = 1001001001\_{2}\\) (binary), is palindromic in both bases.
> 
> Find the sum of all numbers, less than one million, which are palindromic in base 10 and base 2.
> 
> (Please note that the palindromic number, in either base, may not include leading zeros.)

**Алгоритм:**

Заметим, что палиндром не может заканчиваться на четную цифру, 
т.к. в этом случае в двоичном представлении перевернутое число будет начинаться с 0, что противоречит условию задачи.

1. `isPalindrome(number: Long, base: Int): Boolean` - функция проверяет, является ли число `number` палиндромом 
   в системе счисления с основанием `base`.
2. `getAllPalindromes` - функция использует функцию `isPalindrome` для фильтрации всех нечетных чисел от 1 до 999999. 
   Она проверяет, являются ли числа палиндромами в десятичной и двоичной системах счисления. 
   Функция возвращает список всех чисел, которые являются палиндромами в обеих системах счисления.

**Код:**

```dotty
def isPalindrome(number: Long, base: Int): Boolean =
  var revertedNumber = 0L
  var k              = number
  while k > revertedNumber
  do
    revertedNumber = revertedNumber * base + k % base
    k /= base
  k == revertedNumber || k == revertedNumber / base

def getAllPalindromes =
  (1 until 1000000 by 2).filter: i =>
    isPalindrome(i, 10) && isPalindrome(i, 2)
```

## Задача №37

[Задача №37 - Truncatable Primes](https://projecteuler.net/problem=37)

> The number 3797 has an interesting property. Being prime itself, it is possible to continuously remove digits 
> from left to right, and remain prime at each stage: 3797, 797, 97, and 7. 
> Similarly we can work from right to left: 3797, 379, 37, and 3.
>
> Find the sum of the only eleven primes that are both truncatable from left to right and right to left.
> 
> NOTE: 2, 3, 5, and 7 are not considered to be truncatable primes.

**Алгоритм:**

Следующий код реализует алгоритм для нахождения всех «целочисленных чисел, 
которые можно обрезать слева или справа и оставить простыми».
Код находит все 11 чисел, но не гарантирует, что "других нет".

[Ссылка на объяснение, почему таких чисел только 11.](https://projecteuler.net/thread=37;page=5#346003) 

**Код:**

```dotty
def sieveOfEratosthenes(n: Int): Array[Boolean] = ... // Из Problem 10

def allTruncatableNumbers: Seq[Int] =
  val isPrimes = sieveOfEratosthenes(1000000)

  @tailrec
  def isRightTruncatable(n: Int): Boolean =
    isPrimes(n) && (n < 10 || isRightTruncatable(n / 10))

  @tailrec
  def isLeftTruncatable(n: Int): Boolean =
    isPrimes(n) && (n < 10 || isLeftTruncatable(n.toString.tail.toInt))

  (10 until 1000000).filter: n =>
    isPrimes(n) && isRightTruncatable(n) && isLeftTruncatable(n)

allTruncatableNumbers
// Vector(23, 37, 53, 73, 313, 317, 373, 797, 3137, 3797, 739397)
```

## Задача №38

[Задача №38 - Pandigital Multiples](https://projecteuler.net/problem=38)

> Take the number 192 and multiply it by each of 1, 2, and 3: 
> \\(192 \times 1 = 192, 192 \times 2 = 384, 192 \times 3 = 576\\). 
>
> By concatenating each product we get the 1 to 9 pandigital, 192384576. 
> We will call 192384576 the concatenated product of 192 and (1, 2, 3).
> 
> The same can be achieved by starting with 9 and multiplying by 1, 2, 3, 4, and 5, 
> giving the pandigital, 918273645, which is the concatenated product of 9 and (1, 2, 3, 4, 5).
>
> What is the largest 1 to 9 pandigital 9-digit number that can be formed as the concatenated product of 
> an integer with (1, 2, 3, ... , n) where n > 1?

**Алгоритм:**

Следующий код реализует функции для поиска наибольшего панцифрового числа, 
которое можно получить путем объединения произведений целого числа `x` с последовательными целыми числами от `1` до `n`.

1. `isPandigital(number: String): Boolean` - функция, которая проверяет, является ли строка `number` панцифровой.
   Панцифровое число - это число, содержащее каждую цифру от 1 до 9 ровно один раз.
2. `getProduct(n: Int, i: Int, product: String): Option[Long]` - рекурсивная функция, 
   которая вычисляет произведение `n` на `i`, добавляет его к строке `product` и вызывает саму себя с увеличенным `i`, 
   пока длина строки `product` не станет равной или более 9. Если строка `product` является панцифровой, 
   функция возвращает ее в виде числа `Long`. В противном случае возвращает `None`.
3. `getAllNumbers: Long` - функция, которая использует функцию `getProduct` для каждого числа от `2` до `9999` 
   и возвращает наибольшее панцифровое число, которое можно получить.
4. Числа более `9999` не подходят, т.к. уже для `i = 2` превышают 9 знаков.

**Код:**

```dotty
def isPandigital(number: String): Boolean =
  number.length == 9 && !number.contains("0") &&
    number.toSeq.distinct.length == 9

@tailrec
def getProduct(n: Int, i: Int, product: String): Option[Long] =
  val newProduct = product + (n * i).toString
  if newProduct.length < 9 then getProduct(n, i + 1, newProduct)
  else if newProduct.length == 9 && isPandigital(newProduct) then
    Some(newProduct.toLong)
  else None

def getAllNumbers: Long =
  (2 until 10000).flatMap: n =>
    getProduct(n, i = 2, product = n.toString)
  .max
```

## Задача №39

[Задача №39 - Integer Right Triangles](https://projecteuler.net/problem=39)

> If p is the perimeter of a right angle triangle with integral length sides, {a, b, c}, 
> there are exactly three solutions for p = 120.
>
> {20, 48, 52}, {24, 45, 51}, {30, 40, 50}.
> 
> For which value of p <= 1000, is the number of solutions maximised?

**Алгоритм:**

Как вычислить Пифагорову тройку с заданной суммой [объясняется в соответствующем разделе](../../algorithms/algorithms/fibonacci.md).

**Код:**

```dotty
@tailrec
def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)

def pythagoreanTripletsWithGivenSum(sum: Long): Int =
  if sum % 2 == 1 then 0
  else
    var count = 0
    val s2          = sum / 2
    val sqrt        = math.sqrt(s2.toDouble).toLong
    val mLimit      = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

    for m <- 2L to mLimit do
      if s2 % m == 0 then
        var sm = s2 / m
        // сократим пространство поиска, удалив все делители 2
        while sm % 2 == 0 do sm /= 2
        var k = if m % 2 == 1 then m + 2 else m + 1
        while k < 2 * m && k <= sm
        do
          if sm % k == 0 && gcd(k, m) == 1 then
            val d = s2 / (k * m)
            val n = k - m
            val a = d * (m * m - n * n)
            val b = 2 * d * m * n
            val c = d * (m * m + n * n)
            count += 1
          k += 2

    count

val count = (1 to 1000).maxBy: i =>
  pythagoreanTripletsWithGivenSum(i)
```

## Задача №40

[Задача №40 - Champernowne's Constant](https://projecteuler.net/problem=40)

> An irrational decimal fraction is created by concatenating the positive integers:
> 0.123456789101112131415161718192021
> 
> It can be seen that the \\(12^{th}\\) digit of the fractional part is 1.
> 
> If \\(d\_{n}\\) represents the \\(n^{th}\\) digit of the fractional part, find the value of the following expression:
> \\(d\_{1} \times d\_{10} \times d\_{100} \times d\_{1000} \times d\_{10000} \times d\_{100000} \times d\_{1000000}\\)

**Алгоритм:**

Следующий код реализует функции для нахождения произведения определенных цифр в последовательности чисел, 
которая образуется путем объединения всех положительных целых чисел.

1. `getCountOfDigits(d: Int): Long` - функция, которая вычисляет количество цифр в числах, состоящих из `d` и меньше цифр.
2. `getD(d: Int): Int` - функция, которая находит цифру в числе, состоящем из `d` цифр. 
   Она находит число, в котором находится `d`-ая цифра, и определяет, какая цифра находится в этом числе на позиции `d`.
3. В конце кода определяется значение `count`, которое является произведением цифр, 
   находящихся на позициях 1, 10, 100, 1000, 10000, 100000 и 1000000 в последовательности чисел.

**Код:**

```dotty
def getCountOfDigits(d: Int): Long =
  val p = math.pow(10, d).toLong
  d * p - (p - 1) / 9

def getD(d: Int): Int =
  val digits      = (1 to 10000).find(i => getCountOfDigits(i) >= d).getOrElse(0)
  val rest        = d - getCountOfDigits(digits - 1) - 1
  val i           = rest / digits
  val j           = (rest % digits).toInt
  val number      = (math.pow(10, digits - 1).toLong + i).toString
  val resultDigit = number(j).getNumericValue
  resultDigit

val count =
  getD(1) * getD(10) * getD(100) * getD(1000) * getD(10000) *
    getD(100000) * getD(1000000)
```

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
