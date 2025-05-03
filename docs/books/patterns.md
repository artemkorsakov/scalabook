{%
showPrev = true
prevPage = mil-dfp.html
%}

# Паттерны проектирования

Шаблон проектирования обычно рассматривается как многоразовое решение часто встречающейся 
проблемы проектирования в объектно-ориентированном программном обеспечении.

### Проектное пространство

Пространство шаблонов проектирования, представленное в таблице ниже, имеет два измерения: 
цель (_purpose_) и область применения (_scope_). 
Шаблоны целей могут быть классифицированы как _creational_, _structural_ или _behavioral_.
[Порождающие шаблоны][Wikipedia Creational] (_creational_) имеют дело с созданием объектов, 
в то время как [структурные шаблоны][Wikipedia Structural] (_structural_) имеют дело с составом классов или объектов. 
[Поведенческие шаблоны][Wikipedia Behavioral] (_behavioral_) описывают взаимодействие объектов 
и часто распределение ответственности между объектами. 

Измерение области видимости определяет, ["применяется ли шаблон в первую очередь к классам или к объектам"][Design Patterns]. 

Шаблоны классов имеют дело с отношениями между классами и их подклассами. 
Поскольку эти отношения определяются во время компиляции посредством наследования, 
они фиксированы и не могут изменяться во время выполнения. 
В шаблонах классов шаблон в целом содержится в одной иерархии классов.

Объектные шаблоны, с другой стороны, имеют дело с отношениями между объектами, такими как композиция и делегирование. 
Это означает, что связь может изменяться во время выполнения.
В шаблонах объектов комбинированное поведение шаблона распределяется между несколькими объектами во время выполнения.

| **Scope** \ **Purpose** | **Creational**                                  | **Structural**                                                          | **Behavioral**                                                                                    |
|-------------------------|-------------------------------------------------|-------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **Class**               | Factory Method                                  | Adapter(class)                                                          | Interpreter, Template Method                                                                      |
| **Object**              | Abstract Factory, Builder, Prototype, Singleton | Adapter(object), Bridge, Composite, Decorator, Facade, Flyweight, Proxy | Chain of Responsibility, Command, Iterator, Mediator, Memento, Observer, State, Strategy, Visitor |

## Порождающие шаблоны

[Порождающие шаблоны][Wikipedia Creational] (_creational patterns_) имеют дело с созданием объектов.

## Фабричный метод

#### Назначение

Определение интерфейса для создания объекта, в котором подклассы решают, какой класс создавать.
Фабричный метод позволяет классу отложить создание экземпляра для подклассов.
Фабричные методы обычно используются, когда класс не может предвидеть класс объектов, которые он должен создать.

#### Диаграмма

![Factory Method](https://upload.wikimedia.org/wikipedia/ru/f/f0/FactoryMethodPattern.png)

#### Пример

```dotty
trait Document:
  def open(): Unit
  def close(): Unit

trait Application:
  type D <: Document
  def createDocument: D
```

Использование паттерна фабричный метод:

```dotty
class ElectronicDocument extends Document:
  def open(): Unit = println("Open an e-doc")
  def close(): Unit = println("Close an e-doc")

object ElectronicApplication extends Application:
  type D = ElectronicDocument
  def createDocument: D = new ElectronicDocument

ElectronicApplication.createDocument.open()
// Open an e-doc
```

## Абстрактная фабрика

#### Назначение

Предоставить интерфейс для создания семейств связанных или зависимых объектов без указания конкретных классов.

Допустим, необходимо создать библиотеку графического интерфейса,
которая должна работать на разных платформах, таких как Microsoft Windows или Mac OSX.
Чтобы поддерживать собственный внешний вид на каждой платформе, например, для графического окна,
сохраняя при этом переносимость клиентского кода между платформами,
клиентам должен быть доступен только интерфейс окна.
Обычно окно состоит из нескольких виджетов, некоторые из которых могут зависеть от конкретной платформы.
Что именно и как создается конкретное окно, абстрагируется от клиентского кода абстрактной фабрикой.
Это позволяет изменять код создания объектов без изменения клиентского кода.
Поскольку клиент не знает о конкретных классах, в клиенте не вводятся зависимости от платформы.
Казалось бы, можно изменить реализации окна и связанных с ним виджетов
и предоставить новые, если они соответствуют абстрактному интерфейсу.
Обычно фабрика создает семейство продуктов,
в наших настройках графического интерфейса это могут быть окна, меню и т.д.
Важной проблемой является то, что мы не должны смешивать классы, зависящие от платформы,
так как это может привести к ошибкам во время выполнения.
Конкретные фабрики чаще всего реализуются в виде singleton-ов.

#### Диаграмма

![Абстрактная фабрика](https://upload.wikimedia.org/wikipedia/commons/9/9d/Abstract_factory_UML.svg)

#### Пример

`trait WindowFactory` определяет интерфейс для фабрики. Он содержит типы `aWindow` и `aScrollbar`.
Эти типы сверху ограничены trait-ми и должны быть доработаны на конкретных фабриках.
Код создания экземпляра, общий для конкретных фабрик, может быть повторно использован в подклассах,
если они ссылаются только на определенные абстрактные типы.
Абстрактные продукты - это оба trait-а, вложенных в фабричный признак, то есть `Window` и `Scrollbar`.
Любой конкретный продукт должен расширяться их.
Абстрактные фабричные методы `createWindow` и `createScrollbar`
скрывают фактический код создания экземпляра от клиентов.

```dotty
trait WindowFactory:
  type aWindow <: Window
  type aScrollbar <: Scrollbar

  def createWindow(s: aScrollbar): aWindow
  def createScrollbar(): aScrollbar

  trait Window(s: aScrollbar)
  trait Scrollbar
```

Ниже показано, как можно расширить нашу абстрактную фабрику с помощью конкретной фабрики.
Конкретная фабрика в данном примере - это объект singleton, содержащий protected nested классы.
Это конкретная реализация. Поскольку они protected, они скрыты от клиентов.

```dotty
object VistaFactory extends WindowFactory:
  type aWindow = VistaWindow
  type aScrollbar = VistaScrollbar

  def createWindow(s: aScrollbar) = VistaWindow(s)
  def createScrollbar() = new VistaScrollbar

  val scrollbar: aScrollbar = new VistaScrollbar
  val window: aWindow = VistaWindow(scrollbar)

  protected class VistaWindow(s: aScrollbar) extends Window(s)
  protected class VistaScrollbar extends Scrollbar
```

По соображениям технического обслуживания мы можем быть не заинтересованы в том,
чтобы фактический исходный код, реализующий классы продуктов, находился внутри фабрики.
Используя явный тип `self`, можно выразить зависимость, существующую между модулем,
предоставляющим классы продуктов, и любой `WindowFactory`.
Это позволяет расширить `Window` и `Scrollbar` в модуле,
где в противном случае они не были бы видны в области видимости.

```dotty
trait VistaWidgets:
  self: WindowFactory =>
  protected class VistaWindow(s: aScrollbar) extends Window(s)
  protected class VistaScrollbar extends Scrollbar
```

Вложенность классов полезна для инкапсуляции определенных типов.
Фабрика - это, по сути, просто признак пространства имен или репозиторий для конкретных продуктов.
Абстрактные типы позволяют выразить взаимозависимость продуктов.
Это дает статические гарантии того, что продукты с разных фабрик не могут быть смешаны.
Кроме того, они расширяют возможности повторного использования в подклассах фабрики.

## Строитель

#### Назначение

Отделение построения сложного объекта от его представления,
чтобы один и тот же процесс построения мог создавать разные представления.


#### Диаграмма

![Builder](https://upload.wikimedia.org/wikipedia/ru/2/28/Builder.gif)

#### Пример

```dotty
final case class ConnectionConfig private (
    host: String,
    port: Int,
    timeout: Int,
    connectionRetry: Int,
    user: String,
    password: String
)

object ConnectionConfig:
  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  case class ConnectionConfigBuilder private (
      private val host: String,
      private val port: Int,
      private val timeout: Int,
      private val connectionRetry: Int,
      private val user: String,
      private val password: String
  ):
    def withHost(host: String): ConnectionConfigBuilder =
      copy(host = host)

    def withPort(port: Int): ConnectionConfigBuilder =
      copy(port = port)

    def withTimeout(timeout: Int): ConnectionConfigBuilder =
      copy(timeout = timeout)

    def withConnectionRetry(
        connectionRetry: Int
    ): ConnectionConfigBuilder =
      copy(connectionRetry = connectionRetry)

    def withUser(user: String): ConnectionConfigBuilder =
      copy(user = user)

    def withPassword(password: String): ConnectionConfigBuilder =
      copy(password = password)

    def build(): ConnectionConfig =
      new ConnectionConfig(
        host = host,
        port = port,
        timeout = timeout,
        connectionRetry = connectionRetry,
        user = user,
        password = password
      )
  end ConnectionConfigBuilder

  private object ConnectionConfigBuilder:
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        host = "localhost",
        port = 8080,
        timeout = 10000,
        connectionRetry = 3,
        user = "root",
        password = "root"
      )
  end ConnectionConfigBuilder
end ConnectionConfig
```

Использование паттерна строитель:

```dotty
ConnectionConfig
  .builder()
  .withHost("localhost")
  .withPort(9090)
  .withTimeout(1000)
  .withConnectionRetry(1)
  .withUser("user")
  .withPassword("12345")
  .build()
// res3: ConnectionConfig = ConnectionConfig(
//   host = "localhost",
//   port = 9090,
//   timeout = 1000,
//   connectionRetry = 1,
//   user = "user",
//   password = "12345"
// )  
```

## Прототип

#### Назначение

Задаёт виды создаваемых объектов с помощью экземпляра-прототипа
и создаёт новые объекты путём копирования этого прототипа.
Паттерн позволяет уйти от реализации и позволяет следовать принципу "программирование через интерфейсы".
В качестве возвращающего типа указывается интерфейс/абстрактный класс на вершине иерархии,
а классы-наследники могут подставить туда наследника, реализующего этот тип.

Проще говоря, это паттерн создания объекта через клонирование другого объекта вместо создания через конструктор.

#### Использование

Паттерн используется чтобы:

- избежать дополнительных усилий по созданию объекта стандартным путём
  (имеется в виду использование конструктора,
  так как в этом случае также будут вызваны конструкторы всей иерархии предков объекта),
  когда это непозволительно дорого для приложения.
- избежать наследования создателя объекта (object creator) в клиентском приложении,
  как это делает паттерн abstract factory.

Используйте этот шаблон проектирования,
когда приложению безразлично как именно в ней создаются, компонуются и представляются продукты:

- инстанцируемые классы определяются во время выполнения, например с помощью динамической загрузки;
- избежать построения иерархий классов или фабрик, параллельных иерархии классов продуктов;
- экземпляры класса могут находиться в одном из нескольких различных состояний.
  Может оказаться удобнее установить соответствующее число прототипов и клонировать их,
  а не инстанцировать каждый раз класс вручную в подходящем состоянии.

#### Диаграмма

![Prototype](https://upload.wikimedia.org/wikipedia/ru/2/25/Prototype.gif)

#### Пример

```dotty
class A(var state: Int)
extension (a: A) def copy = new A(a.state)
```

Использование паттерна фабричный метод:

```dotty
def a = new A(2)
println(a.state)
// 2
def aCopy = a.copy
println(a.state)
// 2
```

## Одиночка

#### Назначение

Убедиться, что у класса есть только один экземпляр, и обеспечить глобальную точку доступа к нему.

#### Диаграмма

![Singleton](https://upload.wikimedia.org/wikipedia/commons/d/d7/Singleton_classdia.png)

#### Пример

Scala позволяет создавать одноэлементные объекты, используя ключевое слово `object`.
По сути, одноэлементный объект создается автоматически при первом использовании,
и всегда существует только один экземпляр.

```dotty
object Singleton
val s = Singleton
```

В следующей одноэлементной реализации используется сопутствующий объект.
Эта реализация актуальна только в том случае, если нам нужно иметь возможность усовершенствовать синглтон.
В противном случае предпочтительнее краткое решение с использованием одноэлементного объекта (`object`).

```dotty
class Singleton private () // private constructor

object Singleton:
  private val instance: Singleton = new Singleton
  def getInstance() = instance

val s = Singleton.getInstance()
```

## Структурные шаблоны

[Структурные шаблоны][Wikipedia Structural] (_structural patterns_) имеют дело с составом классов или объектов.

## Адаптер

#### Назначение

Преобразовать интерфейс класса в другой интерфейс, ожидаемый клиентами.
Адаптер позволяет классам работать вместе, что иначе было бы невозможно из-за несовместимых интерфейсов.

#### Диаграмма

![Adapter](https://upload.wikimedia.org/wikipedia/ru/0/04/Adapter_pattern.svg)

#### Пример

Решение Scala сочетает в себе большинство преимуществ адаптера класса и адаптера объекта в одном решении.

```dotty
trait Target:
  def f(): Unit

class Adaptee:
  def g(): Unit = println("g")

trait Adapter:
  self: Target with Adaptee =>
  def f(): Unit = g()

val adapter = new Adaptee with Adapter with Target
adapter.f()
// g
adapter.g()
// g
```

## Мост

#### Назначение

Отделение абстракции от ее реализации, чтобы они могли различаться.
Используя шаблон bridge, можно избежать постоянной привязки между абстракцией и ее реализацией.
Шаблон моста является подходящим дизайном, когда у вас есть множество классов, обычно из иерархии классов,
определяющих некоторые центральные абстракции, уточненные путем наследования,
для каждого из которых требуются разные реализации.

#### Диаграмма

![Bridge](https://upload.wikimedia.org/wikipedia/ru/8/81/Bridgeuml.gif)

#### Пример

Сам мост реализуется с помощью композиции и делегирования.
Абстракция содержит ссылку на средство реализации.
Точный тип реализации скрыт абстрактным классом, который в сочетании с композицией обеспечивает разделение.
Здесь действует второй принцип GOF.
Вместо включения рефакторинга иерархии классов с композицией и делегированием
в Scala можно использовать явные типы `self`.

Корень иерархии абстракций с именем `Window` имеет тип `self`,
который ссылается на корень иерархии реализации, `WindowImp`.

```dotty
// common interface for all implementors
trait WindowImp:
  def drawLine(x: Int, y: Int): Unit

trait Window:
  self: WindowImp =>
  def drawRect(x1: Int, x2: Int, x3: Int, x4: Int): Unit =
    drawLine(x1, x2)
    drawLine(x1, x3)
    drawLine(x2, x4)
    drawLine(x3, x4)

// abstractions
trait TransientWindow:
  self: Window with WindowImp =>
  def drawCloseBox(): Unit = drawRect(4, 3, 2, 1)

trait IconWindow:
  self: Window with WindowImp =>
  def drawBorder(): Unit = drawRect(1, 2, 3, 4)
```

```dotty
// implementors
trait WindowOSX extends WindowImp:
  def drawLine(x: Int, y: Int): Unit = println("drawing line in OSX")

trait WindowVista extends WindowImp:
  def drawLine(x: Int, y: Int): Unit = println("drawing line in Vista")
```

```dotty
val windowOSX: Window = new Window with WindowOSX
windowOSX.drawRect(1, 2, 3, 4)
// drawing line in OSX
// drawing line in OSX
// drawing line in OSX
// drawing line in OSX
```

## Компоновщик

#### Назначение

Комбинировать объекты в древовидные структуры для представления иерархий часть-целое.
Composite позволяет клиентам единообразно обрабатывать отдельные объекты и композиции объектов.

#### Диаграмма

![Composite](https://upload.wikimedia.org/wikipedia/commons/5/5a/Composite_UML_class_diagram_%28fixed%29.svg?uselang=ru)

#### Пример

В листинге ниже абстрактный класс `Component` представляет интерфейс для общих операций,
которые мы хотим выполнять с листьями или композитами, в данном случае только с одним методом с именем `display`.
`case class`-ы `Text` и `Picture` — это два листовых объекта, оба они реализуют `display`.
`case class Composite` содержит любое количество дочерних компонентов, то есть листьев или других составных частей.
В зависимости от того, насколько тяжелыми являются наши листовые объекты,
или выполняют ли они другие роли в нашем дизайне или нет,
мы можем сделать дочерние переменные как неизменяемыми, так и изменяемыми.

```dotty
trait Component:
  def display(): Unit

case class Text(text: String) extends Component:
  def display(): Unit = println(text)

case class Picture(picture: String) extends Component:
  def display(): Unit = println(picture)

case class Composite(children: List[Component]) extends Component:
  def display(): Unit = children.foreach(_.display())
```

```dotty
val tree = Composite(List(Composite(List(Text("text1"), Picture("picture1"))), Text("text2")))
// tree: Composite = Composite(
//   children = List(
//     Composite(
//       children = List(Text(text = "text1"), Picture(picture = "picture1"))
//     ),
//     Text(text = "text2")
//   )
// )
tree.display()
// text1
// picture1
// text2
tree.children(1).display()
// text2
```

Ниже показан пример метода, который обходит составную структуру и изменяет все узлы `Text` на месте.

```dotty
def changeAllText(c: Component, s: String): Component =
  c match
    case _: Text    => Text(s)
    case p: Picture => p
    case Composite(children) =>
      val newChildren = children.map(changeAllText(_, s))
      Composite(newChildren)
changeAllText(tree, "text3").display()
// text3
// picture1
// text3
```

## Декоратор

#### Назначение

Динамически прикреплять дополнительные обязанности к объекту.
Декораторы предоставляют гибкую альтернативу подклассам для расширения функциональности.

#### Диаграмма

![Decorator](https://upload.wikimedia.org/wikipedia/ru/0/00/Decorator_template.png)

#### Пример

```dotty
trait Component:
  def draw(): Unit

class TextView(var s: String) extends Component:
  def draw(): Unit = println(s"Drawing..$s")

trait EncapsulateTextView(c: TextView) extends Component:
  def draw(): Unit = c.draw()

trait BorderDecorator extends Component:
  abstract override def draw(): Unit =
    super.draw()
    drawBorder()
  def drawBorder(): Unit =
    println("Drawing border")

trait ScrollDecorator extends Component:
  abstract override def draw(): Unit =
    scrollTo()
    super.draw()
  def scrollTo(): Unit = println("Scrolling..")
```

```dotty
val tw = new TextView("foo")
val etw1 = new EncapsulateTextView(tw) with BorderDecorator with ScrollDecorator
// Scrolling..
// Drawing..foo
// Drawing border
tw.s = "bar"
val etw2 = new EncapsulateTextView(tw) with ScrollDecorator with BorderDecorator
etw2.draw()
// Scrolling..
// Drawing..bar
// Drawing border
```

## Фасад

#### Назначение

Предоставление унифицированного интерфейса набору интерфейсов в подсистеме.
`Facade` определяет высокоуровневый интерфейс, упрощающий использование подсистемы

#### Диаграмма

![Facade](https://upload.wikimedia.org/wikipedia/commons/5/56/UML_DP_Fa%C3%A7ade.png?uselang=ru)

#### Пример

```dotty
trait Facade:
  type A <: SubSystemA
  type B <: SubSystemB
  protected val subA: A
  protected val subB: B
  def foo(): Unit = subB.foo(subA)
  protected class SubSystemA
  protected class SubSystemB:
    def foo(sub: SubSystemA): Unit = println("Calling foo")
end Facade

object FacadeA extends Facade:
  type A = SubSystemA
  type B = SubSystemB
  val subA: A = new SubSystemA
  val subB: B = new SubSystemB
end FacadeA

FacadeA.foo()
// Calling foo
```

## Приспособленец

#### Назначение

Использование совместного использования для эффективной поддержки большого количества мелких объектов.
Оптимизация работы с памятью путём предотвращения создания экземпляров элементов, имеющих общую сущность.

#### Диаграмма

![Flyweight](https://upload.wikimedia.org/wikipedia/commons/e/ee/Flyweight.gif)

#### Пример

Flyweight используется для уменьшения затрат при работе с большим количеством мелких объектов.
При проектировании Flyweight необходимо разделить его свойства на внешние и внутренние.
Внутренние свойства всегда неизменны, тогда как внешние могут отличаться в зависимости от места
и контекста применения и должны быть вынесены за пределы приспособленца.

Flyweight дополняет шаблон Factory Method таким образом,
что при обращении клиента к Factory Method для создания нового объекта
ищет уже созданный объект с такими же параметрами, что и у требуемого, и возвращает его клиенту.
Если такого объекта нет, то фабрика создаст новый.

```dotty
trait FlyWeightFactory[T1, T2] extends Function[T1, T2]:
  import scala.collection.mutable
  private val pool = mutable.Map.empty[T1, T2]

  def createFlyWeight(intrinsic: T1): T2

  def apply(index: T1): T2 =
    pool.getOrElseUpdate(index, createFlyWeight(index))

  def update(index: T1, elem: T2): Unit =
    pool(index) = elem

end FlyWeightFactory
```

```dotty
class Character(char: Char):
  import scala.util.Random
  private lazy val state = Random.nextInt()
  def draw(): Unit =
    println(s"drawing character - $char, state - $state")

object CharacterFactory extends FlyWeightFactory[Char, Character]:
  def createFlyWeight(c: Char) = new Character(c)
```

```dotty
val f1 = CharacterFactory('a')
val f2 = CharacterFactory('b')
val f3 = CharacterFactory('a')
val f4 = new Character('a')
```

```dotty
f1.draw()
// drawing character - a, state - 1796598386
f2.draw()
// drawing character - b, state - -1864173438
f3.draw()
// drawing character - a, state - 1796598386
f4.draw()
// drawing character - a, state - 1632455878
```

Обратите внимание, что `f1` и `f3` указывают на один и тот же общий объект-приспособленец.

## Заместитель

#### Назначение

Предоставление суррогата или заполнителя для другого объекта, чтобы управлять доступом к нему.

"Заместитель" хранит ссылку, которая позволяет заместителю обратиться к реальному субъекту
(объект класса "Заместитель" может обращаться к объекту класса "Субъект",
если интерфейсы "Реального Субъекта" и "Субъекта" одинаковы).
Поскольку интерфейс "Реального Субъекта" идентичен интерфейсу "Субъекта",
так, что "Заместителя" можно подставить вместо "Реального Субъекта",
он контролирует доступ к "Реальному Субъекту",
а также может отвечать за создание или удаление "Реального Субъекта".
"Субъект" определяет общий для "Реального Субъекта" и "Заместителя" интерфейс так,
что "Заместитель" может быть использован везде, где ожидается "Реальный Субъект".
При необходимости запросы могут быть переадресованы "Заместителем" "Реальному Субъекту".

#### Виды

- **Протоколирующий прокси**: сохраняет в лог все вызовы "Субъекта" с их параметрами.
- **Удалённый заместитель** (_remote proxies_): обеспечивает связь с "Субъектом",
  который находится в другом адресном пространстве или на удалённой машине.
  Также может отвечать за кодирование запроса и его аргументов и отправку закодированного запроса реальному "Субъекту".
- **Виртуальный заместитель** (_virtual proxies_): обеспечивает создание реального "Субъекта" только тогда,
  когда он действительно понадобится. Также может кэшировать часть информации о реальном "Субъекте", чтобы отложить его создание.
- **Копировать-при-записи**: обеспечивает копирование "субъекта"
  при выполнении клиентом определённых действий (частный случай "виртуального прокси").
- **Защищающий заместитель** (_protection proxies_): может проверять,
  имеет ли вызывающий объект необходимые для выполнения запроса права.
- **Кэширующий прокси**: обеспечивает временное хранение результатов расчёта до отдачи их множественным клиентам,
  которые могут разделить эти результаты.
- **Экранирующий прокси**: защищает "Субъект" от опасных клиентов (или наоборот).
- **Синхронизирующий прокси**: производит синхронизированный контроль доступа к "Субъекту" в асинхронной многопоточной среде.
- **"Умная" ссылка** (_smart reference proxy_): производит дополнительные действия,
  когда на "Субъект" создается ссылка, например, рассчитывает количество активных ссылок на "Субъект".

#### Диаграмма

![Proxy](https://upload.wikimedia.org/wikipedia/ru/0/08/Proxy_patt.gif)

```dotty
// "Subject"
trait IMath:
  def add(x: Double, y: Double): Double

// "Real Subject"
object Math extends IMath:
  def add(x: Double, y: Double) = x + y

// "Proxy Object"
class MathProxy extends IMath:
  private lazy val math = Math

  def add(x: Double, y: Double) = math.add(x, y)
```

```dotty
val p: IMath = new MathProxy
p.add(4, 2)
// res20: Double = 6.0
```

#### Примеры

##### Протоколирующий прокси

##### Удалённый заместитель

##### Виртуальный заместитель

Отложенное вычисление Scala обеспечивает прямую языковую поддержку некоторых аспектов шаблона Proxy.

```dotty
class VirtualProxy:
  lazy val expensiveOperation = List (1 to 1000000000)
```

##### Копировать-при-записи

##### Защищающий заместитель

##### Кэширующий прокси

##### Экранирующий прокси

##### Синхронизирующий прокси

##### "Умная" ссылка

## Поведенческие шаблоны

[Поведенческие шаблоны][Wikipedia Behavioral] (_behavioral patterns_) описывают взаимодействие объектов
и часто распределение ответственности между объектами.

## Интерпретатор

#### Назначение

Для заданного языка определить представление для его грамматики
вместе с интерпретатором, который использует представление для интерпретации предложений на языке.

#### Диаграмма

![Interpreter](https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Interpreter_UML_class_diagram.svg/804px-Interpreter_UML_class_diagram.svg.png)

#### Пример

```dotty
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

```dotty
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

```dotty
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
// res2: String = "true"

input.value = "44"
expression.interpret(context)
context.result.pop()
// res5: String = "false"
```

## Шаблонный метод

#### Назначение

Определить скелет алгоритма в операции, отложив некоторые шаги на подклассы.
Шаблонный метод позволяет подклассам переопределять определенные шаги алгоритма без изменения структуры алгоритма.

#### Диаграмма

![Template Method](https://upload.wikimedia.org/wikipedia/commons/5/52/Template_Method_UML.svg?uselang=ru)

#### Пример

```dotty
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

```dotty
trait Handler[T]:
  var successor: Option[Handler[T]] = None

  def handleRequest(r: T): Unit =
    if handlingCriteria(r) then doThis(r)
    else successor.foreach(_.handleRequest(r))

  def doThis(v: T): Unit = ()
  def handlingCriteria(request: T): Boolean = false

end Handler
```

```dotty
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

```dotty
class Display2:
  def show(v: Int): Unit = println(s"Display2 prints $v")

trait Display2Handler extends Display2 with Handler[Int]:
  override def doThis(v: Int): Unit = show(v)
  override def handlingCriteria(v: Int): Boolean = v >= 4
```

```dotty
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

```dotty
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

```dotty
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

```dotty
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

```dotty
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

```dotty
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

```dotty
trait Subject[T]:
  self: T =>

  import scala.collection.mutable
  private val observers: mutable.ListBuffer[T => Unit] =
    mutable.ListBuffer.empty[T => Unit]

  def subscribe(obs: T => Unit): Unit =
    observers.addOne(obs)

  def unsubscribe(obs: T => Unit): Unit =
    observers.subtractOne(obs)

  protected def publish(): Unit = observers.foreach(obs => obs(self))
```

```dotty
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

```dotty
val s1: SensorSubject = new Sensor("s1") with SensorSubject
val d1: Display = new Display("d1")
val d2: Display = new Display("d2")
```

```dotty
s1.subscribe(d1.notify)
s1.subscribe(d2.notify)
s1.changeValue(10)
```

## Состояние

#### Назначение

Позволить объекту изменить свое поведение при изменении его внутреннего состояния. Объект изменит свой класс.
Паттерн состояния актуален, когда поведение объекта зависит от его внутреннего состояния.

#### Диаграмма

![State](https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/State_Design_Pattern_UML_Class_Diagram.svg/475px-State_Design_Pattern_UML_Class_Diagram.svg.png)

#### Пример

```dotty
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

```dotty
val c = new Context
c.operation()
// State1
c.operation()
// State1
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

```dotty
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

```dotty
val query = ".txt"
// query: String = ".txt"
FileMatcher.filesContaining(query)
// res24: Seq[String] = List("example.txt", "file.txt.png")
FileMatcher.filesRegex(query)
// res25: Seq[String] = List("1txt")
FileMatcher.filesEnding(query)
// res26: Seq[String] = List("example.txt")
```

## Посетитель

#### Назначение

Представление операции, которая должна быть выполнена над элементами структуры объекта.
Visitor позволяет определить новую операцию без изменения классов элементов, над которыми она работает.

#### Диаграмма

![Visitor](https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/VisitorDiagram.svg/515px-VisitorDiagram.svg.png?uselang=ru)

#### Пример

```dotty
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

```dotty
val e1 = Sum(Sum(Num(1), Num(2)), Num(3))
// e1: Sum = Sum(l = Sum(l = Num(n = 1), r = Num(n = 2)), r = Num(n = 3))
prettyPrint(e1)
// 1 + 2 + 3
print(eval(e1))
// 6
```

---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Design Patterns by Gamma, Helm, Johnson, and Vlissides][Design Patterns]
- [Wikipedia, Порождающие шаблоны проектирования][Wikipedia Creational]
- [Wikipedia, Structural pattern][Wikipedia Structural]
- [Wikipedia, Behavioral pattern][Wikipedia Behavioral]

[Design Patterns]: https://www.amazon.com/Design-Patterns-Elements-Reusable-Object-Oriented/dp/0201633612
[Wikipedia Creational]: https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D1%80%D0%BE%D0%B6%D0%B4%D0%B0%D1%8E%D1%89%D0%B8%D0%B5_%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD%D1%8B_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F
[Wikipedia Structural]: https://en.wikipedia.org/wiki/Structural_pattern
[Wikipedia Behavioral]: https://en.wikipedia.org/wiki/Behavioral_pattern
