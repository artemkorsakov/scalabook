# Глава 1. Построение абстракций с помощью процедур

## 1.1 Элементы программирования

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

---

**Ссылки:**

- [Упражнение 1.6 - 1.8](https://web.mit.edu/6.001/6.037/sicp.pdf#page=60)
