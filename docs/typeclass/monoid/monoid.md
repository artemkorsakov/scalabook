# Моноид

Моноид (_monoid_) — это [полугруппа](semigroup.md) с единичным элементом.

Более формально: `(M, +)` является моноидом для заданного множества `M` и операции `+`,
если удовлетворяет следующим свойствам для любых `x, y, z ∈ M`:

Законы, наследуемые от полугруппы:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ M`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`

Законы моноида:

- _Тождественность_ (_identity_): существует `∃ e ∈ M` такой, что для `∀ x ∈ M` выполняется `e + x = x + e = x`

Также говорится, что _M — моноид относительно +_.

#### Другое определение

Моноид в моноидальной категории — это объект \\(m\\), снабженный двумя морфизмами:

- \\(\mu  : m \bigotimes m \to m\\)
- \\(\eta  : I \to m\\)

удовлетворяющих законам единицы и ассоциативности:

- \\(id\_{m} \bigotimes m = m \bigotimes id\_{m} = m\\)
- \\((m \bigotimes m) \bigotimes m = m \bigotimes (m \bigotimes m)\\)

## Описание 

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

Для операции сложения встречаются наименования, перечисленные в [Полугруппе](semigroup.md).
Для единичного элемента встречаются следующие наименования: `empty`, `mempty` (Haskell), `id`.

Законы моноида принимают следующий вид:

- замыкание следует из определения функции `combine` - тип результата тот же, что и тип переменных
- ассоциативность формулируется так: `m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)`
- тождественность формулируется так: `(m.combine(x, m.empty) == x) && (m.combine(m.empty, x) == x)`

## Примеры

### Числа относительно сложения с 0

`Int` относительно сложения с `0` образуют моноид, потому что: 

- сложение ассоциативно: `(a + b) + c = a + (b + c)`
- `0` - это единичный элемент относительно сложения: `0 + a = a + 0 = a`

```dotty
given sumMonoidInstance: Monoid[Int] with
  val empty = 0
  def combine(x: Int, y: Int): Int = x + y
```

### Числа относительно умножения с 1

`Int` относительно умножения с единичным элементом `1` образуют моноид, потому что:

- умножение ассоциативно: `(a * b) * c = a * (b * c)`
- `1` - это единичный элемент относительно сложения: `1*a = a*1 = a`

```dotty
given productMonoidInstance: Monoid[Int] with
  val empty = 1
  def combine(x: Int, y: Int): Int = x * y
```

### Строки

`String` относительно конкатенации строк с единичным элементом пустой строкой `""` образуют моноид, потому что:

- конкатенация строк ассоциативна: `(a + b) + c = a + (b + c)`
    - `"мама" + ("мыла" + "раму") = ("мама" + "мыла") + "раму" = "мамамылараму"`
- `""` - это единичный элемент относительно конкатенации: `"" + a = a + "" = a`

```dotty
given stringMonoidInstance: Monoid[String] with
  val empty = ""
  def combine(x: String, y: String): String = x + y
```

### Последовательность

```dotty
given listMonoidInstance[T]: Monoid[List[T]] with
  val empty = List.empty[T]
  def combine(x: List[T], y: List[T]): List[T] = x ++ y
```

### Кортеж от двух и более моноидов

```dotty
given nestedMonoidInstance[A, B](using aMonoid: Monoid[A], bMonoid: Monoid[B]): Monoid[(A, B)] with
  lazy val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
  def combine(x: (A, B), y: (A, B)): (A, B) = (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))
```

### Option

`Option` является моноидом, если его параметр типа - полугруппа, например:

```dotty
given optionMonoidInstance[A: Semigroup]: Monoid[Option[A]] with
  val empty: Option[A] = None

  def combine(x: Option[A], y: Option[A]): Option[A] =
    (x, y) match
      case (Some(a1), Some(a2)) => Some(summon[Semigroup[A]].combine(a1, a2))
      case (Some(_), None)      => x
      case (None, Some(_))      => y
      case (None, None)         => None
```

## Реализация

### Реализация в Cats

```dotty
import cats.*
import cats.implicits.*

Monoid[Int].combine(1, Monoid[Int].empty)   // 1
"Hi " |+| "there" |+| Monoid[String].empty  // Hi there
```

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

mzero[List[Int]]             // List()
```

### Реализация в Spire

```dotty
1 |+| Monoid.empty[Int] // 1
Monoid.isEmpty(Monoid.empty[Int])
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/monoid.html)
- [Category Theory for Programmers - Bartosz Milewski](https://bartoszmilewski.com/2014/12/05/categories-great-and-small)
- [Cats](https://typelevel.org/cats/typeclasses/monoid.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Monoid.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monoid.html#Monoid)
- [Of Algebirds, Monoids, Monads, and other Bestiary for Large-Scale Data Analytics](https://www.michael-noll.com/blog/2013/12/02/twitter-algebird-monoid-monad-for-large-scala-data-analytics/#monoids)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-monoid)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Monoid.html)
- [Spire home page](https://typelevel.org/spire)
