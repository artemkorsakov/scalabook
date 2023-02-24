# 1.2.6

## Глава 1. Построение абстракций с помощью процедур

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

> Модифицируйте процедуру **timed-prime-test** из упражнения 1.22 так, 
> чтобы она использовала **fast-prime?** (метод Ферма) и проверьте каждое из 12 простых чисел, найденных в этом упражнении. 
> Исходя из того, что у теста Ферма порядок роста **Θ(log n)**, 
> то какого соотношения времени Вы бы ожидали между проверкой на простоту поблизости от **1 000 000** и поблизости от **1000**
> Подтверждают ли это Ваши данные? 
> Можете ли Вы объяснить наблюдаемое несоответствие, если оно есть?

#### Упражнение 1.25

> Лиза П. Хакер жалуется, что при написании **expmod** мы делаем много лишней работы. 
> В конце концов, говорит она, раз мы уже знаем, как вычислять степени, можно просто написать
>
> ```
> (define (expmod base exp m)
>   (remainder (fast-expt base exp) m))
> ```
>
> Права ли она? Стала бы эта процедура столь же хорошо работать при проверке простых чисел?
> Объясните.

Стала бы работать медленнее по причине того, что много времени (и памяти) уходило бы на вычисление "больших" чисел.


#### Упражнение 1.26

> У Хьюго Дума большие трудности в упражнении 1.24. Процедура **fast-prime?** у него работает медленнее, чем **prime?**. 
> Хьюго просит помощи у своей знакомой Евы Лу Атор. 
> Вместе изучая код Хьюго, они обнаруживают, что тот переписал процедуру **expmod** 
> с явным использованием умножения вместо того, чтобы вызывать **square**:
>
> ```
> (define (expmod base exp m)
>   (cond ((= exp 0) 1)
>         ((even? exp)
>          (remainder (* (expmod base (/ exp 2) m)
>                        (expmod base (/ exp 2) m))
>                     m))
>         (else
>          (remainder (* base (expmod base (- exp 1) m))
>                     m))))
> ```
>
> Хьюго говорит: «Я не вижу здесь никакой разницы». 
> «Зато я вижу, — отвечает Ева. — 
> Переписав процедуру таким образом, ты превратил процесс порядка **Θ(log n)** в процесс порядка **Θ(n)**».
> Объясните

Потому что при использовании процедуры **square** аргумент процедуры **(expmod base (/ exp 2) m)** вычислялся
только 1 раз, а в версии Хьюго - эта же процедура вычисляется два раза (каждый множитель по разу).

Проблемы можно было бы избежать, определив переменную для **(expmod base (/ exp 2) m)**:

```
(define (expmod base exp m)
  (cond ((= exp 0) 1)
        ((even? exp)
         define a (expmod base (/ exp 2) m)
         (remainder (* a a)
                    m))
        (else
         (remainder (* base (expmod base (- exp 1) m))
                    m))))
```


#### Упражнение 1.27

> Покажите, что числа Кармайкла, перечисленные в сноске 47, действительно «обманывают» тест Ферма: 
> напишите процедуру, которая берет целое число **n** и проверяет, правда ли **a<sup>n</sup>**
> равняется **a** по модулю **n** для всех **a < n**, и проверьте эту процедуру на этих числах Кармайкла.

На Scala эта программа будет выглядеть так:

```scala
def expmod(base: Long, exp: Long, m: Long): Long =
  if exp == 0 then 1
  else if exp % 2 == 0 then
    val a = expmod(base, exp / 2, m)
    a * a % m
  else
    val a = base * expmod(base, exp - 1, m)
    a % m

def fermatTest(n: Long): Boolean =
  (2L until n).forall(a => expmod(a, n, n) == a)

fermatTest(561)   // true
fermatTest(1105)  // true
fermatTest(1729)  // true
fermatTest(2465)  // true
fermatTest(2821)  // true
fermatTest(6601)  // true
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-27.worksheet.sc)


#### Упражнение 1.28

> Один из вариантов теста Ферма, который невозможно обмануть, 
> называется *тест Миллера–Рабина* (Miller-Rabin test) (Miller 1976; Rabin 1980). 
> Он основан на альтернативной формулировке Малой теоремы Ферма, которая состоит в том, 
> что если **n** — простое число, а **a** — произвольное положительное целое число, меньшее **n**, 
> то **a** в **n − 1**-ой степени равняется **1** по модулю **n**. 
> 
> Проверяя простоту числа **n** методом Миллера–Рабина, мы берем случайное число **a < n** 
> и возводим его в **(n − 1)**-ю степень по модулю **n** с помощью процедуры **expmod**. 
> Однако когда в процедуре **expmod** мы проводим возведение в квадрат, 
> мы проверяем, не нашли ли мы «нетривиальный квадратный корень из **1** по модулю **n**», 
> то есть число, не равное **1** или **n − 1**, квадрат которого по модулю **n** равен **1**. 
> Можно доказать, что если такой нетривиальный квадратный корень из **1** существует, то **n** - не простое число. 
> Можно, кроме того, доказать, что если **n** — нечетное число, не являющееся простым, 
> то по крайней мере для половины чисел **a<n** вычисление **a<sup>n−1</sup>** 
> с помощью такой процедуры обнаружит нетривиальный квадратный корень из **1** по модулю **n** 
> (вот почему тест Миллера–Рабина невозможно обмануть). 
> 
> Модифицируйте процедуру **expmod** так, чтобы она сигнализировала обнаружение нетривиального квадратного корня из **1**, 
> и используйте ее для реализации теста Миллера–Рабина с помощью процедуры, аналогичной **fermat-test**. 
> Проверьте свою процедуру на нескольких известных Вам простых и составных числах. 
> Подсказка: удобный способ заставить **expmod** подавать особый сигнал — заставить ее возвращать **0**.

На Scala эта программа будет выглядеть так:

```scala
import scala.util.Random

def square(x: Long): Long = x * x

def expmod(base: Long, exp: Long, m: Long): Long =
  if exp == 0 then 1
  else if exp % 2 == 0 then
    val candidate = expmod(base, exp / 2, m)
    val root = square(candidate) % m
    if root == 1 && candidate != 1 && candidate != m - 1 then 0
    else root
  else (base * expmod(base, exp - 1, m)) % m

def fermatTest(n: Long): Boolean =
  def tryIt(a: Long): Boolean =
    expmod(a, n - 1, n) == 1
  tryIt(Random.nextLong(n - 1) + 1)

def fastIsPrime(n: Long, times: Int): Boolean =
  (times <= 0) || (fermatTest(n) && fastIsPrime(n, times - 1))

fastIsPrime(19, 100)   // true
fastIsPrime(199, 100)  // true
fastIsPrime(1999, 100) // true

fastIsPrime(561, 100)  // false
fastIsPrime(1105, 100) // false
fastIsPrime(1729, 100) // false
fastIsPrime(2465, 100) // false
fastIsPrime(2821, 100) // false
fastIsPrime(6601, 100) // false
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-28.worksheet.sc)

---

**Ссылки:**

- [Упражнение 1.21 - 1.28](https://web.mit.edu/6.001/6.037/sicp.pdf#page=98)
