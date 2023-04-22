# Декомпозиция в Scala 3

[Доклад был представлен на Scala Meetup-е в Музее криптографии 20 апреля 2023][Meetup]

В этой статье разберем, что может предложить Scala 3 в плане декомпозиции.

Что можно сделать с таким `trait`-ом,

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))
```

[Его можно реализовать реализовать...](https://scastie.scala-lang.org/UdEjmfOzQcmeP16PGVWPOQ)

```scala
object IntCombinator extends Combinator[Int]:
  val empty: Int                     = 0
  def combine(a1: Int, a2: Int): Int = a1 + a2

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Уже неплохо: теперь можно сворачивать числовые коллекции.

## Параметры конструктора

А ещё [переменную empty можно вынести в параметр конструктора](https://scastie.scala-lang.org/yT58r4nNTsqNvalB1TqFSQ):

```scala
trait Combinator[A](empty: A):
  def combine(a1: A, a2: A): A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int](0):
  def combine(a1: Int, a2: Int): Int = a1 + a2

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

На этом остановимся чуть подробнее...

При использовании параметров конструктора нужно помнить, что:

- `trait`-ы не должны передавать аргументы родительским `trait`-ам
- первый и только первый класс/объект `C`, расширяющий `trait T`, должен передать в него аргументы

Т.е. следующие примеры вызовут ошибку компилятора:

```scala
trait Combinator[A](empty: A):
  def combine(a1: A, a2: A): A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int]:
  def combine(a1: Int, a2: Int): Int = a1 + a2
// missing argument for parameter empty of constructor Combinator 
// in trait Combinator: (empty: Int): Playground.Combinator[Int]
```

```scala
trait Combinator[A](empty: A):
  def combine(a1: A, a2: A): A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

trait IntCombinator extends Combinator[Int](0):
  def combine(a1: Int, a2: Int): Int = a1 + a2
// trait IntCombinator may not call constructor of trait Combinator
```

## Контекстные параметры

А ещё параметры конструктора можно задавать и неявно - вот тогда их можно и не указывать при расширении,
если, конечно, этот неявный параметр доступен в области видимости.

[Например](https://scastie.scala-lang.org/nlKQs01KT6yWb0z85gOykA):

```scala
trait Combinator[A](using empty: A):
  def combine(a1: A, a2: A): A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

given Int = 0

object IntCombinator extends Combinator[Int]:
  def combine(a1: Int, a2: Int): Int = a1 + a2

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

## А в чем разница?

Параметры конструктора инициализируются до инициализации параметров в теле,
что позволяет избегать некоторых ошибок.

[Рассмотрим пример](https://scastie.scala-lang.org/zmV0nmMXRoWbBjOhKrDJlQ):

```scala
trait Combinator[A](empty: A):
  println(s"Пустое значение - $empty")

object StringCombinator extends Combinator[String]("пусто")

StringCombinator
// Пустое значение - пусто
```

[Аналогичный пример](https://scastie.scala-lang.org/SqdW2r1eTwqIuoEHzFVOIA)
с использованием параметра в теле `trait`-а приводит к некорректной инициализации.

```scala
trait Combinator[A]:
  val empty: A
  println(s"Пустое значение - $empty")

object StringCombinator extends Combinator[String]:
  val empty: String = ""

StringCombinator
// Пустое значение - null
```

Объяснение почему так происходит можно найти
[здесь](https://scalapuzzlers.com/index.html#pzzlr-004)
и [здесь](https://scalapuzzlers.com/index.html#pzzlr-005).

> Обновление от 21-04-2023: как правильно подсказали [в комментариях к митапу][Meetup] 
> флаг компилятора `-Ysafe-init` предупреждает о попытке обращения к неинициализированной переменной.
> 
> [Тот же самый пример с предупреждением компилятора](https://scastie.scala-lang.org/u6YgYFgKRXeonRl4RjzdyA)

## Расширение нескольких trait-ов

А ещё можно наследоваться от нескольких `trait`-ов, что делает их более мощным средством декомпозиции.

Расширение нескольких `trait`-ов называется "смешанной композицией" (_mixin composition_).

[Пример](https://scastie.scala-lang.org/us6LAIfaQayWMKRvMsKyuQ):

```scala
trait Empty[A](val empty: A)

trait Combinator[A](combine: (A, A) => A):
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int](_ + _), Empty(0)

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Если же оба `trait`-а реализуют метод с идентичной сигнатурой,
[то в смешанной композиции его нужно будет переопределить](https://scastie.scala-lang.org/gZs4vOfKQZmjHQGay7WuPQ):

```scala
trait EmptyInt1:
  val empty: Int = -1

trait EmptyInt2:
  val empty: Int = 1

trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int], EmptyInt1, EmptyInt2:
  override val empty: Int = 0
  def combine(a1: Int, a2: Int): Int = a1 + a2

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

> При использовании `super.empty` будет браться элемент из последнего `trait`-а в списке наследования.
> В данном случае - `EmptyInt2`.

## Различие trait и abstract class

- Можно расширить несколько trait-ов
- Можно расширить не более 1 абстрактного класса
- Абстрактные классы совместимы с Java

[Рекомендации по выбору](https://www.artima.com/pins1ed/traits.html#12.7):

- Для повторного использования в нескольких несвязанных классах - `trait`
- Если поведение не будет использоваться повторно - `class`
- Если необходимо наследовать в коде Java - `abstract class`

Что ещё, кроме основной функциональности могут предложить `trait`-ы в Scala 3?

## Прозрачные trait-ы

`Trait`-ы используются в двух случаях:

- как примеси для других классов и `trait`-ов
- как типы констант, определений или параметров

Некоторые `trait`-ы используются преимущественно в первой роли, и обычно их нежелательно видеть в выводимых типах.
Примером может служить [`trait Product`](https://scala-lang.org/api/3.x/scala/Product.html),
который компилятор добавляет в качестве примеси к каждому `case class`-у или `case object`-у.

[Или, например, кастомные реализации](https://scastie.scala-lang.org/Js2raBLxTtqSynlLpUHyhw):

```scala
trait IntCombine:
  def combine(a1: Int, a2: Int): Int = a1 + a2

trait EmptyInt1:
  val empty: Int = -1

trait EmptyInt2:
  val empty: Int = 1

trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator1 extends Combinator[Int], EmptyInt1, IntCombine
object IntCombinator2 extends Combinator[Int], EmptyInt2, IntCombine

val condition: Boolean = true
val x = Set(if condition then IntCombinator1 else IntCombinator2)
// Тип х - Set[Combinator[Int] & IntCombine]
```

Здесь предполагаемый тип `x` равен `Set[Combinator[Int] & IntCombine]`,
тогда как не желательно видеть детали реализации в типе, например, такие как `IntCombine`.

Безусловно тип можно не выводить, а указывать конкретный,
но есть и другой вариант:
Scala 3 позволяет помечать `trait` как _прозрачный_, что означает, что он может быть подавлен при выводе типа.

[Например](https://scastie.scala-lang.org/kKNsIEoqRJGAjfGBI9zWpg):

```scala
transparent trait IntCombine:
  def combine(a1: Int, a2: Int): Int = a1 + a2

transparent trait EmptyInt1:
  val empty: Int = -1

transparent trait EmptyInt2:
  val empty: Int = 1

trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator1 extends Combinator[Int], EmptyInt1, IntCombine
object IntCombinator2 extends Combinator[Int], EmptyInt2, IntCombine

val condition: Boolean = true
val x = Set(if condition then IntCombinator1 else IntCombinator2)
// Тип х - Set[Combinator[Int]]
```

Теперь `x` имеет тип `Set[Combinator[Int]]`. Общий `transparent trait IntCombine` не появляется в выводимом типе.

`Trait`-ы [scala.Product][Product],
[java.io.Serializable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Serializable.html)
и [java.lang.Comparable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Comparable.html)
автоматически считаются `transparent`.
Для остальных необходимо указывать этот модификатор.

Как правило, `transparent trait` — это `trait`-ы, влияющие на реализацию наследуемых классов,
и `trait`-ы, которые сами по себе обычно не используются как типы.
Два примера из стандартной библиотеки коллекций:

- [IterableOps](https://scala-lang.org/api/3.x/scala/collection/IterableOps.html),
  который предоставляет реализации методов
  для [Iterable](https://scala-lang.org/api/3.x/scala/collection/Iterable.html).
- [StrictOptimizedSeqOps](https://scala-lang.org/api/3.x/scala/collection/StrictOptimizedSeqOps.html),
  который оптимизирует некоторые из этих реализаций для последовательностей с эффективной индексацией.

## Открытые классы

Поскольку `trait`-ы разработаны как основное средство декомпозиции,
класс, определенный в одном файле, не может быть расширен в другом файле.
Чтобы разрешить это, базовый класс должен быть помечен как открытый.
Маркировка классов с помощью `open` - это новая функция Scala 3.
Необходимость явно помечать классы как открытые позволяет избежать многих распространенных ошибок.
В частности, это требует, чтобы разработчики библиотек явно планировали расширение
и, например, документировали классы, помеченные как открытые.

[Пример](https://scastie.scala-lang.org/OkrUeaF1ROKdiKEO7MPv2A):

```scala
// File Combinator.scala
open class Combinator[A](empty: A, combine: (A, A) => A):
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

// File IntCombinator.scala
object IntCombinator extends Combinator[Int](0, _ + _)

// Где-то ещё
IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Открытый класс обычно поставляется с некоторой документацией,
описывающей внутренние шаблоны вызовов между методами класса, а также хуки, которые можно переопределить.
Это называется контрактом расширения класса (_extension contract_).
Он отличается от внешнего контракта (_external contract_) между классом и его пользователями.

[Подробности об open классах](https://docs.scala-lang.org/scala3/reference/other-new-features/open-classes.html).

## Экспортирование элементов

Ок, с наследованием разобрались!
Но иногда возникают ситуации, когда предоставлять доступ ко всем публичным элементам
родительских `trait`-ов нежелательно, и поэтому хочется избежать наследования.
Что можно сделать?

В этом случае можно:

- либо выделить из родительских структур только нужные элементы в новый `trait`
- либо, если первое невозможно, собрать из родительских структур модуль
  и уже через него предоставить доступ только к "разрешенным" элементам.

Во втором случае особенно полезно предложение `export`,
которое определяет псевдонимы для выбранных членов объекта.

[Например](https://scastie.scala-lang.org/JobduOVQSieuJzt0CeM3Ng):

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator:
  private val combinator: Combinator[Int] = new:
    val empty: Int                     = 0
    def combine(a1: Int, a2: Int): Int = a1 + a2

  export combinator.combineAll

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Предложения `export` особенно полезны при сборе модуля из элементов, изменить которые возможности нет.
Например, потому что они публичные и определены во внешней библиотеке.

[Подробнее об export](https://scalabook.gitflic.space/docs/scala/details/export-causes)

## Type class

В Scala самым мощным средством декомпозиции являются **type class**-ы.
**Type class** — это абстрактный параметризованный тип,
который позволяет добавлять новое поведение к любому закрытому типу данных без использования подтипов.

В статье ["Type Classes as Objects and Implicits"][TypeClasses] (2010 г.)
обсуждаются основные идеи, лежащие в основе **type class**-ов в Scala.

Этот стиль программирования полезен во многих случаях, например:

- выражение того, как тип, которым вы не владеете, например, из стандартной или сторонней библиотеки,
  соответствует заданному поведению
- добавление поведения к нескольким типам без введения отношений подтипов между этими типами
  (например, когда один расширяет другой)

В Scala 3 **type class**-ы — это просто `trait`-ы с одним или несколькими параметрами типа.

[Например](https://scastie.scala-lang.org/nU8Rd8z3Roq1n6e2xJrVCg):

```scala
trait Combinator[A]:
  val empty: A
  def combine(a1: A, a2: A): A
```

И использование:

```scala
given Combinator[Int] with
  val empty: Int = 0
  def combine(a1: Int, a2: Int): Int = a1 + a2

def combineAll[A](list: List[A])(using c: Combinator[A]): A =
  list match
    case Nil    => c.empty
    case h :: t => c.combine(h, combineAll(t))

combineAll(List.empty)    // 0
combineAll(List(1))       // 1
combineAll(List(1, 2, 3)) // 6
```

Основное различие между полиморфизмом подтипа и специальным полиморфизмом с **type class**-ами заключается в том,
как реализуется определение **type class**-а по отношению к типу, на который он действует.
В случае **type class**-а реализация для конкретного типа выражается через определение экземпляра `given`,
предоставляемого как неявный аргумент вместе со значением, на которое он действует.
При полиморфизме подтипов реализация смешивается с родительскими элементами класса,
и для выполнения полиморфной операции требуется только один терм.
Решение **type class**-ов требует больше усилий для настройки, но более расширяемо:
добавление нового интерфейса в класс требует изменения исходного кода этого класса.
Напротив, экземпляры **type class**-ов могут быть определены где угодно.

**type class** - это не отдельное ключевое слово, это способ.
Способ, получивший насколько широкое распространение в Scala,
что порой библиотеки поставляются большей частью с **type class**-ами,
описывающими необходимые операции над данными.
А реализацию этого поведения под данные предлагается сделать самим пользователям библиотек.
**type class**-ы позволяют рассматривать поведение независимо от данных,
исследовать законы, которым удовлетворяет заданное поведение,
и на основе них усложнять поведение.

Например, библиотеки [Cats][Cats] и [ScalaZ](https://scalaz.github.io/7/typeclass/).
Пользователь в первую очередь изучает поведение, реализованное в библиотеке.
А не данные, к которым это поведение можно применить.

#### Для чего нужны type class-ы?

- Отделение поведения от данных
- Добавление поведения к "недоступным" данным
- Добавление поведения без изменения исходного класса
- Определение поведения без данных
- Добавление более 1 поведения к данным.
  Например, мы можем сворачивать
  коллекцию по-другому: [перемножив элементы](https://scastie.scala-lang.org/hwbBr4xpT1OkHrNu2CvLHQ):
  ```scala
  given Combinator[Int] with
      val empty: Int = 1
      def combine(a1: Int, a2: Int): Int = a1 * a2

  combineAll(List(1, 2, 3, 4, 5)) // 120
  ```

Подробнее о **type-class**-ах:

- [The Typeclass Pattern - An Alternative to Inheritance](https://www.youtube.com/watch?v=yYo0gANYViE)
- [Tutorial: Typeclasses in Scala with Dan Rosen](https://www.youtube.com/watch?v=sVMES4RZF-8)
- [Why are Type Classes useful in Scala? - Rock the JVM](https://www.youtube.com/watch?v=bupBZKJT0EA)
- [Typeclasses in Scala 3 - Colombia Scala meetup/Scala at the Sea](https://www.youtube.com/watch?app=desktop&v=co4LSvVoX-s)

## Ошибки при декомпозиции

А что на счет коллекций?

```scala
combineAll(List(List()))             
combineAll(List(List(1), List(1, 2, 3, 4, 5)))
```

[Можно и для коллекции определить "комбинатор"](https://scastie.scala-lang.org/QfiiXNTNT5GVUPeRQUoXKg):

```scala
given list[A]: Combinator[List[A]] with
  val empty: List[A]                             = List.empty[A]
  def combine(a1: List[A], a2: List[A]): List[A] = a1 ++ a2

combineAll(List(List.empty[Int]))              // List()
combineAll(List(List(1), List(1, 2, 3, 4, 5))) // List(1, 1, 2, 3, 4, 5)
```

Допустим!

Но рано или поздно, когда мы привыкаем к какой-нибудь конструкции:

```scala
trait Combinator[A]:
  val empty: A
  def combine(a1: A, a2: A): A
```

появляется структура, обладающая **почти** заданным поведением:

```scala
case class NonEmptyList[A](head: A, tail: List[A])
```

Есть несколько распространенных ошибок, совершаемых разработчиком, когда
необходимо реализовать поведение для структуры, которая этим поведением не обладает.

```scala
given Combinator[NonEmptyList[Int]] with
  val empty: NonEmptyList[Int] = ...
  def combine(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))
```

1) Выдача исключения:

Одной из самых распространенных ошибок в этом случае является использование исключения или
отсутствие реализации:

```scala
given Combinator[NonEmptyList[Int]] with
  val empty: NonEmptyList[Int] = null
  val empty1: NonEmptyList[Int] = throw new Exception("")
  var empty2: NonEmptyList[Int] = _
  val empty3: NonEmptyList[Int] = ???
  def combine ...
```

В этом случае происходит как бы перекладывание ответственности за использование такого "комбинатора"
на разработчика-клиента.
Здесь происходит попытка "сделать вид", что `NonEmptyList` обладает заданным поведением, хотя это не так.
Клиентский код сильно разрастается, потому что пользователи библиотеки вынуждены будут как-то обрабатывать возможные исключения.

Почему не стоит использовать в коде исключения, `null` или изменяемые переменные описано в
книге ["Functional Programming in Scala"][Red book]
и в других книгах, статьях, докладах о функциональном программировании.

2) Ещё одним способом "взломать систему" является использование `Option`,
   что, вроде как, является "чисто функциональным" и даже выглядит как-то "по-Scala":

```scala
trait Combinator[A]:
  val empty: Option[A] = None
  def combine(a1: A, a2: A): A

case class NonEmptyList[A](head: A, tail: List[A])

given Combinator[NonEmptyList[Int]] with
  def combine ...
```

Но в этом случае получается, что дочерняя структура пытается "заглушить" часть функциональности,
которой обладает "родитель".

Одним из самых мощных средств декомпозиции является выделение более абстрактных структур из уже имеющихся.
Ключевое слово _extends_ означает "расширение", когда дочерняя структура расширяет родительскую:
т.е. обладает в точности тем же поведением, что и родитель, плюс добавляет ещё что-то (_расширяет_).

Развитие ситуации, когда разработчик пытается "заглушить" расширение,
со временем может привести к анти-паттерну ["Божественный объект"](https://ru.wikipedia.org/wiki/%D0%91%D0%BE%D0%B6%D0%B5%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D1%8B%D0%B9_%D0%BE%D0%B1%D1%8A%D0%B5%D0%BA%D1%82),
когда во главе иерархии стоит элемент, "способный делать ВСЕ", 
а дочерние элементы лишь "забирают" себе необходимое.
Некоторые дочерние элементы могут даже не иметь между собой общих методов.

В данном случае
рекомендуется [определить более общую структуру](https://scastie.scala-lang.org/TDgsqmaqQraKaLGN8deJjw):

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A

trait FullCombinator[A] extends Combinator[A]:
  val empty: A
```

В этом случае можно "запросить" недостающую информацию у пользователя:

```scala
def combineAll[A](list: List[A], empty: A)(using c: Combinator[A]): A =
  list match
    case Nil    => empty
    case h :: t => c.combine(h, combineAll(t, empty))

def combineAll[A](list: List[A])(using c: FullCombinator[A]): A =
  combineAll(list, c.empty)
```

Пользователь явно видит, какую информацию ему необходимо предоставить и не получает "сюрприза" в виде exception.

## Заключение

- `Trait`-ы отлично подходят для модуляции компонентов и описания интерфейсов
  (с обязательными (абстрактными) и предоставляемыми (определенными) службами)
- отделение данных от операций над ними (по сути выделение поведения в отдельные структуры) позволяет
  расширять и поддерживать их (почти) независимо друг от друга
- возможность "отсечь лишнее" (`export`, `transparent`) помогает
  предоставлять разработчикам-клиентам только необходимую функциональность и избегать ошибок
- при построении архитектуры желательно избегать "ложного наследования": попытки отсечь часть родительской
  функциональности

## Ответы на вопросы

Хотелось бы более детально ответить на вопросы, прозвучавшие на [Meetup-е][Meetup].

#### Вопрос 1: type class instances?



#### Вопрос 2: Можно ли export использовать в паттерне делегирование?

Да, экспортирование можно использовать [в паттерне делегирование](https://ru.wikipedia.org/wiki/%D0%A8%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%B4%D0%B5%D0%BB%D0%B5%D0%B3%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F), 
но только в части делегирования методов, потому что все элементы при экспортировании технически становятся методами,
включая константы.

Рассмотрим пример:

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  def empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int]:
  private val combinator: Combinator[Int] = new:
    val empty: Int = 0
    def combine(a1: Int, a2: Int): Int = a1 + a2

  export combinator.*

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Здесь объект `IntCombinator` наследуется от `Combinator[Int]` и должен реализовать два абстрактных метода.
Эти методы реализуются путем экспортирования методов с той же самой сигнатурой из приватного элемента `combinator`.

[Рассмотренный пример в Scastie](https://scastie.scala-lang.org/RAAnbSkpR0msPI6iOGzeAA)

Но с абстрактными параметрами делегирование не пройдет, потому что, [согласно документации](https://docs.scala-lang.org/scala3/reference/other-new-features/export.html), экспортируемые элементы чисто технически - это методы.
Поэтому компилятор выдаст ошибку конфликта имен если необходимо реализовать абстрактную константу, 
даже если экспортируется реализация константы с той же сигнатурой, потому что она экспортируется как метод.

Пример:

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int]:
  private val combinator: Combinator[Int] = new:
    val empty: Int = 0
    def combine(a1: Int, a2: Int): Int = a1 + a2

  export combinator.*
// Compile error:
// error overriding value empty in trait Combinator of type Int; 
// method empty of type => Int needs to be a stable, immutable value
```

Константа `empty` при экспорте становится методом `empty`.

[Рассмотренный пример в Scastie](https://scastie.scala-lang.org/cIppAzZvSN264IXAPXAYLQ)

Также можно использовать переименование экспортируемых элементов для того, чтобы 
"дополнить" необходимую реализацию. Рассмотрим следующий пример:

```scala
trait Monoid[A]:
  def compose(a1: A, a2: A): A
  val empty: A

trait Combinator[A]:
  def combine(a1: A, a2: A): A
  def empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Combinator[Int]:
  private val monoid: Monoid[Int] = new:
    val empty: Int = 0
    def compose(a1: Int, a2: Int): Int = a1 + a2

  export monoid.{empty, compose as combine}

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Здесь `IntCombinator`-у нужно реализовать методы `def empty: Int` и `def combine(a1: Int, a2: Int): Int`, 
но у него есть только приватный экземпляр `monoid`.
При экспорте элементов приватной переменной, переименовывается метод `compose` для того, 
чтобы добиться нужной для `Combinator` сигнатуры методов.
В результате, `IntCombinator` получает реализацию всех абстрактных членов 
и ему нет необходимости что-то "доопределять".

[Рассмотренный пример в Scastie](https://scastie.scala-lang.org/jHBtvQJQSeO5NuBbwxN1Og)

#### Вопрос 3: Можно ли пользоваться методами прозрачных trait-ов?

Да, несмотря на то, что прозрачные trait-ы не выводятся в типе, все их элементы доступны для использования.

Рассмотрим пример:

```scala
transparent trait IntCombine:
  def combine(a1: Int, a2: Int): Int = a1 + a2

case class Empty(empty: Int)

trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))

object IntCombinator extends Empty(0), Combinator[Int], IntCombine

val x = IntCombinator
x.productArity // 1
```

`IntCombinator` наследуется от `case class Empty`, который в свою очередь наследуется от [scala.Product][Product].
Несмотря на то, что `Product` прозрачный, все его методы доступны. Например, как `productArity` в коде выше.

[Рассмотренный пример в Scastie](https://scastie.scala-lang.org/MWVJoJb6QMGjJ6a6NuyYQg)

#### Вопрос 4: Какое практическое применение прозрачных trait-ов?




## Список литературы

- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/index.html)
- ["Functional Programming in Scala"][Red book]
- ["Type Classes as Objects and Implicits"][TypeClasses]

## Meetup

[Запись meetup на Youtube][Meetup]

[Слайды с meetup-а](https://slides.com/artemkorsakov/decomposition/fullscreen)


[TypeClasses]: https://infoscience.epfl.ch/record/150280/files/TypeClasses.pdf

[Red book]: https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition

[Cats]: https://typelevel.org/cats/typeclasses.html

[Cats effect]: https://typelevel.org/cats-effect/docs/typeclasses

[Meetup]: https://www.youtube.com/watch?v=1W3BHh4vQqs

[Product]: https://scala-lang.org/api/3.x/scala/Product.html