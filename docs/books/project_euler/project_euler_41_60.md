# Задачи №41-№60

## Задача №41

[Задача №41 - Pandigital Prime](https://projecteuler.net/problem=41)

> Будем считать n-значное число пан-цифровым, если каждая из цифр от 1 до n используется в нем ровно один раз. 
> К примеру, 2143 является 4-значным пан-цифровым числом, а также простым числом.
>
> Какое существует наибольшее n-значное пан-цифровое простое число?

**Алгоритм:**

1. `isPrime` - функция, которая проверяет, является ли число простым. Она использует алгоритм "Решето Эратосфена".
2. `xvariations` - функция, которая генерирует все возможные перестановки из списка чисел. 
   Она использует рекурсивный подход, где каждый элемент списка вставляется на каждое возможное место 
   в каждой возможной перестановке остальных элементов.
3. `getAllPandigitalAndPrime` - функция, которая генерирует все панцифровые перестановки чисел от 1 до 7, 
   проверяет каждую на простоту и возвращает список простых панцифровых чисел. 
4. Среди 8 и 9 панцифровых чисел простых нет, т.к. сумма цифр кратна 9, а это означает, что само число делится на 9.

**Код:**

```dotty
def isPrime(n: Long): Boolean = ... // Из Problem 27

def xvariations(l: List[Int], n: Int): List[List[Int]] =
  def mixmany(x: Int, ll: List[List[Int]]): List[List[Int]] =
    ll match
      case hd :: tl => foldone(x, hd) ::: mixmany(x, tl)
      case _        => Nil

  def foldone(x: Int, ll: List[Int]): List[List[Int]] =
    (1 to ll.length).foldLeft(List(x :: ll))((a, i) =>
      mixone(i, x, ll) :: a
    )

  def mixone(i: Int, x: Int, ll: List[Int]): List[Int] =
    ll.slice(0, i) ::: (x :: ll.slice(i, ll.length))

  if n > l.size then Nil
  else
    l match
      case _ :: _ if n == 1 => l.map(List(_))
      case hd :: tl =>
        mixmany(hd, xvariations(tl, n - 1)) ::: xvariations(tl, n)
      case _ => Nil
end xvariations

def getAllPandigitalAndPrime: Seq[Int] =
  xvariations((1 to 7).toList, 7)
    .withFilter(list => isPrime(list.mkString.toInt))
    .map(_.mkString.toInt)
```

## Задача №42

[Задача №42 - Coded Triangle Numbers](https://projecteuler.net/problem=42)

> n-й член последовательности треугольных чисел задается как \\(t\_{n} = \frac{n(n + 1)}{2}\\). 
> Таким образом, первые десять треугольных чисел: 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, ...
>
> Преобразовывая каждую букву в число, соответствующее ее порядковому номеру в алфавите, и складывая эти значения, 
> мы получим числовое значение слова. Для примера, числовое значение слова SKY равно \\(19 + 11 + 25 = 55 = t\_{10}\\).
> Если числовое значение слова является треугольным числом, то мы назовем это слово треугольным словом.
>
> Используя words.txt, 16 КБ текстовый файл, содержащий около двух тысяч часто используемых английских слов, 
> определите, сколько в нем треугольных слов.

**Алгоритм:**

1. `isTriangle` - функция, которая проверяет, является ли число треугольным.
2. `alphabeticalLetterValue` - функция, которая вычисляет значение буквы в алфавитном порядке.
3. `alphabeticalValue` - функция, которая вычисляет значение слова в алфавитном порядке.

**Код:**

```dotty
def isTriangle(n: Long): Boolean =
  val sol = math.round(-1.0 / 2.0 + sqrt(2.0 * n + 1.0 / 4.0))
  sol * (sol + 1) / 2 == n

def alphabeticalLetterValue(char: Char): Int =
  if char.isLetter then char.toUpper.toInt - 'A' + 1 else 0

def alphabeticalValue(word: String): Int =
  word.map(alphabeticalLetterValue).sum
  
val names = Source.fromResource("p042_words.txt").getLines.mkString.split(",")
val count = names.count(name => isTriangle(alphabeticalValue(name)))
```

## Задача №43

[Задача №43 - Sub-string Divisibility](https://projecteuler.net/problem=43)

> Число 1406357289, является пан-цифровым, поскольку оно состоит из цифр от 0 до 9 в определенном порядке. 
> Помимо этого, оно также обладает интересным свойством делимости подстрок.
>
> Пусть \\(d\_{1}\\) будет 1-й цифрой, \\(d\_{2}\\) будет 2-й цифрой, и т.д. 
> В таком случае, можно заметить следующее:
>
> - \\(d\_{2}d\_{3}d\_{4} = 406\\) делится на 2 без остатка
> - \\(d\_{3}d\_{4}d\_{5} = 063\\) делится на 3 без остатка
> - \\(d\_{4}d\_{5}d\_{6} = 635\\) делится на 5 без остатка
> - \\(d\_{5}d\_{6}d\_{7} = 357\\) делится на 7 без остатка
> - \\(d\_{6}d\_{7}d\_{8} = 572\\) делится на 11 без остатка
> - \\(d\_{7}d\_{8}d\_{9} = 728\\) делится на 13 без остатка
> - \\(d\_{8}d\_{9}d\_{10} = 289\\) делится на 17 без остатка
>
> Найдите сумму всех пан-цифровых чисел из цифр от 0 до 9, обладающих данным свойством.

**Алгоритм:**

Из условия задачи следует:

- \\(d\_{6} = 5\\), т.к. \\(d\_{6}\\) кратно 5, но не равно 0 из-за того, что в этом случае \\(d\_{7}d\_{8}\\) было бы
  кратно 11, что невозможно, т.к. все цифры должны быть разными по условию задачи.
- `lastThreeDigits` - вычисляет последние три цифры \\(d\_{8}d\_{9}d\_{10}\\): всего 53 трехзначных числа, 
  кратных 17, ещё меньше тех чисел, цифры которых разные и не содержат 5 - таких ровно 27 штук.
- `lastFiveDigits` - вычисляет \\(d\_{7}\\) или последние 5 цифр \\(d\_{6}d\_{7}d\_{8}d\_{9}d\_{10}, d\_{6} = 5\\),
  проверяя, что \\(d\_{7}d\_{8}d\_{9}\\) делится на 13, \\(d\_{6}d\_{7}d\_{8}\\) делится на 11.
  Таких чисел тоже только 27 штук.
- `lastSixDigits` - вычисляет \\(d\_{5}\\), проверяя, что \\(d\_{5}d\_{6}d\_{7}\\) делится на 7. Таких чисел всего 2.
- `lastEightDigits` - вычисляет \\(d\_{3}d\_{4}\\), проверяя, что \\(d\_{3}d\_{4}d\_{5}\\) делится на 3, 
  при этом \\(d\_{4}\\) - четное. Таких чисел всего 3.
- Оставшиеся две цифры - это \\(d\_{1}d\_{2}\\), они могут стоять в любом порядке. Итого, 2 * 3 = 6 чисел.

**Код:**

```dotty
def calculateNumber: Seq[Long] =
  val lastThreeDigits = (6 to 58)
    .map(_ * 17)
    .filter(i => i.toString.distinct.length == 3 && !i.toString.contains("5"))
  val lastFiveDigits = lastThreeDigits.flatMap: candidate =>
    (0 to 9)
      .withFilter(d7 => d7 != 5 && !candidate.toString.contains(d7.toString))
      .map(d7 => d7 * 1000 + candidate)
      .withFilter(newCandidate =>
        (newCandidate / 10) % 13 == 0 && (500 + newCandidate / 100) % 11 == 0
      )
      .map(newCandidate => 50000 + newCandidate)
  val lastSixDigits = lastFiveDigits.flatMap: candidate =>
    (0 to 9)
      .withFilter(d5 =>
        !candidate.toString.contains(d5.toString) &&
          (d5 * 100 + candidate / 1000) % 7 == 0
      )
      .map(d5 => d5 * 100000 + candidate)
  val lastEightDigits = lastSixDigits.flatMap: candidate =>
    val restDigits =
      (0 to 9).filter(i => !candidate.toString.contains(i.toString))
    for
      d4 <- restDigits
      if d4 % 2 == 0
      d3 <- restDigits
      d5 = candidate / 100000
      if d3 != d4 && (d3 + d4 + d5) % 3 == 0
    yield d3.toString + d4.toString + candidate.toString
  lastEightDigits.flatMap: candidate =>
    val restDigits = (0 to 9).filter(i => !candidate.contains(i.toString))
    for
      d2 <- restDigits
      d1 <- restDigits
      if d1 != d2
    yield (d1.toString + d2.toString + candidate).toLong
end calculateNumber
```

## Задача №44

[Задача №44 - Pentagon Numbers](https://projecteuler.net/problem=44)

> Пятиугольные числа вычисляются по формуле: \\(P\_{n} = \frac{n(3n - 1)}{2}\\).
> Первые десять пятиугольных чисел: 1, 5, 12, 22, 35, 51, 70, 92, 117, 145, ...
>
> Можно убедиться в том, что \\(P\_{4} + P\_{7} = 22 + 70 = 92 = P\_{8}\\). 
> Однако, их разность, 70 − 22 = 48, не является пятиугольным числом.
>
> Найдите пару пятиугольных чисел \\(P\_{j}\\) и \\(P\_{k}\\), для которых сумма и разность 
> являются пятиугольными числами и значение \\(D = |P\_{k} - P\_{j}|\\) минимально, 
> и дайте значение D в качестве ответа.

**Алгоритм:**

1. `pentagonalNumber(n: Long): Long` - функция принимает на вход `n` и возвращает `n`-ое пятиугольное число.
2. `isPentagonal(n: Long): Boolean` - функция проверяет, является ли число `n` пятиугольным.
3. `allPentagonal(limit: Long): IndexedSeq[Long]` - функция генерирует последовательность пятиугольных чисел от 1 до `limit`.
4. `findMin: Option[Long]` - функция ищет минимальное пятиугольное число `a`, такое что существует пятиугольное число `b`, 
   такое что `a + b` и `a + 2 * b` также являются пятиугольными числами. 

**Код:**

```dotty
def pentagonalNumber(n: Long): Long = n * (3 * n - 1) / 2

def isPentagonal(n: Long): Boolean =
  val sol = math.round(1.0 / 6.0 + math.sqrt(2.0 * n / 3.0 + 1.0 / 36.0))
  pentagonalNumber(sol) == n

def allPentagonal(limit: Long): IndexedSeq[Long] =
  (1L to limit).map(pentagonalNumber)

def findMin: Option[Long] =
  val numbers = allPentagonal(10000)
  numbers.find: a =>
    numbers.exists: b =>
      isPentagonal(a + b) && isPentagonal(a + 2 * b)
```

## Задача №45

[Задача №45 - Triangular, Pentagonal, and Hexagonal](https://projecteuler.net/problem=45)

> Треугольные, пятиугольные и шестиугольные числа вычисляются по нижеследующим формулам:
> 
> Треугольные \\(T\_{n} = \frac{n(n + 1)}{2}; 1, 3, 6, 10, 15, ...\\)
> Пятиугольные \\(P\_{n} = \frac{n(3n - 1)}{2}; 1, 5, 12, 22, 35, ...\\)		
> Шестиугольные	\\(H\_{n} = n(2n - 1); 1, 6, 15, 28, 45, ...\\)
> 
> Можно убедиться в том, что \\(T\_{285} = P\_{165} = H\_{143} = 40755\\).
> 
> Найдите следующее треугольное число, являющееся также пятиугольным и шестиугольным.

**Алгоритм:**

1. `isCorrect(n: Long): Boolean` - функция проверяет, является ли `n`-ое шестиугольное число одновременно треугольным и пятиугольным.
2. `val count = (144L to 100000L).find(isCorrect).map(hexagonalNumber)` - код ищет первое шестиугольное число, 
   которое одновременно является треугольным и пятиугольным, в диапазоне от 144 до 100000. 

**Код:**

```dotty
def hexagonalNumber(n: Long): Long = n * (2 * n - 1)

def isTriangle(n: Long): Boolean =
  val sol = math.round(-1.0 / 2.0 + math.sqrt(2.0 * n + 1.0 / 4.0))
  sol * (sol + 1) / 2 == n

def isPentagonal(n: Long): Boolean =
  val sol = math.round(1.0 / 6.0 + math.sqrt(2.0 * n / 3.0 + 1.0 / 36.0))
  sol * (3 * sol - 1) / 2 == n
  
def isCorrect(n: Long): Boolean =
  val h = hexagonalNumber(n)
  isTriangle(h) && isPentagonal(h)
  
val count = (144L to 100000L).find(isCorrect).map(hexagonalNumber)
```

## Задача №46

[Задача №46 - Goldbach's Other Conjecture](https://projecteuler.net/problem=46)

> Кристиан Гольдбах показал, что любое нечетное составное число можно записать в виде суммы простого числа 
> и удвоенного квадрата.
>
> - \\(9 = 7 + 2 \times 1^{2}\\)
> - \\(15 = 7 + 2 \times 2^{2}\\)
> - \\(21 = 3 + 2 \times 3^{2}\\)
> - \\(25 = 7 + 2 \times 3^{2}\\)
> - \\(27 = 19 + 2 \times 2^{2}\\)
> - \\(33 = 31 + 2 \times 1^{2}\\)
> 
> Оказалось, что данная гипотеза неверна.
> 
> Каково наименьшее нечетное составное число, которое нельзя записать в виде суммы простого числа и удвоенного квадрата?

**Алгоритм:**

Следующий код ищет первое нечетное составное число (`n`), которое не может быть представлено как сумма простого числа 
и удвоенного квадрата другого числа.

1. `(9 to 1000001 by 2).find: n =>` - код создает последовательность нечетных чисел от 9 до 1000001 
   и находит первое число `n`, которое удовлетворяет следующему условию:
2. `!isPrimes(n) && !(3 until n - 1).exists: m =>` - условие проверяет, является ли `n` составным числом 
   и не может быть представлено как разность простого числа и удвоенного квадрата другого числа.
3. `isPrime(m) && math.sqrt((n - m) / 2.0).isWhole` - условие проверяет, является ли `m` простым числом 
   и является ли результат выражения `math.sqrt((n - m) / 2.0)` целым числом. 

**Код:**

```dotty
def sieveOfEratosthenes(n: Int): Array[Boolean] = ... // Из Problem 10

def isPrime(n: Long): Boolean = ... // Из Problem 27
    
val count = 
  val isPrimes = sieveOfEratosthenes(1000000)
  (9 to 1000001 by 2).find: n =>
    !isPrimes(n) && !(3 until n - 1).exists: m =>
      isPrime(m) && math.sqrt((n - m) / 2.0).isWhole
```

## Задача №47

[Задача №47 - Distinct Primes Factors](https://projecteuler.net/problem=47)

> Первые два последовательные числа, каждое из которых имеет два отличных друг от друга простых множителя:
>
> - \\(14 = 2 \times 7\\)
> - \\(15 = 3 \times 5\\)
>
> Первые три последовательные числа, каждое из которых имеет три отличных друг от друга простых множителя:
>
> - \\(644 = 2^{2} \times 7 \times 23\\)
> - \\(645 = 3 \times 5 \times 43\\)
> - \\(646 = 2 \times 17 \times 19\\)
>
> Найдите первые четыре последовательных числа, каждое из которых имеет четыре отличных друг от друга простых множителя. 
> Каким будет первое число?

**Алгоритм:**

Следующий код находит первое число, у которого четыре различных простых множителя.

1. `primeFactors` - функция, которая находит простые множители числа `n`.
2. `loop` - рекурсивная функция, использующая `primeFactors` для поиска первого числа, 
   у которого четыре различных простых множителя. А также у трех предыдущих чисел такое же свойство.

**Код:**

```dotty
def primeFactors(n: Long): List[Long] =
  var result = List.empty[Long]
  var number = n

  // Проверяем делимость на 2
  var marker = false
  while number % 2 == 0 do
    marker = true
    number = number >> 1
  if marker then result = 2L :: result

  // Ищем нечетные множители
  var i = 3L
  while i <= math.sqrt(number.toDouble) do
    marker = false
    while number % i == 0 do
      marker = true
      number /= i
    if marker then result = i :: result
    i += 2

  // Если от числа что-то осталось, то остаток тоже множитель
  if number > 1 then number :: result else result
end primeFactors

@tailrec
def loop(i: Int, a: Int, b: Int, c:Int): Int =
  val curPrimeFactors = primeFactors(i).size
  if curPrimeFactors == 4 && a == 4 && b == 4 && c == 4 then i - 3
  else loop(i + 1, b, c, curPrimeFactors)

val count = loop(4, 0, 1, 1)
```

## Задача №48

[Задача №48 - Self Powers](https://projecteuler.net/problem=48)

> Сумма \\(1^{1} + 2^{2} + 3^{3} + ... + 10^{10} = 10405071317\\).
>
> Найдите последние десять цифр суммы \\(1^{1} + 2^{2} + 3^{3} + ... + 1000^{1000}\\).

**Алгоритм:**

1. `getLastTenDigits` - функция, которая вычисляет последние десять цифр произведения числа `n` самим собой `n` раз.
2. `getLastTenDigitsOfSum` - функция, которая вычисляет последние десять цифр суммы последних десяти цифр 
   чисел \\(i^{i}\\), где от 1 до `n`. 

**Код:**

```dotty
val module = 10_000_000_000L

def getLastTenDigits(n: Long): Long =
  (1L until n).foldLeft(n)((acc, _) => (acc * n) % module)

def getLastTenDigitsOfSum(n: Long): Long =
  (1L to n).foldLeft(0L)((acc, m) => (acc + getLastTenDigits(m)) % module)

getLastTenDigitsOfSum(10) // 405071317L
```

## Задача №49

[Задача №49 - Prime Permutations](https://projecteuler.net/problem=49)

> Арифметическая прогрессия: 1487, 4817, 8147, в которой каждый член возрастает на 3330, необычна в двух отношениях: 
> (1) каждый из трех членов является простым числом, (2) все три четырехзначные числа являются перестановками друг друга.
>
> Не существует арифметических прогрессий из трех однозначных, двухзначных и трехзначных простых чисел, 
> демонстрирующих это свойство. Однако, существует еще одна четырехзначная возрастающая арифметическая прогрессия.
>
> Какое 12-значное число образуется, если объединить три члена этой прогрессии?

**Алгоритм:**

- По условию: \\(x, x + a, x + 2a\\) - простые числа
- a кратно 2, т.к. x - нечетное (простое 4-х значное число) и x + a - нечетное
- a кратно 3, т.к. если это не так, то либо x + a, либо x + 2a кратно 3:
    - x % 3 = 1, a % 3 = 1 => x + 2a % 3 = 0
    - x % 3 = 1, a % 3 = 2 => x + a % 3 = 0
    - x % 3 = 2, a % 3 = 1 => x + a % 3 = 0
    - x % 3 = 2, a % 3 = 2 => x + 2a % 3 = 0

**Код:**

```dotty
def sieveOfEratosthenes(n: Int): Array[Boolean] = ... // Из Problem 10

val getAllNumbers: IndexedSeq[(Int, Int, Int)] =
  val isPrimes = sieveOfEratosthenes(10000)
  (1000 to 9999).flatMap: n =>
    if isPrimes(n) then
      val limit = 5000 - n / 2
      val aa = (6 until limit by 6).filter: a =>
        val na  = n + a
        val n2a = na + a
        isPrimes(na) && isPrimes(n2a) &&
        n.toString.sorted == na.toString.sorted &&
        n.toString.sorted == n2a.toString.sorted
      aa.map(a => (n, n + a, n + 2 * a))
    else IndexedSeq.empty
```

## Задача №50

[Задача №50 - Consecutive Prime Sum](https://projecteuler.net/problem=50)

> Простое число 41 можно записать в виде суммы шести последовательных простых чисел:
> 41 = 2 + 3 + 5 + 7 + 11 + 13
> 
> Это - самая длинная сумма последовательных простых чисел, 
> в результате которой получается простое число меньше одной сотни.
>
> Самая длинная сумма последовательных простых чисел, в результате которой получается простое число меньше одной тысячи, 
> содержит 21 слагаемое и равна 953.
>
> Какое из простых чисел меньше одного миллиона можно записать в виде суммы 
> наибольшего количества последовательных простых чисел?

**Алгоритм:**

Следующий код решает задачу нахождения простого числа, которое можно записать как сумма 
наибольшего количества последовательных простых чисел.

1. `primesNoMoreThanN` - функция получения всех простых чисел от `2` до `n`.
2. `getMostConsecutivePrimeStartingFromFirstGivenPrime` - функция, которая находит наибольшее количество 
   последовательных простых чисел, сумма которых является простым числом и начинается с первого заданного простого числа. 
   Она использует рекурсивную функцию `loop` для итерации по массиву простых чисел и накопления суммы.
3. `getMostConsecutivePrimeFromGivenPrimes` - рекурсивная функция, которая находит наибольшее количество 
   последовательных простых чисел, сумма которых является простым числом, начиная с **каждого** простого числа в заданном массиве. Она использует `getMostConsecutivePrimeStartingFromFirstGivenPrime` для нахождения наибольшего количества последовательных простых чисел для каждого простого числа в массиве.
4. `getMostConsecutivePrime` - функция, которая использует `primesNoMoreThanN` для получения всех простых чисел 
   до заданного предела, а затем использует `getMostConsecutivePrimeFromGivenPrimes` для 
   нахождения наибольшего количества последовательных простых чисел, сумма которых является простым числом. 
   Она возвращает пару, где первый элемент - количество последовательных простых чисел в искомой сумме, 
   а второй элемент - сумма этих чисел.

**Код:**

```dotty
def primesNoMoreThanN(n: Int): Array[Int] = ... // Из Problem 27

def getMostConsecutivePrimeStartingFromFirstGivenPrime(
    primes: Array[Int],
    limit: Int,
    max: (Int, Int)
): (Int, Int) =
  @tailrec
  def loop(status: (Int, Int), acc: Int, i: Int): (Int, Int) =
    if i >= primes.length then status
    else
      val newAcc = acc + primes(i)
      if newAcc >= limit then status
      else
        val newStatus =
          if primes.contains(newAcc) then (i + 1, newAcc) else status
        loop(newStatus, newAcc, i + 1)

  val prevCount = max._1
  loop((1, primes(0)), primes.take(prevCount).sum, prevCount)
end getMostConsecutivePrimeStartingFromFirstGivenPrime

@tailrec
def getMostConsecutivePrimeFromGivenPrimes(
    primes: Array[Int],
    limit: Int,
    max: (Int, Int)
): (Int, Int) =
  if primes.length == 1 then (1, primes(0))
  else if primes(0) * max._1 > limit then max
  else
    val nextStatus =
      getMostConsecutivePrimeStartingFromFirstGivenPrime(primes, limit, max)
    val nexMaxStatus =
      if max._1 >= nextStatus._1 then max else nextStatus
    getMostConsecutivePrimeFromGivenPrimes(primes.tail, limit, nexMaxStatus)

def getMostConsecutivePrime(limit: Int): (Int, Int) =
  val primes = primesNoMoreThanN(limit)
  getMostConsecutivePrimeFromGivenPrimes(primes, limit, (1, primes(0)))

getMostConsecutivePrimeStartingFromFirstGivenPrime(primesNoMoreThanN(100), 100, (1, 2)) 
// (6, 41)
getMostConsecutivePrime(100)
// (6, 41)
getMostConsecutivePrimeStartingFromFirstGivenPrime(primesNoMoreThanN(1000), 1000, (1, 2)) 
// (14, 281)
getMostConsecutivePrimeStartingFromFirstGivenPrime(primesNoMoreThanN(1000).drop(3), 1000, (1, 2)) 
// (21, 953)
getMostConsecutivePrime(1000) 
// (21, 953)
```

## Задача №51

[Задача №51 - Prime Digit Replacements](https://projecteuler.net/problem=51)

> Меняя первую цифру числа *3 (двузначного числа, заканчивающегося цифрой 3), оказывается, 
> что шесть из девяти возможных значений - 13, 23, 43, 53, 73 и 83 - являются простыми числами.
>
> При замене третьей и четвертой цифры числа 56**3 одинаковыми цифрами, получаются десять чисел, 
> из которых семь - простые: 56003, 56113, 56333, 56443, 56663, 56773 и 56993. 
> Число 56**3 является наименьшим числом, подставление цифр в которое дает именно семь простых чисел. 
> Соответственно, число 56003, будучи первым из полученных простых чисел, является наименьшим простым числом, 
> обладающим указанным свойством.
>
> Найдите наименьшее простое число, которое является одним из восьми простых чисел, полученных заменой части цифр 
> (не обязательно соседних) одинаковыми цифрами.

**Алгоритм:**

##### Шаблоны

Определим все возможные шаблоны для заданного числа.
Например, для числа `56003` могут использоваться следующие шаблоны: `Vector(*6003, 5*003, 56*03, 560*3, 5600*, 56**3)`

1. `getReplacements(candidate: String): IndexedSeq[String]`: функция принимает строку `candidate` 
   и возвращает последовательность строк, представляющих все возможные варианты замены символов в строке на звездочки.
2. `getReplacements(candidate: String, starsCount: Int, common: Option[Char]): IndexedSeq[String]`: 
   функция является вспомогательной и используется для рекурсивного построения всех возможных вариантов замены 
   символов на звездочки. Она принимает строку `candidate`, количество звездочек `starsCount` 
   и необязательный символ `common`, который должен быть заменен на звездочку. 
   Если количество звездочек меньше или равно 0 или больше лимита, функция возвращает пустую последовательность. 
   Если количество звездочек равно 1, функция заменяет символы в строке на звездочки и возвращает их. 
   В противном случае функция рекурсивно заменяет символы в строке на звездочки.

**Код:**

```dotty
def getReplacements(candidate: String): IndexedSeq[String] =
  (1 until candidate.length).flatMap: starsCount =>
    getReplacements(candidate, starsCount, None)

private def getReplacements(
    candidate: String,
    starsCount: Int,
    common: Option[Char]
): IndexedSeq[String] =
  val limit = common.map(c => candidate.count(_ == c)).getOrElse(candidate.length)
  if starsCount <= 0 || starsCount > limit then IndexedSeq.empty
  else if starsCount == 1 then
    candidate.indices
      .withFilter(i => common.isEmpty || common.contains(candidate(i)))
      .map(i => candidate.updated(i, '*'))
  else
    (0 until candidate.length - 1)
      .withFilter(i => common.isEmpty || common.contains(candidate(i)))
      .flatMap: i =>
        val (first, second) = candidate.splitAt(i)
        val common          = candidate(i)
        getReplacements(second.tail, starsCount - 1, Some(common))
          .map(tail => s"$first*$tail")
end getReplacements
```

##### Использование шаблонов

1. `getPrimeDigitReplacements(limit: Int, count: Int): Option[Int]`: функция ищет число от 10 до `limit`, 
   у которого есть `count` простых перестановок, которые можно получить заменой одной или нескольких цифр на звездочки.
2. `sieveOfEratosthenes(n: Int): Array[Boolean]`: функция реализует алгоритм "Решето Эратосфена" 
   для нахождения всех простых чисел до `n`.
3. `isExistPrimePermutations(candidate: Int, isPrime: Int => Boolean, count: Int): Boolean`: функция проверяет, 
   есть ли у числа `candidate` `count` простых перестановок, которые можно получить заменой одной 
   или нескольких цифр на звездочки. Она использует функцию `getReplacements` для генерации всех возможных вариантов 
   замены цифр на звездочки и проверяет, являются ли они простыми числами.

**Код:**

```dotty
def getPrimeDigitReplacements(
    limit: Int,
    count: Int
): Option[Int] =
  val isPrimeArr = sieveOfEratosthenes(limit + 1)
  (10 to limit).find: candidate =>
    isExistPrimePermutations(candidate, isPrimeArr.apply, count)

def sieveOfEratosthenes(n: Int): Array[Boolean] =
  val result = Array.fill(n + 1)(true)
  result(0) = false
  result(1) = false
  (4 to n by 2).foreach: j =>
    result(j) = false
  for
    i <- 3 to math.sqrt(n).toInt by 2
    if result(i)
    j <- i to n / i
  do
    result(j * i) = false
  result

def isExistPrimePermutations(
    candidate: Int,
    isPrime: Int => Boolean,
    count: Int
): Boolean =
  getReplacements(candidate.toString)
    .exists: sample =>
      val firstIndex = if sample.startsWith("*") then 1 else 0
      val candidates = (firstIndex to 9)
        .flatMap: i =>
          val number = sample.replace("*", i.toString).toInt
          if number >= candidate && isPrime(number) then Some(number)
          else None
      candidates.length >= count
```

## Задача №52

[Задача №52 - Permuted Multiples](https://projecteuler.net/problem=52)

> Очевидно, что число 125874 и в два раза большее число 251748, содержат одни и те же цифры,
> только в другом порядке.
> 
> Найдите такое наименьшее натуральное число x, чтобы 2x, 3x, 4x, 5x и 6x состояли из одних и тех же цифр.

**Алгоритм:**

Следующий код ищет наименьшее положительное целое число, которое имеет одинаковые цифры в своих удвоенных, 
тройных, четверных, пятерных и шестерных разложениях.

1. `containTheSameDigits(n1: Int, n2: Int): Boolean`: функция проверяет, содержат ли два числа одинаковые цифры.
2. `findSmallestNumber(digits: Int): Option[Int]`: функция ищет наименьшее число, которое содержит одинаковые цифры 
   в его удвоенных, тройных, четверных, пятерных и шестерных разложениях и состоящее из `digit` цифр.
3. Очевидно, что `x` начинается с `1`, иначе `6x` имело бы на одну цифру больше, 
   а значит не могла бы иметь тот же набор цифр, что и у `x`.

**Код:**

```dotty
def containTheSameDigits(n1: Int, n2: Int): Boolean =
  val word1 = n1.toString
  val word2 = n2.toString
  (word1.length == word2.length) &&
  word1.toCharArray.sorted.sameElements(word2.toCharArray.sorted)

def findSmallestNumber(digits: Int): Option[Int] =
  val start = math.pow(10, digits - 1).toInt
  (start until 2 * start).find: x =>
    containTheSameDigits(x, 2 * x) &&
      containTheSameDigits(x, 3 * x) &&
      containTheSameDigits(x, 4 * x) &&
      containTheSameDigits(x, 5 * x) &&
      containTheSameDigits(x, 6 * x)

val count = LazyList
  .from(1)
  .flatMap(findSmallestNumber)
  .head
```

## Задача №53

[Задача №53 - Combinatoric Selections](https://projecteuler.net/problem=53)

> Существует ровно десять способов выбора 3 элементов из множества пяти {1, 2, 3, 4, 5}:
> 123, 124, 125, 134, 135, 145, 234, 235, 245, и 345
>
> В комбинаторике для этого используется обозначение \\(\displaystyle \binom 5 3 = 10\\).
> В общем случае, \\(\displaystyle \binom n r = \dfrac{n!}{r!(n-r)!}\\),
> где \\(r \le n\\), \\(n! = n \times (n-1) \times ... \times 3 \times 2 \times 1\\) и \\(0! = 1\\).
>
> Это значение превышает один миллион, начиная с n = 23: \\(\displaystyle \binom {23} {10} = 1144066\\).
>
> Cколько значений (не обязательно различных) \\(\displaystyle \binom n r\\) 
> для \\(1 \le n \le 100\\) больше одного миллиона?

**Алгоритм:**

Следующий код вычисляет биномиальные коэффициенты и подсчитывает количество пар `(n, k)`, 
для которых биномиальный коэффициент `C(n, k)` превышает миллион.

1. `binomialCoefficient(n: Int, k: Int): BigInt`: функция вычисляет биномиальный коэффициент `C(n, k)` для заданных `n` и `k`.
2. `countBinCoeffMoreThanMillion(n: Int): Int`: функция подсчитывает количество пар `(n, k)`, 
   для которых биномиальный коэффициент `C(n, k)` превышает миллион. Она ищет первый `k` 
   (от `1` до `n/2`, т.к. после `n/2` биномиальные коэффициенты повторяются и идут в обратном порядке), 
   для которого `C(n, k)` превышает миллион, и вычисляет количество таких пар `(n, k)`: из общего количества
   биномиальных коэффициентов вычитается двойное количество биномиальных коэффициентов не превышающих миллиона.

**Код:**

```dotty
@tailrec
def binomialCoefficient(n: Int, k: Int): BigInt =
  if k < 0 || k > n then BigInt(0)
  else if k > (n / 2) then binomialCoefficient(n, n - k)
  else
    val (num, den) = (0 until k).foldLeft((BigInt(1), BigInt(1))) {
      case ((num, den), i) =>
        (num * BigInt(n - i), den * BigInt(k - i))
    }
    num / den

def countBinCoeffMoreThanMillion(n: Int): Int =
  val index = (1 to n / 2).find(k => binomialCoefficient(n, k) > 1000000)
  index.map(i => n + 1 - 2 * i).getOrElse(0)

val count = (1 to 100).fold(0)((acc, n) => acc + countBinCoeffMoreThanMillion(n))
```

## Задача №54

[Задача №54 - Poker Hands](https://projecteuler.net/problem=54)

> В карточной игре покер ставка состоит из пяти карт и оценивается от самой младшей до самой старшей в следующем порядке:
> 
> - **High Card** (Старшая карта): Карта наибольшего достоинства.
> - **One Pair** (Одна пара): Две карты одного достоинства.
> - **Two Pairs** (Две пары): Две различные пары карт.
> - **Three of a Kind** (Тройка): Три карты одного достоинства.
> - **Straight** (Стрейт): Все пять карт по порядку, любые масти.
> - **Flush** (Флэш): Все пять карт одной масти.
> - **Full House** (Фул-хаус): Три карты одного достоинства и одна пара карт.
> - **Four of a Kind** (Каре): Четыре карты одного достоинства.
> - **Straight Flush** (Стрейт-флэш): Любые пять карт одной масти по порядку.
> - **Royal Flush** (Роял-флэш): Десятка, валет, дама, король и туз одной масти.
>
> Достоинство карт оценивается по порядку:
> 2, 3, 4, 5, 6, 7, 8, 9, 10, валет, дама, король, туз.
> 
> Если у двух игроков получились ставки одного порядка, то выигрывает тот, у кого карты старше: 
> к примеру, две восьмерки выигрывают две пятерки (см. пример 1 ниже). 
> Если же достоинства карт у игроков одинаковы, к примеру, у обоих игроков пара дам, 
> то сравнивают карту наивысшего достоинства (см. пример 4 ниже); 
> если же и эти карты одинаковы, сравнивают следующие две и т.д.
>
> Допустим, два игрока сыграли 5 ставок следующим образом:
> 
> | Ставка | 1-й игрок                                         | 2-й игрок                                         | Победитель |
> |--------|---------------------------------------------------|---------------------------------------------------|------------|
> | 1      | 5H 5C 6S 7S KD (Пара пятерок)                     | 2C 3S 8S 8D TD (Пара восьмерок)                   | 2-й игрок  |
> | 2      | 5D 8C 9S JS AC (Старшая карта туз)                | 2C 5C 7D 8S QH (Старшая карта дама)               | 1-й игрок  |
> | 3      | 2D 9C AS AH AC (Три туза)                         | 3D 6D 7D TD QD (Флаш, бубны)                      | 2-й игрок  |
> | 4      | 4D 6S 9H QH QC (Пара дам - Старшая карта девятка) | 3D 6D 7H QD QS (Пара дам - Старшая карта семерка) | 1-й игрок  |
> | 5      | 2H 2D 4C 4D 4S (Фул-хаус - Три четверки)          | 3C 3D 3S 9S 9D (Фул-хаус - Три тройки)            | 1-й игрок  |
>
> Файл poker.txt содержит одну тысячу различных ставок для игры двух игроков. 
> В каждой строке файла приведены десять карт (отделенные одним пробелом): 
> первые пять - карты 1-го игрока, оставшиеся пять - карты 2-го игрока. 
> Можете считать, что все ставки верны (нет неверных символов или повторов карт), 
> ставки каждого игрока не следуют в определенном порядке, и что при каждой ставке есть безусловный победитель.
>
> Сколько ставок выиграл 1-й игрок?
>
> Примечание: карты в текстовом файле обозначены в соответствии с английскими наименованиями достоинств и мастей: 
> T - десятка, J - валет, Q - дама, K - король, A - туз; S - пики, C - трефы, H - червы, D - бубны.

**Алгоритм:**

Описание алгоритма определения более сильной руки в Покере [приведено в соответствующем разделе](../../algorithms/algorithms/games.md).

##### Игровая карта

Определение игровой карты в покере на Scala.

Следующий код реализует несколько классов и типов данных, 
которые могут быть использованы для представления карт в игре покер.

1. `PokerSuit` - это перечисление, которое определяет четыре масти в покере: 
   "D" - бубны, "S" - пики, "C" - трефы, "H" - червы.
2. `PokerRank` - это перечисление, которое определяет 13 рангов карт в покере. 
   Каждый ранг имеет целочисленное значение, которое представляет собой его ранг в игре. 
   Например, "A" (туз) имеет значение 14, "K" (король) - 13 и т.д.
3. `PokerCard` - это нерасширяемый кейс-класс, который представляет карту в покере. 
   Он содержит два поля: `pokerRank` и `pokerSuit`, которые представляют собой ранг и масть карты соответственно.
4. В сопутствующих объектах `PokerSuit` и `PokerRank` объявлены служебные методы. 
   В частности, `withNameOpt` - это метод, который пытается найти элемент перечисления по его строковому представлению.
5. В сопутствующем объекте `PokerCard` объявлен дополнительный конструктор `apply`. 
   Этот метод принимает строку, которая представляет собой карту, например "AS" для туза пик, 
   и пытается преобразовать ее в `PokerCard`. Если преобразование невозможно, он возвращает `None`.

**Код:**

```dotty
enum PokerSuit:
  case D, S, C, H

object PokerSuit:
  def withNameOpt(s: Char): Option[PokerSuit] =
    PokerSuit.values.find(_.toString == s.toString)

enum PokerRank(val rank: Int):
  case A     extends PokerRank(14)
  case K     extends PokerRank(13)
  case Q     extends PokerRank(12)
  case J     extends PokerRank(11)
  case TEN   extends PokerRank(10)
  case NINE  extends PokerRank(9)
  case EIGHT extends PokerRank(8)
  case SEVEN extends PokerRank(7)
  case SIX   extends PokerRank(6)
  case FIVE  extends PokerRank(5)
  case FOUR  extends PokerRank(4)
  case THREE extends PokerRank(3)
  case TWO   extends PokerRank(2)

object PokerRank:
  implicit val pokerRankOrdering: Ordering[PokerRank] =
    Ordering.fromLessThan((a, b) => b.rank < a.rank)

  def withNameOpt(s: String): Option[PokerRank] =
    s match
      case "A"        => Some(PokerRank.A)
      case "K"        => Some(PokerRank.K)
      case "Q"        => Some(PokerRank.Q)
      case "J"        => Some(PokerRank.J)
      case "10" | "T" => Some(PokerRank.TEN)
      case "9"        => Some(PokerRank.NINE)
      case "8"        => Some(PokerRank.EIGHT)
      case "7"        => Some(PokerRank.SEVEN)
      case "6"        => Some(PokerRank.SIX)
      case "5"        => Some(PokerRank.FIVE)
      case "4"        => Some(PokerRank.FOUR)
      case "3"        => Some(PokerRank.THREE)
      case "2"        => Some(PokerRank.TWO)
      case _          => None
end PokerRank

final case class PokerCard(pokerRank: PokerRank, pokerSuit: PokerSuit)

object PokerCard:
  def apply(card: String): Option[PokerCard] =
    if card.length < 2 || card.length > 3 then None
    else
      for
        pokerRank <- PokerRank.withNameOpt(card.dropRight(1))
        pokerSuit <- PokerSuit.withNameOpt(card.last)
      yield PokerCard(pokerRank, pokerSuit)
```

##### Покерный расклад

Следующий код реализует логику игры в покер. Он включает в себя определение разных типов карт, их рангов и мастей, 
а также определение руки в игре покер.

1. `PokerHandsType` - это перечисление, определяющее все возможные типы рук в покере.
2. `PokerHand` - это нерасширяемый кейс-класс, который представляет расклад в игре покер. 
   Он содержит два поля: `pokerHandsType` и `ranks`, которые представляют собой тип руки и список рангов карт соответственно. Класс также содержит метод `compareTo`, который сравнивает две руки покер.
3. В сопутствующем объекте `PokerHand` объявлены служебные методы для создания покерной руки из строки, 
   из типа руки и ранга карты, а также для сравнения покерных раскладов.
4. Метод `apply(hand: String)` принимает строку, представляющую собой покерный расклад, 
   и пытается преобразовать ее в `PokerHand`. Если преобразование невозможно, он возвращает `None`.
5. Метод `apply(pokerHandsType: PokerHandsType, rank: PokerRank)` создает покерный расклад из заданного типа руки и ранга карты.
6. Метод `apply(cards: Array[PokerCard])` создает покерный расклад из массива карт. 
   Он определяет тип руки и ранги карт на основе переданных карт и создает `PokerHand` с соответствующими значениями.
7. Методы `isFiveOfAKind`, `isStraightFlush`, `isFourOfAKind`, `isFullHouse`, `isFlush`, `isStraight`, `isThreeOfAKind`, 
   `isTwoPair`, `isOnePair` и `highCard` определяют тип руки на основе переданных карт и возвращают соответствующие значения.
8. Методы `fourOfAKind`, `fullHouse`, `threeOfAKind`, `twoPair` и `onePair` создают покерный расклад 
   определенного типа с соответствующими рангами карт.
9. Метод `highCard` создает покерный расклад типа `HIGH_CARD` с соответствующими рангами карт.
10. Метод `compareTo` сравнивает две руки покер. Он сравнивает типы рук, а затем ранги карт в случае равенства типов.

**Код:**

```dotty
enum PokerHandsType:
  case FIVE_OF_A_KIND,
    STRAIGHT_FLUSH,
    FOUR_OF_A_KIND,
    FULL_HOUSE,
    FLUSH, 
    STRAIGHT, 
    THREE_OF_A_KIND,
    TWO_PAIR, 
    ONE_PAIR, 
    HIGH_CARD 

final case class PokerHand(
    pokerHandsType: PokerHandsType,
    ranks: List[PokerRank]
):
  def compareTo(pokerHand: PokerHand): Int =
    val typesDiff = pokerHandsType.ordinal - pokerHand.pokerHandsType.ordinal
    if typesDiff != 0 then typesDiff
    else
      ranks
        .zip(pokerHand.ranks)
        .map((a, b) => b.rank - a.rank)
        .find(r => r != 0)
        .getOrElse(0)

object PokerHand:
  def apply(hand: String): Option[PokerHand] =
    val cards = hand.split(" ").flatMap(PokerCard.apply)
    if cards.length == 5 then Some(PokerHand(cards)) else None

  def apply(pokerHandsType: PokerHandsType, rank: PokerRank): PokerHand =
    PokerHand(pokerHandsType, List(rank))

  private def apply(cards: Array[PokerCard]): PokerHand =
    val sortedRanks         = cards.map(_.pokerRank).sorted
    val sortedDistinctRanks = sortedRanks.distinct
    lazy val straightPokerRank =
      if sortedRanks.head == PokerRank.A && sortedRanks(1) == PokerRank.FIVE
      then PokerRank.FIVE
      else sortedRanks.head

    if isFiveOfAKind(sortedDistinctRanks) then
      PokerHand(PokerHandsType.FIVE_OF_A_KIND, cards.head.pokerRank)
    else if isStraightFlush(sortedRanks, cards) then
      PokerHand(PokerHandsType.STRAIGHT_FLUSH, straightPokerRank)
    else if isFourOfAKind(sortedDistinctRanks, cards) then
      fourOfAKind(sortedRanks)
    else if isFullHouse(sortedDistinctRanks, cards) then
      fullHouse(sortedRanks)
    else if isFlush(cards) then
      PokerHand(PokerHandsType.FLUSH, sortedRanks.toList)
    else if isStraight(sortedRanks) then
      PokerHand(PokerHandsType.STRAIGHT, straightPokerRank)
    else if isThreeOfAKind(sortedDistinctRanks, cards, sortedRanks) then
      threeOfAKind(sortedRanks)
    else if isTwoPair(sortedDistinctRanks, cards, sortedRanks) then
      twoPair(sortedRanks)
    else if isOnePair(sortedDistinctRanks) then
      onePair(sortedRanks)
    else highCard(sortedRanks)
    end if
  end apply

  private def isFiveOfAKind(sortedDistinctRanks: Array[PokerRank]): Boolean =
    sortedDistinctRanks.length == 1

  private def isStraightFlush(
      sortedRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    isStraight(sortedRanks) && isFlush(cards)

  private def isFourOfAKind(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(_.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 1 || countOfFirstCard == 4)

  private def fourOfAKind(sortedRanks: Array[PokerRank]): PokerHand =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val pokerRank1 =
      if isFirstEqualSecond then sortedRanks.head else sortedRanks(1)
    val pokerRank2 =
      if isFirstEqualSecond then sortedRanks(4) else sortedRanks.head
    PokerHand(PokerHandsType.FOUR_OF_A_KIND, List(pokerRank1, pokerRank2))

  private def isFullHouse(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard]
  ): Boolean =
    val countOfRank      = sortedDistinctRanks.length
    val countOfFirstCard = cards.count(_.pokerRank == cards.head.pokerRank)
    countOfRank == 2 && (countOfFirstCard == 2 || countOfFirstCard == 3)

  private def fullHouse(sortedRanks: Array[PokerRank]): PokerHand =
    val pokerRank1 = sortedRanks(2)
    val pokerRank2 =
      if sortedRanks(1).rank == sortedRanks(2).rank then sortedRanks(3)
      else sortedRanks(1)
    PokerHand(PokerHandsType.FULL_HOUSE, List(pokerRank1, pokerRank2))

  private def isFlush(cards: Array[PokerCard]): Boolean =
    cards.map(_.pokerSuit.ordinal).distinct.length == 1

  private def isStraight(sortedRanks: Array[PokerRank]): Boolean =
    (sortedRanks sameElements
      Array(
        PokerRank.FIVE,
        PokerRank.FOUR,
        PokerRank.THREE,
        PokerRank.TWO,
        PokerRank.A
      )) ||
      (0 until sortedRanks.length - 1).forall: i =>
        sortedRanks(i).rank - sortedRanks(i + 1).rank == 1

  private def isThreeOfAKind(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard],
      sortedRanks: Array[PokerRank]
  ): Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 1 && countOfLastCard == 1) ||
      (countOfFirstCard == 3 && countOfLastCard == 1) ||
      (countOfFirstCard == 1 && countOfLastCard == 3)
    }

  private def threeOfAKind(sortedRanks: Array[PokerRank]): PokerHand =
    val indexThreeOfAKind = (0 until sortedRanks.length - 2)
      .find: i =>
        sortedRanks(i).rank == sortedRanks(i + 1).rank &&
          sortedRanks(i + 1).rank == sortedRanks(i + 2).rank
      .getOrElse(0)
    PokerHand(
      PokerHandsType.THREE_OF_A_KIND,
      List(
        sortedRanks(2),
        sortedRanks(if indexThreeOfAKind == 0 then 3 else 0),
        sortedRanks(if indexThreeOfAKind == 2 then 1 else 4)
      )
    )

  private def isTwoPair(
      sortedDistinctRanks: Array[PokerRank],
      cards: Array[PokerCard],
      sortedRanks: Array[PokerRank]
  ): Boolean =
    sortedDistinctRanks.length == 3 && {
      val countOfFirstCard = cards.count(c => c.pokerRank == sortedRanks.head)
      val countOfLastCard  = cards.count(c => c.pokerRank == sortedRanks.last)
      (countOfFirstCard == 2 && countOfLastCard == 2) ||
      (countOfFirstCard == 2 && countOfLastCard == 1) ||
      (countOfFirstCard == 1 && countOfLastCard == 2)
    }

  private def twoPair(sortedRanks: Array[PokerRank]): PokerHand =
    val isFirstEqualSecond = sortedRanks.head.rank == sortedRanks(1).rank
    val isThirdEqualFourth = sortedRanks(2).rank == sortedRanks(3).rank
    val thirdIndex =
      if isFirstEqualSecond && isThirdEqualFourth then 4
      else if isFirstEqualSecond then 2
      else 0
    PokerHand(
      PokerHandsType.TWO_PAIR,
      List(
        sortedRanks(1),
        sortedRanks(3),
        sortedRanks(thirdIndex)
      )
    )

  private def isOnePair(sortedDistinctRanks: Array[PokerRank]): Boolean =
    sortedDistinctRanks.length == 4

  private def onePair(sortedRanks: Array[PokerRank]): PokerHand =
    val index = (0 until sortedRanks.length - 1)
      .find(i => sortedRanks(i).rank == sortedRanks(i + 1).rank)
      .getOrElse(0)
    PokerHand(
      PokerHandsType.ONE_PAIR,
      List(
        sortedRanks(index),
        sortedRanks(if index == 0 then 2 else 0),
        sortedRanks(if index < 2 then 3 else 1),
        sortedRanks(if index == 3 then 2 else 4)
      )
    )

  private def highCard(sortedRanks: Array[PokerRank]): PokerHand =
    PokerHand(PokerHandsType.HIGH_CARD, sortedRanks.toList)
end PokerHand
```

##### Решение задачи

Осталось только распарсить строковые представления раскладов и подсчитать количество выигрышей у первого игрока.

```dotty
def doesPlayer1Win(player1: String, player2: String): Boolean =
  (PokerHand(player1), PokerHand(player2)) match
    case (Some(player1Hand), Some(player2Hand)) =>
      player1Hand.compareTo(player2Hand) < 0
    case _ => false

doesPlayer1Win("5H 5C 6S 7S KD", "2C 3S 8S 8D TD")  // false
doesPlayer1Win("5D 8C 9S JS AC", "2C 5C 7D 8S QH")  // true
doesPlayer1Win("2D 9C AS AH AC", "3D 6D 7D TD QD")  // false
doesPlayer1Win("4D 6S 9H QH QC", "3D 6D 7H QD QS")  // true
doesPlayer1Win("2H 2D 4C 4D 4S", "3C 3D 3S 9S 9D")  // true

val count =
  Source.fromResource("p054_poker.txt").getLines.toSeq.count: row =>
    row.nonEmpty && {
      val (player1, player2) = row.splitAt(15)
      doesPlayer1Win(player1.trim, player2.trim)
    }
```

## Задача №55

[Задача №55 - Lychrel Numbers](https://projecteuler.net/problem=55)

> Если взять число 47, перевернуть его и прибавить к исходному, т.е. найти 47 + 74 = 121, получится палиндром.
>
> Не из всех чисел таким образом сразу получается палиндром. К примеру,
>
> - 349 + 943   = 1292
> - 1292 + 2921 = 4213
> - 4213 + 3124 = 7337
>
> Т.е., понадобилось 3 итерации для того, чтобы превратить число 349 в палиндром.
>
> Хотя никто еще этого не доказал, считается, что из некоторых чисел, таких как 196, невозможно получить палиндром. 
> Такое число, которое не образует палиндром путем переворачивания и сложения с самим собой, называется числом Личрэла. 
> Ввиду теоретической природы таких чисел, а также цели этой задачи, мы будем считать, 
> что число является числом Личрэла до тех пор, пока не будет доказано обратное. 
> Помимо этого дано, что любое число меньше десяти тысяч либо 
> (1) станет палиндромом меньше, чем за 50 итераций, либо 
> (2) никто, с какой бы-то ни было вычислительной мощностью, не смог получить из него палиндром. 
> Между прочим, 10677 является первым числом, для которого необходимо более 50 итераций, 
> чтобы получить палиндром: 4668731596684224866951378664 (53 итерации, 28-значное число).
>
> На удивление, есть такие палиндромы, которые одновременно являются и числами Личрэла; первое такое число - 4994.
>
> Сколько существует чисел Личрэла меньше десяти тысяч?

**Алгоритм:**

1. `isPalindrome(number: BigInt): Boolean` - функция проверяет, является ли число палиндромом.
2. `getLychrelIterations(n: BigInt, iterations: Int = 0): Int` - функция реализует итеративный процесс, 
   который проверяет, является ли число [Число Лишрел](https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D0%B1%D0%BB%D0%B5%D0%BC%D0%B0_196).
   Число Лишрел - это число, которое не является палиндромом, но при добавлении его реверсированного числа 
   (т.е., числа, записанного теми же цифрами, но в обратном порядке) в результате получается палиндром. 
   Функция использует рекурсию для проверки, сколько итераций необходимо, чтобы число стало палиндромом, 
   или достигается максимальное количество итераций (50).

**Код:**

```dotty
def isPalindrome(number: BigInt): Boolean = ... // Из Problem 4

@tailrec
def getLychrelIterations(n: BigInt, iterations: Int = 0): Int =
  if iterations >= 50 then 50
  else
    val m   = BigInt(n.toString.reverse)
    val sum = n + m
    if isPalindrome(sum) then iterations + 1
    else getLychrelIterations(sum, iterations + 1)

def isLychrel(n: Int): Boolean = getLychrelIterations(n) >= 50

getLychrelIterations(47)   // 1
getLychrelIterations(74)   // 1
getLychrelIterations(4213) // 1
getLychrelIterations(3124) // 1
getLychrelIterations(1292) // 2
getLychrelIterations(2921) // 2
getLychrelIterations(349)  // 3
getLychrelIterations(943)  // 3
```

## Задача №56

[Задача №56 - Powerful Digit Sum](https://projecteuler.net/problem=56)

> Гугол (\\(10^{100}\\)) - гигантское число: один со ста нулями; 
> \\(100^{100}\\) почти невообразимо велико: один с двумястами нулями. 
> Несмотря на их размер, сумма цифр каждого числа равна всего лишь 1.
>
> Рассматривая натуральные числа вида \\(a^{b}\\), где \\(a, b \lt 100\\), какая встретится максимальная сумма цифр числа?

**Код:**

```dotty
val counts =
  for
    a <- 1 until 100
    b <- 1 until 100
  yield BigInt(a).pow(b).toString.map(_.getNumericValue).sum
counts.max  
```

## Задача №57

[Задача №57 - Square Root Convergents](https://projecteuler.net/problem=57)

> Можно убедиться в том, что квадратный корень из двух можно выразить в виде бесконечно длинной дроби.
>
> \\(\sqrt 2 = 1 + \frac 1 {2+ \frac 1 {2 +\frac 1 {2+ \dots}}}\\)
>
> Приблизив это выражение для первых четырех итераций, получим:
>
> - \\(1 + \frac 1 2 = \frac  32 = 1.5\\)
> - \\(1 + \frac 1 {2 + \frac 1 2} = \frac 7 5 = 1.4\\)
> - \\(1 + \frac 1 {2 + \frac 1 {2+\frac 1 2}} = \frac {17}{12} = 1.41666 \dots\\)
> - \\(1 + \frac 1 {2 + \frac 1 {2+\frac 1 {2+\frac 1 2}}} = \frac {41}{29} = 1.41379 \dots\\)
>
> Следующие три приближения: \\(\frac {99}{70}\\), \\(\frac {239}{169}\\) и \\(\frac {577}{408}\\), 
> а восьмое приближение, \\(\frac {1393}{985}\\), является первым случаем, в котором количество цифр 
> в числителе превышает количество цифр в знаменателе.
>
> У скольких дробей длина числителя больше длины знаменателя в первой тысяче приближений выражения?

**Алгоритм:**

Обозначим дробь в текущей итерации как \\(\frac{a}{b}\\). 
Тогда дробь в следующей итерации станет равна \\(1 + \frac{1}{1 + \frac{a}{b}} = \frac{a + 2b}{a + b}\\).
Начав с первой итерации \\(\frac{3}{2}\\) можно пройтись по всем дробям и посчитать искамое количество.

**Код:**

```dotty
def hasNumMoreDigitsThenDen(num: BigInt, den: BigInt): Int =
  if num.toString.length > den.toString.length then 1 else 0

def getNextFraction(num: BigInt, den: BigInt): (BigInt, BigInt) =
  (num + 2 * den, num + den)

val (count, _) =
  (1 to 1000).foldLeft((0, (BigInt(3), BigInt(2)))) {
    case ((count, (num, den)), _) =>
      (count + hasNumMoreDigitsThenDen(num, den), getNextFraction(num, den))
  }
```

## Задача №58

[Задача №58 - Spiral Primes](https://projecteuler.net/problem=58)

> Начиная с 1 и продвигаясь по спирали в направлении против часовой стрелки, 
> получается квадратная спираль с длиной стороны 7
>
> 37 36 35 34 33 32 31
>
> 38 17 16 15 14 13 30
>
> 39 18  5  4  3 12 29
>
> 40 19  6  1  2 11 28
>
> 41 20  7  8  9 10 27
>
> 42 21 22 23 24 25
>
> 43 44 45 46 47 48 49
>
> Интересно заметить, что нечетные квадраты лежат на правой нижней полудиагонали. 
> Еще интереснее то, что среди 13 чисел, лежащих на обеих диагоналях, 8 являются простыми; 
> т.е. отношение составляет \\(8/13 \approx 62\%\\).
>
> Если добавить еще один целый слой вокруг изображенной выше спирали, получится квадратная спираль с длиной стороны 9. 
> Если продолжать данный процесс, какой будет длина стороны квадратной спирали, 
> у которой отношение количества простых чисел к количеству всех чисел на обеих диагоналях упадет ниже \\(10\%\\)?

**Алгоритм:**

Следующий код ищет самую маленькую сторону квадрата, на диагоналях которого доля простых чисел меньше заданного процента.

1. `type PrimesCount = Long`, `type AllCount = Long`, `type Side = Long`, `type State = (PrimesCount, AllCount, Side)` - 
   определение типов, описывающих текущее состояние итерации.
2. `def isPrime(n: Long): Boolean` - функция, которая проверяет, является ли число простым.
3. `def getNextState: State => State` - функция, которая вычисляет следующее состояние системы:
    - `PrimesCount` - вычисляется количество простых чисел на углах квадрата. В правом нижнем углу расположено число,
      равное квадрату стороны (составное), а в остальных углах - квадрат стороны минус `i` умножить на 
      сторону квадрата уменьшенную на 1, где `i` - 1, 2 или 3.
    - `AllCount` - общее количество чисел увеличивается на 4.
    - `Side` - сторона квадрата увеличивается на 2.
4. `@tailrec def findFirstRatioLessThanGiven(state: State, percent: Int): Long` - рекурсивная функция, 
   которая ищет сторону квадрата, на диагонали которого доля простых чисел меньше заданного процента.
5. `val count = findFirstRatioLessThanGiven((3, 5, 3), 10)` - это вызов функции `findFirstRatioLessThanGiven` 
   с начальным состоянием и процентом.

**Код:**

```dotty
type PrimesCount = Long
type AllCount    = Long
type Side        = Long
type State       = (PrimesCount, AllCount, Side)

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

def getNextState: State => State =
  (primesCount, allCount, side) =>
    val nextSide          = side + 2
    val rightBottomNumber = nextSide * nextSide
    val primeCountOnTheSides =
      (1 to 3).count(i => isPrime(rightBottomNumber - i * (nextSide - 1)))
    (primesCount + primeCountOnTheSides, allCount + 4, nextSide)

@tailrec
def findFirstRatioLessThanGiven(state: State, percent: Int): Long =
  val (primesCount, allCount, side) = state
  if primesCount * 100 / allCount < percent then side
  else findFirstRatioLessThanGiven(getNextState(state), percent)

val count = findFirstRatioLessThanGiven((3, 5, 3), 10)
```

## Задача №59

[Задача №59 - XOR Decryption](https://projecteuler.net/problem=59)

> Каждый символ в компьютере имеет уникальный код, предпочитаемым является стандарт ASCII 
> (American Standard Code for Information Interchange - Американский стандартный код для обмена информацией). 
> Для примера, A верхнего регистра = 65, звездочка (*) = 42, а k нижнего регистра = 107.
>
> Современный метод шифровки состоит в том, что берется текстовый файл, конвертируется в байты по ASCII, 
> а потом над каждым байтом выполняется операция XOR с определенным значением, взятым из секретного ключа. 
> Преимущество функции XOR состоит в том, что применяя тот же ключ к зашифрованному тексту, получается исходный; 
> к примеру, 65 XOR 42 = 107, тогда 107 XOR 42 = 65.
>
> Для невзламываемого шифрования ключ должен быть такой же длины, что и сам текст, 
> и ключ должен быть составлен из случайных байтов. Тогда, если пользователь хранит зашифрованное сообщение 
> и ключ шифрования в разных местах, то без обеих "половинок" расшифровать сообщение просто невозможно.
>
> К сожалению, этот метод непрактичен для большинства пользователей, 
> поэтому упрощенный метод использует в качестве ключа пароль. 
> Если пароль короче текстового сообщения, что наиболее вероятно, 
> то ключ циклично повторяется на протяжении всего сообщения. 
> Идеальный пароль для этого метода достаточно длинный, чтобы быть надежным, но достаточно короткий, 
> чтобы его можно было запомнить.
>
> Ваша задача была упрощена, так как пароль состоит из трех символов нижнего регистра. 
> Используя cipher1.txt, содержащий зашифрованные коды ASCII, а также тот факт, 
> что сообщение должно содержать распространенные английские слова, 
> расшифруйте сообщение и найдите сумму всех значений ASCII в исходном тексте.

**Алгоритм:**

Определим следующие функции:

1. Функция `decrypt`, которая принимает ключ и массив символов, и возвращает новый массив символов, 
   полученных путем применения операции XOR к каждому символу исходного массива с соответствующим символом ключа.
2. Функция `toMessage`, которая преобразует массив дешифрованных символов в строку.
3. Функция `findCorrectKey`, которая принимает ключ и массив символов, дешифрует символы, преобразует их в строку, 
   и проверяет, содержит ли эта строка фразу `" and "` (распространенный английский союз).
4. Генерация всех возможных ключей из строк, состоящих из трех символов, где каждый символ - это буква от `'a'` до `'z'`.
5. Поиск первого ключа из сгенерированных, который применяется к массиву символов 
   и дает "правильное" сообщение (содержит фразу `" and "`).
6. Если ключ найден, то он используется для дешифровки массива символов и вычисляется сумма всех дешифрованных символов. 

**Код:**

```dotty
def decrypt(key: String, symbols: Array[Int]): IndexedSeq[Int] =
  symbols.indices.map: i =>
    symbols(i) ^ key(i % 3).toInt

def toMessage(decryptedSymbols: IndexedSeq[Int]): String =
  decryptedSymbols.map(_.toChar).mkString

def findCorrectKey(key: String, symbols: Array[Int]): Boolean =
  val decryptedSymbols = decrypt(key, symbols)
  val message          = toMessage(decryptedSymbols)
  message.contains(" and ")

val symbols: Array[Int] =
  Source
    .fromResource("p059_cipher.txt")
    .getLines
    .toSeq
    .mkString
    .trim
    .split(",")
    .map(_.toInt)
val keys =
  for
    a <- 'a' to 'z'
    b <- 'a' to 'z'
    c <- 'a' to 'z'
  yield s"$a$b$c"

val maybeKey = keys.find(key => findCorrectKey(key, symbols))
val count    = maybeKey.map(key => decrypt(key, symbols).sum).getOrElse(0)
```

## Задача №60

[Задача №60 - Prime Pair Sets](https://projecteuler.net/problem=60)

> Простые числа 3, 7, 109 и 673 достаточно замечательны. 
> Если взять любые два из них и объединить их в произвольном порядке, в результате всегда получится простое число. 
> Например, взяв 7 и 109, получатся простые числа 7109 и 1097. 
> Сумма этих четырех простых чисел, 792, представляет собой наименьшую сумму элементов множества из четырех простых чисел, 
> обладающих данным свойством.
>
> Найдите наименьшую сумму элементов множества из 5 простых чисел, для которых объединение 
> любых двух даст новое простое число.

**Алгоритм:**

##### Добавление нового простого числа к коллекции "взаимно простых при слиянии"

Назовем простые числа "взаимно простыми при слиянии" если их слияние в любом порядке остается простым.
Например, 7 и 109 - "взаимно простые при слиянии", т.к. 7109 и 1097 - простые.

Определим функцию `areConcatenationsPrime`, которая проверяет, являются ли два заданных простыми числа простыми числами, 
если они объединяются друг с другом. Например, если `prime1` равно 7 и `prime2` равно 3, 
то эта функция проверяет, являются ли числа 73 и 37 простыми числами.
Функция `areConcatenationsPrime` использует мемоизацию для хранения результатов проверок,
что позволяет избежать повторных вычислений для одних и тех же пар простых чисел.

Также определяется функция `isPrime`, которая проверяет, является ли число простым. 

Определим функцию `areConcatenationsPrime`, принимающую новое простое число и коллекцию "взаимно простых при слиянии" чисел,
и возвращающую `true` - если это новое простое число можно добавить к коллекции так, чтобы новая коллекция
также осталась "взаимно простой при слиянии".

**Код:**

```dotty
def areConcatenationsPrime(
    prime: Int,
    combination: List[Int]
): Boolean =
  combination.forall: prime2 =>
    areConcatenationsPrime(prime, prime2)

val memory = mutable.Map.empty[(Int, Int), Boolean]

def areConcatenationsPrime(prime1: Int, prime2: Int): Boolean =
  memory.getOrElseUpdate(
    (prime1, prime2), {
      val first = s"$prime1$prime2"
      first.map(_.getNumericValue).sum % 3 != 0 &&
      isPrime(first.toLong) && isPrime(s"$prime2$prime1".toLong)
    }
  )

def isPrime(n: Long): Boolean = ... // Из Problem 27
```

##### Нахождение комбинаций "взаимно простых при слиянии" заданного размера

1. `filterCombinations` - функция, которая фильтрует комбинации простых чисел на основе заданного количества. 
   Она использует рекурсивную функцию `loop` для итеративного увеличения количества комбинаций. 
   В каждой итерации увеличивает текущее количество комбинаций, обновляя `combinations`, 
   используя `enrichCombinations` для дополнения комбинаций новыми элементами. 

2. `enrichCombinations` - функция, которая дополняет комбинации новыми элементами. 
   Она принимает массив простых чисел, индекс и текущую комбинацию. 
   Она использует `areConcatenationsPrime` для проверки, "подходит" ли новое простое число комбинации, 
   и, если это так, добавляет его.

**Код:**

```dotty
def filterCombinations(
    primes: Array[Int],
    count: Int
): IndexedSeq[List[Int]] =
  @tailrec
  def loop(
      current: Int,
      combinations: IndexedSeq[List[Int]]
  ): IndexedSeq[List[Int]] =
    if current == count then combinations
    else
      val newCombinations =
        combinations.flatMap: combination =>
          val max   = combination.head
          val index = primes.indexOf(max) + 1
          enrichCombinations(primes, index, combination)
      loop(current + 1, newCombinations)

  loop(1, primes.map(List(_)))
end filterCombinations

def enrichCombinations(
    primes: Array[Int],
    index: Int,
    combination: List[Int]
): IndexedSeq[List[Int]] =
  (index until primes.length).collect:
    case i if areConcatenationsPrime(primes(i), combination) =>
      primes(i) :: combination
```

##### Поиск комбинации с минимальной суммой

Итого: формируем массив простых чисел меньше 10000 и, используя `filterCombinations`, 
находим искомую комбинацию с минимальной суммой.

**Код:**

```dotty
def primesNoMoreThanN(n: Int): Array[Int] = ... // Из Problem 27

def minCombinations(primes: Array[Int], count: Int): List[Int] =
  filterCombinations(primes, count)
    .minByOption(_.sum)
    .getOrElse(List.empty)
    
val primes = primesNoMoreThanN(10000)    
minCombinations(primes, 4)
// 3, 7, 109, and 673    
```

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]
- [Проект Эйлера на русском](https://euler.jakumo.org/problems.html)

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
