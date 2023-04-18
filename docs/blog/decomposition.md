# Декомпозиция в Scala 3

По IT-меркам я давно работаю в компании “Криптонит” (с октября 2020) и недавно мне вспомнился забавный эпизод, 
произошедший аккурат на следующий день после прохождения испытательного срока. 
Так вот, мне написала незнакомый рекрутер и предложила работу в молодой и очень перспективной компании. 
Она была настойчива и довольно долго описывала прелести работы именно там. 
Когда её запал иссяк рекрутер, наконец-то, сообщила название этой компании… 
Думаю, вы уже догадались, работу в какой именно компании она мне предложила. 
И тогда я ответил ей, что был бы рад устроиться на работу в компанию “Криптонит”, 
но на этом пути есть одно непреодолимое препятствие - я уже там работаю…

Давайте представим, что я все же согласился на “двойной” заход в “Криптонит”, 
пришел на техническое собеседование и ревьюер предложил мне сделать что-нибудь с таким `trait`-ом:

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))
```

Что с ним можно сделать? [Его можно реализовать реализовать...](https://scastie.scala-lang.org/UdEjmfOzQcmeP16PGVWPOQ)

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

А ещё могу вынести [переменную empty в параметр конструктора](https://scastie.scala-lang.org/yT58r4nNTsqNvalB1TqFSQ):

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

На этом я останавливаюсь чуть подробнее - новая же функциональность в Scala 3 и добавлю...

Конечно, с параметрами конструктора нужно иметь в виду, что:

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

## Расширение нескольких trait-ов

А ещё можно наследоваться от нескольких `trait`-ов, что делает их более мощным средством декомпозиции.

[Расширение нескольких `trait`-ов](https://scastie.scala-lang.org/us6LAIfaQayWMKRvMsKyuQ)
называется "смешанной композицией" (_mixin composition_).

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

`Trait`-ы [scala.Product](https://scala-lang.org/api/3.x/scala/Product.html),
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
В случае **type class**-а его реализация для конкретного типа выражается через определение экземпляра `given`,
предоставляемый как неявный аргумент вместе со значением, на которое он действует.
При полиморфизме подтипов реализация смешивается с родительскими элементами класса,
и для выполнения полиморфной операции требуется только один терм.
Решение **type class**-ов требует больше усилий для настройки, но оно более расширяемо:
добавление нового интерфейса в класс требует изменения исходного кода этого класса.
Напротив, экземпляры **type class**-ов могут быть определены где угодно.

**type class** - это не отдельное ключевое слово, это способ.
Способ, получивший насколько широкое распространение в Scala,
что порой библиотеки поставляются большей частью с **type class**-ами,
которые описывают необходимое поведение/операции над данными.
А реализацию этого поведения под пользовательские данные предлагается сделать самим пользователям библиотек.
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

приходит Некто, вечно вставляющий палки в колеса, и говорит: "А реализуй-ка `Combinator` для `NonEmptyList`?"

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

Почему не стоит использовать в коде исключения, `null` или изменяемые переменные очень подробно описано в
замечательной книге ["Functional Programming in Scala"][Red book]
и в других книгах/статьях/докладах о функциональном программировании.

2) Ещё одним способом "взломать систему" является использование `Option`,
   что, вроде как, является "чисто функциональным" и даже выглядит как-то "по-Scala":

```scala
trait Combinator[A]:
  val empty: Option[A]
  def combine(a1: A, a2: A): A

case class NonEmptyList[A](head: A, tail: List[A])

given Combinator[NonEmptyList[Int]] with
  val empty: Option[NonEmptyList[Int]] = None
  def combine ...
```

Но в этом случае получается, что дочерняя структура пытается "заглушить" часть функциональности,
которой обладает "родитель".

Одним из самых мощных средств декомпозиции является умение (и желание) разработчика выделять
более общие структуры из уже имеющихся.
Необходимо помнить, что ключевое слово _extends_ означает "расширение",
когда дочерняя структура расширяет родительскую:
т.е. обладает в точности тем же поведением, что и родитель, плюс добавляет ещё что-то.
А не наоборот.

В коде довольно часто приходится видеть ситуации, описанные выше,
когда разработчик пытается "заглушить" расширение.
Со временем это может привести к анти-паттерну ["Божественный объект"](https://ru.wikipedia.org/wiki/%D0%91%D0%BE%D0%B6%D0%B5%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D1%8B%D0%B9_%D0%BE%D0%B1%D1%8A%D0%B5%D0%BA%D1%82),
когда во главе иерархии стоит элемент, "способный делать ВСЕ", 
а дочерние элементы лишь "забирают" себе необходимое и могут даже не иметь общих методов.

Но в данном случае
следовало [бы определить более общую структуру и использовать её](https://scastie.scala-lang.org/TDgsqmaqQraKaLGN8deJjw):

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

## Список литературы

- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/index.html)
- ["Functional Programming in Scala"][Red book]
- ["Type Classes as Objects and Implicits"][TypeClasses]

## Слайды с митапа

[Ссылка на митап](https://kryptonitecompany.timepad.ru/event/2383352/)

[Ссылка на слайды](https://slides.com/artemkorsakov/decomposition/fullscreen)


[TypeClasses]: https://infoscience.epfl.ch/record/150280/files/TypeClasses.pdf

[Red book]: https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition

[Cats]: https://typelevel.org/cats/typeclasses.html

[Cats effect]: https://typelevel.org/cats-effect/docs/typeclasses
