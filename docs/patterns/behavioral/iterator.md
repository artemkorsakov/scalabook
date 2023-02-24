# Итератор

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


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Iterator_pattern)
