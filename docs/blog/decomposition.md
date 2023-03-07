# Декомпозиция в Scala 3

Посмотрим, в чем главное отличие декомпозиции в Scala 3 по сравнению со Scala 2.

Основным инструментом декомпозиции в Scala 3 становятся `trait`-ы.
И для этого есть несколько причин.


### Теперь trait-ы могут иметь параметры конструктора, как и классы

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


### Расширение нескольких trait-ов

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
Set(if (condition) Val else Var)
// Type: Set[Product with King with Serializable]
```

Здесь предполагаемый тип `x` равен `Set[Kind & Product & Serializable]`,
тогда как можно было бы надеяться, что это будет `Set[Kind]`.

[Пример на Scastie](https://scastie.scala-lang.org/cZ3GfNz9T0O3c2Vp1nxOaw)

Основания для выделения именно этого типа следующие:

- тип условного оператора, приведенного выше, является [типом объединения](https://scalabook.gitflic.space/docs/scala/type-system/types-union) `Val | Var`.
- тип объединения расширяется в выводе типа до наименьшего супертипа, который не является типом объединения.

В примере - это тип `Kind & Product & Serializable`, так как все три `trait`-а являются `trait`-ами обоих `Val` и `Var`.
Таким образом, этот тип становится предполагаемым типом элемента набора.

Scala 3 позволяет помечать `trait` примеси как `transparent`, что означает, что он может быть подавлен при выводе типа.
Вот пример, который повторяет строки приведенного выше кода, но теперь с новым `transparent trait S` вместо `Product`:

```scala
transparent trait S
trait Kind
object Var extends Kind, S
object Val extends Kind, S
val x = Set(if condition then Val else Var)
```

Теперь `x` предположил тип `Set[Kind]`. Общий `transparent trait S` не появляется в выводимом типе.

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

Как правило, любой `trait`, расширяемый рекурсивно, является хорошим кандидатом на объявление `transparent`.

Правила вывода типов говорят, что `transparent trait` удаляются из пересечений, где это возможно.



