# Глава 1.2.4

## Глава 1. Построение абстракций с помощью процедур

## 1.2. Процедуры и порождаемые ими процессы

### 1.2.4. Возведение в степень

#### Упражнение 1.16

> Напишите процедуру, которая развивается в виде итеративного процесса 
> и реализует возведение в степень за логарифмическое число шагов, как `fast-expt`. 
> (Указание: используя наблюдение, что `(b^(n/2))² = (b²)^(n/2)`, 
> храните, помимо значения степени `n` и основания `b`, дополнительную переменную состояния `a`, 
> и определите переход между состояниями так, чтобы произведение `ab^n` от шага к шагу не менялось. 
> Вначале значение `a` берется равным `1`, а ответ получается как значение `a` в момент окончания процесса. 
> В общем случае метод определения инварианта (_invariant quantity_), 
> который не изменяется при переходе между шагами, 
> является мощным способом размышления о построении итеративных алгоритмов.)

На Scala эта программа будет выглядеть так:

```scala
def power(b: Long, n: Long): BigInt =
  @scala.annotation.tailrec
  def loop(base: BigInt, power: Long, acc: BigInt): BigInt =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(b, n, 1)

power(2, 10) // 1024
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-16.worksheet.sc)


#### Упражнение 1.17

> Алгоритмы возведения в степень из этого раздела основаны на повторяющемся умножении. 
> Подобным же образом можно производить умножение с помощью повторяющегося сложения. 
> Следующая процедура умножения (в которой предполагается, что наш язык способен только складывать, но не умножать) 
> аналогична процедуре `expt`:
> 
> ```
> (define (* a b)
>   (if (= b 0)
>       0
>       (+ a (* a (- b 1)))))
> ```
> 
> Этот алгоритм затрачивает количество шагов, линейно пропорциональное `b`. 
> Предположим теперь, что, наряду со сложением, у нас есть операции `double`, которая удваивает целое число, 
> и `halve`, которая делит (четное) число на `2`. 
> Используя их, напишите процедуру, аналогичную `fast-expt`, которая затрачивает логарифмическое число шагов.

На Scala эта программа будет выглядеть так:

```scala
def double(n: Long): Long =
  n << 1

def halve(n: Long): Long =
  n >> 1

def fastMul(a: Long, b: Long): Long =
  if b == 0 then 0L
  else if b % 2 == 0 then double(fastMul(a, halve(b)))
  else a + fastMul(a, b - 1)

fastMul(22, 10) // 220
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-17.worksheet.sc)


#### Упражнение 1.18

> Используя результаты упражнений 1.16 и 1.17, разработайте процедуру, 
> которая порождает итеративный процесс для умножения двух чисел с помощью сложения, удвоения и деления пополам,
> и затрачивает логарифмическое число шагов40.

На Scala эта программа будет выглядеть так:

```scala
def double(n: Long): Long =
  n << 1

def halve(n: Long): Long =
  n >> 1

def fastMul(a: Long, b: Long): Long =
  @scala.annotation.tailrec
  def loop(base: Long, times: Long, acc: Long): Long =
    if times == 0 then acc
    else if times % 2 == 0 then loop(double(base), halve(times), acc)
    else loop(base, times - 1, base + acc)
  loop(a, b, 0)

fastMul(22, 10) // 220
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-18.worksheet.sc)


#### Упражнение 1.19

> Существует хитрый алгоритм получения чисел Фибоначчи за логарифмическое число шагов.
> Вспомните трансформацию переменных состояния `a` и `b` процесса `fib-iter` из раздела 1.2.2:
> `a ← a + b` и `b ← a`. 
> Назовем эту трансформацию `T` и заметим, что `n`-кратное применение `T`, начиная с `1` и `0`, 
> дает нам пару `Fib(n + 1)` и `Fib(n)`. 
> Другими словами, числа Фибоначчи получаются путем применения `T^n`, 
> `n`-ой степени трансформации `T`, к паре `(1,0)`. 
> Теперь рассмотрим `T` как частный случай `p = 0`, `q = 1` в семействе трансформаций `T[pq]`, 
> где `T[pq]` преобразует пару `(a,b)` по правилу `a ← bq + aq + ap`, `b ← bp + aq`. 
> 
> Покажите, что двукратное применение трансформации `T[pq]` 
> равносильно однократному применению трансформации `T[p′q′]` того же типа, 
> и вычислите `p′` и `q′` через `p` и `q`. 
> Это дает нам прямой способ возводить такие трансформации в квадрат, 
> и таким образом, мы можем вычислить `T^n` с помощью последовательного возведения в квадрат, как в процедуре `fast-expt`. 
> Используя все эти идеи, завершите следующую процедуру, которая дает результат за логарифмическое число шагов:
>
> ```
> (define (fib n)
>   (fib-iter 1 0 0 1 n))
> 
> (define (fib-iter a b p q count)
>   (cond ((= count 0) b)
>         ((even? count)
>           (fib-iter a
>                     b
>                     <??> ; вычислить p’
>                     <??> ; вычислить q’
>                     (/ count 2)))
>         (else (fib-iter (+ (* b q) (* a q) (* a p))
>                         (+ (* b p) (* a q))
>                         p
>                         q
>                         (- count 1)))))
> ```

Применим трансформацию два раза:

- `a[2] ← b[1]q + a[1]q + a[1]p ← (b[0]p + a[0]q)q + (b[0]q + a[0]p + a[0]q)(q + p) ← b[0](2pq + q²) + a[0](q² + (p + q)²)`
- `b[2] ← b[1]p + a[1]q ← (b[0]p + a[0]q)p + (b[0]q + a[0]p + a[0]q)q ← b[0](p² + q²) + a[0](2pq + q²)`

Представим, что `p′ = p² + q²` и `q′ = 2pq + q²`. Тогда

- `a[2] ← b[0]q′ + a[0](p′ + q′)`
- `b[2] ← b[0]p′ + a[0]q′`

Это означает, что `(T[pq])² = T[p′q′]`

Что позволяет реализовать программу так:

```scala
def fastFib(n: Int): BigInt =
  @scala.annotation.tailrec
  def loop(a: BigInt, b: BigInt, p: BigInt, q: BigInt, count: Int): BigInt =
    if count == 0 then b
    else if count % 2 == 0 then
      loop(a, b, p * p + q * q, 2 * p * q + q * q, count / 2)
    else loop(b * q + a * (p + q), b * p + a * q, p, q, count - 1)

  loop(1, 0, 0, 1, n)

fastFib(30) // 832040
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-19.worksheet.sc)


---

**Ссылки:**

- [Упражнение 1.16 - 1.19](https://web.mit.edu/6.001/6.037/sicp.pdf#page=87)
