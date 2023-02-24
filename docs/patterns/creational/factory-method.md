# Фабричный метод

#### Назначение

Определение интерфейса для создания объекта, в котором подклассы решают, какой класс создавать. 
Фабричный метод позволяет классу отложить создание экземпляра для подклассов.
Фабричные методы обычно используются, когда класс не может предвидеть класс объектов, которые он должен создать.

#### Диаграмма

![Factory Method](https://upload.wikimedia.org/wikipedia/ru/f/f0/FactoryMethodPattern.png)

#### Пример

```scala
trait Document:
  def open(): Unit
  def close(): Unit

trait Application:
  type D <: Document
  def createDocument: D
```

Использование паттерна фабричный метод:

```scala
class ElectronicDocument extends Document:
  def open(): Unit = println("Open an e-doc")
  def close(): Unit = println("Close an e-doc")

object ElectronicApplication extends Application:
  type D = ElectronicDocument
  def createDocument: D = new ElectronicDocument

ElectronicApplication.createDocument.open()
// Open an e-doc
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Factory_method_pattern)
