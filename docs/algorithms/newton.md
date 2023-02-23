# Метод Ньютона

Метод Ньютона — это итерационный численный метод нахождения корня (нуля) заданной функции. 
Поиск решения осуществляется путём построения последовательных приближений и основан на принципах простой итерации. 
Также метод Ньютона может быть использован для решения задач оптимизации, в которых требуется определить ноль первой производной либо градиента в случае многомерного пространства.

Алгоритм нахождения численного решения уравнения `f(x)=0` сводится к итерационной процедуре вычисления:

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/ad1c904e2d2798c0cbac6365db61c4c6e853d582)

Алгоритм:

- Задается начальное приближение **x<sub>0</sub>**.
- Пока не выполнено условие остановки (например, погрешность в нужных пределах), вычисляется новое приближение по формуле выше.

Чтобы реализовать метод Ньютона в виде процедуры, сначала нужно выразить понятие производной. 
В общем случае, если `g` есть функция, а `dx` — маленькое число, то
производная `Dg` функции `g` есть функция, 
значение которой в каждой точке `x` описывается формулой (при `dx`, стремящемся к нулю):

![](https://latex.codecogs.com/svg.image?Dg(x)%20=%20\frac{g(x%20+%20\mathrm{d}%20x)%20-%20g(x)%20}{\mathrm{d}%20x})

```scala
object NewtonsMethod:
  private val dx = 1e-6

  def derivative(x: Double, g: Double => Double): Double =
    (g(x + dx) - g(x)) / dx
```

С помощью `derivative` можно выразить метод Ньютона как процесс поиска неподвижной точки:

```scala
object NewtonsMethod:
  private val accuracy = 1e-6

  @scala.annotation.tailrec
  def newtonsMethod(g: Double => Double, guess: Double): Double =
    val next = transform(g)(guess)
    if math.abs(next - guess) < accuracy then next
    else newtonsMethod(g, next)

  private def transform(g: Double => Double): Double => Double =
    x => x - g(x) / derivative(x, g)
```

Рассмотрим задачу о нахождении положительных **x**, для которых **cos x=x<sup>3</sup>**. 
Эта задача может быть представлена как задача нахождения нуля функции **f(x)=cos x - x<sup>3</sup>**. 
Так как **cos x <= 1** для всех **x** и **x<sup>3</sup> > 1** для **x > 1**, очевидно, что решение лежит между **0** и **1**. 
Возьмём в качестве начального приближения значение **x<sub>0</sub>=0,5**, тогда:

```scala
val g: Double => Double = x => math.cos(x) - x * x * x

newtonsMethod(g, 0.5) // 0.865474033101...
```

---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Fothers%2FNewtonsMethod.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Fothers%2FNewtonsMethodSuite.scala)
- [Метод Ньютона - wiki](https://ru.wikipedia.org/wiki/%D0%9C%D0%B5%D1%82%D0%BE%D0%B4_%D0%9D%D1%8C%D1%8E%D1%82%D0%BE%D0%BD%D0%B0)
- [SICP: Абельсон Х., Сассман Д. - Структура и интерпретация компьютерных программ][sicp]

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf