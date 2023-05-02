# Паттерны проектирования

Шаблон проектирования обычно рассматривается как многоразовое решение часто встречающейся 
проблемы проектирования в объектно-ориентированном программном обеспечении.

#### Проектное пространство

Пространство шаблонов проектирования, представленное в таблице ниже, имеет два измерения: 
цель (_purpose_) и область применения (_scope_). 
Шаблоны целей могут быть классифицированы как _creational_, _structural_ или _behavioral_.
[Порождающие шаблоны](patterns/creational.md) (_creational_) имеют дело с созданием объектов, 
в то время как [структурные шаблоны](patterns/structural.md) (_structural_) имеют дело с составом классов или объектов. 
[Поведенческие шаблоны](patterns/behavioral.md) (_behavioral_) описывают взаимодействие объектов 
и часто распределение ответственности между объектами. 

Измерение области видимости определяет, ["применяется ли шаблон в первую очередь к классам или к объектам"][Design Patterns]. 

Шаблоны классов имеют дело с отношениями между классами и их подклассами. 
Поскольку эти отношения определяются во время компиляции посредством наследования, 
они фиксированы и не могут изменяться во время выполнения. 
В шаблонах классов шаблон в целом содержится в одной иерархии классов.

Объектные шаблоны, с другой стороны, имеют дело с отношениями между объектами, такими как композиция и делегирование. 
Это означает, что связь может изменяться во время выполнения.
В шаблонах объектов комбинированное поведение шаблона распределяется между несколькими объектами во время выполнения.

| **Scope** \ **Purpose** | **Creational**                                                                                                                                                   | **Structural**                                                                                                                                                                                                                                                 | **Behavioral**                                                                                                                                                                                                                                                                                                                                               |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Class**               | [Factory Method](patterns/creational/factory-method.md)                                                                                                                   | [Adapter(class)](patterns/structural/adapter.md)                                                                                                                                                                                                                        | [Interpreter](patterns/behavioral/interpreter.md),  [Template Method](patterns/behavioral/template-method.md)                                                                                                                                                                                                                                                                  |
| **Object**              | [Abstract Factory](patterns/creational/abstract-factory.md), [Builder](patterns/creational/builder.md), [Prototype](patterns/creational/prototype.md), [Singleton](patterns/creational/singleton.md) | [Adapter(object)](patterns/structural/adapter.md),  [Bridge](patterns/structural/bridge.md),  [Composite](patterns/structural/composite.md),  [Decorator](patterns/structural/decorator.md),  [Facade](patterns/structural/facade.md),  [Flyweight](patterns/structural/flyweight.md),  [Proxy](patterns/structural/proxy.md) | [Chain of Responsibility](patterns/behavioral/chain-of-responsibility.md),  [Command](patterns/behavioral/command.md),  [Iterator](patterns/behavioral/iterator.md),  [Mediator](patterns/behavioral/mediator.md),  [Memento](patterns/behavioral/memento.md),  [Observer](patterns/behavioral/observer.md),  [State](patterns/behavioral/state.md),  [Strategy](patterns/behavioral/strategy.md),  [Visitor](patterns/behavioral/visitor.md) |


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Design Patterns by Gamma, Helm, Johnson, and Vlissides][Design Patterns]

[Design Patterns]: https://www.amazon.com/Design-Patterns-Elements-Reusable-Object-Oriented/dp/0201633612
