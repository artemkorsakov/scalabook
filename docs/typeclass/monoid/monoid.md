# Моноид

Моноиды — это фундаментальная концепция в функциональном программировании, 
которая позволяет абстрагироваться от конкретных типов данных и работать с общими операциями, 
такими как объединение, сложение или конкатенация. 
Эта абстракция не только делает код более универсальным и повторно используемым, 
но и помогает лучше понять принципы композиции данных и функций.

**Что такое моноид?**

Моноид — это математическая структура, которая состоит из:

- **Множества элементов** (например, числа, строки, списки).
- **Бинарной операции** (например, сложение, конкатенация), которая объединяет два элемента множества в один.
- **Нейтрального элемента** (например, 0 для сложения или пустая строка для конкатенации), который не изменяет результат операции.

При этом должны выполняться два ключевых свойства:

- **Ассоциативность**: Результат операции не зависит от порядка вычислений. Например, `(a + b) + c` равно `a + (b + c)`.
- **Нейтральный элемент**: Операция с нейтральным элементом не изменяет значение. Например, `a + 0` равно `a`.

**Зачем нужны моноиды?**

Моноиды играют важную роль в функциональном программировании, так как они позволяют:

- **Абстрагироваться от конкретных типов данных**: Вы можете определить общие операции 
  (например, объединение списков или сложение чисел) независимо от конкретного типа данных.
- **Создавать композируемые функции**: Моноиды позволяют объединять данные в более крупные структуры, 
  что полезно при работе с коллекциями, агрегации данных или параллельной обработке.
- **Упрощать код**: Использование моноидов делает код более декларативным и уменьшает количество шаблонного кода.
- **Работать с параллелизмом**: Ассоциативность моноидов позволяет эффективно разбивать задачи на подзадачи и выполнять их параллельно.

**Примеры моноидов**

- **Числа с операцией сложения**: Множество — числа, операция — сложение, нейтральный элемент — 0.
- **Строки с операцией конкатенации**: Множество — строки, операция — конкатенация, нейтральный элемент — пустая строка.
- **Списки с операцией объединения**: Множество — списки, операция — объединение, нейтральный элемент — пустой список.
- **Булевы значения с операцией "И"**: Множество — булевы значения, операция — логическое "И", нейтральный элемент — `true`.

## Формальное определение

Моноид (_monoid_) — это [полугруппа](semigroup.md) с единичным (нейтральным) элементом.

\\((M, +)\\) является моноидом для заданного множества \\(M\\) и операции \\(+\\),
если удовлетворяет следующим свойствам для любых \\(x, y, z \in M\\):

- _Замыкание_ (_closure_): для \\(\forall x, y \in M\\) выполняется \\(x + y \in M\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in M\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Тождественность_ или _существования нейтрального элемента_ (_identity_): 
  существует \\(\exists e \in M\\) такой, что для \\(\forall x \in M\\) выполняется \\(e + x = x + e = x\\)

> Первые два закона наследуются от полугруппы.

Также говорится, что _M — моноид относительно +_.

#### Определение из моноидальной категории

Моноид в моноидальной категории — это объект \\(m\\), снабженный двумя морфизмами:

- \\(\mu  : m \bigotimes m \to m\\)
- \\(\eta  : I \to m\\)

удовлетворяющих законам единицы и ассоциативности:

- \\(id\_{m} \bigotimes m = m \bigotimes id\_{m} = m\\)
- \\((m \bigotimes m) \bigotimes m = m \bigotimes (m \bigotimes m)\\)

## Код

```dotty
trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

Для операции сложения встречаются наименования, перечисленные в [Полугруппе](semigroup.md):
`combine`, `append`, `mappend` (Haskell), `op`, `plus`, `times`.
Для единичного элемента встречаются следующие наименования: `empty`, `mempty` (Haskell), `id`.

### Числа относительно сложения с 0

`Int` относительно сложения с `0` образуют моноид, потому что: 

- сложение ассоциативно: `(a + b) + c = a + (b + c)`
- `0` - это единичный элемент относительно сложения: `0 + a = a + 0 = a`

```dotty
given additionMonoid: Monoid[Int] with
  val empty                        = 0
  def combine(x: Int, y: Int): Int = x + y
```

### Числа относительно умножения с 1

`Int` относительно умножения с единичным элементом `1` образуют моноид, потому что:

- умножение ассоциативно: `(a * b) * c = a * (b * c)`
- `1` - это единичный элемент относительно сложения: `1*a = a*1 = a`

```dotty
given multiplicationMonoid: Monoid[Int] with
  val empty = 1
  def combine(x: Int, y: Int): Int = x * y
```

### Строки

`String` относительно конкатенации строк с единичным элементом пустой строкой `""` образуют моноид, потому что:

- конкатенация строк ассоциативна: `(a + b) + c = a + (b + c)`
    - `"мама" + ("мыла" + "раму") = ("мама" + "мыла") + "раму" = "мамамылараму"`
- `""` - это единичный элемент относительно конкатенации: `"" + a = a + "" = a`

```dotty
given concatenationMonoid: Monoid[String] with
  val empty                                 = ""
  def combine(x: String, y: String): String = x + y
```

### Последовательность

Последовательность относительно операции объединения с пустой последовательностью в качестве единичного элемента.

```dotty
given listMonoid[T]: Monoid[List[T]] with
  val empty                                    = List.empty[T]
  def combine(x: List[T], y: List[T]): List[T] = x ++ y
```

### Кортеж от двух и более моноидов

```dotty
given nestedMonoid[A, B](using
    aMonoid: Monoid[A],
    bMonoid: Monoid[B]
): Monoid[(A, B)] with
  val empty: (A, B) = (aMonoid.empty, bMonoid.empty)
  def combine(x: (A, B), y: (A, B)): (A, B) =
    (aMonoid.combine(x._1, y._1), bMonoid.combine(x._2, y._2))
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

### Множество - `Boolean`, операция - логическое И (`&&`), пустой элемент - `true`

```dotty
given booleanAnd: Monoid[Boolean] with
  val empty: Boolean                             = true
  def combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2 
```

### Множество - `Boolean`, операция - логическое Или (`||`), пустой элемент - `false`

```dotty
given booleanOr: Monoid[Boolean] with
  val empty: Boolean                             = false
  def combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2
```

### Законы

- Замыкание следует из определения функции `combine`: тип результата этой функции совпадает с типом переменных,
  которые она принимает.
- Ассоциативность выражается следующим образом:
  `m.combine(x, m.combine(y, z))` должно быть равно `m.combine(m.combine(x, y), z)`.  
  Это означает, что порядок объединения элементов не влияет на итоговый результат.
- Тождественность означает, что если вы объединяете любой элемент `x` с нейтральным элементом (`m.empty`), 
  то результат всегда будет равен `x`. Другими словами, нейтральный элемент не меняет значение `x` при объединении. 
  Это можно записать так:
    - `m.combine(x, m.empty) == x`
    - `m.combine(m.empty, x) == x`

```dotty
... // Законы полугруппы

def checkRightIdentity[A](x: A)(using
    m: Monoid[A]
): ValidatedNel[String, Unit] =
  Either.cond[String, Unit](
    m.combine(x, m.empty) == x,
    (),
    "Не соблюдается тождественность справа: x + e = x"
  ).toValidatedNel

def checkLeftIdentity[A](x: A)(using
    m: Monoid[A]
): ValidatedNel[String, Unit] =
  Either.cond[String, Unit](
    m.combine(m.empty, x) == x,
    (),
    "Не соблюдается тождественность слева: e + x = x"
  ).toValidatedNel
```

## Дополнительные свойства

В любом моноиде имеется ровно один нейтральный элемент. 
Это следует из свойств нейтрального элемента: \\(e\_1 = e\_1 + e\_2 = e\_2 \to e\_1 = e\_2\\).

Используя определение натуральной степени элемента полугруппы ("сложить n раз") как:
\\(a^{n} = \overset{n}{a + ... + a}\\), можно расширить определение до 0 степени: \\(a^{0} = e\\).

## Использование

Моноиды тесно связаны со списками.
Например, вот как можно свернуть список, используя моноид:

```dotty
def combineAll[A](as: List[A], m: Monoid[A]): A =
  as.foldLeft(m.empty)(m.combine)
combineAll(List(1, 2, 3, 4), intMultiplication)
// 24
```

Тот факт, что операция `combine` моноида является ассоциативной, означает, что можно выбирать,
как сворачивать структуру данных, такую как список.
Если есть моноид, можно уменьшить список, используя сбалансированное сворачивание (_balanced fold_),
которое может быть более эффективным для некоторых операций, а также допускает параллелизм.

В качестве примера предположим, что есть последовательность `a`, `b`, `c`, `d`,
которую необходимо сократить, используя некоторый моноид.
_Balanced fold_ выглядит так:

```dotty
combine(combine(a, b), combine(c, d))
```

Обратите внимание, что _balanced fold_ допускает параллелизм,
потому что два внутренних вызова независимы друг от друга и могут выполняться одновременно.
Но помимо этого, более сбалансированная древовидная структура может быть более эффективной в тех случаях,
когда стоимость каждого из них пропорциональна размеру его комбинированных аргументов.

Например, рассмотрим производительность этого выражения во время выполнения:

```dotty
List("lorem", "ipsum", "dolor", "sit").foldLeft("")(_ + _)
```

На каждом этапе сворачивания (_fold_) выделяется полная промежуточная строка только лишь для того,
чтобы быть отброшенной, а `String` выделяет более крупную строку на следующем шаге.
Напомним, что значения являются неизменяемыми, и что `String` вычисляется для строк
и требует выделения нового массива символов и копирования `a + b` для строк `a` и `b` в этот новый массив.
Это занимает время, пропорциональное `a.length + b.length`.

Вот трассировка вычисляемого предыдущего выражения:

```dotty
List("lorem", "ipsum", "dolor", "sit").foldLeft("")(_ + _)
List("ipsum", "dolor", "sit").foldLeft("lorem")(_ + _)
List("dolor", "sit").foldLeft("loremipsum")(_ + _)
List("sit").foldLeft("loremipsumdolor")(_ + _)
List().foldLeft("loremipsumdolorsit")(_ + _)
"loremipsumdolorsit"
```

Обратите внимание, что промежуточные строки создаются, а затем сразу же отбрасываются.
Более эффективной стратегией было бы объединение половинок последовательности,
что называется сбалансированным сворачиванием (_balanced fold_) —
сначала строятся `"loremipsum"` и `"dolorsit"`, а затем они складываются вместе.

## Операции с моноидами

Моноиды можно объединять:
если `A` и `B` - это моноиды, то кортеж `(A, B)` также является моноидом (и называется _продуктом_)

```dotty
given productMonoid[A, B](using ma: Monoid[A], mb: Monoid[B]): Monoid[(A, B)] with
  def combine(x: (A, B), y: (A, B)): (A, B) =
    (ma.combine(x(0), y(0)), mb.combine(x(1), y(1)))

  val empty: (A, B) = (ma.empty, mb.empty)
```

Моноидом также является `Map`, если доступен моноид для его значений:

```dotty
given mapMergeMonoid[K, V](using mv: Monoid[V]): Monoid[Map[K, V]] with
  def combine(a: Map[K, V], b: Map[K, V]): Map[K, V] =
    (a.keySet ++ b.keySet).foldLeft(empty) { (acc, k) =>
      acc.updated(k, mv.combine(a.getOrElse(k, mv.empty), b.getOrElse(k, mv.empty)))
    }
    
  val empty: Map[K, V] = Map()
```

Моноидом является функция, результаты которой являются моноидами.

```dotty
given functionMonoid[A, B](using mb: Monoid[B]): Monoid[A => B] with
  def combine(f: A => B, g: A => B): A => B = a => mb.combine(f(a), g(a))

  val empty: A => B = _ => mb.empty
```

Тот факт, что несколько моноидов могут быть объединены в один, означает,
что можно одновременно выполнять несколько вычислений при свертывании структуры данных.
Например, можно взять длину и сумму списка одновременно, чтобы вычислить среднее значение:

```dotty
val p = List(1, 2, 3, 4).foldMap(a => (1, a))
// p: Tuple2[Int, Int] = (4, 10)
val mean = p(0) / p(1).toDouble
// mean: Double = 0.4
```

## Схема

```mermaid
classDiagram
    class Semigroup~A~{
        +combine(x: A, y: A) A
    }  
    class Monoid~A~{
        +empty() A
    }
    Semigroup <|-- Monoid
```

## Реализация в библиотеках

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
1 |+| Monoid.empty[Int]           // 1
Monoid.isEmpty(Monoid.empty[Int]) // true
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/monoid.html)
- [Category Theory for Programmers - Bartosz Milewski](https://bartoszmilewski.com/2014/12/05/categories-great-and-small)
- [Cats](https://typelevel.org/cats/typeclasses/monoid.html)
- [Functional Programming in Scala, Second Edition, Chapter 10](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/Monoid.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monoid.html#Monoid)
- [Of Algebirds, Monoids, Monads, and other Bestiary for Large-Scale Data Analytics](https://www.michael-noll.com/blog/2013/12/02/twitter-algebird-monoid-monad-for-large-scala-data-analytics/#monoids)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-monoid)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Monoid.html)
- [Semigroups and Monoids in Scala](https://www.youtube.com/watch?v=LmBTiFYa-V4&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [Spire home page](https://typelevel.org/spire)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D0%BD%D0%BE%D0%B8%D0%B4)
