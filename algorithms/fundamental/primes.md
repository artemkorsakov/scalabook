# Простые числа

[Простое число](https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D1%81%D1%82%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) — 
натуральное число, имеющее ровно два различных натуральных делителя. 
Другими словами, натуральное число `p` является простым, если оно отлично от `1` 
и делится без остатка только на `1` и на само `p`.
Это бесконечная последовательность: `<2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,... >`. 
Одним из популярных применений простых чисел является криптография. 
Простые числа также используются в хеш-функциях, чтобы избежать коллизий, 
в генераторах случайных чисел для равномерного распределения и в коде с исправлением ошибок для устранения шума. 

"Бесконечный" список простых чисел можно реализовать через `LazyList`:

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




[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FPrimes.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FPrimesSuite.scala)


---

**References:**
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Решето Эратосфена](https://ru.wikipedia.org/wiki/%D0%A0%D0%B5%D1%88%D0%B5%D1%82%D0%BE_%D0%AD%D1%80%D0%B0%D1%82%D0%BE%D1%81%D1%84%D0%B5%D0%BD%D0%B0)
- [Project Euler, Problem 7](https://projecteuler.net/problem=7)
