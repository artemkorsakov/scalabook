# Структурные шаблоны

[Структурные шаблоны][Wikipedia] (_structural patterns_) имеют дело с составом классов или объектов. 

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

---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia, Structural pattern][Wikipedia]
- [Wikipedia, Adapter](https://en.wikipedia.org/wiki/Adapter_pattern)
- [Wikipedia, Bridge](https://en.wikipedia.org/wiki/Bridge_pattern)
- [Wikipedia, Composite](https://en.wikipedia.org/wiki/Composite_pattern)
- [Wikipedia, Decorator](https://en.wikipedia.org/wiki/Decorator_pattern)
- [Wikipedia, Facade](https://en.wikipedia.org/wiki/Facade_pattern)
- [Wikipedia, Flyweight](https://en.wikipedia.org/wiki/Flyweight_pattern)
- [Wikipedia, Proxy](https://en.wikipedia.org/wiki/Proxy_pattern)

[Wikipedia]: https://en.wikipedia.org/wiki/Structural_pattern