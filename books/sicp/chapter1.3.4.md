# Глава 1. Построение абстракций с помощью процедур

## 1.3. Формулирование абстракций с помощью процедур высших порядков

### 1.3.4. Процедуры как возвращаемые значения

#### Упражнение 1.40

> Определите процедуру **cubic**, которую можно было бы использовать совместно с процедурой **newtons-method** 
> в выражениях вида **(newtons-method (cubic a b c) 1)** для приближенного вычисления нулей 
> кубических уравнений **x<sup>3</sup> + ax<sup>2</sup> + bx + c**.


Решение на Scala:

```scala
def square(x: Double): Double = x * x

def cube(x: Double): Double = x * x * x

def cubic(a: Double, b: Double, c: Double): Double => Double = x =>
  cube(x) + a * square(x) + b * x + c
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-40.worksheet.sc)


#### Упражнение 1.41

> Определите процедуру **double**, которая принимает как аргумент процедуру с одним аргументом 
> и возвращает процедуру, которая применяет исходную процедуру дважды. 
> Например, если процедура **inc** добавляет к своему аргументу **1**, 
> то **(double inc)** должна быть процедурой, которая добавляет **2**. 
> 
> Скажите, какое значение возвращает **(((double (double double)) inc) 5)**


Решение на Scala:

```scala
val inc: Int => Int = _ + 1

def double[T](f: T => T): T => T = x => f(f(x))

// (((double (double double)) inc) 5)
val f: (Int => Int) => (Int => Int) = double(double(double))
f(inc)(5) // 21
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-41.worksheet.sc)


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

```scala
def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

val square: Int => Int = x => x * x
val inc: Int => Int = x => x + 1

compose(square, inc)(6) // 49
```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-42.worksheet.sc)


#### Упражнение 1.43

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-35.worksheet.sc)


#### Упражнение 1.44

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-35.worksheet.sc)


#### Упражнение 1.45

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-35.worksheet.sc)


#### Упражнение 1.46

> 

Решение на Scala:

```scala

```

[Scala worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Fbooks%2Fsicp%2FExercise1-35.worksheet.sc)


---

**Ссылки:**
- [Упражнение 1.40 - 1.46](https://web.mit.edu/6.001/6.037/sicp.pdf#page=131)
