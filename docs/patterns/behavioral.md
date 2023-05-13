# Поведенческие шаблоны

[Поведенческие шаблоны][Wikipedia] (_behavioral patterns_) описывают взаимодействие объектов
и часто распределение ответственности между объектами.

## Интерпретатор

#### Назначение

Для заданного языка определить представление для его грамматики
вместе с интерпретатором, который использует представление для интерпретации предложений на языке.

#### Диаграмма

![Interpreter](https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Interpreter_UML_class_diagram.svg/804px-Interpreter_UML_class_diagram.svg.png)

#### Пример

```scala mdoc:silent
class Context:
  import scala.collection.mutable
  val result: mutable.Stack[String] = mutable.Stack.empty[String]

trait Expression:
  def interpret(context: Context): Unit

trait OperatorExpression extends Expression:
  val left: Expression
  val right: Expression

  def interpret(context: Context): Unit =
    left.interpret(context)
    val leftValue = context.result.pop()

    right.interpret(context)
    val rightValue = context.result.pop()

    doInterpret(context, leftValue, rightValue)

  protected def doInterpret(context: Context, leftValue: String, rightValue: String): Unit

end OperatorExpression
```

```scala mdoc:silent
trait EqualsExpression extends OperatorExpression:
  protected override def doInterpret(context: Context, leftValue: String, rightValue: String): Unit =
    context.result.push(if leftValue == rightValue then "true" else "false")

trait OrExpression extends OperatorExpression:
  protected override def doInterpret(context: Context, leftValue: String, rightValue: String): Unit =
    context.result.push(if leftValue == "true" || rightValue == "true" then "true" else "false")

trait MyExpression extends Expression:
  var value: String

  def interpret(context: Context): Unit =
    context.result.push(value)
```

```scala mdoc
val context = Context()
val input = new MyExpression() { var value = "" }

var expression = new OrExpression {
  val left: Expression = new EqualsExpression {
    val left = input
    val right = new MyExpression { var value = "4" }
  }
  val right: Expression = new EqualsExpression {
    val left = input
    val right = new MyExpression { var value = "четыре" }
  }
}

input.value = "четыре"
expression.interpret(context)
context.result.pop()

input.value = "44"
expression.interpret(context)
context.result.pop()
```

## Шаблонный метод

#### Назначение

Определить скелет алгоритма в операции, отложив некоторые шаги на подклассы.
Шаблонный метод позволяет подклассам переопределять определенные шаги алгоритма без изменения структуры алгоритма.

#### Диаграмма

![Template Method](https://upload.wikimedia.org/wikipedia/commons/5/52/Template_Method_UML.svg?uselang=ru)

#### Пример

```scala
trait Template extends (Unit => Int):
  def subStepA(): Unit
  def subStepB: Int
  def apply: Int =
    subStepA()
    subStepB
```

## Цепочка обязанностей

#### Назначение

Поведенческий шаблон проектирования, предназначенный для организации в системе уровней ответственности.
Избегайте связывания отправителя запроса с его получателем,
предоставляя более чем одному объекту возможность обработать запрос.
Цепляйте принимающие объекты и передайте запрос по цепочке, пока объект не обработает его.

Идея шаблона состоит в том, чтобы отделить отправителей и получателей сообщения.
Шаблон позволяет определить точного получателя сообщения во время выполнения.

#### Диаграмма

![Chain of Responsibility](https://upload.wikimedia.org/wikipedia/ru/a/ae/Chain.png)

#### Пример

```scala
trait Handler[T]:
  var successor: Option[Handler[T]] = None

  def handleRequest(r: T): Unit =
    if handlingCriteria(r) then doThis(r)
    else successor.foreach(_.handleRequest(r))

  def doThis(v: T): Unit = ()
  def handlingCriteria(request: T): Boolean = false

end Handler
```

```scala
class Sensor extends Handler[Int]:
  var value = 0
  def changeValue(v: Int): Unit =
    value = v
    handleRequest(value)

class Display1 extends Handler[Int]:
  def show(v: Int): Unit = println(s"Display1 prints $v")
  override def doThis(v: Int): Unit = show(v)
  override def handlingCriteria(v: Int): Boolean = v < 4
```

Другое решение, специфичный для шаблона код хранится в отдельном trait.

```scala
class Display2:
  def show(v: Int): Unit = println(s"Display2 prints $v")

trait Display2Handler extends Display2 with Handler[Int]:
  override def doThis(v: Int): Unit = show(v)
  override def handlingCriteria(v: Int): Boolean = v >= 4
```

```scala
val sensor = new Sensor
val display1 = new Display1
val display2 = new Display2 with Display2Handler
sensor.successor = Some(display1)
display1.successor = Some(display2)
sensor.changeValue(2)
// Display1 prints 2
sensor.changeValue(4)
// Display2 prints 4
```

## Команда

#### Назначение

Инкапсулировать запрос как объект, тем самым позволяя параметризовать клиентов с различными запросами,
ставить в очередь или регистрировать запросы и поддерживать операции, которые можно отменить.

#### Диаграмма

![Command](https://upload.wikimedia.org/wikipedia/ru/0/0c/Command.gif)

#### Пример

Класс `Button` ожидает функцию обратного вызова, которую он будет выполнять при вызове метода `click`.

```scala
class Button(val click: () => Unit)
val button = new Button(() => println("click!"))
button.click()
// click!
```

## Итератор

#### Назначение

Предоставление способа последовательного доступа к элементам агрегатного объекта
без раскрытия его базового представления.

#### Диаграмма

![Iterator](https://upload.wikimedia.org/wikipedia/commons/1/13/Iterator_UML_class_diagram.svg)

#### Пример

```scala
trait Iterator[A]:
  def first: A

  def next: A

  def isDone: Boolean

  def currentItem: A
```

## Посредник

#### Назначение

Определение объекта, который инкапсулирует способ взаимодействия набора объектов.
Медиатор способствует слабой связи, не позволяя объектам явно ссылаться друг на друга,
и позволяет независимо изменять их взаимодействие.

#### Диаграмма

![Mediator](https://upload.wikimedia.org/wikipedia/commons/e/e4/Mediator_design_pattern.png?uselang=ru)

#### Пример

Классы `ListBox` и `EntryField` — наши классы-коллеги, оба — `Widget`-ы.
`DialogDirector` содержит вложенный трейт `ListBoxDir`, который перехватывает каждый раз, когда щелкают наш список.
При нажатии на него вызывается `listBoxChanged`, что приводит к установке текста в нашем поле ввода с текущим выбором списка.
Это простой пример взаимодействия объектов.
Обратите внимание, что коллеги совершенно не знают о посреднике и, следовательно, о самой схеме.

```scala
// Widgets
class ListBox:
  def getSelection: String = "selected"
  def click(): Unit = ()

class EntryField:
  def setText(s: String): Unit = println(s)

class DialogDirector:
  protected trait ListBoxDir extends ListBox:
    abstract override def click(): Unit =
      super.click()
      listBoxChanged()

  // Colleagues
  val listBox: ListBox = new ListBox with ListBoxDir
  val entryField: EntryField = new EntryField

  // Directing methods
  def showDialog(): Unit = ()

  // called when listBox is clicked via advice
  def listBoxChanged(): Unit = entryField.setText(listBox.getSelection)

end DialogDirector
```

```scala
val dialog = new DialogDirector
val listBox = dialog.listBox
val entryField = dialog.entryField
listBox.click()
// selected
```

## Хранитель

#### Назначение

Не нарушая инкапсуляцию, захватить и вывести наружу внутреннее состояние объекта,
чтобы объект можно было позже восстановить в это состояние.

#### Диаграмма

![Memento](https://upload.wikimedia.org/wikipedia/commons/1/18/Memento_design_pattern.png?uselang=ru)

#### Пример

```scala
trait Originator:
def createMemento: Memento

def setMemento(m: Memento): Unit

trait Memento:
def getState: Originator
def setState(originator: Originator): Unit
```

## Наблюдатель

#### Назначение

Определение зависимости "один ко многим" между объектами,
чтобы при изменении состояния одного объекта все его иждивенцы уведомлялись и обновлялись автоматически.

Шаблон также известен как публикация/подписка, что указывает на структуру шаблона:
объект играет роль издателя, любое количество объектов может подписаться на издателя,
тем самым получая уведомление всякий раз, когда в издателе происходит определенное событие.
Обычно издатель передает свой экземпляр в уведомлении.
Это позволяет подписчику запрашивать у издателя любую соответствующую информацию,
например, чтобы иметь возможность синхронизировать состояние.

#### Диаграмма

![Observer](https://upload.wikimedia.org/wikipedia/commons/b/bd/Observer_UML_smal.png?uselang=ru)

#### Пример

```scala
trait Subject[T] { self: T =>
  import scala.collection.mutable
  private val observers: mutable.ListBuffer[T => Unit] =
    mutable.ListBuffer.empty[T => Unit]

  def subscribe(obs: T => Unit): Unit =
    observers.addOne(obs)

  def unsubscribe(obs: T => Unit): Unit =
    observers.subtractOne(obs)

  protected def publish(): Unit = observers.foreach(obs => obs(self))
}
```

```scala
trait Sensor(val label: String):
var value: Double = _
def changeValue(v: Double): Unit = value = v

// Pattern specific code
trait SensorSubject extends Sensor with Subject[Sensor]:
override def changeValue(v: Double): Unit =
  super.changeValue(v)
publish()

class Display(label: String):
def notify(s: Sensor): Unit =
  println(s"$label ${s.label} ${s.value}")
```

```scala
val s1: SensorSubject = new Sensor("s1") with SensorSubject
val d1: Display = new Display("d1")
val d2: Display = new Display("d2")
s1.subscribe(d1.notify)
s1.subscribe(d2.notify)
s1.changeValue(10)
// d1 s1 10.0
// d2 s1 10.0
```

## Состояние

#### Назначение

Позволить объекту изменить свое поведение при изменении его внутреннего состояния. Объект изменит свой класс.
Паттерн состояния актуален, когда поведение объекта зависит от его внутреннего состояния.

#### Диаграмма

![State](https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/State_Design_Pattern_UML_Class_Diagram.svg/475px-State_Design_Pattern_UML_Class_Diagram.svg.png)

#### Пример

```scala
class Context:
private var currentState: State = State1

def operation(): Unit = currentState.operation()

trait State:
def operation(): Unit

private object State1 extends State:
def operation(): Unit =
  println("State1")
currentState = State2

private object State2 extends State:
def operation(): Unit =
  println("State2")
currentState = State1

end Context
```

```scala
val c = new Context
c.operation()
// State1
c.operation()
// State2
c.operation()
// State1
```

## Стратегия

#### Назначение

Определение семейства алгоритмов, инкапсулирование каждого из них и создание их взаимозаменяемыми.
Стратегия позволяет алгоритму изменяться независимо от клиентов, которые его используют.

#### Диаграмма

![Strategy](https://upload.wikimedia.org/wikipedia/ru/4/4c/Strategy_pattern.PNG)

#### Пример

`Strategy` определяет интерфейс алгоритма.
`Context` поддерживает ссылку на текущий объект `Strategy` и перенаправляет запросы от клиентов конкретному алгоритму.

```scala
object FileMatcher:
private val filesHere: Seq[String] =
  Seq(
    "example.txt",
    "file.txt.png",
    "1txt"
  )

// Strategy selection
def filesContaining(query: String): Seq[String] =
  filesMatching(_.contains(query)) // inline strategy

def filesRegex(query: String): Seq[String] =
  filesMatching(matchRegex(query)) // using a method

def filesEnding(query: String): Seq[String] =
  filesMatching(new FilesEnding(query).matchEnding) // lifting a method

// matcher is a strategy
private def filesMatching(matcher: String => Boolean): Seq[String] =
  for
    file <- filesHere
if matcher(file)
yield file

// Strategies
private def matchRegex(query: String): String => Boolean =
  (s: String) => s.matches(query)

private class FilesEnding(query: String):
def matchEnding(s: String): Boolean = s.endsWith(query)
```

```scala
val query = ".txt"
FileMatcher.filesContaining(query)
// res0: Seq[String] = List("example.txt", "file.txt.png")
FileMatcher.filesRegex(query)
// res1: Seq[String] = List("1txt")
FileMatcher.filesEnding(query)
// res2: Seq[String] = List("example.txt")
```

## Посетитель

#### Назначение

Представление операции, которая должна быть выполнена над элементами структуры объекта.
Visitor позволяет определить новую операцию без изменения классов элементов, над которыми она работает.

#### Диаграмма

![Visitor](https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/VisitorDiagram.svg/515px-VisitorDiagram.svg.png?uselang=ru)

#### Пример

```scala
trait Expr
case class Num(n: Int) extends Expr
case class Sum(l: Expr, r: Expr) extends Expr

def prettyPrint(e: Expr): Unit =
  e match
case Num(n) => print(n)
case Sum(l, r) =>
prettyPrint(l)
print(" + ")
prettyPrint(r)

def eval(e: Expr): Int =
  e match
case Num(n)    => n
case Sum(l, r) => eval(l) + eval(r)
```

```scala
val e1 = Sum(Sum(Num(1), Num(2)), Num(3))
prettyPrint(e1)
// 1 + 2 + 3
print(eval(e1))
// 6
```

---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia, Behavioral pattern][Wikipedia]
- [Wikipedia, Interpreter](https://en.wikipedia.org/wiki/Interpreter_pattern)
- [Wikipedia, Template method](https://en.wikipedia.org/wiki/Template_method_pattern)
- [Wikipedia, Chain of responsibility](https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern)
- [Wikipedia, Command](https://en.wikipedia.org/wiki/Command_pattern)
- [Wikipedia, Iterator](https://en.wikipedia.org/wiki/Iterator_pattern)
- [Wikipedia, Mediator](https://en.wikipedia.org/wiki/Mediator_pattern)
- [Wikipedia, Memento](https://en.wikipedia.org/wiki/Memento_pattern)
- [Wikipedia, Observer](https://en.wikipedia.org/wiki/Observer_pattern)
- [Wikipedia, State](https://en.wikipedia.org/wiki/State_pattern)
- [Wikipedia, Strategy](https://en.wikipedia.org/wiki/Strategy_pattern)
- [Wikipedia, Visitor](https://en.wikipedia.org/wiki/Visitor_pattern)

[Wikipedia]: https://en.wikipedia.org/wiki/Behavioral_pattern
