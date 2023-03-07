# Декомпозиция в Scala 3

Посмотрим, в чем главное отличие декомпозиции в Scala 3 по сравнению со Scala 2.

Основным инструментом декомпозиции в Scala 3 становятся `trait`-ы.
И для этого есть несколько причин.


### 1. Теперь trait-ы могут иметь параметры конструктора, как и классы

Напомним, что `trait`-ы могут содержать:

- абстрактные методы и поля
- конкретные методы и поля
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

Объект `EnglishGreeting` расширяет параметризованный `trait` и "подмешивает" `trait Hello`, 
реализующий абстрактный элемент.

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


### 2. Расширение нескольких trait-ов

Классы, объекты и `trait`-ы могут расширять несколько `trait`-ов, в отличие от абстрактных классов,
что делает `trait`-ы более мощным средством декомпозиции.

Это называется "смешанной композицией" (_mixin composition_).
Пример был показан выше.

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

