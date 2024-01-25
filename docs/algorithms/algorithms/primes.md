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

**Алгоритм:**

"Бесконечный" список простых чисел можно реализовать через `LazyList` по следующему алгоритму:

- Начинаем список простых чисел с бесконечного ленивого списка, стартующего с `2`
- Для каждого следующего числа `x`, начиная с `3`, берем этот же список простых чисел до корня из `x` 
  и проверяем, что ни одно из них не является делителем `x`.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
lazy val primes: LazyList[Int] =
  2 #:: LazyList
    .from(3)
    .filter: x =>
      val sqrtOfPrimes = primes.takeWhile(p => p <= math.sqrt(x))
      sqrtOfPrimes.forall(p => x % p != 0)

primes.take(10).toList
// List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
```

**Метрики:**

Вычисление списка первых 100000 простых чисел занимает примерно 1,5 секунды.

```dotty
@main def primesBench(): Boolean =
  primes.take(100_000).length == 100_000
      
// CPU Time: 1598 ms
// Allocation memory size: 1,68 GB      
```

## Проверка, простое ли заданное число

**Алгоритм:**

Проверка заданного числа на простоту может происходить по алгоритму, предложенному 
[в ревью следующей задачи](https://projecteuler.net/problem=7).

**Пояснение:**

- `1` не является простым числом. 
- Все простые числа, кроме `2`, нечетны. 
- Все простые числа больше `3` можно записать в виде `6k ± 1`

**Оценка:**

- **Время** - ???
- **Память** - ???

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

isPrime(999_999_000_001L)
// true
```

**Метрики:**

Проверка на простоту первого миллиона чисел занимает меньше секунды.

```dotty
@main def isPrimeBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    isPrime(i)
      
// CPU Time: 368 ms
// Allocation memory size: 25,24 MB      
```

#### Тест простоты Ферма

Тест простоты Ферма позволяет определить, простое ли число [с определенной вероятностью](https://ru.wikipedia.org/wiki/%D0%92%D0%B5%D1%80%D0%BE%D1%8F%D1%82%D0%BD%D0%BE%D1%81%D1%82%D0%BD%D1%8B%D0%B9_%D0%B0%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC).

[Малая теорема Ферма](https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D0%BB%D0%B0%D1%8F_%D1%82%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%A4%D0%B5%D1%80%D0%BC%D0%B0) 
гласит: если p - простое число, а 1 <= n < p, то \\(n^{p-1}\\) % p = 1.

Эта формула может быть верна и когда p - составное число.
В таком случае n называется _обманщиком Ферма_, поскольку ошибочно указывает, что p - простое.
Если же \\(n^{p-1}\\) % p != 1, то n называют _свидетелем Ферма_ и эта величина показывает,
что p не является простым числом.

Строго говоря, для натурального числа p минимум половина значений n между 1 и p - свидетели Ферма.
Получается, что если запустить k тестов и все они проходят успешно, то p является составным 
с вероятностью \\(\frac{1}{2^{k}}\\).

Вот вероятностный алгоритм проверки числа на простоту, который ошибается в \\(\frac{1}{2^{max}}\\) случаях.

```dotty
@tailrec
def isProbablyPrime(p: Long, max_test: Int): Boolean =
  (max_test <= 0) || {
    (BigInt(Random.nextLong(p)).modPow(p - 1, p) == 1) && isProbablyPrime(p, max_test - 1)
  }
```

## Решето Эратосфена

**Алгоритм:**

[Решето Эратосфена][sieve] на Scala можно реализовать следующим образом:

- Инициализировать массив от `0` до заданного числа со значением `true` - 
  показателем, что число, соответствующее заданному индексу является простым
- Вычеркнуть `0` и `1`: Значение по индексам `0` и `1` выставить в `false` - числа не простые
- Вычеркнуть делители `2`: для индексов от `4` до заданного с шагом `2` выставить в `false`
- Взять следующий индекс после `2`, у которого значение равно `true`, 
  и "вычеркнуть" все числа, делящиеся на взятое.

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
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
// Array(
//   false, false, true, true, false,
//   true, false, true, false, false, false
// )
```

**Метрики:**

Определение простых чисел с помощью решета Эратосфена до миллиона занимает меньше секунды.

```dotty
@main def sieveOfEratosthenesBench(): Array[Boolean] =
  sieveOfEratosthenes(1_000_000) 
      
// CPU Time: 323 ms
// Allocation memory size: 44,75 MB      
```

## Нахождение простых множителей со степенями вхождения

**Алгоритм:**

Простые множители числа со степенями вхождения можно найти следующим образом:

- Берем заданное число
- Делим число на 2 до тех пор, пока результат четный, и сохраняем количество делений: \\((2 \to k\_{2})\\)
- Ищем нечетные множители: от 3 до корня от заданного числа для каждого найденного нечетного делителя
  повторяем процедуру: делим полученный от предыдущего пункта результат на найденный нечетный делитель
  до тех пор, пока делится. Добавляем полученное количество делений: \\((2 \to k\_{2}, d \to k\_{d})\\)
- Если от числа что-то осталось после поиска нечетных делителей, 
  то остаток тоже множитель со степенью 1 (потому что он больше корня исходного числа)

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
def primeFactorsWithPow(n: Long): Map[Long, Int] =
  var number = n

  // Проверяем делимость на 2
  var powOfTwo = 0
  while number % 2 == 0 do
    powOfTwo += 1
    number = number >> 1
  var map =
    if powOfTwo > 0 then Map(2L -> powOfTwo) else Map.empty[Long, Int]

  // Ищем нечетные множители
  var i = 3L
  while i <= math.sqrt(number.toDouble) do
    var pow = 0
    while number % i == 0 do
      number /= i
      pow += 1
    if pow > 0 then map += i -> pow
    i += 2

  // Если от числа что-то осталось, то остаток тоже множитель
  if number > 1 then map + (number -> 1) else map
end primeFactorsWithPow

primeFactorsWithPow(777111)
// Map(3L -> 1, 37L -> 1, 7001L -> 1)
```

**Метрики:**

Разложение на простые множители первого миллиона чисел занимает примерно секунду.

```dotty
@main def primeFactorsWithPowBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    primeFactorsWithPow(i) 
      
// CPU Time: 1079 ms
// Allocation memory size: 283,17 MB      
```


## Нахождение простых множителей

**Алгоритм:**

Простые множители числа можно найти следующим образом:

- Берем заданное число
- Делим число на 2 до тех пор, пока результат четный, и сохраняем 2 (если исходное число было четным)
- Ищем нечетные множители: от 3 до корня от заданного числа для каждого найденного нечетного делителя
  повторяем процедуру: делим полученный от предыдущего пункта результат на найденный нечетный делитель
  до тех пор, пока делится. Добавляем полученный нечетный делитель.
- Если от числа что-то осталось после поиска нечетных делителей, то остаток тоже делитель

**Оценка:**

- **Время** - ???
- **Память** - ???

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
  var i     = 3L
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

primeFactors(777111)
// List(7001, 37, 3)
```

**Метрики:**

Разложение на простые множители первого миллиона чисел занимает примерно секунду.

```dotty
@main def primeFactorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    primeFactors(i)
      
// CPU Time: 1054 ms
// Allocation memory size: 117,45 MB      
```

---

**Ссылки:**

- [Решето Эратосфена][sieve]
- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Project Euler, Problem 7](https://projecteuler.net/problem=7)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sieve]: https://ru.wikipedia.org/wiki/%D0%A0%D0%B5%D1%88%D0%B5%D1%82%D0%BE_%D0%AD%D1%80%D0%B0%D1%82%D0%BE%D1%81%D1%84%D0%B5%D0%BD%D0%B0