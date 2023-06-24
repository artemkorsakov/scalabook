# Глава 2. Построение абстракций с помощью данных

## 2.1. Введение в абстракцию данных

### 2.1.1. Пример: арифметические операции над рациональными числами

В качестве пары можно было бы использовать кортежи:

```dotty
opaque type Pair = (Long, Long)
val cons: Pair = (-5, 7)
// cons: Tuple2[Long, Long] = (-5L, 7L)
val car = cons._1
// car: Long = -5L
val cdr = cons._2
// cdr: Long = 7L
```

но в дальшейшем для удобства и наглядности будем использовать case class.

Рациональные числа на Scala можно выразить вот так:

```dotty
final case class Rational private (numer: Long, denom: Long):
  import Rational.makeRat

  lazy val print: String = s"$numer/$denom"

  def +(that: Rational): Rational =
    makeRat(numer * that.denom + that.numer * denom, denom * that.denom)

  def -(that: Rational): Rational =
    makeRat(numer * that.denom - that.numer * denom, denom * that.denom)

  def *(that: Rational): Rational =
    makeRat(numer * that.numer, denom * that.denom)

  def /(that: Rational): Rational =
    makeRat(numer * that.denom, denom * that.numer)

  def ===(that: Rational): Boolean =
    numer * that.denom == denom * that.numer

object Rational:
  def makeRat(numer: Long, denom: Long): Rational =
    Rational(numer, denom)

Rational.makeRat(-5, 7).print
// res0: String = "-5/7"
Rational.makeRat(-5, 7) + Rational.makeRat(3, 7)
// res1: Rational = Rational(numer = -14L, denom = 49L)
Rational.makeRat(-5, 7) - Rational.makeRat(3, 7)
// res2: Rational = Rational(numer = -56L, denom = 49L)
Rational.makeRat(-5, 7) * Rational.makeRat(3, 7)
// res3: Rational = Rational(numer = -15L, denom = 49L)
Rational.makeRat(-5, 7) / Rational.makeRat(3, 7)
// res4: Rational = Rational(numer = -35L, denom = 21L)
Rational.makeRat(-5, 7) === Rational.makeRat(-10, 14)
// res5: Boolean = true
```

#### Упражнение 2.1

> Определите улучшенную версию mul-rat, которая принимала бы как положительные, так и отрицательные аргументы. 
> Make-rat должна нормализовывать знак так, 
> чтобы в случае, если рациональное число положительно, то и его числитель, и знаменатель были бы положительны, 
> а если оно отрицательно, то чтобы только его числитель был отрицателен.

```dotty
object Rational:
  def makeRat(numer: Long, denom: Long): Rational =
    val g = gcd(numer, denom)
    val sign = if denom < 0 then -1 else 1
    Rational(sign * numer / g, sign * denom / g)
```

```dotty
Rational.makeRat(14, 49).print
// res7: String = "2/7"
Rational.makeRat(1, 2).print
// res8: String = "1/2"
Rational.makeRat(-1, -2).print
// res9: String = "1/2"
Rational.makeRat(-1, 2).print
// res10: String = "-1/2"
Rational.makeRat(1, -2).print
// res11: String = "-1/2"
```

[Scala worksheet](https://scastie.scala-lang.org/E3EOUMyRRIKLm2KnmdZ7XA)

### 2.1.2. Барьеры абстракции

#### Упражнение 2.2

> Рассмотрим задачу представления отрезков прямой на плоскости. 
> Каждый отрезок представляется как пара точек: начало и конец. 
> Определите конструктор make-segment и селекторы startsegment и end-segment, 
> которые определяют представление отрезков в терминах точек. 
> Далее, точку можно представить как пару чисел: координата x и координата y. 
> Соответственно, напишите конструктор make-point и селекторы x-point и y-point, которые определяют такое представление. 
> Наконец, используя свои селекторы и конструктор, напишите процедуру midpointsegment, 
> которая принимает отрезок в качестве аргумента и возвращает его середину 
> (точку, координаты которой являются средним координат концов отрезка). 
> Чтобы опробовать эти процедуры, Вам потребуется способ печатать координаты точек

```dotty
final case class Point(x: Double, y: Double):
  override def toString(): String = s"($x,$y)"

final case class Segment(start: Point, end: Point):
  lazy val midpoint: Point = Point((start.x + end.x) / 2, (start.y + end.y) / 2)

Segment(Point(1, 0), Point(0, 1)).midpoint.toString
// res12: String = "(0.5,0.5)"
```

[Scala worksheet](https://scastie.scala-lang.org/3SvZKbYZRPaSqiCyNtwikQ)

#### Упражнение 2.3

> Реализуйте представление прямоугольников на плоскости. 
> (Подсказка: Вам могут потребоваться результаты упражнения 2.2.) 
> Определите в терминах своих конструкторов и селекторов процедуры, которые вычисляют периметр и площадь прямоугольника. 
> Теперь реализуйте другое представление для прямоугольников. 
> Можете ли Вы спроектировать свою систему с подходящими барьерами абстракции так, 
> чтобы одни и те же процедуры вычисления периметра и площади работали с любым из Ваших представлений?

```dotty
final case class Point(x: Double, y: Double):
  override def toString(): String = s"($x,$y)"

final case class Segment(start: Point, end: Point):
  lazy val midpoint: Point = Point((start.x + end.x) / 2, (start.y + end.y) / 2)
  lazy val length: Double =
    math.sqrt(math.pow(start.x - end.x, 2.0) + math.pow(start.y - end.y, 2.0))

final case class Rectangle(
    topLeft: Point,
    topRight: Point,
    bottomRight: Point,
    bottomLeft: Point
):
  private lazy val topSideLength: Double = Segment(topLeft, topRight).length
  private lazy val leftSideLength: Double = Segment(topLeft, bottomLeft).length

  lazy val perimeter: Double =
    2 * (topSideLength + leftSideLength)
  lazy val square: Double =
    topSideLength * leftSideLength

object Rectangle:
  def apply(segment1: Segment, segment2: Segment): Rectangle =
    Rectangle(segment1.start, segment1.end, segment2.start, segment2.end)

val rect = Rectangle(Point(0, 0), Point(1, 0), Point(1, 1), Point(0, 1))
rect.perimeter
// res14: Double = 4.0
rect.square
// res15: Double = 1.0
```

[Scala worksheet](https://scastie.scala-lang.org/WOqHhQ9qRBmR8K0fueiA3g)

#### Упражнение 2.4

> Вот еще одно процедурное представление для пар. 
> Проверьте для этого представления, что при любых двух объектах x и y (car (cons x y)) возвращает x
> ```
> (define (cons x y)
>   (lambda (m) (m x y)))
> (define (car z)
>   (z (lambda (p q) p)))
> ```
> Каково соответствующее определение cdr? 
> (Подсказка: Чтобы проверить, что это работает, используйте подстановочную модель из раздела 1.1.5.)

```dotty
final case class Cons[A](x: A, y: A):
  def lambda(f: (A, A) => A): A =
    f(x, y)

def car[A](z: Cons[A]): A =
  z.lambda((p: A, q: A) => p)

def cdr[A](z: Cons[A]): A =
  z.lambda((p: A, q: A) => q)
```

Рассмотрим цепочку преобразований:

- `(car (cons x y))`
- `(cons x y).lambda((p: A, q: A) => p)`
- `((p: A, q: A) => p)(x, y)`
- `x`

Аналогично для `cdr`.

#### Упражнение 2.5

> Покажите, что можно представлять пары неотрицательных целых чисел, 
> используя только числа и арифметические операции, если представлять пару a и b 
> как произведение \\(2^{a}*3^{b}\\). 
> Дайте соответствующие определения процедур cons, car и cdr.

```dotty
final case class Cons(x: Int, y: Int):
  lazy val cons: Int = math.pow(2, x).toInt * math.pow(3, y).toInt

  def degreeOfFactor(a: Int): Int =
    def loop(n: Int, count: Int): Int =
      if n % a == 0 then loop(n / a, count + 1)
      else count 
    loop(cons, 0)

def car(z: Cons): Int =
  z.degreeOfFactor(2)

def cdr(z: Cons): Int =
  z.degreeOfFactor(3)
```

#### Упражнение 2.6

> Если представление пар как процедур было для Вас еще недостаточно сумасшедшим, 
> то заметьте, что в языке, который способен манипулировать процедурами, 
> мы можем обойтись и без чисел (по крайней мере, пока речь идет о неотрицательных числах), 
> определив 0 и операцию прибавления 1 так:
> ```
> (define zero (lambda (f) (lambda (x) x)))
> 
> (define (add-1 n)
>   (lambda (f) (lambda (x) (f ((n f) x)))))
> ```
> Такое представление известно как числа Чёрча (Church numerals), 
> по имени его изобретателя, Алонсо Чёрча, того самого логика, который придумал λ-исчисление.
> Определите one (единицу) и two (двойку) напрямую (не через zero и add-1). 
> (Подсказка: вычислите (add-1 zero) с помощью подстановки.) 
> Дайте прямое определение процедуры сложения + (не в терминах повторяющегося применения add-1).

Число Чёрча можно определить как n-кратное применение заданной функции к аргументу.

Например, вот так:

```dotty
def zero[A](f: A => A): A => A = x => x

def add1[A](f: A => A, n: Int): A => A =
  if n == 0 then zero(f)
  else (a: A) => add1(f, n - 1)(a)
```

Тогда единица - это однократное применение функции, двойка - двукратное, а сложение `a` и `b` - 
это применение функции `f` к аргументу `a + b` раз.

#### Упражнение 2.7

> Программа Лизы неполна, поскольку она не определила, как реализуется абстракция интервала. 
> Вот определение конструктора интервала: `(define (make-interval a b) (cons a b))`
> Завершите реализацию, определив селекторы upper-bound и lower-bound.

```dotty
final case class Interval(a: Double, b: Double):
  lazy val upperBound: Double = math.max(a, b)
  lazy val lowerBound: Double = math.min(a, b)

  def add(that: Interval): Interval =
    Interval(
      this.lowerBound + that.lowerBound,
      this.upperBound + that.upperBound
    )

  def mul(that: Interval): Interval =
    val p1 = this.lowerBound * that.lowerBound
    val p2 = this.lowerBound * that.upperBound
    val p3 = this.upperBound * that.lowerBound
    val p4 = this.upperBound * that.upperBound
    Interval(
      math.min(math.min(p1, p2), math.min(p3, p4)),
      math.max(math.max(p1, p2), math.max(p3, p4))
    )

  def div(that: Interval): Interval =
    mul(Interval(1.0 / that.upperBound, 1.0 / that.lowerBound))
```

#### Упражнение 2.8

> Рассуждая в духе Лизы, опишите, как можно вычислить разность двух интервалов. 
> Напишите соответствующую процедуру вычитания, называемую sub-interval.

```dotty
def sub(that: Interval): Interval =
  Interval(
    this.lowerBound - that.upperBound,
    this.upperBound - that.lowerBound
  )
```

#### Упражнение 2.9

> Радиус (width) интервала определяется как половина расстояния между его верхней и нижней границами. 
> Радиус является мерой неопределенности числа, которое обозначает интервал. 
> Есть такие математические операции, для которых радиус результата зависит только от радиусов интервалов-аргументов, 
> а есть такие, для которых радиус результата не является функцией радиусов аргументов. 
> Покажите, что радиус суммы (или разности) двух интервалов зависит только от радиусов интервалов, 
> которые складываются (или вычитаются). 
> Приведите примеры, которые показывают, что для умножения или деления это не так.

```dotty
lazy val width: Double = (upperBound - lowerBound) / 2.0
```

Радиус суммы интервалов будет вычисляться так:

- `((this.upperBound + that.upperBound) - (this.lowerBound + that.lowerBound)) / 2.0`
- `((this.upperBound - this.lowerBound) / 2.0 + (that.upperBound - that.lowerBound) / 2.0)`
- `this.width + that.width`

Радиус разности интервалов будет вычисляться так:

- `((this.upperBound - that.lowerBound) - (this.lowerBound - that.upperBound)) / 2.0`
- `((this.upperBound - this.lowerBound) / 2.0 + (that.upperBound - that.lowerBound) / 2.0)`
- `this.width + that.width`

С умножением и делением ситуация иная: если взять два интервала радиуса `1`: `(0, 2)` и `(2, 4)`,
то произведением интервалов будет интервал `(0, 8)` с радиусом `4`, 
а результат деления - интервал `(0, 1)` с радиусом `0.5`.

#### Упражнение 2.10

> Бен Битобор, системный программист-эксперт, смотрит через плечо Лизы и замечает: 
> неясно, что должно означать деление на интервал, пересекающий ноль. 
> Модифицируйте код Лизы так, чтобы программа проверяла это условие и сообщала об ошибке, если оно возникает.

```dotty
def div(that: Interval): Option[Interval] =
  if that.lowerBound <= 0.0 && that.upperBound >= 0.0 then None
  else Some(mul(Interval(1.0 / that.upperBound, 1.0 / that.lowerBound)))
```

#### Упражнение 2.11

> Проходя мимо, Бен делает туманное замечание: 
> «Если проверять знаки концов интервалов, можно разбить mul-interval на девять случаев, 
> из которых только в одном требуется более двух умножений». 
> Перепишите эту процедуру в соответствии с предложением Бена.

```dotty
def mul(that: Interval): Interval =
  (
    this.lowerBound < 0,
    this.upperBound < 0,
    that.lowerBound < 0,
    that.upperBound < 0
  ) match
    case (true, true, true, true) =>
      Interval(
        this.upperBound * that.upperBound,
        this.lowerBound * that.lowerBound
      )
    case (true, true, true, false) =>
      Interval(
        this.lowerBound * that.upperBound,
        this.lowerBound * that.lowerBound
      )
    case (true, true, false, false) =>
      Interval(
        this.lowerBound * that.upperBound,
        this.upperBound * that.lowerBound
      )
    case (true, false, true, true) =>
      Interval(
        this.upperBound * that.lowerBound,
        this.lowerBound * that.lowerBound
      )
    case (true, false, true, false) =>
      val p1 = this.lowerBound * that.lowerBound
      val p2 = this.lowerBound * that.upperBound
      val p3 = this.upperBound * that.lowerBound
      val p4 = this.upperBound * that.upperBound
      Interval(
        math.min(p2, p3),
        math.max(p1, p4)
      )
    case (true, false, false, false) =>
      Interval(
        this.lowerBound * that.upperBound,
        this.upperBound * that.upperBound
      )
    case (false, false, true, true) =>
      Interval(
        this.upperBound * that.lowerBound,
        this.lowerBound * that.upperBound
      )
    case (false, false, true, false) =>
      Interval(
        this.upperBound * that.lowerBound,
        this.upperBound * that.upperBound
      )
    case (false, false, false, false) =>
      Interval(
        this.lowerBound * that.lowerBound,
        this.upperBound * that.upperBound
      )
```

#### Упражнение 2.12

> Определите конструктор make-center-percent, 
> который принимает среднее значение и погрешность в процентах и выдает требуемый интервал. 
> Нужно также определить селектор percent, который для данного интервала выдает погрешность в процентах. 
> Селектор center остается тем же, что приведен выше.

```dotty
final case class Interval(a: Double, b: Double):
  lazy val upperBound: Double = math.max(a, b)
  lazy val lowerBound: Double = math.min(a, b)
  lazy val center: Double = (upperBound + lowerBound) / 2.0
  lazy val width: Double = (upperBound - lowerBound) / 2.0
  lazy val percent: Double = (width / center) * 100

  // ...

object Interval:
  def makeCenterWidth(center: Double, width: Double): Interval =
    Interval(center - width, center + width)

  def makeCenterPercent(center: Double, percent: Double): Interval =
    val width: Double = percent * center / 100.0
    makeCenterWidth(center, width)
```

#### Упражнение 2.13

> Покажите, что, если предположить, что погрешность составляет малую долю величины интервала, 
> то погрешность в процентах произведения двух интервалов можно получить 
> из погрешности в процентах исходных интервалов по простой приближенной формуле. 
> Задачу можно упростить, если предположить, что все числа положительные.

Рассмотрим произведение двух интервалов с маленькой погрешностью:

- `Interval(c1 - w1, c1 + w1) * Interval(c2 - w2, c2 + w2)`
- `Interval((c1 - w1) * (c2 - w2), (c1 + w1) * (c2 + w2))`
- `mulCenter = ((c1 * c2 + w1 * c2 + w2 * c1 + w1 * w2) + (c1 * c2 - w1 * c2 - w2 * c1 + w1 * w2)) / 2`
- `mulCenter = (c1 * c2 + w1 * w2)`
- т.к. погрешность малая величина, то слагаемым `w1 * w2` можно пренебречь
- `mulWidth = ((c1 * c2 + w1 * c2 + w2 * c1 + w1 * w2) - (c1 * c2 - w1 * c2 - w2 * c1 + w1 * w2)) / 2`
- `mulWidth = (w1 * c2 + w2 * c1)`
- `mulPercent = ((w1 * c2 + w2 * c1) / (c1 * c2)) * 100 = (w1 / c1) * 100 + (w2 / c2) * 100`
- таким образом погрешность произведения приближенно равна сумме погрешностей множителей

#### Упражнение 2.14

> Покажите, что Дайко прав. 
> Исследуйте поведение системы на различных арифметических выражениях. 
> Создайте несколько интервалов A и B и вычислите с их помощью выражения A/A и A/B. 
> Наибольшую пользу Вы получите, если будете использовать интервалы, 
> радиус которых составляет малую часть от среднего значения. 
> Исследуйте результаты вычислений в форме центр/проценты (см. упражнение 2.12).

```dotty
def par1(r1: Interval, r2: Interval): Option[Interval] =
  (r1.mul(r2)).div(r1.add(r2))

def par2(r1: Interval, r2: Interval): Option[Interval] =
  for
    one <- Interval(1, 2).div(Interval(1, 2))
    oneDivR1 <- one.div(r1)
    oneDivR2 <- one.div(r2)
    result <- one.div(oneDivR1.add(oneDivR2))
  yield result

val r1 = Interval.makeCenterWidth(10, 0.01)
// r1: Interval = Interval(a = 9.99, b = 10.01)
val r2 = Interval.makeCenterWidth(20, 0.01)
// r2: Interval = Interval(a = 19.99, b = 20.01)
par1(r1, r2)
// res17: Option[Interval] = Some(
//   value = Interval(a = 6.652235176548966, b = 6.681124082721815)
// )
par2(r1, r2)
// res18: Option[Interval] = Some(
//   value = Interval(a = 1.6652776851234157, b = 26.6888874083944)
// )
```

Методы действительно отдают различные результаты, потому что деление интервала на себя (A/A) - это не единица,
а интервал, имеющий небольшую погрешность.

[Scala worksheet](https://scastie.scala-lang.org/56fiIZVmSTitSmDRQmMFtw)

#### Упражнение 2.15

> Ева Лу Атор, другой пользователь Лизиной программы, тоже заметила, что алгебраически эквивалентные, но различные выражения могут давать разные результаты. Она говорит, что формула для
вычисления интервалов, которая использует Лизину систему, будет давать более узкие границы
погрешности, если ее удастся записать так, чтобы ни одна переменная, представляющая неточную величину, не повторялась. Таким образом, говорит она, par2 «лучше» как программа для
параллельных резисторов, чем par1. Права ли она? Почему?

#### Упражнение 2.16

> Объясните в общем случае, почему эквивалентные алгебраические выражения могут давать разные
результаты. Можете ли Вы представить себе пакет для работы с интервальной арифметикой,
который бы не обладал этим недостатком, или такое невозможно? (Предупреждение: эта задача
очень сложна.)

[SICP section 2.1.4 - Eli Bendersky](https://eli.thegreenplace.net/2007/07/27/sicp-section-214)

## 2.2. Иерархические данные и свойство замыкания


---

**Ссылки:**

- [Abelson Harold, Sussman Gerald Jay, Sussman Julie - Structure and Interpretation of Computer Programs](https://web.mit.edu/6.001/6.037/sicp.pdf)
