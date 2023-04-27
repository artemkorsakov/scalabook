# Хранитель

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


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Memento_pattern)
