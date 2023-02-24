# Паттерны проектирования

Шаблон проектирования обычно рассматривается как многоразовое решение часто встречающейся 
проблемы проектирования в объектно-ориентированном программном обеспечении.

#### Проектное пространство

Пространство шаблонов проектирования, представленное в таблице ниже, имеет два измерения: 
цель (_purpose_) и область применения (_scope_). 
Шаблоны целей могут быть классифицированы как _creational_, _structural_ или _behavioral_.
[Порождающие шаблоны](creational) (_creational_) имеют дело с созданием объектов, 
в то время как [структурные шаблоны](structural) (_structural_) имеют дело с составом классов или объектов. 
[Поведенческие шаблоны](behavioral) (_behavioral_) описывают взаимодействие объектов 
и часто распределение ответственности между объектами. 

Измерение области видимости определяет, ["применяется ли шаблон в первую очередь к классам или к объектам"][Design Patterns]. 

Шаблоны классов имеют дело с отношениями между классами и их подклассами. 
Поскольку эти отношения определяются во время компиляции посредством наследования, 
они фиксированы и не могут изменяться во время выполнения. 
В шаблонах классов шаблон в целом содержится в одной иерархии классов.

Объектные шаблоны, с другой стороны, имеют дело с отношениями между объектами, такими как композиция и делегирование. 
Это означает, что связь может изменяться во время выполнения.
В шаблонах объектов комбинированное поведение шаблона распределяется между несколькими объектами во время выполнения.

| **Scope** \ **Purpose** | **Creational**                                                                                                                                                                    | **Structural**                                                                                                                                                                                                                                                                                                                | **Behavioral**                                                                                                                                                                                                                                                                                                                                                                                                                  |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Class**               | [Factory Method](creational/factory-method)                                                                                                                                       | [Adapter(class)](structural/adapter)                                                                                                                                                                                                                                                                                          | [Interpreter](behavioral/interpreter) <br /> [Template Method](behavioral/template-method)                                                                                                                                                                                                                                                                                                                                      |
| **Object**              | [Abstract Factory](creational/abstract-factory) <br /> [Builder](creational/builder) <br /> [Prototype](creational/prototype) <br /> [Singleton](creational/singleton)            | [Adapter(object)](structural/adapter) <br /> [Bridge](structural/bridge) <br /> [Composite](structural/composite) <br /> [Decorator](structural/decorator) <br /> [Facade](structural/facade) <br /> [Flyweight](structural/flyweight) <br /> [Proxy](structural/proxy)                                                       | [Chain of Responsibility](behavioral/chain-of-responsibility) <br /> [Command](behavioral/command) <br /> [Iterator](behavioral/iterator) <br /> [Mediator](behavioral/mediator) <br /> [Memento](behavioral/memento) <br /> [Observer](behavioral/observer) <br /> [State](behavioral/state) <br /> [Strategy](behavioral/strategy) <br /> [Visitor](behavioral/visitor)                                                       |


---

**Ссылки:**
- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Design Patterns by Gamma, Helm, Johnson, and Vlissides][Design Patterns]

[Design Patterns]: https://www.amazon.com/Design-Patterns-Elements-Reusable-Object-Oriented/dp/0201633612
