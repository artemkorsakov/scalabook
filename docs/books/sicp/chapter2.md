# Глава 2. Построение абстракций с помощью данных

## 2.1. Введение в абстракцию данных

### 2.1.1. Пример: арифметические операции над рациональными числами

В качестве пары можно было бы использовать [кортежи](../../scala/collections/tuple.md):

```scala mdoc
opaque type Pair = (Long, Long)
val cons: Pair = (-5, 7)
val car = cons._1
val cdr = cons._2
```

но в дальшейшем для удобства и наглядности будем использовать [case class](../../scala/modeling/case-class.md).

Рациональные числа на Scala можно выразить вот так:

```scala mdoc:silent
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
```

```scala mdoc
Rational.makeRat(-5, 7).print
Rational.makeRat(-5, 7) + Rational.makeRat(3, 7)
Rational.makeRat(-5, 7) - Rational.makeRat(3, 7)
Rational.makeRat(-5, 7) * Rational.makeRat(3, 7)
Rational.makeRat(-5, 7) / Rational.makeRat(3, 7)
Rational.makeRat(-5, 7) === Rational.makeRat(-10, 14)
```

#### Упражнение 2.1

> Определите улучшенную версию mul-rat, которая принимала бы как положительные, так и отрицательные аргументы. 
> Make-rat должна нормализовывать знак так, 
> чтобы в случае, если рациональное число положительно, то и его числитель, и знаменатель были бы положительны, 
> а если оно отрицательно, то чтобы только его числитель был отрицателен.

```scala mdoc:reset:invisible
final case class Rational private (numer: Long, denom: Long):
  lazy val print: String = s"$numer/$denom"

  import Rational.makeRat

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
    val g = gcd(numer, denom)
    val sign = if denom < 0 then -1 else 1
    Rational(sign * numer / g, sign * denom / g)

  private def gcd(a: Long, b: Long): Long =
    val u = math.abs(a)
    val v = math.abs(b)
    if u == v then u
    else if u == 0 then v
    else if v == 0 then u
    else
      (~u & 1, ~v & 1) match
        case (1, 1) => gcd(u >> 1, v >> 1) << 1
        case (1, 0) => gcd(u >> 1, v)
        case (0, 1) => gcd(u, v >> 1)
        case (_, _) => if (u > v) gcd(u - v, v) else gcd(v - u, u)
```

```scala
object Rational:
  def makeRat(numer: Long, denom: Long): Rational =
    val g = gcd(numer, denom)
    val sign = if denom < 0 then -1 else 1
    Rational(sign * numer / g, sign * denom / g)
```

```scala mdoc
Rational.makeRat(14, 49).print
Rational.makeRat(1, 2).print
Rational.makeRat(-1, -2).print
Rational.makeRat(-1, 2).print
Rational.makeRat(1, -2).print
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

```scala mdoc
final case class Point(x: Double, y: Double):
  override def toString(): String = s"($x,$y)"

final case class Segment(start: Point, end: Point):
  lazy val midpoint: Point = Point((start.x + end.x) / 2, (start.y + end.y) / 2)

Segment(Point(1, 0), Point(0, 1)).midpoint.toString
```

[Scala worksheet](https://scastie.scala-lang.org/3SvZKbYZRPaSqiCyNtwikQ)

#### Упражнение 2.3

> Реализуйте представление прямоугольников на плоскости. 
> (Подсказка: Вам могут потребоваться результаты упражнения 2.2.) 
> Определите в терминах своих конструкторов и селекторов процедуры, которые вычисляют периметр и площадь прямоугольника. 
> Теперь реализуйте другое представление для прямоугольников. 
> Можете ли Вы спроектировать свою систему с подходящими барьерами абстракции так, 
> чтобы одни и те же процедуры вычисления периметра и площади работали с любым из Ваших представлений?

```scala mdoc:reset:silent
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
```

```scala mdoc
rect.perimeter
rect.square
```

[Scala worksheet](https://scastie.scala-lang.org/WOqHhQ9qRBmR8K0fueiA3g)

---

**Ссылки:**

- [Abelson Harold, Sussman Gerald Jay, Sussman Julie - Structure and Interpretation of Computer Programs](https://web.mit.edu/6.001/6.037/sicp.pdf)
