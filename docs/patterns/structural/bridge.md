# Мост

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

```scala
// common interface for all implementors
trait WindowImp:
  def drawLine(x: Int, y: Int): Unit

trait Window { self: WindowImp =>
  def drawRect(x1: Int, x2: Int, x3: Int, x4: Int): Unit =
    drawLine(x1, x2)
    drawLine(x1, x3)
    drawLine(x2, x4)
    drawLine(x3, x4)
}

// abstractions
trait TransientWindow { self: Window with WindowImp =>
  def drawCloseBox(): Unit = drawRect(4, 3, 2, 1)
}

trait IconWindow { self: Window with WindowImp =>
  def drawBorder(): Unit = drawRect(1, 2, 3, 4)
}
```

```scala
// implementors
trait WindowOSX extends WindowImp:
  def drawLine(x: Int, y: Int): Unit = println("drawing line in OSX")

trait WindowVista extends WindowImp:
  def drawLine(x: Int, y: Int): Unit = println("drawing line in Vista")
```

```scala
val windowOSX: Window = new Window with WindowOSX
windowOSX.drawRect(1, 2, 3, 4)
// drawing line in OSX
// drawing line in OSX
// drawing line in OSX
// drawing line in OSX
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Bridge_pattern)
