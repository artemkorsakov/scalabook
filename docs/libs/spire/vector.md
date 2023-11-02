# Vector и компания

Семейство классов типов векторного пространства предоставляет базовые векторные операции. 
Они параметризованы на 2 типа: векторный тип и скалярный тип.

Очень полезное векторное пространство — это пространство функций `T => Double`. 
Функции можно добавлять, просто складывая их по точкам, `(f + g)(x) = f(x) + g(x)`
и умножение обрабатывается аналогичным образом. 
Учитывая это определение, определение функционального пространства довольно просто:

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

given vspace[T]: VectorSpace[T => Double, Double] with
  def scalar: Field[Double] = summon[Field[Double]]

  def zero: T => Double                                 = _ => 0.0
  def negate(f: T => Double): T => Double               = x => -1 * f(x)
  def plus(f: T => Double, g: T => Double): T => Double = x => f(x) + g(x)
  def timesl(r: Double, f: T => Double): T => Double    = x => r * f(x)
```

Как только это будет сделано, мы сможем рассматривать функции типа `T => Double` как векторы:

```dotty
def f(x: String) = x.length.toDouble
def g(x: String) = x.hashCode.toDouble

val h: String => Double = 4.0 *: f _ + g _
h("test")
// val res0: Double = 3556514.0
```

Эквивалентно мы могли бы определить `h` как:

```dotty
def h(x: String) = 4*x.length.toDouble + x.hashCode.toDouble
```

## LeftModule

Левый модуль — это обобщение векторного пространства над полем, 
где скаляры — это элементы кольца (не обязательно коммутативного).

Левый модуль определяет левое умножение на скаляры. 

- `timesl` (`*:`): скалярное умножение

```dotty
trait LeftModule[V, R]:
  def timesl(r: R, v: V): V
```

Пусть `V` — абелева группа (с аддитивными обозначениями), а `R` — скалярное кольцо. 
Должны соблюдаться следующие законы для `x`, `y` в `V` и `r`, `s` в `R`:

1. `r *: (x + y) = r *: x + r *: y`
2. `(r + s) *: x = r *: x + s *: x`
3. `(r * s) *: x = r *: (s *: x)`
4. `R.one * x = x`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

4.0 *: Vector(1.0, 5.0, 3.0)
// val res0: Vector[Double] = Vector(4.0, 20.0, 12.0)
```

## RightModule

Правый модуль — это обобщение векторного пространства над полем, 
где скаляры — это элементы кольца (не обязательно коммутативного).

Правый модуль определяет правое умножение на скаляры. 

- `timesr` (`:*`): скалярное умножение

```dotty
trait RightModule[V, R]:
  def timesr(v: V, r: R): V
```

Пусть `V` — абелева группа (с аддитивными обозначениями), а `R` — скалярное кольцо.
Должны соблюдаться следующие законы для `x`, `y` в `V` и `r`, `s` в `R`:

1. `(x + y) :* r = x :* r + y :* r`
2. `x :* (r + s) = x :* r + x :* s`
3. `x :* (r * s) = (x :* r) :* s`
4. `x :* R.one = x`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

Vector(1.0, 5.0, 3.0) :* 4.0
// val res0: Vector[Double] = Vector(4.0, 20.0, 12.0)
```

## CModule

Модуль над коммутативным кольцом по определению имеет эквивалентные левый и правый модули.

```dotty
trait CModule[V, R] extends LeftModule[V, R], RightModule[V, R]:
  override def timesr(v: V, r: R): V = timesl(r, v)
```

В дополнение к законам левых и правых модулей имеем:

- `(r *: x) :* s = r *: (x :* s)`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

0.25 *: Vector(1.0, 5.0, 3.0) :* 4.0
// val res0: Vector[Double] = Vector(1.0, 5.0, 3.0)
```

## VectorSpace

Векторное пространство — это группа `V`, которую можно умножить на скаляры из поля `F`. 

Векторное пространство определяет скалярное деление:

- `divr` (`:/`): скалярное деление

```dotty
trait VectorSpace[V, F] extends CModule[V, F]:
  given def scalar: Field[F]

  def divr(v: V, f: F): V = timesl(scalar.reciprocal(f), v)
```

Для векторного пространства должны соблюдаться следующие законы: 

- _Дистрибутивный закон_: скалярное умножение должно распределяться по сложению векторов:
    - `x *: (v + w) === x *: v + x *: w`
    - и скалярное сложение `(x + y) *: v === x *: v + y *: v`
- Скалярное умножение на 1 в `F` является тождественной функцией:
    - `1 *: v === v`
- Скалярное умножение "ассоциативно": 
    - `x *: y *: v === (x * y) *: v`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

Vector(1.0, 5.0, 3.0) :/ 0.5
// val res0: Vector[Double] = Vector(2.0, 10.0, 6.0)
```

## MetricSpace\[V,R\]

`MetricSpace[V,R]` - типы со связанной метрикой

## NormedVectorSpace

Нормированное векторное пространство — это векторное пространство, снабженное функцией нормирования: `V => F`.
Основное ограничение состоит в том, что норма вектора должна масштабироваться линейно при масштабировании вектора; 
что означает `norm(k *: v) == k.abs * norm(v)`. 
Кроме того, нормированное векторное пространство также является `MetricSpace`, 
где `distance(v, w) = norm(v - w)`, и поэтому должно подчиняться неравенству треугольника.
Примером нормированного векторного пространства является \\(R^{n}\\), 
снабженное длиной евклидового вектора в качестве нормы.

```dotty
trait NormedVectorSpace[V, F] extends VectorSpace[V, F], MetricSpace[V, F]:
  def norm(v: V): F
  
  def normalize(v: V): V = divr(v, norm(v))
  def distance(v: V, w: V): F = norm(minus(v, w))
```

```dotty
```

Операции нормированного векторного пространства:

- `norm`: векторная норма
- `normalize`: нормализация вектора (поэтому норма равна 1)

## InnerProductSpace

- `dot` (`⋅`, `dot`): векторный внутренний продукт



---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Learning Spire - Vector Spaces!](https://www.chrisstucchio.com/blog/2013/learning_spire_vector_space.html)
