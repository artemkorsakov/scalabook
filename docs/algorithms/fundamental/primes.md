# Простые числа

[Простое число](https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D1%81%D1%82%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) — 
натуральное число, имеющее ровно два различных натуральных делителя. 
Другими словами, натуральное число `p` является простым, если оно отлично от `1` 
и делится без остатка только на `1` и на само `p`.
Это бесконечная последовательность: `<2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,... >`. 
Одним из популярных применений простых чисел является криптография. 
Простые числа также используются в хеш-функциях, чтобы избежать коллизий, 
в генераторах случайных чисел для равномерного распределения и в коде с исправлением ошибок для устранения шума. 

## Список простых чисел

"Бесконечный" список простых чисел можно реализовать через `LazyList` по следующему алгоритму:

- Начинаем список простых чисел с бесконечного ленивого списка, стартующего с `2`
- Для каждого следующего числа `x`, начиная с `3`, берем этот же список простых чисел до корня из `x` 
  и проверяем, что ни одно из них не является делителем `x`.

```scala
lazy val primes: LazyList[Int] =
  2 #:: LazyList
    .from(3)
    .filter { x =>
      val sqrtOfPrimes = primes.takeWhile(p => p <= math.sqrt(x))
      sqrtOfPrimes.forall(p => x % p != 0)
    }

primes.take(15).toList
// List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
```

## Проверка, простое ли заданное число

Проверка заданного числа на простоту может происходить по алгоритму, предложенному 
[в ревью следующей задачи](https://projecteuler.net/problem=7).

Заметки:

- `1` не является простым числом. 
- Все простые числа, кроме `2`, нечетны. 
- Все простые числа больше `3` можно записать в виде `6k ± 1`

```scala
def isPrime(n: Long): Boolean =
  if n < 2 then false
  else if n < 4 then true // 2 и 3 - простые
  else if n % 2 == 0 then false
  else if n < 9 then true // мы уже исключили 4,6 и 8
  else if n % 3 == 0 then false
  else
    val limit = math.ceil(math.sqrt(n)).toLong

    @annotation.tailrec
    def loop(f: Long): Boolean =
      if f > limit then true
      else if n % f == 0 || n % (f + 2) == 0 then false
      else loop(f + 6)

    loop(5)

isPrime(999_999_000_001L)
// true
```

#### Тест простоты Ферма

Тест простоты Ферма позволяет определить, простое ли число [с определенной вероятностью](https://ru.wikipedia.org/wiki/%D0%92%D0%B5%D1%80%D0%BE%D1%8F%D1%82%D0%BD%D0%BE%D1%81%D1%82%D0%BD%D1%8B%D0%B9_%D0%B0%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC).

[Малая теорема Ферма](https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D0%BB%D0%B0%D1%8F_%D1%82%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%A4%D0%B5%D1%80%D0%BC%D0%B0) 
гласит: если **p** - простое число, а **1 <= n < p**, то **n^(p-1) % p = 1**.

Эта формула может быть верна и когда **p** - составное число.
В таком случае **n** называется _обманщиком Ферма_, поскольку ошибочно указывает, что **p** - простое.
Если же **n^(p-1) % p != 1**, то **n** называют _свидетелем Ферма_ и эта величина показывает,
что **p** не является простым числом.

Строго говоря, для натурального числа **p** минимум половина значений **n** между **1** и **p** - свидетели Ферма.
Получается, что если запустить **k** тестов и все они проходят успешно, то **p** является составным 
с вероятностью **1/(2^k)**.

Вот вероятностный алгоритм проверки числа на простоту, который ошибается в **1/(2^max_test)** случаях.

```scala
@tailrec
def isProbablyPrime(p: Long, max_test: Int): Boolean =
  (max_test <= 0) || {
    (BigInt(Random.nextLong(p)).modPow(p - 1, p) == 1) && isProbablyPrime(p, max_test - 1)
  }
```


## Решето Эратосфена

[Решето Эратосфена][sieve] на Scala можно реализовать следующим образом:

```scala
def sieveOfEratosthenes(n: Int): Array[Boolean] =
  val result = Array.fill(n + 1)(true)
  result(0) = false
  result(1) = false
  (4 to n by 2).foreach(j => result(j) = false)
  for
    i <- 3 to math.sqrt(n).toInt by 2
    if result(i)
    j <- i to n / i
  do result(j * i) = false
  result

sieveOfEratosthenes(10)
// val res0: Array[Boolean] = Array(false, false, true, true, false, true, false, true, false, false, false)
```


## Нахождение простых множителей

Простые множители числа можно найти следующим образом:

```scala
def primeFactorsWithPow(n: Long): Map[Long, Long] =
  var number = n

  // Проверяем делимость на 2
  var powOfTwo = 0L
  while number % 2 == 0 do
    powOfTwo += 1
    number = number >> 1
  var map = if powOfTwo > 0 then Map(2L -> powOfTwo) else Map.empty[Long, Long]

  // Ищем нечетные множители
  var i = 3L
  while i <= math.sqrt(number) do
    var pow = 0L
    while number              % i == 0 do
      number /= i
      pow += 1
    if pow > 0 then map += i -> pow
    i += 2

  // Если от числа что-то осталось, то остаток тоже множитель
  if number > 1 then map + (number -> 1) else map

primeFactorsWithPow(777111) // Map(3 -> 1, 37 -> 1, 7001 -> 1)
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FPrimes.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FPrimesSuite.scala)
- [Решето Эратосфена][sieve]
- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Project Euler, Problem 7](https://projecteuler.net/problem=7)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sieve]:https://ru.wikipedia.org/wiki/%D0%A0%D0%B5%D1%88%D0%B5%D1%82%D0%BE_%D0%AD%D1%80%D0%B0%D1%82%D0%BE%D1%81%D1%84%D0%B5%D0%BD%D0%B0