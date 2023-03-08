# Декомпозиция в Scala 3

Посмотрим, в чем главное отличие декомпозиции в Scala 3 по сравнению со Scala 2.

Основным инструментом декомпозиции в Scala 3 становятся `trait`-ы.
И для этого есть несколько причин.


### Теперь trait-ы могут иметь параметры конструктора, как и классы

Напомним, что `trait`-ы могут содержать:

- абстрактные методы, поля и типы
- конкретные методы, поля и типы
- могут иметь параметры конструктора, как и классы

Например:

```scala
trait Greeting(val name: String):
  protected val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

object EnglishGreeting extends Greeting("Bob"), Hello

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

Если в предыдущих версиях Scala часто позиционировалась как смесь ООП и ФП, то теперь 
Scala все больше становится языком функционального программирования. 


### Расширение нескольких trait-ов

Классы, объекты и `trait`-ы могут расширять несколько `trait`-ов, в отличие от расширения нескольких классов,
что делает `trait`-ы более мощным средством декомпозиции.

Расширение нескольких `trait`-ов называется "смешанной композицией" (_mixin composition_).

При этом должна соблюдаться сигнатура методов:

```scala
trait GreetingService:
  def translate(text: String): String
  def sayHello = translate("Hello")
  
trait TranslationService:
  def translate(text: String): Long = 123L

trait ComposedService extends GreetingService, TranslationService
// error overriding method translate in trait GreetingService of type (text: String): String;
// method translate in trait TranslationService of type (text: String): Long has incompatible type
```

Если же оба `trait`-а реализуют метод с идентичной сигнатурой, то в смешанной композиции его нужно будет переопределить:

```scala
trait First:
  def hello: String = "First"

trait Second:
  def hello: String = "Second"

trait Third extends First, Second:
  override def hello: String = "Third"

object Third extends Third

Third.hello
// Third
```


### Контекстные параметры

`Trait`-ы могут принимать параметры контекста. 
В этом случае классам, расширяющим `trait` необязательно явно передавать параметры конструктора.

Например:

```scala
case class ImpliedName(name: String):
  override def toString = name

trait ImpliedGreeting(using val iname: ImpliedName):
  def msg = s"How are you, $iname"

trait ImpliedFormalGreeting extends ImpliedGreeting:
  override def msg = s"How do you do, $iname"

class F(using iname: ImpliedName) extends ImpliedFormalGreeting

given ImpliedName = ImpliedName("Bob")
(new F).msg
// How do you do, Bob
```

### Прозрачные trait-ы

`Trait`-ы используются в двух случаях:

- как примеси для других классов и `trait`-ов
- как типы констант, определений или параметров

Некоторые `trait`-ы используются преимущественно в первой роли, и обычно их нежелательно видеть в выводимых типах.
Примером может служить [`trait Product`](https://scala-lang.org/api/3.x/scala/Product.html),
который компилятор добавляет в качестве примеси к каждому case class-у или case object-у.
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
Другие трейты превращаются в `transparent trait` с помощью модификатора `transparent`.

Как правило, `transparent trait` — это `trait`-ы, влияющие на реализацию наследуемых классов,
и `trait`-ы, которые сами по себе обычно не используются как типы.
Два примера из стандартной библиотеки коллекций:

- [IterableOps](https://scala-lang.org/api/3.x/scala/collection/IterableOps.html),
  который предоставляет реализации методов для [Iterable](https://scala-lang.org/api/3.x/scala/collection/Iterable.html).
- [StrictOptimizedSeqOps](https://scala-lang.org/api/3.x/scala/collection/StrictOptimizedSeqOps.html),
  который оптимизирует некоторые из этих реализаций для последовательностей с эффективной индексацией.

### Открытые классы

Поскольку `trait`-ы разработаны как основное средство декомпозиции, 
класс, определенный в одном файле, не может быть расширен в другом файле. 
Чтобы разрешить это, базовый класс должен быть помечен как открытый:

```
open class Person(name: String)
```

В этом случае получается, что в Scala 3 нет необходимости в специальном запрете на расширение классов 
(ключевое слово `sealed`) - классы и так нерасширяемые по умолчанию.

Маркировка классов с помощью `open` - это новая функция Scala 3.
Необходимость явно помечать классы как открытые позволяет избежать многих распространенных ошибок в ООП.
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

### export


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

Предложения `export` особенно полезны, когда мы собираем модуль из элементов, изменить которые мы не можем.
Например, потому что они публичные и определены во внешней библиотеке.


### Ковариантность и контравариантность


### Типовые классы


### For comprehensions

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

Пример выше показывает, что любая структура, обладающая заданным поведением,
может использоваться в _for comprehension_.

Если конструкция заданным поведением не обладает (переименуем `flatMap` в `flatMap1`), то будет выдана ошибка компиляции:

```text
2 |  a <- Container(7)
  |       ^^^^^^^^^^^^
  |value flatMap is not a member of Container[Int] - did you mean Container[Int].flatMap1?
1 error found
```


### Ошибки при декомпозиции

Отдельно хотелось бы остановиться на ошибках, которые возникают при декомпозиции.

```scala
null
Exception
_
???
None: Option[A]
```


## Заключение

`Trait`-ы отлично подходят для модуляции компонентов и описания интерфейсов (обязательных и предоставляемых). 

