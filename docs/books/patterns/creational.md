# Порождающие шаблоны

[Порождающие шаблоны][Wikipedia] (_creational patterns_) имеют дело с созданием объектов. 

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

[Более детализированное рассмотрение шаблона][Builder]

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

---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia, Порождающие шаблоны проектирования][Wikipedia]
- [Wikipedia, Factory method pattern](https://en.wikipedia.org/wiki/Factory_method_pattern)
- [Wikipedia, Abstract factory](https://en.wikipedia.org/wiki/Abstract_factory_pattern)
- [Более детализированное рассмотрение шаблона Builder][Builder]
- [Wikipedia, Builder](https://en.wikipedia.org/wiki/Builder_pattern)
- [Wikipedia, Singleton](https://en.wikipedia.org/wiki/Singleton_pattern)

[Wikipedia]: https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D1%80%D0%BE%D0%B6%D0%B4%D0%B0%D1%8E%D1%89%D0%B8%D0%B5_%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD%D1%8B_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F
[Builder]: ../../blog/builder-pattern.md
