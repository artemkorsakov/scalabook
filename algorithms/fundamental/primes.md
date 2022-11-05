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
- Все простые числа больше `3` можно записать в виде `6k+/-1`

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

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FPrimes.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FPrimesSuite.scala)


---

## References

- [Решето Эратосфена][sieve]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Project Euler, Problem 7](https://projecteuler.net/problem=7)

[sieve]:https://ru.wikipedia.org/wiki/%D0%A0%D0%B5%D1%88%D0%B5%D1%82%D0%BE_%D0%AD%D1%80%D0%B0%D1%82%D0%BE%D1%81%D1%84%D0%B5%D0%BD%D0%B0