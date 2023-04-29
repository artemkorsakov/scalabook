# Паттерны проектирования

Шаблон проектирования обычно рассматривается как многоразовое решение часто встречающейся 
проблемы проектирования в объектно-ориентированном программном обеспечении.

#### Проектное пространство

Пространство шаблонов проектирования, представленное в таблице ниже, имеет два измерения: 
цель (_purpose_) и область применения (_scope_). 
Шаблоны целей могут быть классифицированы как _creational_, _structural_ или _behavioral_.
[Порождающие шаблоны](creational.md) (_creational_) имеют дело с созданием объектов, 
в то время как [структурные шаблоны](structural.md) (_structural_) имеют дело с составом классов или объектов. 
[Поведенческие шаблоны](behavioral.md) (_behavioral_) описывают взаимодействие объектов 
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
| **Class**               | [Factory Method](creational/factory-method.md)                                                                                                                   | [Adapter(class)](structural/adapter.md)                                                                                                                                                                                                                        | [Interpreter](behavioral/interpreter.md),  [Template Method](behavioral/template-method.md)                                                                                                                                                                                                                                                                  |
| **Object**              | [Abstract Factory](creational/abstract-factory.md), [Builder](creational/builder.md), [Prototype](creational/prototype.md), [Singleton](creational/singleton.md) | [Adapter(object)](structural/adapter.md),  [Bridge](structural/bridge.md),  [Composite](structural/composite.md),  [Decorator](structural/decorator.md),  [Facade](structural/facade.md),  [Flyweight](structural/flyweight.md),  [Proxy](structural/proxy.md) | [Chain of Responsibility](behavioral/chain-of-responsibility.md),  [Command](behavioral/command.md),  [Iterator](behavioral/iterator.md),  [Mediator](behavioral/mediator.md),  [Memento](behavioral/memento.md),  [Observer](behavioral/observer.md),  [State](behavioral/state.md),  [Strategy](behavioral/strategy.md),  [Visitor](behavioral/visitor.md) |


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Design Patterns by Gamma, Helm, Johnson, and Vlissides][Design Patterns]

[Design Patterns]: https://www.amazon.com/Design-Patterns-Elements-Reusable-Object-Oriented/dp/0201633612
