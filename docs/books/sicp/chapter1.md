# Глава 1. Построение абстракций с помощью процедур

## 1.1 Элементы программирования

### 1.1.1 Выражения - 1.1.6 Условные выражения и предикаты

#### Упражнение 1.1

> Ниже приведена последовательность выражений. 
> Какой результат напечатает интерпретатор в ответ на каждое из них? 
> Предполагается, что выражения вводятся в том же порядке, в каком они написаны.

```scala mdoc
10                    
(5 + 3 + 4)           
(9 - 1)               
(6 / 2)               
((2 * 4) + (4 - 6))   
val a = 3
val b = a + 1        
(a + b + (a * b))    
(a == b)             

if (b > a) && (b < (a * b)) then b else a                  

if a == 4 then 6
else if b == 4 then 6 + 7 + a
else 25                                                    
 
2 + (if (b > a) then b else a)                             

((if a > b then a else if a < b then b else -1) * (a + 1)) 
```

[Scala worksheet](https://scastie.scala-lang.org/ADHhKgP4S6q1CWdlWSqRfQ)

#### Упражнение 1.2

> Переведите следующее выражение в префиксную форму:
> ```text
> (5 + 4 + (2 - (3 - (6 + 5 / 4)))) / (3(6 - 2)(2 - 7))
> ```
 
Выражение в префиксной форме выглядит так:

```text
(/ (+ 5 4 (- 2 (- 3 (+ 6 (/ 4 5))))) (* 3 (- 6 2) (- 7 2)))
```

#### Упражнение 1.3 

> Определите процедуру, которая принимает в качестве аргументов три числа и возвращает сумму
> квадратов двух больших из них.

```scala mdoc
def square(x: Int): Int = x * x

def sumOfSquares(x: Int, y: Int): Int = square(x) + square(y)

def f(a: Int, b: Int, c: Int): Int =
  if a <= b && a <= c then sumOfSquares(b, c)
  else if b <= c then sumOfSquares(a, c)
  else sumOfSquares(a, b)

f(5, 3, 4)
```

[Scala worksheet](https://scastie.scala-lang.org/i5pFJYMHTjurWwltcAkjNA)

#### Упражнение 1.4

> Заметим, что наша модель вычислений разрешает существование комбинаций, операторы которых — составные выражения. 
> С помощью этого наблюдения опишите, как работает следующая процедура:
>
> ```text
> (define (a-plus-abs-b a b)
>   ((if (> b 0) + -) a b))
> ```

Процедура `a-plus-abs-b` работает так:

- если `b` больше `0`, то выполняется процедура `(+ a b)`
- в ином случае, выполняется процедура `(- a b)`

Эта процедура складывает `a` и модуль числа `b`.

#### Упражнение 1.5

> Бен Битобор придумал тест для проверки интерпретатора на то, с каким порядком вычислений он
> работает, аппликативным или нормальным. Бен определяет такие две процедуры:
> 
> ```
> (define (p) (p))
> ```
> 
> ```
> (define (test x y)
>   (if (= x 0)
>     0
>     y))
> ```
> 
> Затем он вычисляет выражение
> 
> ```
> (test 0 (p))
> ```
> 
> Какое поведение увидит Бен, если интерпретатор использует аппликативный порядок вычислений?
> Какое поведение он увидит, если интерпретатор использует нормальный порядок?

При аппликативном порядке исчисления результат `(test 0 (p))` не будет получен, потому что невозможно вычислить второй аргумент.

При нормально порядке исчисления результат `(test 0 (p))` будет равен `0`, потому что аргументы не вычисляются
и `(test 0 (p))` разложится до:

```
if (= 0 0)
  0
  y
```

, что в свою очередь вернет `0`.


### 1.1.7 Пример: вычисление квадратного корня методом Ньютона

#### Упражнение 1.6

> Лиза П. Хакер не понимает, почему `if` должна быть особой формой.
> «Почему нельзя просто определить ее как обычную процедуру с помощью cond?» — спрашивает она.
> Лизина подруга Ева Лу Атор утверждает, что, разумеется, можно, и определяет новую версию `if`:
>
> ```
> (define (new-if predicate then-clause else-clause)
>   (cond (predicate then-clause)
>         (else else-clause)))
> ```
>
> Ева показывает Лизе новую программу:
>
> ```
> (new-if (= 2 3) 0 5)
> 5
> (new-if (= 1 1) 0 5)
> 0
> ```
>
> Обрадованная Лиза переписывает через `new-if` программу вычисления квадратного корня:
>
> ```
> (define (sqrt-iter guess x)
>   (new-if (good-enough? guess x)
>           guess
>           (sqrt-iter (improve guess x)
>                      x)))
> ```
>
> Что получится, когда Лиза попытается использовать эту процедуру для вычисления квадратных корней?

При аппликативном порядке будут пытаться вычисляться все аргументы процедуры `sqrt-iter`, в том числе и последний,
который использует `sqrt-iter`, а значит процедура войдет в бесконечную рекурсию.

#### Упражнение 1.7

> Проверка `good-enough?`, которую мы использовали для вычисления квадратных корней,
> будет довольно неэффективна для поиска квадратных корней от очень маленьких чисел.
> Кроме того, в настоящих компьютерах арифметические операции почти всегда вычисляются с ограниченной точностью.
> Поэтому наш тест оказывается неадекватным и для очень больших чисел.
> Альтернативный подход к реализации `good-enough?` состоит в том, чтобы следить,
> как от одной итерации к другой изменяется `guess`,
> и остановиться, когда изменение оказывается небольшой долей значения приближения.
> Разработайте процедуру вычисления квадратного корня, которая использует такой вариант проверки на завершение.
> Верно ли, что на больших и маленьких числах она работает лучше?

Можно предложить следующую реализацию:

```scala mdoc
def goodEnough(guess: Double, next: Double): Boolean = 
  math.abs(guess - next) < 0.001

def average(x: Double, y: Double): Double = (x + y) / 2

def improve(guess: Double, x: Double): Double =
  average(guess, x / guess)

def sqrtIter(guess: Double, x: Double): Double =
  val next = improve(guess, x)
  if goodEnough(guess, next) then guess
  else sqrtIter(next, x)

def sqrt(x: Double): Double = sqrtIter(1.0, x)

sqrt(2.0)
sqrt(1000000) 
```

[Scala worksheet](https://scastie.scala-lang.org/9m6naSpLQMKkqMvJeGvjNw)

#### Упражнение 1.8

> Метод Ньютона для кубических корней основан на том,
> что если `y` является приближением к кубическому корню из `x`,
> то мы можем получить лучшее приближение по формуле
> `(x/(y^2)+2y)/3`
>
> С помощью этой формулы напишите процедуру вычисления кубического корня, подобную процедуре для квадратного корня.

Процедура вычисления кубического корня:

```scala mdoc:reset
def square(x: Double): Double = x * x

def goodEnough(guess: Double, next: Double): Boolean = 
  math.abs(guess - next) < 0.001

def improve(guess: Double, x: Double): Double =
  ((x / square(guess)) + 2 * guess) / 3

def cubeIter(guess: Double, x: Double): Double =
  val next = improve(guess, x)
  if goodEnough(guess, next) then next
  else cubeIter(next, x)

def cubeOf(x: Double): Double = cubeIter(1.0, x)

cubeOf(2.0)
cubeOf(1000000)
```

[Scala worksheet](https://scastie.scala-lang.org/MPOYyJyFRoGnIYUbxzTHMw)

### 1.1.8. Процедуры как абстракции типа «черный ящик»

```scala mdoc
def sqrt(x: Double): Double = 
  def goodEnough(guess: Double, next: Double): Boolean =  math.abs(guess - next) < 0.001

  def average(a: Double, b: Double): Double = (a + b) / 2

  def improve(guess: Double): Double = average(guess, x / guess)

  def sqrtIter(guess: Double): Double =
    val next = improve(guess)
    if goodEnough(guess, next) then guess
    else sqrtIter(next)

  sqrtIter(1.0)

sqrt(2.0)
sqrt(1000000) 
```


## 1.2. Процедуры и порождаемые ими процессы

### 1.2.1. Линейные рекурсия и итерация

#### Упражнение 1.9

> Каждая из следующих двух процедур определяет способ сложения двух положительных целых чисел
> с помощью процедур `inc`, которая добавляет к своему аргументу 1,
> и `dec`, которая отнимает от своего аргумента 1.
>
> ```
> (define (+ a b)
>   (if (= a 0)
>       b
>       (inc (+ (dec a) b))))
> ```
>
> ```
> (define (+ a b)
>   (if (= a 0)
>       b
>       (+ (dec a) (inc b))))
> ```
>
> Используя подстановочную модель, проиллюстрируйте процесс, порождаемый каждой из этих процедур, вычислив `(+ 4 5)`.
> Являются ли эти процессы итеративными или рекурсивными?

Первая функция является рекурсивной и иллюстрируется следующим образом:

```
(+ 4 5)
(inc (+ (dec 4) 5))
(inc (+ 3 5))
(inc (inc (+ (dec 3) 5)))
(inc (inc (+ 2 5)))
(inc (inc (inc (+ (dec 2) 5))))
(inc (inc (inc (+ 1 5))))
(inc (inc (inc (inc (+ (dec 1) 5)))))
(inc (inc (inc (inc (+ 0 5)))))
(inc (inc (inc (inc 5))))
(inc (inc (inc 6)))
(inc (inc 7))
(inc 8)
9
```

Вторая функция - итеративная (при каждом шаге достаточно помнить только два значения - `a` и `b`).
Иллюстрация:

```
(+ 4 5)
(+ (dec 4) (inc 5))
(+ 3 6)
(+ (dec 3) (inc 6))
(+ 2 7)
(+ (dec 2) (inc 7))
(+ 1 8)
(+ (dec 1) (inc 8))
(+ 0 9)
9
```

#### Упражнение 1.10

> Следующая процедура вычисляет математическую функцию, называемую [функцией Аккермана](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%90%D0%BA%D0%BA%D0%B5%D1%80%D0%BC%D0%B0%D0%BD%D0%B0).
>
> ```
> (define (A x y)
>   (cond ((= y 0) 0)
>         ((= x 0) (* 2 y))
>         ((= y 1) 2)
>         (else (A (- x 1)
>                  (A x (- y 1))))))
> ```
>
> Каковы значения следующих выражений?
> - `(A 1 10)`
> - `(A 2 4)`
> - `(A 3 3)`

Функцию Аккермана можно определить так:

```scala mdoc
def functionAckermann(x: Int, y: Int): Int =
  if y == 0 then 0
  else if x == 0 then 2 * y
  else if y == 1 then 2
  else functionAckermann(x - 1, functionAckermann(x, y - 1))

functionAckermann(1, 10)
functionAckermann(2, 4)
functionAckermann(3, 3)
```

> Рассмотрим следующие процедуры, где `A` — процедура, определенная выше:
> - `(define (f n) (A 0 n))`
> - `(define (g n) (A 1 n))`
> - `(define (h n) (A 2 n))`
>
> Дайте краткие математические определения функций, вычисляемых процедурами `f`, `g` и `h` для положительных целых значений `n`.

Для малых `x` функция Аккермана равносильна следующим функциям:

- `f <=> 2*n`
- `g <=> 2^n`
- `h <=> 2^2^...^2 (n раз)`

[Scala worksheet](https://scastie.scala-lang.org/AaBfKhBDTI6LT8PayhkQlg)


### 1.2.2. Древовидная рекурсия

#### Упражнение 1.11

> Функция `f` определяется правилом: `f(n) = n`, если `n < 3`,
> и `f(n) = f(n − 1) + f(n − 2) + f(n − 3)`, если `n ≥ 3`.
>
> Напишите процедуру, вычисляющую `f` с помощью рекурсивного процесса.
>
> Напишите процедуру, вычисляющую `f` с помощью итеративного процесса.

Рекурсивный процесс:

```scala
def recursion(n: Int): Int =
  if n < 3 then n
  else recursion(n - 1) + recursion(n - 2) + recursion(n - 3)
```

Итеративный процесс:

```scala
def iteration(n: Int): Int =
  def loop(a: Int, b: Int, c: Int, count: Int): Int =
    if count == 0 then c
    else loop(a + b + c, a, b, count - 1)
  loop(2, 1, 0, n)
```

[Scala worksheet](https://scastie.scala-lang.org/H7ln1CFmTrusvslOjsHgBw)

#### Упражнение 1.12

> Приведенная ниже таблица называется треугольником Паскаля (Pascal’s triangle):
>
> 1
>
> 1 1
>
> 1 2 1
>
> 1 3 3 1
>
> 1 4 6 4 1
>
> . . .
>
> Все числа по краям треугольника равны `1`,
> а каждое число внутри треугольника равно сумме двух чисел над ним.
>
> Напишите процедуру, вычисляющую элементы треугольника Паскаля с помощью рекурсивного процесса.

```scala mdoc
def pascalsTriangle(n: Int, k: Int): Int =
  if n == 0 || k == n || k == 0 then 1
  else pascalsTriangle(n - 1, k - 1) + pascalsTriangle(n - 1, k)

pascalsTriangle(4, 2)
```

[Scala worksheet](https://scastie.scala-lang.org/qxmgWhSsS2exUsfTtboTkA)

#### Упражнение 1.13

> Докажите, что `Fib(n)` есть целое число, ближайшее к `φ^n/√5`, где `φ = (1 + √5)/2`.
>
> Указание: пусть `ψ = (1 − √5)/2`.
> С помощью определения чисел Фибоначчи и индукции докажите, что `Fib(n) = (φ^n − ψ^n)/√5`.

Доказательство:

- Заметим, что `φ^2 = φ^1 + 1`:
    - `φ^2 = (1 + √5)^2/4 = (6 + 2√5)/4 = 1 + (2 + 2√5)/4 = 1 + φ`
- а также, что `ψ^2 = ψ^1 + 1`:
    - `ψ^2 = (1 - √5)^2/4 = (6 - 2√5)/4 = 1 + (2 - 2√5)/4 = 1 + ψ`
- `n == 0`, тогда `Fib(0) = (φ^0 − ψ^0)/√5 = (1 − 1)/√5 = 0` - верно.
- `n > 0`,
    - `Fib(n) = `
    - `Fib(n - 1) + Fib(n - 2) =`
    - `(φ^(n-1) − ψ^(n-1))/√5 + (φ^(n-2) − ψ^(n-2))/√5 =`
    - `((φ^(n-1) + φ^(n-2)) - (ψ^(n-1) +  ψ^(n-2)))/√5 =`
    - `(φ^(n-2) * (φ + 1) - ψ^(n-2) * (ψ + 1))/√5 =`
    - `(φ^(n-2) * φ^2 - ψ^(n-2) * ψ^2)/√5 =`
    - `(φ^n - ψ^n)/√5`


### 1.2.3. Порядки роста

#### Упражнение 1.14

> Нарисуйте дерево, иллюстрирующее процесс,
> который порождается процедурой `count-change` из раздела 1.2.2 при размене `11` центов.
>
> Каковы порядки роста памяти и числа шагов,
> используемых этим процессом при увеличении суммы, которую требуется разменять?

Поддерево `cc 6 1`:

- `cc 6 1`
    - `cc 6 0`
        - `0`
    - `cc 5 1`
        - `cc 5 0`
            - `0`
        - `cc 4 1`
            - `cc 4 0`
                - `0`
            - `cc 3 1`
                - `cc 3 0`
                    - `0`
                - `cc 2 1`
                    - `cc 2 0`
                        - `0`
                    - `cc 1 1`
                        - `cc 1 0`
                            - `0`
                        - `cc 0 1`
                            - `1`


Дерево `count-change 11`:

- `cc 11 5`
    - `cc 11 4`
        - `cc 11 3`
            - `cc 11 2`
                - `cc 11 1`
                    - `cc 11 0`
                        - `0`
                    - `cc 10 1`
                        - `cc 10 0`
                            - `0`
                        - `cc 9 1`
                            - `cc 9 0`
                                - `0`
                            - `cc 8 1`
                                - `cc 8 0`
                                    - `0`
                                - `cc 7 1`
                                    - `cc 7 0`
                                        - `0`
                                    - Поддерево `cc 6 1`
                - `cc 6 2`
                    - Поддерево `cc 6 1`
                    - `cc 1 2`
                        - `cc 1 1`
                            - `cc 1 0`
                                - `0`
                            - `cc 0 1`
                                - `1`
                        - `cc -4 2`
                            - `0`
            - `cc 1 3`
                - `cc 1 2`
                    - `cc 1 1`
                        - `cc 1 0`
                            - `0`
                        - `cc 0 1`
                            - `1`
                    - `cc -4 2`
                        - `0`
                - `cc -9 3`
                    - `0`
        - `cc -14 4`
            - `0`
    - `cc -39 5`
        - `0`

#### Упражнение 1.15

> Синус угла (заданного в радианах) можно вычислить, если воспользоваться приближением `sin x ≈ x` при малых `x`
> и употребить тригонометрическое тождество `sin(x) = 3sin(x/3) − 4sin³(x/3)`
> для уменьшения значения аргумента `sin`.
> (В этом упражнении будем считать, что угол «достаточно мал», если он не больше 0.1 радиана.)
>
> Эта идея используется в следующих процедурах:
>
> ```
> (define (cube x) (* x x x))
> 
> (define (p x) (- (* 3 x) (* 4 (cube x))))
> 
> (define (sine angle)
>    (if (not (> (abs angle) 0.1))
>        angle
>        (p (sine (/ angle 3.0)))))
> ```
>
> а. Сколько раз вызывается процедура `p` при вычислении `(sine 12.15)`?
>
> б. Каковы порядки роста в терминах количества шагов и используемой памяти (как функция `a`) для процесса,
> порождаемого процедурой `sine` при вычислении `(sine a)`?

На Scala эта программа будет выглядеть так:

```scala mdoc
def cube(x: Double): Double = x * x * x

def p(x: Double): Double =
  3 * x - 4 * cube(x)

def sine(angle: Double): Double =
  if math.abs(angle) <= 0.1 then angle
  else p(sine(angle / 3.0))

sine(12.15)
```

а) Процедура `p` будет вызываться `roundUp(12.15 / 3) = 5` раз.

б) Количество шагов будет равняться `2 * roundUp(a / 3)`, столько же будет использование памяти.

[Scala worksheet](https://scastie.scala-lang.org/4qEcEUi1Q7GDvTDGrkAbQQ)

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

```scala mdoc
def power(b: Long, n: Long): BigInt =
  @scala.annotation.tailrec
  def loop(base: BigInt, power: Long, acc: BigInt): BigInt =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(b, n, 1)

power(2, 10)
```

[Scala worksheet](https://scastie.scala-lang.org/Vwu5s7IbS8upuU83PIWiJA)


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

```scala mdoc
def double(n: Long): Long =
  n << 1

def halve(n: Long): Long =
  n >> 1

def fastMul(a: Long, b: Long): Long =
  if b == 0 then 0L
  else if b % 2 == 0 then double(fastMul(a, halve(b)))
  else a + fastMul(a, b - 1)

fastMul(22, 10)
```

[Scala worksheet](https://scastie.scala-lang.org/v7bXGTUPRpi6YMrDC7LnUg)

#### Упражнение 1.18

> Используя результаты упражнений 1.16 и 1.17, разработайте процедуру,
> которая порождает итеративный процесс для умножения двух чисел с помощью сложения, удвоения и деления пополам,
> и затрачивает логарифмическое число шагов40.

На Scala эта программа будет выглядеть так:

```scala mdoc:reset
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

fastMul(22, 10)
```

[Scala worksheet](https://scastie.scala-lang.org/MNMMlUBVTuejpjevIBuz3g)


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

```scala mdoc
def fastFib(n: Int): BigInt =
  @scala.annotation.tailrec
  def loop(a: BigInt, b: BigInt, p: BigInt, q: BigInt, count: Int): BigInt =
    if count == 0 then b
    else if count % 2 == 0 then
      loop(a, b, p * p + q * q, 2 * p * q + q * q, count / 2)
    else loop(b * q + a * (p + q), b * p + a * q, p, q, count - 1)

  loop(1, 0, 0, 1, n)

fastFib(30)
```

[Scala worksheet](https://scastie.scala-lang.org/B4ptJqqXS3CEsvtC9rsHtw)


### 1.2.5. Нахождение наибольшего общего делителя

#### Упражнение 1.20

> Процесс, порождаемый процедурой, разумеется, зависит от того, по каким правилам работает интерпретатор.
> В качестве примера рассмотрим итеративную процедуру **gcd**, приведенную выше.
> Предположим, что мы вычисляем эту процедуру с помощью нормального порядка, описанного в разделе 1.1.5.
> (Правило нормального порядка вычислений для **if** описано в упражнении 1.5.)
> Используя подстановочную модель для нормального порядка, проиллюстрируйте процесс,
> порождаемый при вычислении **(gcd 206 40)** и укажите, какие операции вычисления остатка действительно выполняются.
> Сколько операций **remainder** выполняется на самом деле при вычислении **(gcd 206 40)** в нормальном порядке?
> При вычислении в аппликативном порядке?

```
(define (gcd a b)
  (if (= b 0)
      a
      (gcd b (remainder a b))))
```

Нормальный порядок:

???

Аппликативный порядок:

```
if (= 40 0)
   206
   (gcd 40 (remainder 206 40))

gcd 40 (remainder 206 40)     // 1

gcd 40 6

if (= 6 0)
   40
   (gcd 6 (remainder 40 6))    

gcd 6 (remainder 40 6)        // 2

gcd 6 4

if (= 4 0)
   6
   (gcd 4 (remainder 6 4))    

gcd 4 (remainder 6 4)         // 3

gcd 4 2

if (= 2 0)
   4
   (gcd 2 (remainder 4 2))

gcd 2 (remainder 4 2)         // 4

gcd 2 0

if (= 0 0)
   2
   (gcd 0 (remainder 2 0))

2   
```


### 1.2.6. Пример: проверка на простоту

#### Упражнение 1.21

> С помощью процедуры **smallest-divisor** найдите наименьший делитель следующих чисел: **199**, **1999**, **19999**.

Решение на Scala:

```scala mdoc
def divides(a: Long, b: Long): Boolean = a % b == 0

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, d + 1)

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

smallestDivisor(199)   
smallestDivisor(1999) 
smallestDivisor(19999)
```

[Scala worksheet](https://scastie.scala-lang.org/1kSbB13fQhG0DhEOngZFEQ)


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

[Scala worksheet](https://scastie.scala-lang.org/JOA2TWkATHiF4t3P9V7l8Q)


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

```scala mdoc:reset
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

[Scala worksheet](https://scastie.scala-lang.org/xE0njNosQWK9zjDUHgw7aQ)


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
> напишите процедуру, которая берет целое число `n` и проверяет, правда ли `a^n`
> равняется `a` по модулю `n` для всех `a < n`, и проверьте эту процедуру на этих числах Кармайкла.

На Scala эта программа будет выглядеть так:

```scala mdoc
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

fermatTest(561)  
fermatTest(1105) 
fermatTest(1729) 
fermatTest(2465) 
fermatTest(2821) 
fermatTest(6601) 
```

[Scala worksheet](https://scastie.scala-lang.org/YAeohITfTfCb1kwDB9VNGw)


#### Упражнение 1.28

> Один из вариантов теста Ферма, который невозможно обмануть,
> называется *тест Миллера–Рабина* (Miller-Rabin test) (Miller 1976; Rabin 1980).
> Он основан на альтернативной формулировке Малой теоремы Ферма, которая состоит в том,
> что если `n` — простое число, а `a` — произвольное положительное целое число, меньшее `n`,
> то `a` в `n − 1`-ой степени равняется `1` по модулю `n`.
>
> Проверяя простоту числа `n` методом Миллера–Рабина, мы берем случайное число `a < n`
> и возводим его в `(n − 1)`-ю степень по модулю `n` с помощью процедуры `expmod`.
> Однако когда в процедуре `expmod` мы проводим возведение в квадрат,
> мы проверяем, не нашли ли мы «нетривиальный квадратный корень из `1` по модулю `n`»,
> то есть число, не равное `1` или `n − 1`, квадрат которого по модулю `n` равен `1`.
> Можно доказать, что если такой нетривиальный квадратный корень из `1` существует, то `n` - не простое число.
> Можно, кроме того, доказать, что если `n` — нечетное число, не являющееся простым,
> то по крайней мере для половины чисел `a<n` вычисление `a^(n−1)`
> с помощью такой процедуры обнаружит нетривиальный квадратный корень из `1` по модулю `n`
> (вот почему тест Миллера–Рабина невозможно обмануть).
>
> Модифицируйте процедуру `expmod` так, чтобы она сигнализировала обнаружение нетривиального квадратного корня из `1`,
> и используйте ее для реализации теста Миллера–Рабина с помощью процедуры, аналогичной `fermat-test`.
> Проверьте свою процедуру на нескольких известных Вам простых и составных числах.
> Подсказка: удобный способ заставить `expmod` подавать особый сигнал — заставить ее возвращать `0`.

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

fastIsPrime(19, 100)   
fastIsPrime(199, 100)  
fastIsPrime(1999, 100) 

fastIsPrime(561, 100)  
fastIsPrime(1105, 100) 
fastIsPrime(1729, 100) 
fastIsPrime(2465, 100) 
fastIsPrime(2821, 100) 
fastIsPrime(6601, 100) 
```

[Scala worksheet](https://scastie.scala-lang.org/DRqIItJmTxmmhIDoRYaTzw)


## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.1. Процедуры в качестве аргументов

#### Упражнение 1.29

> Правило Симпсона — более точный метод численного интегрирования, чем представленный выше.
> С помощью правила Симпсона интеграл функции `f` между `a` и `b` приближенно вычисляется в виде
> `(h / 3) * [y[0] + 4y[1] + 2y[2] + 4y[3] + 2y[4] + . . . + 2y[n-2] + 4y[n-1] + y[n]]`
> , где `h = (b − a)/n`, для какого-то четного целого числа `n`, а `y[k] = f(a + kh)`.
> (Увеличение `n` повышает точность приближенного вычисления.)
> Определите процедуру, которая принимает в качестве аргументов `f`, `a`, `b` и `n`,
> и возвращает значение интеграла, вычисленное по правилу Симпсона.
> С помощью этой процедуры проинтегрируйте `cube` между `0` и `1` (с `n = 100` и `n = 1000`)
> и сравните результаты с процедурой `integral`, приведенной выше.


Решение на Scala:

```scala mdoc
def simpsonRule(f: Double => Double, a: Double, b: Double, n: Int): Double =
  val h = (b - a) / n
  def y(k: Int): Double =
    val co = if k == 0 || k == n then 1 else if k % 2 == 0 then 2 else 4
    co * f(a + k * h)

  (0 to n).foldLeft(0.0)((acc, k) => acc + y(k)) * h / 3

simpsonRule(cube, 0.0, 1.0, 100)   
simpsonRule(cube, 0.0, 1.0, 1000)  
```

[Scala worksheet](https://scastie.scala-lang.org/4ZbXwD7YR7eMwKtOrLCALg)


#### Упражнение 1.30

> Процедура `sum` порождает линейную рекурсию.
> Ее можно переписать так, чтобы суммирование выполнялось итеративно.
> Покажите, как сделать это, заполнив пропущенные выражения в следующем определении:
>
> ```
> (define (sum term a next b)
>   (define (iter a result)
>      (if <??>
>          <??>
>          (iter <??> <??>)))
>   (iter <??> <??>))
> ```

Решение на Scala:

```scala mdoc
def cube(a: Double): Double = a * a * a

def sum(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result + term(a))
  iter(a, 0.0)

def integral(f: Double => Double, a: Double, b: Double, dx: Double): Double =
  def addDx(x: Double): Double = x + dx
  sum(f, a + dx / 2, addDx, b) * dx

integral(cube, 0.0, 1.0, 0.01)  
integral(cube, 0.0, 1.0, 0.001) 
```

[Scala worksheet](https://scastie.scala-lang.org/MdvSkJvWT0qfjA9UgrGceg)


#### Упражнение 1.31

> а. Процедура `sum` — всего лишь простейшая из обширного множества подобных абстракций,
> которые можно выразить через процедуры высших порядков.
> Напишите аналогичную процедуру под названием `product`,
> которая вычисляет произведение значений функции в точках на указанном интервале.
> Покажите, как с помощью этой процедуры определить `factorial`.
> Кроме того, при помощи `product` вычислите приближенное значение `π` по формуле:
> `π / 4 = (2 / 3) * (4 / 3) * (4 / 5) * (6 / 5) * (6 / 7) * (8 / 7) * ...`
>
> б. Если Ваша процедура `product` порождает рекурсивный процесс, перепишите ее так, чтобы она порождала итеративный.
> Если она порождает итеративный процесс, перепишите ее так, чтобы она порождала рекурсивный.

Решение на Scala:

```scala mdoc:reset:silent
val identity: Double => Double = x => x
val nextNum: Double => Double = x => x + 1
val square: Double => Double = x => x * x
val piFraction: Double => Double = k =>
  (2 * k) * (2 * k + 2) / square(2 * k + 1)
```
```scala mdoc
def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then 1.0
  else term(a) * productRec(term, next(a), next, b)

def factorialRec(n: Int): Int =
  productRec(identity, 1, nextNum, n).toInt

def calculatePiRec(n: Int): Double =
  4 * productRec(piFraction, 1, nextNum, n)

factorialRec(10)     
calculatePiRec(1000) 

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result * term(a))
  iter(a, 1.0)

def factorialIter(n: Int): Int =
  productIter(identity, 1, nextNum, n).toInt

def calculatePiIter(n: Int): Double =
  4 * productIter(piFraction, 1, nextNum, n)

factorialIter(10)     
calculatePiIter(1000) 
```

[Scala worksheet](https://scastie.scala-lang.org/csS3FdhYQxuUZC12CbgqrQ)


#### Упражнение 1.32

> а. Покажите, что **sum** и **product** (упражнение 1.31) являются частными случаями еще более общего понятия,
> называемого накопление (**accumulation**), которое комбинирует множество термов
> с помощью некоторой общей функции накопления **(accumulate combiner null-value term a next b)**
>
> **Accumulate** принимает в качестве аргументов те же описания термов и диапазона, что и **sum** с **product**,
> а еще процедуру **combiner** (двух аргументов), которая указывает,
> как нужно присоединить текущий терм к результату накопления предыдущих,
> и **null-value**, базовое значение, которое нужно использовать, когда термы закончатся.
> Напишите **accumulate** и покажите, как и **sum**, и **product** можно определить в виде простых вызовов **accumulate**.
>
> б. Если Ваша процедура **accumulate** порождает рекурсивный процесс, перепишите ее так, чтобы она порождала итеративный.
> Если она порождает итеративный процесс, перепишите ее так, чтобы она порождала рекурсивный

Решение на Scala:

```scala mdoc:reset
def accumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then nullValue
  else
    combiner(
      term(a),
      accumulateRec(combiner, nullValue, term, next(a), next, b)
    )

def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateRec(_ * _, 1.0, term, a, next, b)

def accumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), combiner(result, term(a)))
  iter(a, nullValue)

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateIter(_ * _, 1.0, term, a, next, b)
```

[Scala worksheet](https://scastie.scala-lang.org/DkL9dDr2S8aQdjwTs2p0OQ)


#### Упражнение 1.33

> Можно получить еще более общую версию **accumulate** (упражнение 1.32),
> если ввести понятие фильтра (**filter**) на комбинируемые термы.
> То есть комбинировать только те термы, порожденные из значений диапазона, которые удовлетворяют указанному условию.
>
> Получающаяся абстракция **filtered-accumulate** получает те же аргументы, что и **accumulate**,
> плюс дополнительный одноаргументный предикат, который определяет фильтр.
> Запишите **filtered-accumulate** в виде процедуры.
> Покажите, как с помощью **filtered-accumulate** выразить следующее:
>
> а. сумму квадратов простых чисел в интервале от **a** до **b**
> (в предположении, что процедура **prime?** уже написана);
>
> б. произведение всех положительных целых чисел меньше **n**,
> которые просты по отношению к **n** (то есть всех таких положительных целых чисел **i < n**, что **НОД(i, n) = 1**).

Решение на Scala:

```scala
def filteredAccumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  if a > b then nullValue
  else
    val nextA = term(a)
    lazy val res =
      filteredAccumulateRec(combiner, nullValue, term, next(a), next, b, filter)
    if filter(nextA) then combiner(nextA, res)
    else res

def filteredAccumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else
      val nextA = term(a)
      val nextResult =
        if filter(nextA) then combiner(result, nextA)
        else result
      iter(next(a), nextResult)
  iter(a, nullValue)

def filteredAccumulate(
    combiner: (Int, Int) => Int,
    nullValue: Int,
    term: Int => Int,
    a: Int,
    next: Int => Int,
    b: Int,
    filter: Int => Boolean
): Int = ???

def isPrime(n: Int): Boolean = ???

def sumPrimeSquares(a: Int, b: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x + y,
    nullValue = 0,
    term = x => x * x,
    a = a,
    next = x => x + 1,
    b = b,
    filter = isPrime
  )

def gcd(a: Int, b: Int): Int = ???

def productPrimesLessThanN(n: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x * y,
    nullValue = 1,
    term = x => x,
    a = 1,
    next = x => x + 1,
    b = n - 1,
    filter = i => gcd(i, n) == 1
  )
```

[Scala worksheet](https://scastie.scala-lang.org/KXhnTQAvTeW7NsV9Wy3hRg)


### 1.3.2. Построение процедур с помощью lambda

#### Упражнение 1.34

> Допустим, мы определили процедуру
>
> ```
> (define (f g)
>   (g 2))
> ```
>
> Тогда мы имеем
>
> ```
> (f square)
> 4
> ```
>
> ```
> (f (lambda (z) (* z (+ z 1))))
> 6
> ```
>
> Что случится, если мы (извращенно) попросим интерпретатор вычислить комбинацию **(f f)**?
> Объясните.

[Ссылка на объяснение](https://eli.thegreenplace.net/2007/07/13/sicp-sections-132-133)


### 1.3.3. Процедуры как обобщенные методы

#### Упражнение 1.35

> Покажите, что золотое сечение `φ` (раздел 1.2.2) есть неподвижная точка трансформации `x → 1 + 1/x`,
> и используйте этот факт для вычисления `φ` с помощью процедуры `fixed-point`.

`φ² = φ + 1 => φ = 1 + 1/φ`

Решение на Scala:

```scala mdoc
val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => 1 + (1.0 / x), 1.0) 
```

[Scala worksheet](https://scastie.scala-lang.org/kyMNERl5SnK8o9ePk1vrzg)

#### Упражнение 1.36

> Измените процедуру `fixed-point` так, чтобы она печатала последовательность приближений,
> которые порождает, с помощью примитивов `newline` и `display`, показанных в упражнении 1.22.
> Затем найдите решение уравнения `x^x = 1000` путем поиска неподвижной точки `x → log(1000)/ log(x)`.
> (Используйте встроенную процедуру `Scheme log`, которая вычисляет натуральные логарифмы.)
> Посчитайте, сколько шагов это занимает при использовании торможения усреднением и без него.
> (Учтите, что нельзя начинать `fixed-point` со значения `1`, поскольку это вызовет деление на `log(1) = 0`.)

Решение на Scala:

```scala
def average(a: Double, b: Double): Double = (a + b) / 2

val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    println(next)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => math.log(1000) / math.log(x), 10.0)

fixedPoint(x => average(x, math.log(1000) / math.log(x)), 10.0)
```

В первом случае мы получаем такое приближение (33 итерации):

```text
res0: Double = 4.555532257016376
// 2.9999999999999996
// 6.2877098228681545
// 3.7570797902002955
// 5.218748919675316
// 4.1807977460633134
// 4.828902657081293
// 4.386936895811029
// 4.671722808746095
// 4.481109436117821
// 4.605567315585735
// 4.522955348093164
// 4.577201597629606
// 4.541325786357399
// 4.564940905198754
// 4.549347961475409
// 4.5596228442307565
// 4.552843114094703
// 4.55731263660315
// 4.554364381825887
// 4.556308401465587
// 4.555026226620339
// 4.55587174038325
// 4.555314115211184
// 4.555681847896976
// 4.555439330395129
// 4.555599264136406
// 4.555493789937456
// 4.555563347820309
// 4.555517475527901
// 4.555547727376273
// 4.555527776815261
// 4.555540933824255
// 4.555532257016376
```

Во втором - 9:

```text
res1: Double = 4.555536206185039
// 6.5
// 5.095215099176933
// 4.668760681281611
// 4.57585730576714
// 4.559030116711325
// 4.55613168520593
// 4.555637206157649
// 4.55555298754564
// 4.555538647701617
// 4.555536206185039
```

[Scala worksheet](https://scastie.scala-lang.org/GixzWz6oQRuj4BWMrbU4hw)

#### Упражнение 1.37

> а. Бесконечная цепная дробь (`continued fraction`) есть выражение вида
> `f = N[1] / (D[1] + N[2] / (D[2] + N[3] /(D[3] + ...)))`
> В качестве примера можно показать, что расширение бесконечной цепной дроби при всех `N[i]` и
> `D[i]`, равных `1`, дает `1/φ`, где `φ` — золотое сечение (описанное в разделе 1.2.2).
> Один из способов вычислить цепную дробь состоит в том, чтобы после заданного количества термов оборвать вычисление.
> Такой обрыв — так называемая конечная цепная дробь (`finite continued fraction`) из `k` элементов, —
> имеет вид `f = N[1] / (D[1] + N[2] / (D[2] + N[3] / (D[3] + ... + N[k] / D[k])))`.
> Предположим, что `n` и `d` — процедуры одного аргумента (номера элемента `i`),
> возвращающие `N[i]` и `D[i]` элементов цепной дроби.
> Определите процедуру `cont-frac` так, чтобы вычисление `(cont-frac n d k)` давало значение `k`-элементной конечной цепной дроби.
> Проверьте свою процедуру, вычисляя приближения к `1/φ` с помощью
>
> ```
> (cont-frac (lambda (i) 1.0)
>            (lambda (i) 1.0)
>            k)
> ```
>
> для последовательных значений `k`.
>
> Насколько большим пришлось сделать `k`, чтобы получить приближение, верное с точностью 4 цифры после запятой?
>
> б. Если Ваша процедура `cont-frac` порождает рекурсивный процесс, напишите вариант, который порождает итеративный процесс.
> Если она порождает итеративный процесс, напишите вариант, порождающий рекурсивный процесс.

Для k == 11 точность составляет 4 знака.

Решение на Scala:

Рекурсивный процесс:

```scala mdoc
def contFracRec(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int): Double =
    if i == k then n(i) / d(i)
    else n(i) / (d(i) + loop(i + 1))
  loop(1)

contFracRec(_ => 1.0, _ => 1.0, 11)
```

Итеративный процесс:

```scala mdoc:reset
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

contFracIter(_ => 1.0, _ => 1.0, 11)
```

[Scala worksheet](https://scastie.scala-lang.org/o4M3kiklSiStzl3LUN91lg)

#### Упражнение 1.38

> В 1737 году швейцарский математик Леонард Эйлер опубликовал статью *De functionibus Continuis*,
> которая содержала расширение цепной дроби для `e−2`, где `e` — основание натуральных логарифмов.
> В этой дроби все `N[i]` равны `1`, а `D[i]` последовательно равны `1, 2, 1, 1, 4, 1, 1, 6, 1, 1, 8, ...`
>
> Напишите программу, использующую Вашу процедуру `cont-frac` из упражнения 1.37
> для вычисления `e` на основании формулы Эйлера

Решение на Scala:

```scala mdoc:reset:silent
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

val n: Int => Double = _ => 1.0

val d: Int => Double = i =>
  if i % 3 == 2 then (i / 3 + 1) * 2
  else 1.0
```
```scala mdoc
contFracIter(n, d, 1000)
```

[Scala worksheet](https://scastie.scala-lang.org/usu62Nt0TsqHQHYA97wZBA)

#### Упражнение 1.39

> Представление тангенса в виде цепной дроби было опубликовано в 1770 году немецким математиком Й.Х. Ламбертом:
>
> `tg x = x / (1 − x² / (3 − x² / (5 − ...)))`
>
> где `x` дан в радианах.
>
> Определите процедуру `(tan-cf x k)`, которая вычисляет приближение к тангенсу на основе формулы Ламберта.
> `K` указывает количество термов, которые требуется вычислить, как в упражнении 1.37.


Решение на Scala:

```scala mdoc:reset:silent
def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

def n(x: Double)(i: Int): Double =
  if i == 1 then x
  else -x * x

val d: Int => Double = i => 2 * (i - 1) + 1

def tanCf(x: Double, k: Int): Double =
  contFracIter(n(x), d, k)
```
```scala mdoc
tanCf(1.0, 100)
```

[Scala worksheet](https://scastie.scala-lang.org/lZ9YXPg8S9ij3UZBAz8tUw)


### 1.3.4. Процедуры как возвращаемые значения

#### Упражнение 1.40

> Определите процедуру `cubic`, которую можно было бы использовать совместно с процедурой `newtons-method`
> в выражениях вида `(newtons-method (cubic a b c) 1)` для приближенного вычисления нулей
> кубических уравнений `x³ + ax² + bx + c`.


Решение на Scala:

```scala
def square(x: Double): Double = x * x

def cube(x: Double): Double = x * x * x

def cubic(a: Double, b: Double, c: Double): Double => Double = x =>
  cube(x) + a * square(x) + b * x + c
```

[Scala worksheet](https://scastie.scala-lang.org/5K8VDuv3QhqbnVbO955asw)


#### Упражнение 1.41

> Определите процедуру **double**, которая принимает как аргумент процедуру с одним аргументом
> и возвращает процедуру, которая применяет исходную процедуру дважды.
> Например, если процедура **inc** добавляет к своему аргументу **1**,
> то **(double inc)** должна быть процедурой, которая добавляет **2**.
>
> Скажите, какое значение возвращает **(((double (double double)) inc) 5)**


Решение на Scala:

```scala mdoc:silent
val inc: Int => Int = _ + 1

def double[T](f: T => T): T => T = x => f(f(x))

// (((double (double double)) inc) 5)
val f: (Int => Int) => (Int => Int) = double(double(double))
f(inc)(5)
```
```scala mdoc
f(inc)(5)
```

[Scala worksheet](https://scastie.scala-lang.org/GwhY4i7zTse1kU4xAw6LKQ)


#### Упражнение 1.42

> Пусть **f** и **g** — две одноаргументные функции.
> По определению, композиция (*composition*) **f** и **g** есть функция **x → f(g(x))**.
> Определите процедуру **compose** которая реализует композицию.
> Например, если **inc** — процедура, добавляющая к своему аргументу **1**,
>
> ```
> ((compose square inc) 6)
> 49
> ```


Решение на Scala:

```scala mdoc:reset:silent
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

val square: Int => Int = x => x * x
val inc: Int => Int = x => x + 1

compose(square, inc)(6)
```
```scala mdoc
compose(square, inc)(6)
```

[Scala worksheet](https://scastie.scala-lang.org/hKt6WdQETDCLqfafsx7uHQ)


#### Упражнение 1.43

> Если `f` есть численная функция, а `n` — положительное целое число,
> то мы можем построить `n`-кратное применение `f`,
> которое определяется как функция, значение которой в точке `x` равно `f(f(... (f(x)) ...))`.
> Например, если `f` есть функция `x → x + 1`, то `n`-кратным применением `f` будет функция `x → x + n`.
> Если `f` есть операция возведения в квадрат,
> то `n`-кратное применение `f` есть функция, которая возводит свой аргумент в `2^n`-ю степень.
>
> Напишите процедуру, которая принимает в качестве ввода процедуру, вычисляющую `f`, и положительное целое `n`,
> и возвращает процедуру, вычисляющую `n`-кратное применение `f`.
>
> Требуется, чтобы Вашу процедуру можно было использовать в таких контекстах:
>
> ```
> ((repeated square 2) 5)
> 625
> ```
>
> Подсказка: может оказаться удобно использовать `compose` из упражнения 1.42

Решение на Scala:

```scala mdoc:reset:silent
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

val square: Int => Int = x => x * x
```
```scala mdoc
repeated(square, 2)(5)
```

[Scala worksheet](https://scastie.scala-lang.org/YnKFqK1dTEaThXI09u1s1A)


#### Упражнение 1.44

> Идея сглаживания (*smoothing a function*) играет важную роль в обработке сигналов.
> Если `f` — функция, а `dx` — некоторое малое число, то сглаженная версия `f` есть функция,
> значение которой в точке `x` есть среднее между `f(x − dx)`, `f(x)` и `f(x + dx)`.
> Напишите процедуру `smooth`, которая в качестве ввода принимает процедуру, вычисляющую `f`,
> и возвращает процедуру, вычисляющую сглаженную версию `f`.
>
> Иногда бывает удобно проводить повторное сглаживание (то есть сглаживать сглаженную функцию и т.д.),
> получая `n`-кратно сглаженную функцию (*n-fold smoothed function*).
> Покажите, как породить `n`-кратно сглаженную функцию с помощью `smooth` и `repeated` из упражнения 1.43.


Решение на Scala:

```scala
val dx: Double = 1e-6

def smooth(f: Double => Double): Double => Double =
  x => (f(x - dx) + f(x) + f(x + dx)) / 3

def nFoldSmoothedFunction(f: Double => Double, n: Int): Double => Double =
  repeated(smooth, n)(f)
```

[Scala worksheet](https://scastie.scala-lang.org/ofcf1izRRLKiYkw2dGP0zA)


#### Упражнение 1.45

> В разделе 1.3.3 мы видели, что попытка вычисления квадратных корней путем наивного поиска неподвижной точки `y → x/y` не сходится,
> и что это можно исправить путем торможения усреднением.
> Тот же самый метод работает для нахождения кубического корня как неподвижной точки `y → x/y²`, заторможенной усреднением.
> К сожалению, этот процесс не работает для корней четвертой степени — однажды примененного торможения усреднением недостаточно,
> чтобы заставить сходиться процесс поиска неподвижной точки `y → x/y³`.
> С другой стороны, если мы применим торможение усреднением дважды
> (т.е. применим торможение усреднением к результату торможения усреднением от `y → x/y³`),
> то поиск неподвижной точки начнет сходиться.
> Проделайте эксперименты, чтобы понять, сколько торможений усреднением нужно,
> чтобы вычислить корень `n`-ой степени как неподвижную точку
> на основе многократного торможения усреднением функции `y → x/y^(n−1)`.
> Используя свои результаты для написания простой процедуры вычисления корней `n`-ой степени с помощью процедур
> `fixed-point`, `average-damp` и `repeated` из упражнения 1.43.
> Считайте, что все арифметические операции, какие Вам понадобятся, присутствуют в языке как примитивы.

Судя по наблюдениям, для вычисления корней `n`-ой степени достаточно `log(n) + 1` торможений.

Решение на Scala:

```scala mdoc:reset
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

def average(a: Double, b: Double): Double = (a + b) / 2

def averageDamp(f: Double => Double): Double => Double =
  x => average(x, f(x))

val tolerance = 1e-5

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

def nFoldAverageFunction(f: Double => Double, n: Int): Double => Double =
  repeated(averageDamp, n)(f)

def nRoot(x: Double, n: Int): Double =
  val f: Double => Double = y => (x / math.pow(y, n - 1))
  fixedPoint(nFoldAverageFunction(f, math.log(n).toInt + 1), 1.0)

nRoot(math.pow(2, 100), 100)
```

[Scala worksheet](https://scastie.scala-lang.org/tm9BwnTRTJ2iFzNuzBVKDA)


#### Упражнение 1.46

> Некоторые из вычислительных методов, описанных в этой главе,
> являются примерами чрезвычайно общей вычислительной стратегии, называемой пошаговое улучшение (*iterative improvement*).
> Пошаговое улучшение состоит в следующем: чтобы что-то вычислить, нужно взять какое-то начальное значение,
> проверить, достаточно ли оно хорошо, чтобы служить ответом,
> и если нет, то улучшить это значение и продолжить процесс с новым значением.
>
> Напишите процедуру `iterative-improve`, которая принимает в качестве аргументов две процедуры:
> проверку, достаточно ли хорошо значение, и метод улучшения значения.
> `Iterative-improve` должна возвращать процедуру, которая принимает начальное значение в качестве аргумента
> и улучшает его, пока оно не станет достаточно хорошим.
>
> Перепишите процедуру `sqrt` из раздела 1.1.7 и
> процедуру `fixed-point` из раздела 1.3.3 в терминах `iterative-improve`.

Решение на Scala:

```scala
def iterativeImprove(
    doesCloseEnough: (Double, Double) => Boolean,
    next: Double => Double
): Double => Double = firstGuess =>
  def tryGuess(guess: Double): Double =
    val nextGuess = next(guess)
    if doesCloseEnough(nextGuess, guess) then nextGuess
    else tryGuess(nextGuess)
  tryGuess(firstGuess)

def doesCloseEnough(a: Double, b: Double): Boolean =
  math.abs(a - b) < 1e-5

def average(x: Double, y: Double): Double = (x + y) / 2

def sqrt(x: Double): Double =
  iterativeImprove(doesCloseEnough, y => average(y, x / y))(1.0)

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  iterativeImprove(doesCloseEnough, f)(firstGuess)
```

[Scala worksheet](https://scastie.scala-lang.org/WyKYYyNVSBeB0tzxRl5t2Q)


---

**Ссылки:**

- [Abelson Harold, Sussman Gerald Jay, Sussman Julie - Structure and Interpretation of Computer Programs](https://web.mit.edu/6.001/6.037/sicp.pdf)
