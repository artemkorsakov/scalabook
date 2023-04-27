# Адаптер

#### Назначение

Преобразовать интерфейс класса в другой интерфейс, ожидаемый клиентами. 
Адаптер позволяет классам работать вместе, что иначе было бы невозможно из-за несовместимых интерфейсов.

#### Диаграмма

![Adapter](https://upload.wikimedia.org/wikipedia/ru/0/04/Adapter_pattern.svg)

#### Пример

Решение Scala сочетает в себе большинство преимуществ адаптера класса и адаптера объекта в одном решении.

```scala
trait Target:
  def f(): Unit

class Adaptee:
  def g(): Unit = println("g")

trait Adapter { self: Target with Adaptee =>
  def f(): Unit = g()
}
```

```scala
val adapter = new Adaptee with Adapter with Target
adapter.f()
// g
adapter.g()
// g
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Adapter_pattern)
