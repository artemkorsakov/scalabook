# Состояние

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


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/State_pattern)
