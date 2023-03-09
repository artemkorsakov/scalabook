# Декомпозиция в Scala 3

Несмотря на то, что изначально Scala позиционировался, как язык, 
который объединяет объектно-ориентированное программирование (ООП)
и функциональное программирование (ФП), по большому счету Scala всегда был 
языком ФП, потому что данные и операции над этими данными разрабатываются отдельно.
С учетом изменений произошедших в Scala 3 `trait`-ы становятся по сути
чуть ли не единственным средством декомпозиции, что не уберегает, к сожалению, от ошибок.

В ФП данные и операции над этими данными — это две разные вещи; 
их необязательно инкапсулировать вместе, как в ООП.
Концепция аналогична числовой алгебре.
Представим целые неотрицательные числа `0, 1, 2, ...`.
Игнорируя деление, возможные операции над этими значениями такие: `+, -, *`.

Схема ФП реализуется аналогично:

- описывается набор значений (данные)
- описываются операции, которые работают с этими значениями (функции)

Рассмотрим, что могут предложить `trait`-ы в Scala 3.

## Параметры конструктора

`trait`-ы могут содержать:

- абстрактные методы, поля и типы
- конкретные методы, поля и типы
- могут иметь параметры конструктора, как и классы

Например:

```scala
trait Greeting(val name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

object EnglishGreeting extends Greeting("Bob"), Hello  // можно указать в любом порядке

EnglishGreeting.msg  // Hello Bob
```

Объект `EnglishGreeting` расширяет параметризованный `trait Greeting` и "подмешивает" `trait Hello`, 
реализующий абстрактный (для `Greeting`) параметр.

При расширении параметризованного `trait`-а действуют следующие правила:

- Если класс `C` расширяет параметризованный `trait` `T`, 
  а его суперкласс — нет, то `C` должен передать аргументы в `T`.
- Если класс `C` расширяет параметризованный `trait` `T` 
  и его суперкласс тоже, то `C` не должен передавать аргументы в `T`.
- `trait`-ы никогда не должны передавать аргументы родительским `trait`-ам.

Т.е. следующие примеры вызовут ошибку компилятора:

```scala
trait Greeting(val name: String):
  lazy val msg: String = s"Hello $name"

class EnglishGreeting extends Greeting
// missing argument for parameter name of constructor Greeting in trait Greeting: (name: String)
```

```scala
trait Greeting(val name: String):
  lazy val msg: String = s"Hello $name"

class EnglishGreeting extends Greeting("Bob")

class FormalEnglishGreeting extends EnglishGreeting, Greeting("Robert")
// trait Greeting is already implemented by superclass EnglishGreeting,
// its constructor cannot be called again
```

```scala
trait Greeting(val name: String):
  lazy val msg: String = s"Hello $name"

trait EnglishGreeting extends Greeting("Bob")
// trait EnglishGreeting may not call constructor of trait Greeting
```

Возможность передавать параметры в `trait` продолжают тенденцию отхода от формирования структур (`abstract class`)
к описанию поведений (`trait`-ы).

## Контекстные параметры

`Trait`-ы могут принимать и контекстные параметры.
В этом случае классам, расширяющим `trait`, необязательно явно передавать параметры конструктора -
это можно сделать неявно.

Например:

```scala
trait Greeting(using val name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

given String = "Bob"

object EnglishGreeting extends Greeting, Hello

EnglishGreeting.msg
// Hello Bob
```


## Расширение нескольких trait-ов

Классы, объекты и `trait`-ы могут расширять несколько `trait`-ов, в отличие от расширения нескольких классов,
что делает `trait`-ы более мощным средством декомпозиции.

Расширение нескольких `trait`-ов называется "смешанной композицией" (_mixin composition_).

При этом должна соблюдаться сигнатура методов:

```scala
trait Greeting(using val name: String):
  val firstPart: Long                    // Заменим на Long
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

given String = "Bob"

object EnglishGreeting extends Greeting, Hello

EnglishGreeting.msg
// error overriding value firstPart in trait Greeting of type Long;
// value firstPart in trait Hello of type String has incompatible type
```

Если же оба `trait`-а реализуют метод с идентичной сигнатурой, то в смешанной композиции его нужно будет переопределить:

```scala
trait Greeting(using val name: String):
  val firstPart: String = "Hi"
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

given String = "Bob"

object EnglishGreeting extends Greeting, Hello:
  override val firstPart: String = "Good day"

EnglishGreeting.msg
// Good day Bob
```

`trait`-ы более гибки в составлении, потому что можно смешивать (наследовать) несколько `trait`-ов. 
В большинстве случаев их следует предпочитать классам и абстрактным классам.
Правило выбора состоит в том, чтобы использовать классы всякий раз, 
когда необходимо создавать экземпляры определенного типа, 
и `trait`-ы, когда желательно декомпозировать/разложить и повторно использовать поведение.

Что ещё, кроме основной функциональности могут предложить trait-ы в Scala 3?

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
  который предоставляет реализации методов для [Iterable](https://scala-lang.org/api/3.x/scala/collection/Iterable.html).
- [StrictOptimizedSeqOps](https://scala-lang.org/api/3.x/scala/collection/StrictOptimizedSeqOps.html),
  который оптимизирует некоторые из этих реализаций для последовательностей с эффективной индексацией.

## Открытые классы

Поскольку `trait`-ы разработаны как основное средство декомпозиции, 
класс, определенный в одном файле, не может быть расширен в другом файле. 
Чтобы разрешить это, базовый класс должен быть помечен как открытый:

```
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

## Экспортирование элементов

Предложение `export` определяет псевдонимы для выбранных членов объекта.

Например:

```scala
class BitMap
class InkJet

class Printer:
  type PrinterType
  def print(bits: BitMap): Unit = println("Printer.print()")
  def status: List[String] = ???

class Scanner:
  def scan(): BitMap =
    println("Scanner.scan()")
    BitMap()
  def status: List[String] = ???

class Copier:
  private val printUnit = new Printer { type PrinterType = InkJet }
  private val scanUnit = new Scanner

  export scanUnit.scan
  export printUnit.{status as _, *}

  def status: List[String] = printUnit.status ++ scanUnit.status
```

Два пункта `export` определяют следующие псевдонимы экспорта в классе `Copier`:

```scala
final def scan(): BitMap            = scanUnit.scan()
final def print(bits: BitMap): Unit = printUnit.print(bits)
final type PrinterType              = printUnit.PrinterType
```

Доступ к ним возможен как изнутри `Copier`, так и снаружи:

```scala
val copier = new Copier
copier.print(copier.scan())
// Scanner.scan()
// Printer.print()
```

Предложения `export` особенно полезны при сборе модуля из элементов, изменить которые возможности нет.
Например, потому что они публичные и определены во внешней библиотеке.

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

Основное различие между полиморфизмом подтипа и специальным полиморфизмом с типовыми классами заключается в том, 
как реализуется определение типового класса по отношению к типу, на который он действует. 
В случае типового класса его реализация для конкретного типа выражается через определение экземпляра `given`, 
предоставляемый как неявный аргумент вместе со значением, на которое он действует. 
При полиморфизме подтипов реализация смешивается с родительскими элементами класса, 
и для выполнения полиморфной операции требуется только один терм. 
Решение типовых классов требует больше усилий для настройки, но оно более расширяемо: 
добавление нового интерфейса в класс требует изменения исходного кода этого класса. 
Напротив, экземпляры типовых классов могут быть определены где угодно.

## For comprehensions

Показательно, что одна из самых распространенных конструкций Scala - _for comprehension_ зависит от поведения,
а не от самого объекта.

Рассмотрим пример:

```scala
case class Container[A](value: A):
  def flatMap[B](f: A => Container[B]): Container[B] = f(value)
  def map[B](f: A => B): Container[B]                = Container(f(value))

val f: Int => Container[Boolean] = i => Container(i % 2 == 0)
val g: Boolean => String         = b => if b then "четное" else "нечетное"

for
  a <- Container(7)
  b <- f(a)
yield g(b)

// Эквивалентно: Container(7).flatMap(f).map(g)
```

Пример выше показывает, что любая структура, обладающая заданным поведением 
(реализация `flatMap`, `map` и, опционально, `withFilter` для `if`),
может использоваться в _for comprehension_.

Если конструкция заданным поведением не обладает (переименуем `flatMap` в `flatMap1`), 
то при попытке использовать _for comprehension_ будет выдана ошибка компиляции:

```text
2 |  a <- Container(7)
  |       ^^^^^^^^^^^^
  |value flatMap is not a member of Container[Int] - did you mean Container[Int].flatMap1?
1 error found
```

## Ковариантность и контравариантность

## Ошибки при декомпозиции

Отдельно хотелось бы остановиться на ошибках, которые возникают при декомпозиции.
Рассмотрим типовой класс `Monoid`:

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
  def compose(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))
```

В этом случае происходит как бы перекладывание ответственности за использование такого "моноида"
на разработчика-клиента.
Здесь происходит попытка "сделать вид", что `NonEmptyList` обладает заданным поведением, хотя это не так.

Почему не стоит использовать в коде исключения, null или изменяемые переменные очень подробно описано в 
замечательной книжке ["Functional Programming in Scala"][red book]
и в других книгах о функциональном программировании.

2) Ещё одним способом "взломать систему" является использование `Option`,
что, вроде как, является "чисто функциональным" и даже выглядит как-то "по-Scala":

```scala
trait Monoid[A]:
  def empty: Option[A]
  def compose(a1: A, a2: A): A

final case class NonEmptyList[A](head: A, tail: List[A])

object MonoidWithOption extends Monoid[NonEmptyList[Int]]:
  val empty: Option[NonEmptyList[Int]] = None
  def compose(l1: NonEmptyList[Int], l2: NonEmptyList[Int]): NonEmptyList[Int] =
    NonEmptyList(head = l1.head, tail = l1.tail ++ (l2.head :: l2.tail))
```

Но в этом случае получается, что дочерняя структура пытается "заглушить" часть функциональности,
которой обладает "родитель".

Одним из самых мощных средств декомпозиции является умение (и желание) разработчика выделять
более общие структуры из уже имеющихся.
Необходимо помнить, что ключевое слово _extends_ означает "расширение",
когда дочерняя структура расширяет родительскую: т.е. обладает в точности тем же поведением,
что и родитель, плюс добавляет ещё что-то.

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

## Заключение

- `Trait`-ы отлично подходят для модуляции компонентов и описания интерфейсов 
  (с обязательными (абстрактными) и предоставляемыми (определенными) службами)
- отделение данных от операций над ними (по сути выделение поведения в отдельные структуры) позволяет
  расширять и поддерживать их (почти) независимо друг от друга
- возможность "отсечь лишнее" (`export`, `transparent`) помогает 
  предоставлять разработчикам-клиентам только необходимую функциональность, избегать ошибок и соблюдать чистоту в коде
- взломать можно все что угодно, поэтому при построении архитектуры надо не забывать о том,
  что выделять общее из существующих элементов тоже важно


## Список литературы

- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/index.html)
- ["Functional Programming in Scala"][red book]
- ["Type Classes as Objects and Implicits"][TypeClasses]

[TypeClasses]: https://infoscience.epfl.ch/record/150280/files/TypeClasses.pdf
[red book]: https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition

