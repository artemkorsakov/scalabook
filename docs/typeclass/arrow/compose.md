# Compose

## Формальное определение

`Compose` объединяет две функции в одну.
Функция `compose` позволяет реализовать [Semigroup](../monoid/semigroup.md) 
и [Plus](../monad/plus.md) в терминах объединения функций.

`Compose` должен удовлетворять следующим законам:

- _Ассоциативность_: `compose(compose(f, g), h) = compose(f, compose(g, h))`

#### Композиция

Имея стрелку **f** от **a** к **b** и стрелку **g** от **b** к **c**,
их _композиция_ представляет собой стрелку, идущую непосредственно от **a** к **c**.
Другими словами, если имеются две стрелки, цель одной из которых совпадает с источником другой,
то всегда можно скомпоновать их, чтобы получить третью стрелку.
Обозначается как: \\(h = g \circ f\\)

#### Ассоциативность

Предположим, что удалось разложить g на \\(j \circ k\\).
Тогда \\(h = (j \circ k) \circ f\\)
Желательно, чтобы это разложение было таким же, как и \\(h = j \circ (k \circ f)\\).
При этом должна существовать возможность заявить,
что h была декомпозирована на три более простые функции \\(h = j \circ k \circ f\\)
и не нужно отслеживать, какая декомпозиция была первой.
Это свойство называется _ассоциативностью_ композиции.

## Определение в виде кода на Scala

```dotty
trait Compose[=>:[_, _]]:
  self =>

  /** Ассоциативный `=>:` бинарный оператор. */
  def compose[A, B, C](f: B =>: C, g: A =>: B): A =>: C

  protected[this] trait ComposePlus extends Plus[[A] =>> A =>: A]:
    def plus[A](f1: A =>: A, f2: => A =>: A): A =>: A = self.compose(f1, f2)

  protected[this] trait ComposeSemigroup[A] extends Semigroup[A =>: A]:
    def combine(f1: A =>: A, f2: A =>: A): A =>: A = self.compose(f1, f2)

  def plus: Plus[[A] =>> A =>: A] = new ComposePlus {}

  def semigroup[A]: Semigroup[A =>: A] = new ComposeSemigroup {}
```

## Примеры

### Функция от одной переменной

```dotty
given Compose[Function1] with
  override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
```

## Реализация

### Реализация в Cats

```dotty
import cats.*, cats.data.*, cats.syntax.all.*

lazy val f = (_:Int) + 1
lazy val g = (_:Int) * 100

(f >>> g)(2)
// res0: Int = 300

(f <<< g)(2)
// res1: Int = 201
```

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

val f1 = (_:Int) + 1
val f2 = (_:Int) * 100

(f1 >>> f2)(2)   // 300
(f1 <<< f2)(2)   // 201
```

---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Compose.html)
