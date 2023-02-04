# Глава 1. Построение абстракций с помощью процедур

## 1.2. Процедуры и порождаемые ими процессы

### 1.2.6. Пример: проверка на простоту

#### Упражнение 1.21

> С помощью процедуры **smallest-divisor** найдите наименьший делитель следующих чисел: **199**, **1999**, **19999**.

Решение на Scala:

```scala
def divides(a: Long, b: Long): Boolean = a % b == 0

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, d + 1)

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

smallestDivisor(199)    // 199
smallestDivisor(1999)   // 1999
smallestDivisor(19999)  // 7
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-21.worksheet.sc)

#### Упражнение 1.22

> Большая часть реализаций Лиспа содержат элементарную процедуру **runtime**, которая возвращает целое число, 
> показывающее, как долго работала система (например, в миллисекундах). 
> Следующая процедура **timed-prime-test**, будучи вызвана с целым числом **n**, печатает **n** и проверяет, простое ли оно. 
> Если **n** простое, процедура печатает три звездочки и количество времени, затраченное на проверку.
>
> ```
> (define (timed-prime-test n)
>   (newline)
>   (display n)
>   (start-prime-test n (runtime)))
> 
> (define (start-prime-test n start-time)
>   (if (prime? n)
>       (report-prime (- (runtime) start-time))))
> 
> (define (report-prime elapsed-time)
>   (display " *** ")
>   (display elapsed-time))
> ```
> 
> Используя эту процедуру, напишите процедуру **search-for-primes**, 
> которая проверяет на простоту все нечетные числа в заданном диапазоне. 
> 
> С помощью этой процедуры найдите наименьшие три простых числа после **1000**; 
> после **10 000**; после **100 000**; после **1 000 000**. 
> 
> Посмотрите, сколько времени затрачивается на каждое простое число. 
> Поскольку алгоритм проверки имеет порядок роста **Θ(√n)**, Вам следовало бы ожидать, 
> что проверка на простоту чисел, близких к **10 000**,
> занимает в **√10** раз больше времени, чем для чисел, близких к **1000**. 
> 
> Подтверждают ли это Ваши замеры времени? 
> 
> Хорошо ли поддерживают предсказание **√n** данные для **100 000** и **1 000 000**?
> 
> Совместим ли Ваш результат с предположением, 
> что программы на Вашей машине затрачивают на выполнение задач время, пропорциональное числу шагов?

На Scala эта программа будет выглядеть так:

```scala
import java.time.Instant

def divides(a: Long, b: Long): Boolean = a % b == 0

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, d + 1)

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

def isPrime(n: Long): Boolean = smallestDivisor(n) == n

def reportPrime(elapsedTime: Long): Unit =
  println(" *** ")
  println(s"${elapsedTime} ms")

def startPrimeTest(n: Long, startTime: Long): Option[Long] =
  if isPrime(n) then
    println(s"n = $n")
    reportPrime(Instant.now().toEpochMilli - startTime)
    Some(n)
  else None

def timedPrimeTest(n: Long): Option[Long] =
  startPrimeTest(n, Instant.now().toEpochMilli())

def searchForPrimes(start: Long, end: Long): Seq[Long] =
  (start to end).flatMap(timedPrimeTest)

searchForPrimes(1000, 1000 + 1000)        // Vector(1009, 1013, 1019, ...)
searchForPrimes(10000, 10000 + 1000)      // Vector(10007, 10009, 10037, ...)
searchForPrimes(100000, 100000 + 1000)    // Vector(100003, 100019, 100043, ...)
searchForPrimes(1000000, 1000000 + 1000)  // Vector(1000003, 1000033, 1000037, ...)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-22.worksheet.sc)

#### Упражнение 1.23

> Процедура **smallest-divisor** в начале этого раздела проводит множество лишних проверок: 
> после того, как она проверяет, делится ли число на **2**, нет никакого смысла проверять делимость на другие четные числа. 
> Таким образом, вместо последовательности **2, 3, 4, 5, 6...**, используемой для **test-divisor**, 
> было бы лучше использовать **2, 3, 5, 7, 9...**. 
> 
> Чтобы реализовать такое улучшение, напишите процедуру **next**, которая имеет результатом **3**, 
> если получает **2** как аргумент, а иначе возвращает свой аргумент плюс **2**. 
> Используйте **(next test-divisor)** вместо **(+ test-divisor 1)** в **smallest-divisor**. 
> 
> Используя процедуру **timed-prime-test** с модифицированной версией **smallest-divisor**, 
> запустите тест для каждого из **12** простых чисел, найденных в упражнении **1.22**. 
> Поскольку эта модификация снижает количество шагов проверки вдвое, Вы должны ожидать двукратного ускорения проверки. 
> Подтверждаются ли эти ожидания? 
> Если нет, то каково наблюдаемое соотношение скоростей двух алгоритмов, и как Вы объясните то, что оно отличается от 2?

На Scala эта программа будет выглядеть так:

```scala
def divides(a: Long, b: Long): Boolean = a % b == 0

def next(a: Long): Long =
  if a == 2 then 3 else a + 2

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, next(d))

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

def isPrime(n: Long): Boolean = smallestDivisor(n) == n

Seq(1009, 1013, 1019, 10007, 10009, 10037, 100003, 100019, 100043, 1000003,
  1000033, 1000037).forall(isPrime)
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-23.worksheet.sc)



#### Упражнение 1.24

#### Упражнение 1.25

#### Упражнение 1.26

#### Упражнение 1.27

#### Упражнение 1.28



---

**Ссылки:**
- [Упражнение 1.21 - 1.28](https://web.mit.edu/6.001/6.037/sicp.pdf#page=98)
