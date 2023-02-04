# Глава 1. Построение абстракций с помощью процедур

## 1.1 Элементы программирования

### 1.1.1 Выражения - 1.1.6 Условные выражения и предикаты

#### Упражнение 1.1

> Ниже приведена последовательность выражений. 
> Какой результат напечатает интерпретатор в ответ на каждое из них? 
> Предполагается, что выражения вводятся в том же порядке, в каком они написаны.

```scala
10                    // 10
(5 + 3 + 4)           // 12
(9 - 1)               // 8
(6 / 2)               // 3
((2 * 4) + (4 - 6))   // 6
val a = 3
val b = a + 1         // 4
(a + b + (a * b))     // 19
(a == b)              // false

if (b > a) && (b < (a * b)) then b else a                  // 4

if a == 4 then 6
else if b == 4 then 6 + 7 + a
else 25                                                    // 16
 
2 + (if (b > a) then b else a)                             // 6

((if a > b then a else if a < b then b else -1) * (a + 1)) // 16
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-1.worksheet.sc)

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

```scala
def square(x: Int): Int = x * x

def sumOfSquares(x: Int, y: Int): Int = square(x) + square(y)

def f(a: Int, b: Int, c: Int): Int =
  if a <= b && a <= c then sumOfSquares(b, c)
  else if b <= c then sumOfSquares(a, c)
  else sumOfSquares(a, b)

f(5, 3, 4)  // 41
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-3.worksheet.sc)

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

```scala
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
// res0: Double = 1.4142156862745097
sqrt(1000000) 
// res1: Double = 1000.0001533016629
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-7.worksheet.sc)

#### Упражнение 1.8

> Метод Ньютона для кубических корней основан на том, 
> что если **y** является приближением к кубическому корню из **x**, 
> то мы можем получить лучшее приближение по формуле
> **(x/y<sup>2</sup>+2y)/3**
> 
> С помощью этой формулы напишите процедуру вычисления кубического корня, подобную процедуре для квадратного корня. 

Процедура вычисления кубического корня:

```scala
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
// res0: Double = 1.2599210500177698
cubeOf(1000000)
// res1: Double = 100.00000000005313 
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-8.worksheet.sc)

### 1.1.8. Процедуры как абстракции типа «черный ящик»

```scala
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
// res0: Double = 1.4142156862745097
sqrt(1000000) 
// res1: Double = 1000.0001533016629
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

```scala
def functionAckermann(x: Int, y: Int): Int =
  if y == 0 then 0
  else if x == 0 then 2 * y
  else if y == 1 then 2
  else functionAckermann(x - 1, functionAckermann(x, y - 1))

functionAckermann(1, 10)
// res0: Int = 1024
functionAckermann(2, 4)
// res1: Int = 65536
functionAckermann(3, 3)
// res2: Int = 65536
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

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-10.worksheet.sc)


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

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-11.worksheet.sc)

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

```scala
def pascalsTriangle(n: Int, k: Int): Int =
  if n == 0 || k == n || k == 0 then 1
  else pascalsTriangle(n - 1, k - 1) + pascalsTriangle(n - 1, k)

pascalsTriangle(4, 2) // 6
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-12.worksheet.sc)

#### Упражнение 1.13

> Докажите, что **Fib(n)** есть целое число, ближайшее к **φ<sup>n</sup>/√5**, где **φ = (1 + √5)/2**. 
> 
> Указание: пусть **ψ = (1 − √5)/2**. 
> С помощью определения чисел Фибоначчи и индукции докажите, что **Fib(n) = (φ<sup>n</sup> − ψ<sup>n</sup>)/√5**.

Доказательство:

- Заметим, что **φ<sup>2</sup> = φ<sup>1</sup> + 1**:
  - **φ<sup>2</sup> = (1 + √5)<sup>2</sup>/4 = (6 + 2√5)/4 = 1 + (2 + 2√5)/4 = 1 + φ**
- а также, что **ψ<sup>2</sup> = ψ<sup>1</sup> + 1**:
  - **ψ<sup>2</sup> = (1 - √5)<sup>2</sup>/4 = (6 - 2√5)/4 = 1 + (2 - 2√5)/4 = 1 + ψ**  
- **n == 0**, тогда **Fib(0) = (φ<sup>0</sup> − ψ<sup>0</sup>)/√5 = (1 − 1)/√5 = 0** - верно.
- **n > 0**, 
  - **Fib(n) =** 
  - **Fib(n - 1) + Fib(n - 2) =**
  - **(φ<sup>n-1</sup> − ψ<sup>n-1</sup>)/√5 + (φ<sup>n-2</sup> − ψ<sup>n-2</sup>)/√5 =**
  - **((φ<sup>n-1</sup> + φ<sup>n-2</sup>) - (ψ<sup>n-1</sup> +  ψ<sup>n-2</sup>))/√5 =**
  - **(φ<sup>n-2</sup> * (φ + 1) - ψ<sup>n-2</sup> * (ψ +  1))/√5 =**
  - **(φ<sup>n-2</sup> * φ<sup>2</sup> - ψ<sup>n-2</sup> * ψ<sup>2</sup>)/√5 =**
  - **(φ<sup>n</sup> - ψ<sup>n</sup>)/√5**


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

> Синус угла (заданного в радианах) можно вычислить, если воспользоваться приближением **sin x ≈ x** при малых **x** 
> и употребить тригонометрическое тождество **sin(x) = 3sin(x/3) − 4sin<sup>3</sup>(x/3)**
> для уменьшения значения аргумента **sin**. 
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
> а. Сколько раз вызывается процедура **p** при вычислении **(sine 12.15)**?
> 
> б. Каковы порядки роста в терминах количества шагов и используемой памяти (как функция **a**) для процесса, 
> порождаемого процедурой **sine** при вычислении **(sine a)**?

На Scala эта программа будет выглядеть так:

```scala
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

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-15.worksheet.sc)

### 1.2.4. Возведение в степень

#### Упражнение 1.16

> Напишите процедуру, которая развивается в виде итеративного процесса 
> и реализует возведение в степень за логарифмическое число шагов, как **fast-expt**. 
> (Указание: используя наблюдение, что **(b<sup>n/2</sup>)<sup>2</sup> = (b<sup>2</sup>)<sup>n/2</sup>**, 
> храните, помимо значения степени **n** и основания **b**, дополнительную переменную состояния **a**, 
> и определите переход между состояниями так, чтобы произведение **ab<sup>n</sup>** от шага к шагу не менялось. 
> Вначале значение **a** берется равным **1**, а ответ получается как значение **a** в момент окончания процесса. 
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
> Вспомните трансформацию переменных состояния **a** и **b** процесса **fib-iter** из раздела 1.2.2:
> **a ← a + b** и **b ← a**. 
> Назовем эту трансформацию **T** и заметим, что **n**-кратное применение **T**, начиная с **1** и **0**, 
> дает нам пару **Fib(n + 1)** и **Fib(n)**. 
> Другими словами, числа Фибоначчи получаются путем применения **T<sup>n</sup>**, 
> **n**-ой степени трансформации **T**, к паре **(1,0)**. 
> Теперь рассмотрим **T** как частный случай **p = 0**, **q = 1** в семействе трансформаций **T<sub>pq</sub>**, 
> где **T<sub>pq</sub>** преобразует пару **(a,b)** по правилу **a ← bq + aq + ap**, **b ← bp + aq**. 
> 
> Покажите, что двукратное применение трансформации **T<sub>pq</sub>** 
> равносильно однократному применению трансформации **T<sub>p′q′</sub>** того же типа, 
> и вычислите **p′** и **q′** через **p** и **q**. 
> Это дает нам прямой способ возводить такие трансформации в квадрат, 
> и таким образом, мы можем вычислить **T<sup>n</sup>** с помощью последовательного возведения в квадрат, как в процедуре **fast-expt**. 
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

- **a<sub>2</sub> ← b<sub>1</sub>q + a<sub>1</sub>q + a<sub>1</sub>p ← (b<sub>0</sub>p + a<sub>0</sub>q)q + (b<sub>0</sub>q + a<sub>0</sub>p + a<sub>0</sub>q)(q + p) ← b<sub>0</sub>(2pq + q<sup>2</sup>) + a<sub>0</sub>(q<sup>2</sup> + (p + q)<sup>2</sup>)**
- **b<sub>2</sub> ← b<sub>1</sub>p + a<sub>1</sub>q ← (b<sub>0</sub>p + a<sub>0</sub>q)p + (b<sub>0</sub>q + a<sub>0</sub>p + a<sub>0</sub>q)q ← b<sub>0</sub>(p<sup>2</sup> + q<sup>2</sup>) + a<sub>0</sub>(2pq + q<sup>2</sup>)**

Представим, что **p′ = p<sup>2</sup> + q<sup>2</sup>** и **q′ = 2pq + q<sup>2</sup>**. Тогда

- **a<sub>2</sub> ← b<sub>0</sub>q′ + a<sub>0</sub>(p′ + q′)**
- **b<sub>2</sub> ← b<sub>0</sub>p′ + a<sub>0</sub>q′**

Это означает, что **(T<sub>pq</sub>)<sup>2</sup> = T<sub>p′q′</sub>**

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
> (define (start-prime-test n start-time)
>   (if (prime? n)
>       (report-prime (- (runtime) start-time))))
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
> занимает в **√10** раз больше времени, чем для чисел, близких к **1000**. Подтверждают ли это Ваши замеры времени? 
> Хорошо ли поддерживают предсказание **√n** данные для **100 000** и **1 000 000**?
> Совместим ли Ваш результат с предположением, 
> что программы на Вашей машине затрачивают на выполнение задач время, пропорциональное числу шагов?



#### Упражнение 1.23

#### Упражнение 1.24

#### Упражнение 1.25

#### Упражнение 1.26

#### Упражнение 1.27

#### Упражнение 1.28



---

**Ссылки:**
- [Упражнение 1.1 - 1.5](https://web.mit.edu/6.001/6.037/sicp.pdf#page=54)
- [Упражнение 1.6 - 1.8](https://web.mit.edu/6.001/6.037/sicp.pdf#page=60)
- [Упражнение 1.9 - 1.10](https://web.mit.edu/6.001/6.037/sicp.pdf#page=74)
- [Упражнение 1.11 - 1.13](https://web.mit.edu/6.001/6.037/sicp.pdf#page=81)
- [Упражнение 1.14 - 1.15](https://web.mit.edu/6.001/6.037/sicp.pdf#page=84)
- [Упражнение 1.16 - 1.19](https://web.mit.edu/6.001/6.037/sicp.pdf#page=87)
- [Упражнение 1.20](https://web.mit.edu/6.001/6.037/sicp.pdf#page=93)
- [Упражнение 1.21 - 1.28](https://web.mit.edu/6.001/6.037/sicp.pdf#page=98)
