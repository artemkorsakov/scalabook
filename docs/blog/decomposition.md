# Декомпозиция в Scala 3

Я достаточно давно по IT-меркам работаю в компании "Криптонит" (с октября 2020)
и недавно мне вспомнился довольно забавный случай, произошедший со мной аккурат на следующий день
после прохождения мной испытательного срока в "Криптоните".

Так вот, мне написала незнакомый рекрутер и предложила работу в молодой и очень перспективной компании.
Она была достаточно настойчива и довольно долго описывала прелести работы в этой компании.
Знакомая для всех IT-шников ситуация.
Когда её запал иссяк она, наконец-то, сообщила название этой компании...
Я думаю, вы догадываетесь, работу в какой именно компании она мне предложила.

Когда я ответил ей, что был бы рад устроиться на работу в компанию "Криптонит",
но на этом пути есть одно непреодолимое препятствие -
я уже там работаю и только-только прошел испытательный срок...

Давайте представим, что я все же согласился на "двойной" заход в "Криптонит",
пришел на техническое собеседование и мне предложили сделать что-нибудь с таким `trait`-ом:

```scala
trait Combinator[A]:
  def combine(a1: A, a2: A): A
  val empty: A
  def combineAll(list: List[A]): A =
    list match
      case Nil    => empty
      case h :: t => combine(h, combineAll(t))
```

Что я могу с ним сделать? [Я могу его реализовать...](https://scastie.scala-lang.org/UdEjmfOzQcmeP16PGVWPOQ)

```scala
object IntCombinator extends Combinator[Int]:
  override val empty: Int                     = 0
  override def combine(a1: Int, a2: Int): Int = a1 + a2

IntCombinator.combineAll(List.empty)    // 0
IntCombinator.combineAll(List(1))       // 1
IntCombinator.combineAll(List(1, 2, 3)) // 6
```

Уже неплохо: теперь можно сворачивать числовые коллекции.

## Параметры конструктора

А ещё можно вынести переменную [в параметр конструктора](https://scastie.scala-lang.org/yT58r4nNTsqNvalB1TqFSQ):

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

Я останавливаюсь подробнее на этом - новая же тема в Scala 3 и добавляю...

Конечно, с параметрами конструктора не все так просто, нужно следовать правилам:

- `trait`-ы не должны передавать аргументы родительским `trait`-ам.
- первый и только первый дочерний класс `C` должен передать аргументы в `T`.

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
если этот неявный параметр доступен в области видимости.

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

## А в чем разница от параметров в теле?

Параметры конструктора инициализируются до инициализации параметров в теле.

[Рассмотрим пример](https://scastie.scala-lang.org/zmV0nmMXRoWbBjOhKrDJlQ):

```scala
trait Combinator[A](empty: A):
  println(s"Пустое значение - $empty")

object StringCombinator extends Combinator[String]("пусто")

StringCombinator
// Пустое значение - пусто
```

Аналогичный пример с использованием параметра в теле trait-а
[приводит к некорректной инициализации](https://scastie.scala-lang.org/SqdW2r1eTwqIuoEHzFVOIA).

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

Классы, объекты и `trait`-ы могут расширять несколько `trait`-ов, в отличие от расширения нескольких классов,
что делает `trait`-ы более мощным средством декомпозиции.

[Расширение нескольких `trait`-](https://scastie.scala-lang.org/us6LAIfaQayWMKRvMsKyuQ)
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

- Для повторного использования в нескольких несвязанных классах - trait
- Если поведение не будет использоваться повторно - class
- Если необходимо наследовать в коде Java - abstract class

Что ещё, кроме основной функциональности могут предложить `trait`-ы в Scala 3?

## Прозрачные trait-ы

`Trait`-ы используются в двух случаях:

- как примеси для других классов и `trait`-ов
- как типы констант, определений или параметров

Некоторые `trait`-ы используются преимущественно в первой роли, и обычно их нежелательно видеть в выводимых типах.
Примером может служить [`trait Product`](https://scala-lang.org/api/3.x/scala/Product.html),
который компилятор добавляет в качестве примеси к каждому `case class`-у или `case object`-у.
В Scala 2 этот родительский `trait` иногда делает выводимые типы более сложными, чем они должны быть.
Пример:

```scala
trait Kind
case object Var extends Kind
case object Val extends Kind
val condition: Boolean = true
val x = Set(if (condition) Val else Var)
// Type: Set[Product with King with Serializable]
```

Здесь предполагаемый тип `x` равен `Set[Kind & Product & Serializable]`,
тогда как можно было бы надеяться, что это будет `Set[Kind]`.

[Пример на Scastie](https://scastie.scala-lang.org/cZ3GfNz9T0O3c2Vp1nxOaw)

Безусловно тип можно не выводить, а указывать конкретный,
но есть и другой вариант:
Scala 3 позволяет помечать `trait` как _прозрачный_, что означает, что он может быть подавлен при выводе типа.
Например:

```scala
transparent trait S
trait Kind
object Var extends Kind, S
object Val extends Kind, S
val x = Set(if condition then Val else Var)
```

Теперь `x` имеет тип `Set[Kind]`. Общий `transparent trait S` не появляется в выводимом типе.

`Trait`-ы [scala.Product](https://scala-lang.org/api/3.x/scala/Product.html),
[java.io.Serializable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Serializable.html)
и [java.lang.Comparable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Comparable.html)
автоматически считаются `transparent`.
Для остальных необходимо указывать модификатор `transparent`.

Как правило, `transparent trait` — это `trait`-ы, влияющие на реализацию наследуемых классов,
и `trait`-ы, которые сами по себе обычно не используются как типы.
Два примера из стандартной библиотеки коллекций:

- [IterableOps](https://scala-lang.org/api/3.x/scala/collection/IterableOps.html),
  который предоставляет реализации методов
  для [Iterable](https://scala-lang.org/api/3.x/scala/collection/Iterable.html).
- [StrictOptimizedSeqOps](https://scala-lang.org/api/3.x/scala/collection/StrictOptimizedSeqOps.html),
  который оптимизирует некоторые из этих реализаций для последовательностей с эффективной индексацией.

## Экспортирование элементов

Иногда возникают ситуации, когда нежелательно предоставлять доступ ко всем публичным элементам
родительских `trait`-ов или классов, и поэтому хочется избежать наследования.

В этом случае можно:

- либо выделить из родительских структур только нужные элементы в новый `trait`
- либо, если первое невозможно, собрать из них модуль и через него предоставить доступ только к нужным элементам.

Во втором случае особенно полезно предложение `export`,
которое определяет псевдонимы для выбранных членов объекта.

Например:

```scala
trait Greeting(val name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

object EnglishGreetingModule:
  private val greeting = new Greeting("Bob"):
    override val firstPart: String = "Hi"

  export greeting.msg

EnglishGreetingModule.msg 
// Hi Bob
```

Предложения `export` особенно полезны при сборе модуля из элементов, изменить которые возможности нет.
Например, потому что они публичные и определены во внешней библиотеке.

## Открытые классы

Поскольку `trait`-ы разработаны как основное средство декомпозиции,
класс, определенный в одном файле, не может быть расширен в другом файле.
Чтобы разрешить это, базовый класс должен быть помечен как открытый:

```scala
open class Person(name: String)
```

Маркировка классов с помощью `open` - это новая функция Scala 3.
Необходимость явно помечать классы как открытые позволяет избежать многих распространенных ошибок.
В частности, это требует, чтобы разработчики библиотек явно планировали расширение
и, например, документировали классы, помеченные как открытые.

Пример:

```scala
// File Writer.scala
package p

open class Writer[T]:

  /** Sends to stdout, can be overridden */
  def send(x: T) = println(x)

  /** Sends all arguments using `send` */
  def sendAll(xs: T*) = xs.foreach(send)
end Writer

// File EncryptedWriter.scala
package p

class EncryptedWriter[T: Encryptable] extends Writer[T]:
  override def send(x: T) = super.send(encrypt(x))
```

Открытый класс обычно поставляется с некоторой документацией,
описывающей внутренние шаблоны вызовов между методами класса, а также хуки, которые можно переопределить.
Это называется контрактом расширения класса (_extension contract_).
Он отличается от внешнего контракта (_external contract_) между классом и его пользователями.

[Подробности об open классах](https://docs.scala-lang.org/scala3/reference/other-new-features/open-classes.html).

## Типовые классы

Ещё одним мощным средством декомпозиции являются _type classes_.
Type class — это абстрактный параметризованный тип,
который позволяет добавлять новое поведение к любому закрытому типу данных без использования подтипов.

В статье ["Type Classes as Objects and Implicits"][TypeClasses] (2010 г.)
обсуждаются основные идеи, лежащие в основе type class-ов в Scala.

Этот стиль программирования полезен во многих случаях, например:

- выражение того, как тип, которым вы не владеете, например, из стандартной или сторонней библиотеки,
  соответствует такому поведению
- добавление поведения к нескольким типам без введения отношений подтипов между этими типами
  (например, когда один расширяет другой)

В Scala 3 type class-ы — это просто `trait`-ы с одним или несколькими параметрами типа.

Например:

```scala
trait Show[A]:
  extension (a: A) def show: String
```

И использование:

```scala
case class Person(firstName: String, lastName: String)

given Show[Person] with
  extension (p: Person)
    def show: String =
      s"${p.firstName} ${p.lastName}"

def showAll[S: Show](xs: List[S]): Unit =
  xs.foreach(x => println(x.show))

showAll(List(Person("Jane", "Jackson"), Person("Mary", "Jameson")))
// Jane Jackson
// Mary Jameson
```

Основное различие между полиморфизмом подтипа и специальным полиморфизмом с type class-ами заключается в том,
как реализуется определение type class-а по отношению к типу, на который он действует.
В случае type class-а его реализация для конкретного типа выражается через определение экземпляра `given`,
предоставляемый как неявный аргумент вместе со значением, на которое он действует.
При полиморфизме подтипов реализация смешивается с родительскими элементами класса,
и для выполнения полиморфной операции требуется только один терм.
Решение type class-ов требует больше усилий для настройки, но оно более расширяемо:
добавление нового интерфейса в класс требует изменения исходного кода этого класса.
Напротив, экземпляры type class-ов могут быть определены где угодно.

type class - это не отдельное ключевое слово, это способ.
Способ, получивший насколько широкое распространение в Scala,
что порой библиотеки поставляются большей частью с type class-ами,
которые описывают необходимое поведение/операции над данными.
А реализацию этого поведения под пользовательские данные предлагается сделать самим пользователям библиотек.
type class-ы позволяют рассматривать поведение независимо от данных,
исследовать законы, которым удовлетворяет заданное поведение,
и на основе них усложнять поведение.

Например, библиотека [Cats effect][Cats effect].
Пользователь в первую очередь изучает поведение, реализованное в библиотеке.
А не данные, к которым это поведение можно применить.

## Ошибки при декомпозиции

Отдельно хотелось бы остановиться на ошибках, которые возникают при декомпозиции.
Рассмотрим type class `Monoid`:

```scala
trait Monoid[A]:
  def empty: A
  def compose(a1: A, a2: A): A
```

Что если в определенный момент возникла необходимость реализовать `Monoid` для `NonEmptyList`?

```scala
final case class NonEmptyList[A](head: A, tail: List[A])
```

Есть несколько распространенных ошибок, совершаемых разработчиком, когда
необходимо реализовать поведение для структуры, которая этим поведением не обладает.

```scala
object NonEmptyListMonoid extends Monoid[NonEmptyList[Int]]:
  val empty: NonEmptyList[Int] = ...
  def compose(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))
```

1) Выдача исключения:

Одной из самых распространенных ошибок в этом случае является использование исключения или
отсутствие реализации:

```scala
object MonoidWithException extends Monoid[NonEmptyList[Int]]:
  val empty: NonEmptyList[Int] = ???
  val empty1: NonEmptyList[Int] = throw new Exception("")
  var empty2: NonEmptyList[Int] = _
  val empty3: NonEmptyList[Int] = null
  def compose ...
```

В этом случае происходит как бы перекладывание ответственности за использование такого "моноида"
на разработчика-клиента.
Здесь происходит попытка "сделать вид", что `NonEmptyList` обладает заданным поведением, хотя это не так.

Почему не стоит использовать в коде исключения, `null` или изменяемые переменные очень подробно описано в
замечательной книге ["Functional Programming in Scala"][red book]
и в других книгах/статьях/докладах о функциональном программировании.

2) Ещё одним способом "взломать систему" является использование `Option`,
   что, вроде как, является "чисто функциональным" и даже выглядит как-то "по-Scala":

```scala
trait Monoid[A]:
  def empty: Option[A]
  def compose(a1: A, a2: A): A

final case class NonEmptyList[A](head: A, tail: List[A])

object MonoidWithOption extends Monoid[NonEmptyList[Int]]:
  val empty: Option[NonEmptyList[Int]] = None
  def compose ...
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

Но в данном случае следовало бы определить более общую структуру
(в теории категорий моноид без единичного элемента - это полугруппа) и использовать её:

```scala
trait Semigroup[A]:
  def compose(a1: A, a2: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

В этом случае можно "запросить" недостающую информацию у пользователя:

```scala
def foldSemigroup[A](list: List[A], empty: A)(using m: Semigroup[A]): A =
  list match
    case Nil      => empty
    case h :: Nil => h
    case h :: t   => m.compose(h, foldSemigroup(t, empty))

def foldMonoid[A](list: List[A])(using m: Monoid[A]): A =
  foldSemigroup(list, m.empty)
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
- ["Functional Programming in Scala"][red book]
- ["Type Classes as Objects and Implicits"][TypeClasses]

[TypeClasses]: https://infoscience.epfl.ch/record/150280/files/TypeClasses.pdf

[red book]: https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition

[Cats effect]: https://typelevel.org/cats-effect/docs/typeclasses
